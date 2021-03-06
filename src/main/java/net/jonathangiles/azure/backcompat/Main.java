package net.jonathangiles.azure.backcompat;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.jonathangiles.azure.backcompat.archive.MavenAwareFileArchive;
import net.jonathangiles.azure.backcompat.gson.DeserializerForProject;
import net.jonathangiles.azure.backcompat.model.CompareRequest;
import net.jonathangiles.azure.backcompat.report.Result;
import net.jonathangiles.azure.backcompat.report.Summary;
import org.jboss.shrinkwrap.resolver.api.maven.*;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepository;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.AcceptAllStrategy;
import org.revapi.*;
import org.revapi.java.JavaApiAnalyzer;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final File REPORTS_DIR = new File("./reports");

    public static final DifferenceSeverity MIN_SEVERITY_LEVEL = DifferenceSeverity.POTENTIALLY_BREAKING;

    public static void main(String[] args) {
        REPORTS_DIR.mkdir();

        List<CompareRequest> releasesToCheck = loadSDKs();

        List<Summary> summaries = new ArrayList<>();
        releasesToCheck.forEach(sdk -> compare(sdk, summaries));

        // write out summary details of scan
        try (Writer writer = new FileWriter(REPORTS_DIR.getName() + "/summary.json")) {
            new GsonBuilder().setPrettyPrinting().create().toJson(summaries, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("Complete");
        System.exit(0);
    }

    private static List<CompareRequest> loadSDKs() {
        try (Reader reader = new FileReader("./releases.json")) {
            List<List<CompareRequest>> sdks = new GsonBuilder()
                                     .registerTypeAdapter(CompareRequest.class, new DeserializerForProject())
                                     .create()
                                     .fromJson(reader, new TypeToken<List<CompareRequest>>(){}.getType());

            LOGGER.info("Read in the following SDKs from the releases.json file:");
            sdks.forEach(System.out::println);

            return sdks.stream().flatMap(List::stream).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static void compare(CompareRequest sdk, List<Summary> summaries) {
        // add support for maven central snapshots repo
        MavenRemoteRepository mavenCentralSnapshots = MavenRemoteRepositories.createRemoteRepository(
                "ossrh",
                "https://oss.sonatype.org/content/repositories/snapshots/",
                "default");

        ConfigurableMavenResolverSystem cfgOld = Maven.configureResolver()
                .withRemoteRepo(mavenCentralSnapshots)
                .withMavenCentralRepo(true);

        ConfigurableMavenResolverSystem cfgNew = Maven.configureResolver()
                .withRemoteRepo(mavenCentralSnapshots)
                .withMavenCentralRepo(true);

        MavenResolvedArtifact[] oldArtifacts = cfgOld.resolve(sdk.getOldVersionGAV()).using(AcceptAllStrategy.INSTANCE).asResolvedArtifact();
        MavenResolvedArtifact[] newArtifacts = cfgNew.resolve(sdk.getNewVersionGAV()).using(AcceptAllStrategy.INSTANCE).asResolvedArtifact();

        API oldApi = API
                .of(new MavenAwareFileArchive(sdk.getGA() + ":" + sdk.getOldVersion(), oldArtifacts[0].asFile()))
                .addSupportArchives(Stream.of(oldArtifacts)
                    .skip(1)
                    .map(a -> new MavenAwareFileArchive(a.getCoordinate().toCanonicalForm(), a.asFile()))
                    .toArray(MavenAwareFileArchive[]::new))
                .build();


        API newApi = API
                .of(new MavenAwareFileArchive(sdk.getGA() + ":" + sdk.getNewVersion(), newArtifacts[0].asFile()))
                .addSupportArchives(Stream.of(newArtifacts)
                    .skip(1)
                    .map(a -> new MavenAwareFileArchive(a.getCoordinate().toCanonicalForm(), a.asFile()))
                    .toArray(MavenAwareFileArchive[]::new))
                .build();

        Revapi revapi = Revapi.builder()
                .withAnalyzers(JavaApiAnalyzer.class)
                .withReporters(CollectingReporter.class)
                .build();

        AnalysisContext analysisContext = AnalysisContext.builder(revapi)
                .withOldAPI(oldApi)
                .withNewAPI(newApi)
                .build();

        LOGGER.info("Starting analysis of " + sdk.getGA() + " between " + sdk.getOldVersion().getVersionString() + " and " + sdk.getNewVersion().getVersionString());
        try (AnalysisResult result = revapi.analyze(analysisContext)) {
            result.throwIfFailed();
            CollectingReporter reporter = result.getExtensions().getFirstExtension(CollectingReporter.class, null);

            List<Result> results = reporter.getReports().stream()
                    .map(Result::create)
                    .collect(Collectors.toList());

            int issues = (int)reporter.getReports().stream().filter(report -> report.getDifferences().size() > 0).count();
            summaries.add(new Summary(sdk, issues));

            try (Writer writer = new FileWriter(REPORTS_DIR.getName() + "/" + sdk.getGA() + ":" + sdk.getVersionsString() + ".json")) {
                new GsonBuilder().setPrettyPrinting().create().toJson(results, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("Completed analysis of " + sdk.getGA());
    }
}
