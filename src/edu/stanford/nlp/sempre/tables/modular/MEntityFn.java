package edu.stanford.nlp.sempre.tables.modular;

import java.util.*;

import edu.stanford.nlp.sempre.*;
import edu.stanford.nlp.sempre.tables.StringNormalizationUtils;
import edu.stanford.nlp.sempre.tables.TableCell;
import edu.stanford.nlp.sempre.tables.TableColumn;
import edu.stanford.nlp.sempre.tables.TableKnowledgeGraph.*;
import edu.stanford.nlp.sempre.tables.modular.ModularUtils.NGram;
import fig.basic.*;

/**
 * Entity generation module.
 *
 * Modes:
 * - High-Recall: Perform fuzzy match
 * - High-Precision: Perform exact match
 *
 * @author ppasupat
 */
public class MEntityFn extends ModularSemanticFn {
  public static class Options {
    public double modularFuzzyMatchThreshold = 0.8;
  }
  public static Options opts = new Options();

  public void init(LispTree tree) {
    super.init(tree);
  }

  @Override
  public DerivationStream call(Example ex, Callable c) {
    return new LazyMEntityFnDerivs(ex, c, getPreciseProb());
  }

  // ============================================================
  // Derivation Stream
  // ============================================================

  public class LazyMEntityFnDerivs extends ModularDerivationStream {
    protected String getName() { return "MEntityFn"; }

    public LazyMEntityFnDerivs(Example ex, Callable c, double preciseProb) {
      super(ex, c, preciseProb);
    }

    protected void getFormulas() {
      formulas = new ArrayList<>();
      // Get all utterance n-grams (any length)
      List<NGram> utteranceNgrams = ModularUtils.getUtteranceNgrams(ex);
      if (opts.verbosity >= 5)
        LogInfo.logs("[%s] | %s", ex.utterance, utteranceNgrams);
      // Try to match each cell
      Map<NameValue, TableCell> cells = new HashMap<>();
      for (TableColumn column : graph.columns) {
        for (TableCell cell : column.children) {
          cells.put(cell.properties.entityNameValue, cell);
        }
      }
      for (Map.Entry<NameValue, TableCell> entry : cells.entrySet()) {
        String cellString = entry.getKey().description;
        boolean matched = false;
        // High-precision matching
        String collapseNormalized = StringNormalizationUtils.collapseNormalize(cellString);
        if (opts.verbosity >= 5)
          LogInfo.logs("%s | %s", cellString, collapseNormalized);
        for (NGram utteranceNgram : utteranceNgrams) {
          if (utteranceNgram.collapsed.equals(collapseNormalized)) {
            formulas.add(new Pair<>(new ValueFormula<>(entry.getKey()), new FeatureVector()));
            matched = true; break;
          }
        }
        if (matched) continue;
        // High-recall matching
        if (getPreciseProb() == 1) continue;
        String aggressivelyNormalized = StringNormalizationUtils.aggressiveNormalize(cellString);
        if (opts.verbosity >= 5)
          LogInfo.logs("%s | %s", cellString, aggressivelyNormalized);
        for (NGram utteranceNgram : utteranceNgrams) {
          if (ModularUtils.similarity(utteranceNgram.joined, aggressivelyNormalized)
                > MEntityFn.opts.modularFuzzyMatchThreshold) {
            formulas.add(new Pair<>(new ValueFormula<>(entry.getKey()), new FeatureVector()));
            matched = true; break;
          }
        }
      }
    }
  }

}
