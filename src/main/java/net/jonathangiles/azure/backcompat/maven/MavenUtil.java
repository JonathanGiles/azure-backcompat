package net.jonathangiles.azure.backcompat.maven;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenFormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystemBase;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;
import org.jboss.shrinkwrap.resolver.api.maven.MavenVersionRangeResult;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.PomlessResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenChecksumPolicy;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenUpdatePolicy;

import java.util.ArrayList;
import java.util.List;

public class MavenUtil {

    private MavenUtil() {  }

    public static MavenResolverSystemBase<PomEquippedResolveStage, PomlessResolveStage, MavenStrategyStage, MavenFormatStage> getMavenResolver() {
        // TODO externalise configuration of which repos to use
        return Maven.configureResolver()
                .withRemoteRepo(MavenRemoteRepositories
                        .createRemoteRepository("snapshots", "https://oss.sonatype.org/content/repositories/snapshots/", "default")
                        .setChecksumPolicy(MavenChecksumPolicy.CHECKSUM_POLICY_IGNORE)
                        .setUpdatePolicy(MavenUpdatePolicy.UPDATE_POLICY_NEVER))
                .withMavenCentralRepo(true);
    }

    public static Version getVersionFromGAV(String gav) {
        String version = gav.substring(gav.lastIndexOf(":") + 1);
        return Version.build(version);
    }

    public static Version getLatestVersionInMavenCentral(String ga, boolean acceptQualifiers) {
        List<Version> result = getLatestVersionInMavenCentral(ga, acceptQualifiers, 1);
        return result.isEmpty() ? Version.UNKNOWN : result.get(0);
    }

    public static List<Version> getLatestVersionInMavenCentral(String ga, boolean acceptQualifiers, int elements) {
            MavenVersionRangeResult rangeResult = getMavenResolver().resolveVersionRange(ga + ":[0.1,)");
            if (rangeResult.getVersions().isEmpty()) {
                System.err.println("Failed to get any versions for " + ga + " - exiting");
                System.exit(-1);
            }

            List<MavenCoordinate> versions = rangeResult.getVersions();
            List<Version> result = new ArrayList<>();

            for (int i = versions.size() - 1; i >= 0; i--) {
                MavenCoordinate coor = versions.get(i);
                if (acceptQualifiers || !coor.getVersion().contains("-")) {
                    result.add(0, Version.build(coor.getVersion()));
                }

                if (result.size() == elements) break;
            }

            return result;
    }
}
