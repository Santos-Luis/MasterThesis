package edu.stanford.nlp.sempre.fbalignment.bipartite.filters;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteNode;

public class NodeFrequencyFilter implements BipartiteNodeFilter {

  private int minCount = 3;

  public NodeFrequencyFilter() { }

  public NodeFrequencyFilter(int minCount) { this.minCount = minCount; }

  @Override
  public boolean filterNode(BipartiteNode node) {
    return node.getMidIdPairsCount() < minCount;
  }

}
