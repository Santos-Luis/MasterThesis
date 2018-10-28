package edu.stanford.nlp.sempre.tables.modular;

import edu.stanford.nlp.sempre.*;

public abstract class ModularSemanticFn extends SemanticFn {

  public ModularPrecisionRecallTuner tuner;

  protected double getPreciseProb() {
    return tuner.getPreciseProb(this);
  }

}
