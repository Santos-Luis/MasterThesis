package edu.stanford.nlp.sempre.logicpuzzles;

import fig.basic.*;
import java.util.*;

import edu.stanford.nlp.sempre.*;

/**
 * A FeatureComputer for logicpuzzles.
 *
 * @author Robin Jia
 */
public class PuzzleFeatureComputer implements FeatureComputer {
  @Override public void extractLocal(Example ex, Derivation deriv) {
    addProjectiveLinkageFeatures(ex, deriv);
    addAnchorCountFeatures(ex, deriv);
  }

  private void computeAllAnchorsHelper(Derivation deriv, Map<String, List<Integer>> anchorMap) {
    List<Integer> anchors = new ArrayList<Integer>();
    Rule rule = deriv.rule;
    if (rule.isAnchored()) {
      if (deriv.start < 0) {
        throw new RuntimeException("Anchored rule starts at " + deriv.start);
      }
      anchors.add(deriv.start);
    } else {
      for (Derivation child: deriv.getChildren()) {
        computeAllAnchorsHelper(child, anchorMap);
        anchors.addAll(anchorMap.get(child.toString()));
      }
    }
    anchorMap.put(deriv.toString(), anchors);
  }

  private Map<String, List<Integer>> computeAllAnchors(Derivation deriv) {
    Map<String, List<Integer>> anchorMap = new HashMap<String, List<Integer>>();
    computeAllAnchorsHelper(deriv, anchorMap);
    return anchorMap;
  }

  private int computeLinkage(Derivation deriv, Map<String, List<Integer>> anchorMap) {
    List<Derivation> children = deriv.getChildren();
    if (children.size() == 2) {
      int d1 = computeLinkage(children.get(0), anchorMap);
      int d2 = computeLinkage(children.get(1), anchorMap);
      List<Integer> anchors1 = anchorMap.get(children.get(0).toString());
      List<Integer> anchors2 = anchorMap.get(children.get(1).toString());
      if (anchors1.isEmpty() || anchors2.isEmpty()) {
        return d1 + d2;
      } else {
        int minDistance = Integer.MAX_VALUE;
        for (int a1: anchors1) {
          for (int a2: anchors2) {
            int curDistance = Math.abs(a2 - a1);
            if (curDistance < minDistance) {
              minDistance = curDistance;
            }
          }
        }
        return d1 + d2 + minDistance;
      }
    } else if (children.size() == 1) {
      return computeLinkage(children.get(0), anchorMap);
    } else if (children.size() == 0) {
      return 0;
    }
    throw new RuntimeException("PuzzleFeatureComputer.computeLinkage: got derivation with "
        + children.size() + "children");
  }

  private void addProjectiveLinkageFeatures(Example ex, Derivation deriv) {
    if (!FeatureExtractor.containsDomain("projectivity")) return;
    if (!deriv.cat.equals(Rule.rootCat)) return;
    Map<String, List<Integer>> anchorMap = computeAllAnchors(deriv);
    int linkage = computeLinkage(deriv, anchorMap);
    LogInfo.logs("%s: projectivity.singleLinkage = %d", deriv.toString(), linkage);
    deriv.addFeature("projectivity", "singleLinkage", -linkage);
  }

  private void addAnchorCountFeatures(Example ex, Derivation deriv) {
    if (!FeatureExtractor.containsDomain("anchorCounts")) return;
    if (!deriv.cat.equals(Rule.rootCat)) return;
    List<Derivation> anchors = DerivationHelper.findAnchors(deriv);
    Set<Integer> anchoredIndices = new HashSet<Integer>();  // Which words in the sentence were used
    for (Derivation anchor: anchors) {
      for (int i = anchor.start; i < anchor.end; ++i) {
        anchoredIndices.add(i);
      }
    }
    deriv.addFeature("anchorCounts", "all", anchoredIndices.size());
  }
}
