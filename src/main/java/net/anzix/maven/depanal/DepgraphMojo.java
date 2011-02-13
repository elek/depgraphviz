package net.anzix.maven.depanal;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Create graphviz dot file from maven dependency graph.
 *
 * @goal graph
 * @requiresDependencyResolution test
 *
 */
public class DepgraphMojo extends AbstractDepanalMojo {

    /**
     * @parameter expression=${depanal.showVersion} default-value="false"
     */
    private boolean showVersion;

    /**
     * @parameter expression="${depanal.showGroupId}" default-value="false"
     */
    private boolean showGroupId;

    /**
     * @parameter expression="${depanal.format}" default-value="graphml"
     */
    private String format;

    private Map<String, GraphWriter> writers = new HashMap<String, GraphWriter>();

    public DepgraphMojo() {
        writers.put("dot", new DotWriter());
        writers.put("graphml", new GraphMLWriter());
    }

    @Override
    public void doProcess() throws Exception {
        GraphWriter writer = writers.get(format);
        if (writer == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Wrong output format type. Supported formats are: ");
            for (String key : writers.keySet()) {
                sb.append(key).append(" ");
            }
            throw new MojoExecutionException(sb.toString());
        }

        File outputFile = new File(outputDirectory, "dep." + writer.getExtension());
        writer.write(outputFile, graph);
    }

    @Override
    protected Node artifactToNode(Artifact a) {

        StringBuilder b = new StringBuilder();
        if (showGroupId) {
            b.append(a.getGroupId()).append(":");
        }
        b.append(a.getArtifactId());
        if (showVersion) {
            b.append(":").append(a.getVersion());
        }
        Node n = new Node(b.toString());       
        return n;

    }
}
