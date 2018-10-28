package edu.stanford.nlp.sempre.fbalignment.bipartite.learner;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.FourPartiteGraph;
import fig.basic.Option;

import java.io.IOException;

public final class AlignmentLearnerFactory {
  private AlignmentLearnerFactory() { }

  public static class Options {
    @Option(gloss = "Type of learning algorithm") public String learnerType;
  }

  public static Options opts = new Options();

  public static AlignmentLearner createLearner(FourPartiteGraph graph) throws IOException, ClassNotFoundException {

    String learnerType = opts.learnerType;
    if (learnerType.equals("choose-best-relation-for-predicate")) {
      return new ChooseBestRelation(graph);
    } else if (learnerType.equals("exhaustive-nl-typing"))
      return new ExhaustiveNlTyping(graph);
    else if (learnerType.equals("exhaustive-nl-typing-no-commit"))
      return new ExhaustiveNlTypingNoCommit(graph);
    else if (learnerType.equals("fb-based-nl-typing"))
      return new FbBasedNlTyping(graph);
    else if (learnerType.equals("choose-best-type-compatible-relation"))
      return new ChooseBestTypeCompatibleRelation(graph);
    else if (learnerType.equals("split-predicate-to-disjoint-types"))
      return new SplitPredicateToDisjointTypes(graph);
    else
      throw new IllegalArgumentException("Illegal type of learner: " + learnerType);
  }
}
