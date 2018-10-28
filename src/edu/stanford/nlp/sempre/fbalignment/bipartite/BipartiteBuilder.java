package edu.stanford.nlp.sempre.fbalignment.bipartite;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;

import java.io.IOException;

/**
 * Interface for classes that build the bipartite graph
 *
 * @author jonathanberant
 */
public interface BipartiteBuilder<V, E> {
  UndirectedSparseGraph<V, E> constructBipartiteGraph() throws IOException, ClassNotFoundException;
}
