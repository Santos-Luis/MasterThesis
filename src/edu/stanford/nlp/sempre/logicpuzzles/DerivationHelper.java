package edu.stanford.nlp.sempre.logicpuzzles;

import java.util.*;

import edu.stanford.nlp.sempre.*;

/**
 * Utilities for dealing with derivations
 *
 * @author Robin Jia
 */
public final class DerivationHelper {
  private DerivationHelper() { }

  /**
   * Returns a list of anchored child derivations that are of the given categories.
   *
   * Does a DFS over the derivation tree.
   * If categories is empty, return all anchored children.
   */
  public static List<Derivation> findAnchors(Derivation deriv, String... categories) {
    List<String> categoryList = Arrays.asList(categories);
    List<Derivation> answer = new ArrayList<Derivation>();
    Stack<Derivation> stack = new Stack<Derivation>();
    stack.add(deriv);
    while (!stack.isEmpty()) {
      Derivation cur = stack.pop();
      Rule rule = cur.rule;
      if (rule.isAnchored()) {
        if (categoryList.isEmpty() || categoryList.contains(rule.lhs)) {
          answer.add(cur);
        }
      }
      stack.addAll(cur.children);
    }
    return answer;
  }
}
