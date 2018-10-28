package edu.stanford.nlp.sempre.fbalignment.bipartite.rep;

import java.util.LinkedList;
import java.util.List;


public class FbBipartiteNode extends BipartiteNode {

  private static final long serialVersionUID = -8085022279843478314L;
  protected List<String> compositePredicate;
  protected boolean reversed;

  public FbBipartiteNode() {
    super();
    nodeType = BipartiteNodeType.FB;
    compositePredicate = new LinkedList<String>();
    reversed = false;
  }

  public FbBipartiteNode(List<String> compositePredicate, boolean reversed) {
    super();
    nodeType = BipartiteNodeType.FB;
    this.compositePredicate = compositePredicate;
    this.reversed = reversed;
    description = reversed ? "!" + compositePredicate.toString() : compositePredicate.toString();
  }

  @Override
  public BipartiteNodeType getType() {
    return nodeType;
  }

  public List<String> getCompositePredicate() { return compositePredicate; }

  public static FbBipartiteNode fromCompositePredicateDescription(String compositePredicateDescription) {

    boolean reversed = false;
    if (compositePredicateDescription.startsWith("!")) {
      reversed = true;
      compositePredicateDescription = compositePredicateDescription.substring(1);
    }

    List<String> properties = new LinkedList<String>();
    // strip brackets
    compositePredicateDescription = compositePredicateDescription.substring(1, compositePredicateDescription.length() - 1);
    String[] tokens = compositePredicateDescription.split(",");
    for (int i = 0; i < tokens.length; ++i) {
      properties.add(tokens[i].trim());
    }
    return new FbBipartiteNode(properties, reversed);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (reversed ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    FbBipartiteNode other = (FbBipartiteNode) obj;
    if (reversed != other.reversed)
      return false;
    return true;
  }

  public boolean isReversed() {
    return reversed;
  }


}
