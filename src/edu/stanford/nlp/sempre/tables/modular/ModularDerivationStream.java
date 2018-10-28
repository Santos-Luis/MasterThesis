package edu.stanford.nlp.sempre.tables.modular;

import java.util.*;

import edu.stanford.nlp.sempre.*;
import edu.stanford.nlp.sempre.SemanticFn.Callable;
import edu.stanford.nlp.sempre.tables.TableKnowledgeGraph;
import fig.basic.LogInfo;
import fig.basic.Option;
import fig.basic.Pair;

public abstract class ModularDerivationStream extends MultipleDerivationStream {
  public static class Options {
    @Option(gloss = "Verbosity") public int verbosity = 0;
  }
  public static Options opts = new Options();

  protected final Example ex;
  protected final Callable c;
  protected final double preciseProb;
  protected final TableKnowledgeGraph graph;

  protected int index = 0;
  protected List<Pair<Formula, FeatureVector>> formulas;

  public ModularDerivationStream(Example ex, Callable c, double preciseProb) {
    this.ex = ex;
    this.c = c;
    this.preciseProb = preciseProb;
    this.graph =
        (ex.context == null || !(ex.context.graph instanceof TableKnowledgeGraph))
          ? null : (TableKnowledgeGraph) ex.context.graph;
    if (opts.verbosity >= 2)
      LogInfo.logs("%s: called on %s", getName(), ex.utterance);
  }

  @Override
  public Derivation createDerivation() {
    if (graph == null) return null;

    // Compute the formulas if not computed yet
    if (formulas == null) {
      getFormulas();
    }

    // Use the next formula to create a derivation
    if (index >= formulas.size()) return null;
    Pair<Formula, FeatureVector> ff = formulas.get(index++);
    Formula formula = ff.getFirst();
    FeatureVector features = ff.getSecond();
    SemType type = TypeInference.inferType(formula);

    return new Derivation.Builder()
        .withCallable(c)
        .formula(formula)
        .type(type)
        .localFeatureVector(features)
        .createDerivation();
  }

  protected abstract String getName();
  protected abstract void getFormulas();
}