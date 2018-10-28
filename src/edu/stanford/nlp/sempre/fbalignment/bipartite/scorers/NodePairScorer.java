package edu.stanford.nlp.sempre.fbalignment.bipartite.scorers;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteNode;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.FourPartiteGraph;

public interface NodePairScorer {
  double scoreNodePair(FourPartiteGraph graph, BipartiteNode node1, BipartiteNode node2);
}
