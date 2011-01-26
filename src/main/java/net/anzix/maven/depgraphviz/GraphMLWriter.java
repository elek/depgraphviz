/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anzix.maven.depgraphviz;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author elek
 */
public class GraphMLWriter implements GraphWriter {

    private final String NS = "http://graphml.graphdrawing.org/xmlns";

    private final String YED_NS = "http://www.yworks.com/xml/graphml";

    @Override
    public void write(File outputFile, Graph graph) throws Exception {
        Document d = new Document(new Element("graphml", NS));
        Element graphElement = new Element("graph", NS);
        graphElement.setAttribute("id", "G");
        graphElement.setAttribute("edgedefault", "directed");
        d.getRootElement().addContent(graphElement);


        Element key = new Element("key", NS);
        key.setAttribute("for", "node");
        key.setAttribute("yfiles.type", "nodegraphics");
        key.setAttribute("id", "d0");

        d.getRootElement().addContent(key);

        SAXBuilder builder = new SAXBuilder();
        URL templateURL = getClass().getResource("/graphml_node_template.xml");
        System.out.println(templateURL);
        Document template = builder.build(templateURL);
        Element tr = template.getRootElement();
        tr.detach();

        for (Node node : graph.getNodes()) {
            Element n = new Element("node", NS);
            n.setAttribute("id", node.getId());
            graphElement.addContent(n);

            Element shape = (Element) tr.clone();

            shape.getChild("NodeLabel",Namespace.getNamespace("y", YED_NS)).setText(node.getId());

            Element data = new Element("data", NS).setAttribute("key", "d0");
            data.addContent(shape);
            n.addContent(data);



        }

        for (Edge edge : graph.getEdges()) {
            Element e = new Element("edge", NS);
            e.setAttribute("source", edge.getStart().getId());
            e.setAttribute("target", edge.getEnd().getId());
            graphElement.addContent(e);
        }

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        FileWriter writer = new FileWriter(outputFile);
        outputter.output(d, writer);
        writer.close();
    }

    @Override
    public String getExtension() {
        return "graphml";
    }
}
