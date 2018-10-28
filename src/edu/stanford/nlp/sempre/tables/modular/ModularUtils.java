package edu.stanford.nlp.sempre.tables.modular;

import java.util.*;

import edu.stanford.nlp.sempre.*;
import edu.stanford.nlp.sempre.tables.StringNormalizationUtils;
import edu.stanford.nlp.sempre.tables.TableTypeSystem;
import fig.basic.LispTree;

public class ModularUtils {

  public static class NGram {
    public final int fromIndex, toIndex;
    public final List<String> tokens, lemmaTokens;
    public final String joined, lemmaJoined;
    public final String collapsed, lemmaCollapsed;
    public NGram(Example ex, int fromIndex, int toIndex) {
      this.fromIndex = fromIndex;
      this.toIndex = toIndex;
      tokens = ex.getTokens().subList(fromIndex, toIndex);
      lemmaTokens = ex.getLemmaTokens().subList(fromIndex, toIndex);
      joined = String.join(" ", tokens);
      lemmaJoined = String.join(" ", lemmaTokens);
      collapsed = StringNormalizationUtils.collapseNormalize(joined);
      lemmaCollapsed = StringNormalizationUtils.collapseNormalize(lemmaJoined);
    }
    @Override public String toString() { return collapsed; }
  }

  /**
   * Return a list of n-grams from the utterance.
   */
  public static List<NGram> getUtteranceNgrams(Example ex) {
    List<NGram> ngrams = new ArrayList<>();
    int n = ex.numTokens();
    for (int s = 1; s <= n; s++) {
      for (int i = 0; i <= n - s; i++) {
        NGram ngram = new NGram(ex, i, i + s);
        if (!ngram.collapsed.isEmpty()) ngrams.add(ngram);
      }
    }
    return ngrams;
  }

  public static double similarity(String a, String b) {
    if (a.isEmpty() && b.isEmpty()) return 1;
    return levenshtein(a, b) * 2.0 / (a.length() + b.length());
  }

  public static int levenshtein(String a, String b) {
    int[][] m = new int[a.length() + 1][b.length() + 1];
    for (int i = 1; i <= a.length(); i++)
      m[i][0] = i;
    for (int j = 1; j <= b.length(); j++)
      m[0][j] = j;
    for (int i = 1; i <= a.length(); i++) {
      for (int j = 1; j <= b.length(); j++) {
        m[i][j] = Math.min(m[i-1][j-1] + ((a.charAt(i-1) == b.charAt(j-1)) ? 0 : 1), m[i-1][j] + 1);
        m[i][j] = Math.min(m[i][j], m[i][j-1] + 1);
      }
    }
    return m[a.length()][b.length()];
  }

  // ============================================================
  // LispTree Building
  // ============================================================

  public static LispTree L(Object ...stuff) {
    LispTree tree = LispTree.proto.newList();
    for (Object o : stuff) {
      LispTree child;
      if (o instanceof LispTree) {
        child = (LispTree) o;
      } else if (o instanceof String) {
        child = LispTree.proto.newLeaf((String) o);
      } else if (o instanceof NameValue) {
        child = LispTree.proto.newLeaf(((NameValue) o).id);
      } else {
        throw new RuntimeException("L(): Unknown type of " + o + ": " + o.getClass().getCanonicalName());
      }
      tree.addChild(child);
    }
    return (stuff.length == 1) ? tree.child(0) : tree;
  }

  public static String R(NameValue x) {
    return "!" + x.id;
  }

  // ============================================================
  // Test
  // ============================================================

  public static void main(String[] args) {
    NameValue baseRelation = new NameValue("fb:row.row.year");
    List<Formula> formulasSet = new ArrayList<>();
    formulasSet.add(Formulas.fromLispTree(L(baseRelation)));
    formulasSet.add(Formulas.fromLispTree(L("lambda", "x", L(baseRelation, L(TableTypeSystem.CELL_NUMBER_VALUE, L("var", "x"))))));
    formulasSet.add(Formulas.fromLispTree(L("lambda", "x", L(baseRelation, L(TableTypeSystem.CELL_DATE_VALUE, L("var", "x"))))));
    formulasSet.add(Formulas.fromLispTree(L(R(baseRelation))));
    formulasSet.add(Formulas.fromLispTree(L("lambda", "x", L(R(TableTypeSystem.CELL_NUMBER_VALUE), L(R(baseRelation), L("var", "x"))))));
    formulasSet.add(Formulas.fromLispTree(L("lambda", "x", L(R(TableTypeSystem.CELL_DATE_VALUE), L(R(baseRelation), L("var", "x"))))));
    for (Formula f : formulasSet) System.out.println(f);
  }


}
