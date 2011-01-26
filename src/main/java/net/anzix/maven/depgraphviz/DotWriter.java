/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anzix.maven.depgraphviz;

import java.io.File;
import java.io.FileWriter;

/**
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
