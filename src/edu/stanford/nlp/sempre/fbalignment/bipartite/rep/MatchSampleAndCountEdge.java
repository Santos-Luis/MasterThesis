package edu.stanford.nlp.sempre.fbalignment.bipartite.rep;

import edu.stanford.nlp.util.Pair;

import java.util.HashSet;
import java.util.Set;

/**
 * An edge in the bipartite graph that keeps the count but also a sample of the
 * matching MIDs
 *
 * @author jonathanberant
 */
public class MatchSampleAndCountEdge implements BipartiteEdge {

  /**
   *
   */
  private static final long serialVersionUID = -8082489855085996386L;
  private static final int SAMPLE_SIZE = 3;
  private int count = 0;
  private Set<Pair<String, String>> samples;

  public MatchSampleAndCountEdge() {
    samples = new HashSet<Pair<String, String>>();
  }

  @Override
  public int value() { return count; }

  @Override
  public void addMatch(Pair<String, String> match) {
    count++;
    if (samples.size() < SAMPLE_SIZE)
      samples.add(match);
  }

  public String toString() {

    StringBuilder sb = new StringBuilder();
    sb.append(count + "\t[");
    for (Pair<String, String> sample : samples) {
      sb.append(sample + " ");
    }
    sb.append("]");
    return sb.toString();
  }

  @Override
  public double score() {
    return count;
  }

}
