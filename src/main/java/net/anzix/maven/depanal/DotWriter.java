
package net.anzix.maven.depanal;

import java.io.File;
import java.io.FileWriter;

/**
 * Write Graphviz file.
 *
 * @author elek
 */
public class DotWriter implements GraphWriter {

    public void write(File outputFile, Graph graph) throws Exception {
        FileWriter writer = new FileWriter(outputFile);
        writer.write("digraph G {\n");
        for (Edge e : graph.getEdges()) {
            writer.write("\"" + e.getStart() + "\" -> \"" + e.getEnd() + "\";\n");
        }
        for (Node n : graph.getOprhanNodes()) {
            writer.write("\"" + n + "\";\n");
        }
        writer.write("}\n");
        writer.close();
    }

    @Override
    public String getExtension() {
        return "dot";
    }
}
