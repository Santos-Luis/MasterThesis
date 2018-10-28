package edu.stanford.nlp.sempre.fbalignment.unary;

public class NlUnaryAlignmentNode extends UnaryAlignmentNode {

  public String description;

  public NlUnaryAlignmentNode(String description) {
    super(description);
    this.description = description;
  }

  public String toString() {
    return description + "\t" + getIds();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((description == null) ? 0 : description.hashCode());
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
    NlUnaryAlignmentNode other = (NlUnaryAlignmentNode) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    return true;
  }

  @Override
  public String getDescription() {
    return description;
  }


}
