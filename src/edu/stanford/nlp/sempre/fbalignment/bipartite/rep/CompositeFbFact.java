package edu.stanford.nlp.sempre.fbalignment.bipartite.rep;

import java.util.LinkedList;
import java.util.List;

public class CompositeFbFact {

  private String mid1;
  private String mid2;
  private List<String> properties;
  private boolean isReversed = false; // whether the order of MIDs in swit

  public CompositeFbFact(String mid1, String mid2, List<String> properties) {
    this.mid1 = mid1;
    this.mid2 = mid2;
    this.properties = properties;
  }

  public CompositeFbFact(String mid1, String mid2, List<String> properties, boolean isReversed) {
    this.mid1 = mid1;
    this.mid2 = mid2;
    this.properties = properties;
    this.isReversed = isReversed;
  }

  public CompositeFbFact(String mid1, String mid2, String property) {
    this.mid1 = mid1;
    this.mid2 = mid2;
    properties = new LinkedList<String>();
    properties.add(property);
  }

  public CompositeFbFact(String mid1, String mid2, String property, boolean isReversed) {
    this.mid1 = mid1;
    this.mid2 = mid2;
    properties = new LinkedList<String>();
    properties.add(property);
    this.isReversed = isReversed;
  }

  public String getMid1() { return mid1; }
  public String getMid2() { return mid2; }
  public List<String> getProperties() { return properties; }

  public String getPropertyString() {

    StringBuilder sb = new StringBuilder();
    for (String property : properties) {
      sb.append(property + ";");
    }
    return sb.toString();
  }

  public String toString() {
    return mid1 + "\t" + properties + "\t" + mid2 + "\t" + isReversed;
  }

  public boolean isReversedFact() {
    return isReversed;
  }


}
