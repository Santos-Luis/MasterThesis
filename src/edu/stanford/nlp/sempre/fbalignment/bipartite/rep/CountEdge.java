package edu.stanford.nlp.sempre.fbalignment.bipartite.rep;

import edu.stanford.nlp.util.Pair;


/**
 * A class for storing a mutable int
 *
 * @author jonathanberant
 */
public class CountEdge implements BipartiteEdge {

  private static final long serialVersionUID = -911790554283478225L;

  private int count;

  public CountEdge() { this.count = 0; }

  public CountEdge(int count) { this.count = count; }

  public void inc() { count++; }

  public void dec() { count--; }

  public void inc(int n) { count += n; }

  public void dec(int n) { count -= n; }

  public int value() { return count; }

  public String toString() { return "" + count; }

  @Override
  public void addMatch(Pair<String, String> match) {
    count++;
  }

  @Override
  public double score() {
    return count;
  }


}
