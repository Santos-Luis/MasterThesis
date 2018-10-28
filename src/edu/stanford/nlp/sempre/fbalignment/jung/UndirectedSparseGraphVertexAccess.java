package edu.stanford.nlp.sempre.fbalignment.jung;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;

import java.util.HashMap;
import java.util.Map;

public class UndirectedSparseGraphVertexAccess<V, E> extends UndirectedSparseGraph<V, E> {

  private static final long serialVersionUID = 9217188491654742509L;

  Map<V, V> verticesIdentityMap;

  public UndirectedSparseGraphVertexAccess() {
    super();
    verticesIdentityMap = new HashMap<V, V>();
  }

  @Override
  public boolean addVertex(V vertex) {
    if (!vertices.containsKey(vertex)) {
      verticesIdentityMap.put(vertex, vertex);
    }
    return super.addVertex(vertex);
  }

  public boolean removeVertex(V vertex) {
    verticesIdentityMap.remove(vertex);
    return super.removeVertex(vertex);
  }

  public V getRealVertex(V vertex) {
    return verticesIdentityMap.get(vertex);
  }
}

