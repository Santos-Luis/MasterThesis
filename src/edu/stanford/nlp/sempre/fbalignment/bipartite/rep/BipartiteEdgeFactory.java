package edu.stanford.nlp.sempre.fbalignment.bipartite.rep;

/**
 * Factory for edges - initializes with zero count
 *
 * @author jonathanberant
 */
public final class BipartiteEdgeFactory {
  private BipartiteEdgeFactory() { }

  public static BipartiteEdge createEdge(String edgeDesc) {

    if (edgeDesc.equals("count-edge"))
      return new CountEdge();
    else if (edgeDesc.equals("match-sample-and-count-edge"))
      return new MatchSampleAndCountEdge();
    else if (edgeDesc.equals("unlabeled-edge"))
      return new UnlabeledEdge();
    else
      throw new IllegalArgumentException("Edge description is illegal: " + edgeDesc);
  }

}
