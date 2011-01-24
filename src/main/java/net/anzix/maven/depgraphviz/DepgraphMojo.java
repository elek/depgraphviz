package net.anzix.maven.depgraphviz;


import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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

/**
 * Create graphviz dot file from maven dependency graph.
 *
 * @goal depgraphviz
 * 
 */
public class DepgraphMojo
        extends AbstractMojo {

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
    private File outputDirectory;

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
     * @parameter expression=${depgraphviz.reactorOnly} default-value="true"
     */
    private boolean reactorOnly = true;

    /**
     * @parameter expression=${depgraphviz.reactorOnly} default-value="false"
     */
    private boolean showVersion = false;

    /**
     * @parameter expression=${depgraphviz.showGroupId} default-value="false"
     */
    private boolean showGroupId = false;

    FileWriter writer;

    ArtifactFilter filter;

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

            if (!outputDirectory.exists()){
                outputDirectory.mkdirs();
            }
            writer = new FileWriter(new File(outputDirectory,"dep.dot"));
            writer.write("digraph G {\n");
            for (MavenProject proj : reactorProjects) {
                DependencyNode depNode = dependencyTreeBuilder.buildDependencyTree(proj, localRepository, artifactFactory, artifactMetadataSource, filter, artifactCollector);
                printDependency(depNode);
            }

            writer.write("}\n");
            writer.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("Error on plugin execution", ex);
        }
    }

    protected void printDependency(DependencyNode depNode) throws IOException {
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

                writer.write("\"");
                writeArtifactName(depNode.getArtifact(), writer);
                writer.write("\" -> \"");
                writeArtifactName(cn.getArtifact(), writer);
                writer.write("\";\n");

                printDependency(cn);
                isDependencyExist = true;
            }
        }
        if (!isDependencyExist){
            writer.write("\"");
                writeArtifactName(depNode.getArtifact(), writer);
                writer.write("\";\n");
        }

    }

    protected void writeArtifactName(Artifact a, Writer writer) throws IOException {
        if (showGroupId) {
            writer.write(a.getGroupId() + ":");
        }
        writer.write(a.getArtifactId());
        if (showVersion) {
            writer.write(":" + a.getVersion());
        }
    }
}
