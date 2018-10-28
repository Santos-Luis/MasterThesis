package edu.stanford.nlp.sempre.fbalignment.unary;

import java.util.HashSet;
import java.util.Set;


public abstract class UnaryAlignmentNode {

  private Set<String> idSet;

  public UnaryAlignmentNode(String description) {
    idSet = new HashSet<String>();
  }

  public boolean addId(String id) {
    return idSet.add(id);
  }

  public Set<String> getIds() {
    return idSet;
  }

  public int getIdsCount() {
    return idSet.size();
  }

  public abstract String getDescription();


}
