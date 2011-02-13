/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anzix.maven.depanal;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.reactor.MavenExecutionException;

/**
 * Write out all of the dependencies to a txt file.
 * 
 * @goal list
 * @requiresDependencyResolution test
 * @author elek
 */
public class DeplistMojo extends AbstractDepanalMojo {

    /**
     * List of Remote Repositories used by the resolver
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    private java.util.List remoteRepos;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @component
     */
    private org.apache.maven.artifact.resolver.ArtifactResolver resolver;

    /**
     * Location of the local repository.
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    private org.apache.maven.artifact.repository.ArtifactRepository local;

    @Override
    public void doProcess() throws Exception {
        GraphWriter writer = new PathWriter();
        File outputFile = new File(outputDirectory, "dep." + writer.getExtension());
        writer.write(outputFile, graph);
    }

    @Override
    protected Node artifactToNode(Artifact a) {

        StringBuilder b = new StringBuilder();

        b.append(a.getGroupId()).append(":");
        b.append(a.getArtifactId());
        b.append(":").append(a.getVersion());

        Node n = new Node(b.toString());
        if (!a.isResolved()) {
            try {
                resolver.resolve(a, remoteRepos, local);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Artifact Can't be resolved the artifact" + a, ex);
            }
        }
        n.setLocation(a.getFile());
        return n;
    }
}
