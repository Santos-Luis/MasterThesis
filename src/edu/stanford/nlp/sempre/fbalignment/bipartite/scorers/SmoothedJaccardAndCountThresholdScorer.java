package edu.stanford.nlp.sempre.fbalignment.bipartite.scorers;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteNode;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.FourPartiteGraph;

public class SmoothedJaccardAndCountThresholdScorer implements NodePairScorer {

  public SmoothedJaccardScorer jaccardScorer;
  public CountScorer countScorer;
  public static final int THRESHOLD = 3;

  public SmoothedJaccardAndCountThresholdScorer() {
    jaccardScorer = new SmoothedJaccardScorer();
    countScorer = new CountScorer();
  }

  @Override
  public double scoreNodePair(FourPartiteGraph graph, BipartiteNode node1, BipartiteNode node2) {

    double jaccard = jaccardScorer.scoreNodePair(graph, node1, node2);
    double count = countScorer.scoreNodePair(graph, node1, node2);
    return count >= THRESHOLD ? jaccard : 0;
  }


}
