package edu.stanford.nlp.sempre.fbalignment.bipartite.rep;

public class ScoreEdge extends CountEdge {

  private static final long serialVersionUID = 6476268211266436012L;
  private double score;

  public ScoreEdge(int count, double score) {
    super(count);
    this.score = score;
  }

  @Override
  public double score() {
    return score;
  }

  public String toString() {
    return super.toString() + "\t" + score;
  }

}
