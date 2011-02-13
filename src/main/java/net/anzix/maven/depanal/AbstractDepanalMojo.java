package net.anzix.maven.depanal;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;

public abstract class AbstractDepanalMojo extends AbstractMojo {

    /**
     * Maven Project. Default value: ${project}
     *
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * Local Maven repository.
     *
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;

    /**
     * Artifact collector, needed to resolve dependencies.
     *
     * @component role="org.apache.maven.artifact.resolver.ArtifactCollector"
     * @required
     * @readonly
     */
    private ArtifactCollector artifactCollector;

    /**
     * @component role="org.apache.maven.artifact.metadata.ArtifactMetadataSource"
     * hint="maven"
     */
    protected ArtifactMetadataSource artifactMetadataSource;

    /**
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private DependencyTreeBuilder dependencyTreeBuilder;

    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    protected File outputDirectory;

    /**
     * @parameter default-value="${reactorProjects}"
     * @required
     * @readonly
     */
    private List<MavenProject> reactorProjects;

    /**
     * Reactor project as an Artifact list;
     */
    private List<Artifact> reactorArtifacts = new ArrayList();

    /**
     * Artifacts already printed out
     */
    private Set<Artifact> processed = new HashSet();

    /**
     * @parameter expression="${depanal.reactorOnly}" default-value="true"
     */
    private boolean reactorOnly;      
    /**
     * the dependency graph structure.
     */
    protected Graph graph;

    /**
     * Artifact filter to ignore dependencies.
     */
    ArtifactFilter filter;

    @Override
    public void execute()
            throws MojoExecutionException {


        try {
            filter = new ArtifactFilter() {

                @Override
                public boolean include(Artifact artfct) {
                    if (Artifact.SCOPE_TEST.equals(artfct.getScope())) {
                        return false;
                    } else if (reactorOnly && !reactorArtifacts.contains(artfct)) {
                        return false;
                    } else {
                        return true;
                    }
                }
            };

            for (MavenProject proj : reactorProjects) {
                reactorArtifacts.add(proj.getArtifact());
            }

            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }
            graph = new Graph();

            for (MavenProject proj : reactorProjects) {
                DependencyNode depNode = dependencyTreeBuilder.buildDependencyTree(proj, localRepository, artifactFactory, artifactMetadataSource, filter, artifactCollector);
                collectDependencies(depNode);
            }


            doProcess();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("Error on plugin execution", ex);
        }
    }

    public abstract void doProcess() throws Exception;

    protected void collectDependencies(DependencyNode depNode) throws IOException {
        if (!filter.include(depNode.getArtifact())) {
            return;
        }
        //avoid loops
        if (processed.contains(depNode.getArtifact())) {
            return;
        } else {
            processed.add(depNode.getArtifact());
        }

        boolean isDependencyExist = false;
        for (DependencyNode cn : (List<DependencyNode>) depNode.getChildren()) {

            if (filter.include(cn.getArtifact())) {
                graph.addEdge(artifactToNode(depNode.getArtifact()), artifactToNode(cn.getArtifact()));

                collectDependencies(cn);
                isDependencyExist = true;
            }
        }
        if (!isDependencyExist) {
            graph.addNode(artifactToNode(depNode.getArtifact()));
        }

    }

    protected abstract Node artifactToNode(Artifact a);

   
    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public boolean isReactorOnly() {
        return reactorOnly;
    }

    public void setReactorOnly(boolean reactorOnly) {
        this.reactorOnly = reactorOnly;
    }

   
}
