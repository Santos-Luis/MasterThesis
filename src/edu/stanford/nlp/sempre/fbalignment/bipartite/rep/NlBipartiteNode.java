package edu.stanford.nlp.sempre.fbalignment.bipartite.rep;


public class NlBipartiteNode extends BipartiteNode {

  private static final long serialVersionUID = -1026373717791595286L;

  public NlBipartiteNode(String predicate) {
    super();
    description = predicate;
    nodeType = BipartiteNodeType.NL;
  }

  @Override
  public BipartiteNodeType getType() {
    return nodeType;
  }
}
