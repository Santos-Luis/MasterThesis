package edu.stanford.nlp.sempre.fbalignment.bipartite.scorers;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteNode;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.FourPartiteGraph;
import edu.stanford.nlp.util.Sets;

public class CountScorer implements NodePairScorer {

  public double scoreNodePair(FourPartiteGraph graph, BipartiteNode node1, BipartiteNode node2) {

    return Sets.intersection(node1.getMidIdPairSet(), node2.getMidIdPairSet()).size();
  }

}
