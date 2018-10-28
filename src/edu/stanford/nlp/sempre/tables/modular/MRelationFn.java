package edu.stanford.nlp.sempre.tables.modular;

import java.util.*;

import edu.stanford.nlp.sempre.*;
import edu.stanford.nlp.sempre.tables.StringNormalizationUtils;
import edu.stanford.nlp.sempre.tables.TableColumn;
import edu.stanford.nlp.sempre.tables.TableTypeSystem;
import edu.stanford.nlp.sempre.tables.TableKnowledgeGraph.*;
import edu.stanford.nlp.sempre.tables.modular.ModularUtils.NGram;
import fig.basic.*;
import static edu.stanford.nlp.sempre.tables.modular.ModularUtils.L;
import static edu.stanford.nlp.sempre.tables.modular.ModularUtils.R;
/**
 * Relation generation module.
 *
 * Modes:
 * - High-Recall: Generate all possible relations
 * - High-Precision: Generate only the relations mentioned in the utterance
 *
 * @author ppasupat
 */
public class MRelationFn extends ModularSemanticFn {

  private boolean reverse = false;

  public void init(LispTree tree) {
    super.init(tree);
    for (int i = 1; i < tree.children.size(); i++) {
      String value = tree.child(i).value;
      if ("reverse".equals(value)) this.reverse = true;
      else throw new RuntimeException("Invalid argument: " + value);
    }
  }

  @Override
  public DerivationStream call(Example ex, Callable c) {
    return new LazyMRelationFnDerivs(ex, c, getPreciseProb());
  }

  // ============================================================
  // Derivation Stream
  // ============================================================

  public class LazyMRelationFnDerivs extends ModularDerivationStream {
    protected String getName() { return "MRelationFn"; }

    public LazyMRelationFnDerivs(Example ex, Callable c, double preciseProb) {
      super(ex, c, preciseProb);
    }

    protected void getFormulas() {
      formulas = new ArrayList<>();
      List<NGram> utteranceNgrams = ModularUtils.getUtteranceNgrams(ex);
      if (opts.verbosity >= 5)
        LogInfo.logs("[%s] | %s", ex.utterance, utteranceNgrams);
      for (TableColumn column : graph.columns) {
        if (preciseProb != 1) {
          // High recall --> add all relations
          generateRelations(column.propertyNameValue);
        } else {
          // High precision --> add only matched relations
          String columnString = StringNormalizationUtils.collapseNormalize(column.originalString);
          if (opts.verbosity >= 5)
            LogInfo.logs("%s", columnString);
          for (NGram utteranceNgram : utteranceNgrams) {
            if (utteranceNgram.collapsed.equals(columnString)) {
              generateRelations(column.propertyNameValue);
            }
          }
        }
      }

    }

    private void generateRelations(NameValue baseRelation) {
      if (!reverse) {
        formulas.add(new Pair<>(Formulas.fromLispTree(L(baseRelation)), new FeatureVector()));
        formulas.add(new Pair<>(Formulas.fromLispTree(L("lambda", "x",
            L(baseRelation, L(TableTypeSystem.CELL_NUMBER_VALUE, L("var", "x"))))),
            new FeatureVector()));
        formulas.add(new Pair<>(Formulas.fromLispTree(L("lambda", "x",
            L(baseRelation, L(TableTypeSystem.CELL_DATE_VALUE, L("var", "x"))))),
            new FeatureVector()));
      } else {
        formulas.add(new Pair<>(Formulas.fromLispTree(L(R(baseRelation))), new FeatureVector()));
        formulas.add(new Pair<>(Formulas.fromLispTree(L("lambda", "x",
            L(R(TableTypeSystem.CELL_NUMBER_VALUE), L(R(baseRelation), L("var", "x"))))),
            new FeatureVector()));
        formulas.add(new Pair<>(Formulas.fromLispTree(L("lambda", "x",
            L(R(TableTypeSystem.CELL_DATE_VALUE), L(R(baseRelation), L("var", "x"))))),
            new FeatureVector()));
      }
    }

  }

}
