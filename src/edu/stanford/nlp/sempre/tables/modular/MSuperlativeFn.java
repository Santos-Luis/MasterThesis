package edu.stanford.nlp.sempre.tables.modular;

import java.util.*;

import edu.stanford.nlp.sempre.*;
import fig.basic.*;
import static edu.stanford.nlp.sempre.tables.modular.ModularUtils.L;

/**
 * Superlative generation module.
 *
 * Modes:
 * - High-Recall: Generate all possible options.
 * - High-Precision: Only generate when there are trigger words.
 *
 * @author ppasupat
 */
public class MSuperlativeFn extends ModularSemanticFn {

  private boolean aIndex = false;

  public void init(LispTree tree) {
    super.init(tree);
    for (int i = 1; i < tree.children.size(); i++) {
      String value = tree.child(i).value;
      if ("index".equals(value)) this.aIndex = true;
      else throw new RuntimeException("Invalid argument: " + value);
    }
  }

  @Override
  public DerivationStream call(Example ex, Callable c) {
    return new LazyMSuperlativeFnDerivs(ex, c, getPreciseProb());
  }

  // ============================================================
  // Derivation Stream
  // ============================================================

  public class LazyMSuperlativeFnDerivs extends ModularDerivationStream {
    protected String getName() { return "MSuperlativeFn"; }

    public LazyMSuperlativeFnDerivs(Example ex, Callable c, double preciseProb) {
      super(ex, c, preciseProb);
    }

    List<String> INDEX_TRIGGER_LEMMA = Arrays.asList("top", "first", "bottom", "last");
    List<String> NONINDEX_TRIGGER_POS = Arrays.asList("JJS", "RBS");

    protected void getFormulas() {
      formulas = new ArrayList<>();
      if (getPreciseProb() == 1) {
        boolean triggered = false;
        if (aIndex) {
          for (String lemma : ex.getLemmaTokens()) {
            if (INDEX_TRIGGER_LEMMA.contains(lemma)) {
              triggered = true; break;
            }
          }
        } else {
          for (String pos : ex.languageInfo.posTags) {
            if (NONINDEX_TRIGGER_POS.contains(pos)) {
              triggered = true; break;
            }
          }
        }
        if (!triggered) return;
      }
      Formula head = c.child(0).formula;
      Formula relation = aIndex ? Formulas.fromLispTree(L("fb:row.row.index")) : c.child(1).formula;
      for (SuperlativeFormula.Mode mode : SuperlativeFormula.Mode.values()) {
        formulas.add(new Pair<>(new SuperlativeFormula(mode, new ValueFormula<>(new NumberValue(1)),
            new ValueFormula<>(new NumberValue(1)), head, relation), new FeatureVector()));
      }
    }

  }

}
