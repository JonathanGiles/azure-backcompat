package net.jonathangiles.azure.backcompat;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.jonathangiles.azure.backcompat.archive.MavenAwareFileArchive;
import net.jonathangiles.azure.backcompat.report.Result;
import net.jonathangiles.azure.backcompat.report.Summary;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
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

        List<SDK> releasesToCheck = loadSDKs();

        List<Summary> summaries = new ArrayList<>();
        releasesToCheck.parallelStream().forEach(sdk -> compare(sdk, summaries));

        // write out summary details of scan
        try (Writer writer = new FileWriter(REPORTS_DIR.getName() + "/summary.json")) {
            new GsonBuilder().setPrettyPrinting().create().toJson(summaries, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("Complete");
        System.exit(0);
    }

    private static List<SDK> loadSDKs() {
        try (Reader reader = new FileReader("./releases.json")) {
            List<SDK> sdks = new GsonBuilder().create().fromJson(reader, new TypeToken<List<SDK>>(){}.getType());

            LOGGER.info("Read in the following SDKs from the releases.json file:");
            sdks.forEach(System.out::println);

            return sdks;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static void compare(SDK sdk, List<Summary> summaries) {
        String oldRepo = null;
        String newRepo = null;

        ConfigurableMavenResolverSystem cfgOld = Maven.configureResolver();
        ConfigurableMavenResolverSystem cfgNew = Maven.configureResolver();

        if (oldRepo == null || oldRepo.equals("null")) {
            cfgOld = cfgOld.withMavenCentralRepo(true);
        } else {
            cfgOld = cfgOld.withRemoteRepo("remote", oldRepo, "default");
        }

        if (newRepo == null || newRepo.equals("null")) {
            cfgNew = cfgNew.withMavenCentralRepo(true);
        } else {
            cfgNew = cfgNew.withRemoteRepo("remote", newRepo, "default");
        }

        MavenResolvedArtifact[] oldArtifacts = cfgOld.resolve(sdk.getOldRelease()).using(AcceptAllStrategy.INSTANCE).asResolvedArtifact();
        MavenResolvedArtifact[] newArtifacts = cfgNew.resolve(sdk.getNewRelease()).using(AcceptAllStrategy.INSTANCE).asResolvedArtifact();

        API oldApi = API
                .of(new MavenAwareFileArchive(sdk.getOldRelease(), oldArtifacts[0].asFile()))
                .addSupportArchives(
                        Stream.of(oldArtifacts)
                                .skip(1)
                                .map(a -> new MavenAwareFileArchive(a.getCoordinate().toCanonicalForm(), a.asFile()))
                                .toArray(MavenAwareFileArchive[]::new))
                .build();


        API newApi = API
                .of(new MavenAwareFileArchive(sdk.getNewRelease(), newArtifacts[0].asFile()))
                .addSupportArchives(
                        Stream.of(newArtifacts)
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

        try (AnalysisResult result = revapi.analyze(analysisContext)) {
            result.throwIfFailed();
            CollectingReporter reporter = result.getExtensions().getFirstExtension(CollectingReporter.class, null);

            List<Result> results = reporter.getReports().stream()
                    .map(Result::create)
                    .collect(Collectors.toList());

            int issues = (int)reporter.getReports().stream().filter(report -> report.getDifferences().size() > 0).count();
            summaries.add(new Summary(sdk.getName(), issues));

            try (Writer writer = new FileWriter(REPORTS_DIR.getName() + "/" + sdk.getName() + ".json")) {
                new GsonBuilder().setPrettyPrinting().create().toJson(results, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
