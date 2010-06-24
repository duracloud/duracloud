/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.ops4j.pax.exam.mavenplugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;

public class TransitiveDependencyCollector
        implements DependencyCollector {

    private final MavenProject project;

    private final ArtifactFactory artifactFactory;

    private final MavenProjectBuilder mavenProjectBuilder;

    private final List<ArtifactRepository> remoteRepositories;

    private final ArtifactRepository localRepository;

    public TransitiveDependencyCollector(MavenProject project,
                                         ArtifactFactory artifactFactory,
                                         MavenProjectBuilder mavenProjectBuilder,
                                         List<ArtifactRepository> remoteRepositories,
                                         ArtifactRepository localRepository) {
        this.project = project;
        this.artifactFactory = artifactFactory;
        this.mavenProjectBuilder = mavenProjectBuilder;
        this.remoteRepositories = remoteRepositories;
        this.localRepository = localRepository;
    }

    @SuppressWarnings("unchecked")
    public List<Dependency> getDependencies() throws MojoExecutionException {
        List<Dependency> dependencies = new ArrayList<Dependency>();

        List<Dependency> moduleDependencies = project.getDependencies();
        for (Dependency dependency : moduleDependencies) {
            dependencies.add(dependency);

            if (isDuraCloudProject(dependency)) {
                MavenProject pom = getPom(dependency);
                dependencies.addAll(pom.getDependencies());
            }
        }
        return dependencies;
    }

    private boolean isDuraCloudProject(Dependency dependency) {
        String groupId = dependency.getGroupId();

        return (groupId.equals("org.duracloud") || groupId
                .equals("org.duracloud.services"));
    }

    private MavenProject getPom(Dependency dependency)
            throws MojoExecutionException {
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        String version = dependency.getVersion();
        String classifier = dependency.getClassifier();

        Artifact pomArtifact =
                artifactFactory.createArtifact(groupId,
                                               artifactId,
                                               version,
                                               classifier,
                                               "pom");

        MavenProject pomProject = null;
        try {
            pomProject =
                    mavenProjectBuilder.buildFromRepository(pomArtifact,
                                                            remoteRepositories,
                                                            localRepository);
        } catch (ProjectBuildingException e) {
        }

        if (pomProject == null) {
            throw new MojoExecutionException("Unable to build pom: " + groupId
                    + ":" + artifactId);
        }
        return pomProject;
    }

}
