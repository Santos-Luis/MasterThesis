package edu.stanford.nlp.sempre.tables.modular;

import java.util.*;

import edu.stanford.nlp.sempre.*;
import fig.basic.Fmt;
import fig.basic.LogInfo;

/**
 * Tune the precision and recall of multiple ModularSemanticFn.
 *
 * preciseProbs = probability of choosing the high precision module (interpolation ratios)
 *
 * @author ppasupat
 */
public class ModularPrecisionRecallTuner implements Iterable<ModularSemanticFn> {

  protected final Map<ModularSemanticFn, Double> preciseProbs = new LinkedHashMap<>();
  protected final Parser parser;

  public ModularPrecisionRecallTuner(Parser parser) {
    this.parser = parser;
    for (Rule rule : parser.grammar.getRules()) {
      if (rule.sem instanceof ModularSemanticFn) {
        ModularSemanticFn sem = (ModularSemanticFn) rule.sem;
        LogInfo.logs("Found modular rule: %s", rule);
        preciseProbs.put(sem, 0d);
        sem.tuner = this;
      }
    }
  }

  @Override
  public Iterator<ModularSemanticFn> iterator() {
    return preciseProbs.keySet().iterator();
  }


  // ============================================================
  // Getting and Setting interpolation ratio
  // ============================================================

  public double getPreciseProb(ModularSemanticFn fn) {
    return preciseProbs.get(fn);
  }

  public double[] getAllPreciseProbs() {
    double[] probs = new double[preciseProbs.size()];
    int i = 0;
    for (Double p : preciseProbs.values()) probs[i++] = p;
    return probs;
  }

  public String getPreciseProbsString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<ModularSemanticFn, Double> entry : preciseProbs.entrySet()) {
      sb.append("[").append(entry.getKey()).append(":").append(Fmt.D((double) entry.getValue())).append("]");
    }
    return sb.toString();
  }

  public void setAllProbs(double preciseProb) {
    for (ModularSemanticFn fn : preciseProbs.keySet()) {
      preciseProbs.put(fn, preciseProb);
    }
  }
  public void setAllRecall() { setAllProbs(0); }
  public void setAllPrecise() { setAllProbs(1); }

  public void setOddOneOutProbs(ModularSemanticFn fn, double fnPreciseProb, double otherFnsPreciseProb) {
    for (ModularSemanticFn f : preciseProbs.keySet()) {
      preciseProbs.put(f, f == fn ? fnPreciseProb : otherFnsPreciseProb);
    }
  }
  public void setSingleRecall(ModularSemanticFn fn) { setOddOneOutProbs(fn, 0, 1); }

  // ============================================================
  // Run floating parser with the current interpolation ratios
  // ============================================================

  public ParserState getParserState(Params params, Example ex, boolean computeExpectedCounts) {
    ParserState ps = parser.newParserState(params, ex, computeExpectedCounts);
    ps.infer();
    return ps;
  }

}
