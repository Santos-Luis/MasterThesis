package edu.stanford.nlp.sempre.fbalignment.unary;

import fig.basic.LispTree;

public class FbUnaryAlignmentNode extends UnaryAlignmentNode {

  private LispTree formula;

  public FbUnaryAlignmentNode(String description) {
    super(description);
    formula = LispTree.proto.parseFromString(description);
  }

  public LispTree getFormula() {
    return formula;
  }

  public String toString() {
    return formula.toString() + getIds();
  }

  public String getUnaryEdge() {
    return formula.child(0).value;
  }

  public String getUnaryNode() {
    return formula.child(1).value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((formula.toString() == null) ? 0 : formula.toString().hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FbUnaryAlignmentNode other = (FbUnaryAlignmentNode) obj;
    if (formula.toString() == null) {
      if (other.formula.toString() != null)
        return false;
    } else if (!formula.toString().equals(other.formula.toString()))
      return false;
    return true;
  }

  @Override
  public String getDescription() {
    return formula.toString();
  }


}
