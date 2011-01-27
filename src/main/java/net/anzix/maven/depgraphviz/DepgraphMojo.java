package net.anzix.maven.depgraphviz;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
 * @goal graph
 * @requiresDependencyResolution test
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
     * @parameter expression="${depgraphviz.reactorOnly}" default-value="true"
     */
    private boolean reactorOnly;

    /**
     * @parameter expression=${depgraphviz.reactorOnly} default-value="false"
     */
    private boolean showVersion;

    /**
     * @parameter expression="${depgraphviz.showGroupId}" default-value="false"
     */
    private boolean showGroupId;

    /**
     * @parameter expression="${depgraphviz.format}" default-value="graphml"
     */
    private String format;

    /**
     * the dependency graph structure.
     */
    private Graph graph;

    /**
     * Artifact filter to igore dependencies.
     */
    ArtifactFilter filter;

    private Map<String, GraphWriter> writers = new HashMap<String, GraphWriter>();

    public DepgraphMojo() {
        writers.put("dot", new DotWriter());
        writers.put("graphml", new GraphMLWriter());
    }

    public void execute()
            throws MojoExecutionException {

        GraphWriter writer = writers.get(format);
        if (writer == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Wrong output format type. Supporteed formats are: ");
            for (String key : writers.keySet()) {
                sb.append(key).append(" ");
            }
            throw new MojoExecutionException(sb.toString());
        }

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
                collectDepndencies(depNode);
            }

            File outputFile = new File(outputDirectory, "dep." + writer.getExtension());

            writer.write(outputFile, graph);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("Error on plugin execution", ex);
        }
    }

    protected void collectDepndencies(DependencyNode depNode) throws IOException {
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
                graph.addEdge(artifactToNodeName(depNode.getArtifact()), artifactToNodeName(cn.getArtifact()));
                collectDepndencies(cn);
                isDependencyExist = true;
            }
        }
        if (!isDependencyExist) {
            graph.addNode(artifactToNodeName(depNode.getArtifact()));
        }

    }

    protected String artifactToNodeName(Artifact a) {
        StringBuilder b = new StringBuilder();
        if (showGroupId) {
            b.append(a.getGroupId()).append(":");
        }
        b.append(a.getArtifactId());
        if (showVersion) {
            b.append(":").append(a.getVersion());
        }
        return b.toString();
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

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

    public boolean isShowGroupId() {
        return showGroupId;
    }

    public void setShowGroupId(boolean showGroupId) {
        this.showGroupId = showGroupId;
    }

    public boolean isShowVersion() {
        return showVersion;
    }

    public void setShowVersion(boolean showVersion) {
        this.showVersion = showVersion;
    }
}
