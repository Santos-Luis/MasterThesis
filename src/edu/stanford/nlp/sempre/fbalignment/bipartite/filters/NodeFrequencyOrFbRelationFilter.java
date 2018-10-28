package edu.stanford.nlp.sempre.fbalignment.bipartite.filters;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteNode;

public class NodeFrequencyOrFbRelationFilter implements BipartiteNodeFilter {
  NodeFrequencyFilter nodeFreqFilter;
  FbRelationFilter fbRelFilter;

  public NodeFrequencyOrFbRelationFilter() {
    nodeFreqFilter = new NodeFrequencyFilter();
    fbRelFilter = new FbRelationFilter();
  }

  @Override
  public boolean filterNode(BipartiteNode node) {
    return nodeFreqFilter.filterNode(node) || fbRelFilter.filterNode(node);
  }
}
