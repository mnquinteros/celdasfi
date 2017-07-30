package celdas.fiuba.assets;

import java.util.List;

public class Graph {
        private final List<Vertex> vertexes;
        private final List<Edge> edges;

        public Graph(List<Vertex> vertexes, List<Edge> edges) {
                this.vertexes = vertexes;
                this.edges = edges;
        }

        public List<Vertex> getVertexes() {
                return vertexes;
        }

        public List<Edge> getEdges() {
                return edges;
        }

        public Vertex getNode(int idNode) {
        	String idString = Integer.toString(idNode);
        	for (Vertex vertex: this.vertexes)
        		if (vertex.getId().equals(idString))
        			return vertex;
        	return null;
        }

}