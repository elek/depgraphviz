package net.anzix.maven.depanal;

import java.io.File;
import java.io.FileWriter;

/**
 * Write out absolute path to a text file.
 * 
 * @author elek
 */
public class PathWriter implements GraphWriter {

    @Override
    public void write(File outputFile, Graph graph) throws Exception {
        FileWriter writer = new FileWriter(outputFile);
        for (Node node : graph.getNodes()) {
            if (node.getLocation().getAbsolutePath() != null) {
                writer.write(node.getLocation().getAbsolutePath()+"\n");
            }
        }
        writer.close();
    }

    @Override
    public String getExtension() {
        return "txt";
    }
}
