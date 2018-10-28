package edu.stanford.nlp.sempre.fbalignment.fbgraph;

import java.io.Serializable;

/**
 * Class representing a Freebase entity
 *
 * @author jonathanberant
 */
public class FbEntity implements Serializable {

  private static final long serialVersionUID = 7658837122573943533L;
  private String mid; // Freebase identifier

  public FbEntity(String mid) {
    this.mid = mid;
  }

  public String getId() { return mid; }

  public String toString() {
    return mid;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((mid == null) ? 0 : mid.hashCode());
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
    FbEntity other = (FbEntity) obj;
    if (mid == null) {
      if (other.mid != null)
        return false;
    } else if (!mid.equals(other.mid))
      return false;
    return true;
  }


}
