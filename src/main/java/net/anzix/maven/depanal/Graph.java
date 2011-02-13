
package net.anzix.maven.depanal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dependency graph.
 * 
 * @author elek
 */
public class Graph {

    private Map<String, Node> nodes = new HashMap<String, Node>();

    private List<Edge> edges = new ArrayList<Edge>();


     public void addEdge(Node startNode, Node endNode) {
        checkNode(startNode);
        checkNode(endNode);
        edges.add(new Edge(startNode, endNode));
    }

    public void checkNode(Node n){
        if (!nodes.containsKey(n.getId())){
            addNode(n);
        }
    }
    public void addEdge(String startNodeId, String endNodeId) {
        Node start = getNode(startNodeId);
        Node end = getNode(endNodeId);
        edges.add(new Edge(start, end));

    }

    private Node getNode(String nodeId) {
        Node n = nodes.get(nodeId);
        if (n == null) {
            n = new Node(nodeId);
            nodes.put(nodeId, n);
        }
        return n;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public List<Node> getNodes() {
        return new ArrayList(nodes.values());
    }

    public Node addNode(Node n) {
        nodes.put(n.getId(), n);
        return n;
    }

    public Node addNode(String nodeId) {
        return getNode(nodeId);
    }

    public Collection<Node> getOprhanNodes() {
        Set<Node> allNodes = new HashSet<Node>(nodes.values());
        for (Edge edge : edges) {
            allNodes.remove(edge.getStart());
        }
        return allNodes;
    }
}
