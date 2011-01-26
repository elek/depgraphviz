/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.anzix.maven.depgraphviz;

import java.io.File;

/**
 *
 * @author elek
 */
public interface GraphWriter {

    void write(File outputFile, Graph graph) throws Exception;

    String getExtension();
}
