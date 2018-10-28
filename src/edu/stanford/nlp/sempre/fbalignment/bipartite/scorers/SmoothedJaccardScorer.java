package edu.stanford.nlp.sempre.fbalignment.bipartite.scorers;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteNode;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.FourPartiteGraph;
import edu.stanford.nlp.util.Sets;

public class SmoothedJaccardScorer implements NodePairScorer {

  private static final double SMOOTHING = 5.0;

  @Override
  public double scoreNodePair(FourPartiteGraph graph, BipartiteNode node1, BipartiteNode node2) {

    int nominator = Sets.intersection(node1.getMidIdPairSet(), node2.getMidIdPairSet()).size();
    double denominator = Sets.union(node1.getMidIdPairSet(), node2.getMidIdPairSet()).size() + SMOOTHING;
    if (nominator > Math.round(denominator))
      throw new IllegalStateException("Error: nominator larger than denominator, node1 ID pairs: " + node1.getMidIdPairSet() + ", node2 ID pairs: " + node2.getMidIdPairSet());
    return (double) nominator / denominator;
  }
}
