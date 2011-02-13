
package net.anzix.maven.depanal;

import java.io.File;

/**
 * Write out the graph in a specific format.
 *
 * @author elek
 */
public interface GraphWriter {

    void write(File outputFile, Graph graph) throws Exception;

    String getExtension();
}
