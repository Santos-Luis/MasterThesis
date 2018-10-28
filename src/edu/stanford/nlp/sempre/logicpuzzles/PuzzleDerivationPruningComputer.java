package edu.stanford.nlp.sempre.logicpuzzles;

import fig.basic.*;
import java.util.*;

import edu.stanford.nlp.sempre.*;

/**
 * A DerivationPruningComputer for logicpuzzles.
 *
 * @author Robin Jia
 */
public class PuzzleDerivationPruningComputer extends DerivationPruningComputer {
  public PuzzleDerivationPruningComputer(DerivationPruner pruner) {
    super(pruner);
  }

  public boolean isPruned(Derivation deriv) {
    if (pruneFormula(deriv)) return true;
    if (pruneExprs(deriv)) return true;
    return false;
  }

  private boolean pruneFormula(Derivation deriv) {
    if (containsStrategy("duplicateBinary") && pruneDuplicateBinary(deriv)) return true;
    if (containsStrategy("subjectAndObject") && pruneSubjectAndObject(deriv)) return true;
    if (containsStrategy("numberAsIdentifier") && pruneNumberAsIdentifier(deriv)) return true;
    if (containsStrategy("maximalEntity") && pruneMaximalEntity(deriv)) return true;
    if (containsStrategy("orderedAnchors")) {
      if (pruneOrderedAnchors(deriv)) return true;
    } else if (containsStrategy("orderedEntities")) {
      if (pruneOrderedEntities(deriv)) return true;
    }
    return false;
  }

  private boolean pruneExprs(Derivation deriv) {
    List<BooleanExpression> exprs = getExprs(deriv);
    if (exprs == null) return false;
    if (containsStrategy("allFalseDenotation") && pruneAllFalse(exprs)) return true;
    if (containsStrategy("allTrueDenotation") && pruneAllTrue(exprs)) return true;
    if (containsStrategy("nestedCount") && pruneNestedCount(exprs)) return true;
    if (containsStrategy("vacuousImplication") && pruneVacuousImplication(exprs)) return true;
    return false;
  }

  /**
   * Prune relations that look like (lambda x ((reverse binary) (binary (var x)))).
   *
   * Only prunes if b is a primitive binary.  Then, these relations are not useful.
   */
  private boolean pruneDuplicateBinary(Derivation deriv) {
    Formula formula = deriv.formula;
    if (!(formula instanceof LambdaFormula)) return false;
    Formula body = ((LambdaFormula) formula).body;
    if (!(body instanceof JoinFormula)) return false;
    Formula relation = ((JoinFormula) body).relation;
    if (!(relation instanceof ReverseFormula)) return false;
    Formula firstRelation = ((ReverseFormula) relation).child;
    if (!(firstRelation instanceof ValueFormula)) return false;
    Formula child = ((JoinFormula) body).child;
    if (!(child instanceof JoinFormula)) return false;
    Formula secondRelation = ((JoinFormula) child).relation;
    if (!(secondRelation instanceof ValueFormula)) return false;
    if (firstRelation.equals(secondRelation)) {
      if (DerivationPruner.opts.pruningVerbosity >= 2) {
        LogInfo.logs("PRUNED [duplicateBinary] %s", formula);
      }
      return true;
    }
    return false;
  }

  private String derivToStringWithSpan(Derivation deriv) {
    return deriv.toString() + "[" + deriv.start + "," + deriv.end + "]";
  }

  /**
   * Prune when the same mention of the same entity shows up as subject and object.
   */
  private boolean pruneSubjectAndObject(Derivation deriv) {
    List<Derivation> subjectMentions = DerivationHelper.findAnchors(deriv, "$SubjectEntity");
    List<Derivation> objectMentions = DerivationHelper.findAnchors(deriv, "$ObjectEntity");
    for (Derivation d1: subjectMentions) {
      String s1 = derivToStringWithSpan(d1);
      for (Derivation d2: objectMentions) {
        String s2 = derivToStringWithSpan(d2);
        if (s1.equals(s2)) {
          if (DerivationPruner.opts.pruningVerbosity >= 2) {
            LogInfo.logs("PRUNED [subjectAndObject] %s", deriv.formula);
          }
          return true;
        }

      }
    }
    return false;
  }

  /**
   * Do not generate numbers if they actually look like identifiers.
   *
   * e.g. if we see "table 2", don't generate the number 2.
   */
  private boolean pruneNumberAsIdentifier(Derivation deriv) {
    KnowledgeGraph graph = pruner.ex.context.graph;

    /* We want the current rule to have generated $Number */
    Rule rule = deriv.rule;
    if (!rule.lhs.equals("$Number")) return false;
    if (deriv.start < 0) return false;
    if (deriv.end - deriv.start > 1) return false;  // Only consider anchors of length 1

    /* Add token, previous word + token, and next word + token */
    List<String> queries = new ArrayList<String>();
    List<String> tokens = pruner.ex.getTokens();
    queries.add(tokens.get(deriv.start));
    if (deriv.start > 0) {
      queries.add(tokens.get(deriv.start - 1) + " " + tokens.get(deriv.start));
    }
    if (deriv.start < tokens.size() - 1) {
      queries.add(tokens.get(deriv.start) + " " + tokens.get(deriv.start + 1));
    }

    /* Check if this phrase shows up in the knowledge graph */
    for (String query: queries) {
      if (graph.getFuzzyMatchedFormulas(query, FuzzyMatchFn.FuzzyMatchFnMode.ENTITY).size() > 0) {
        if (DerivationPruner.opts.pruningVerbosity >= 2) {
          LogInfo.logs("numberAsIdentifier: " + query + " returned " +
              graph.getFuzzyMatchedFormulas(query, FuzzyMatchFn.FuzzyMatchFnMode.ENTITY));
          LogInfo.logs("PRUNED [numberAsIdentifier] %s", deriv.formula);
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Only match maximal spans when matching to an entity
   *
   * e.g. If the sentence contains "front position", don't match front to an entity.
   * Only allow the match of the entire phrase "front position".
   */
  private boolean pruneMaximalEntity(Derivation deriv) {
    KnowledgeGraph graph = pruner.ex.context.graph;

    /* We want the current rule to have generated $SubjectEntity or $ObjectEntity */
    Rule rule = deriv.rule;
    if (!(rule.lhs.equals("$SubjectEntity") || rule.lhs.equals("$ObjectEntity"))) return false;
    if (deriv.start < 0) return false;
    if (deriv.end - deriv.start > 1) return false;  // Only consider anchors of length 1

    /* Try previous word + token and next word + token */
    List<String> queries = new ArrayList<String>();
    List<String> tokens = pruner.ex.getTokens();
    if (deriv.start > 0) {
      queries.add(tokens.get(deriv.start - 1) + " " + tokens.get(deriv.start));
    }
    if (deriv.start < tokens.size() - 1) {
      queries.add(tokens.get(deriv.start) + " " + tokens.get(deriv.start + 1));
    }

    /* Check if this phrase shows up in the knowledge graph */
    for (String query: queries) {
      if (graph.getFuzzyMatchedFormulas(query, FuzzyMatchFn.FuzzyMatchFnMode.ENTITY).size() > 0) {
        if (DerivationPruner.opts.pruningVerbosity >= 2) {
          LogInfo.logs("maximalEntity: " + query + " returned " +
              graph.getFuzzyMatchedFormulas(query, FuzzyMatchFn.FuzzyMatchFnMode.ENTITY));
          LogInfo.logs("PRUNED [maximalEntity] %s", deriv.formula);
        }
        return true;
      }
    }
    return false;

  }

  /**
   * Enforce that entities are used in rules in the same order as in the sentence
   */
  private boolean pruneProjectiveAnchors(Derivation deriv, String name, String... categories) {
    List<Derivation> children = deriv.getChildren();
    if (children.size() < 2) return false;
    List<Derivation> leftDerivs = DerivationHelper.findAnchors(children.get(0), categories);
    List<Derivation> rightDerivs = DerivationHelper.findAnchors(children.get(1), categories);
    for (Derivation d1: leftDerivs) {
      for (Derivation d2: rightDerivs) {
        if (d1.start > d2.start) {
          if (DerivationPruner.opts.pruningVerbosity >= 2) {
            LogInfo.logs("PRUNED [%s] %s", name, deriv.formula);
          }
          return true;
        }
      }
    }
    return false;
  }

  private boolean pruneOrderedEntities(Derivation deriv) {
    return pruneProjectiveAnchors(deriv, "pruneOrderedEntities", "$SubjectEntity", "$ObjectEntity");
  }

  private boolean pruneOrderedAnchors(Derivation deriv) {
    return pruneProjectiveAnchors(deriv, "pruneOrderedAnchors");
  }

  /**
   * Extract the list of BooleanExpressions in the unary of a derivation
   */
  private List<BooleanExpression> getExprs(Derivation deriv) {
    deriv.ensureExecuted(pruner.parser.executor, pruner.ex.context);
    if (!(deriv.value instanceof ListValue)) return null;
    List<Value> values = ((ListValue) deriv.value).values;
    List<BooleanExpression> exprs = new ArrayList<BooleanExpression>();
    for (Value v: values) {
      if (!(v instanceof ListValue)) {
        LogInfo.logs("PuzzleDerivationPruningComputer.getExprs: found unexpected value.");
        return null;
      }
      List<Value> curPair = ((ListValue) v).values;
      if (!(curPair.get(1) instanceof StringValue)) {
        LogInfo.logs("PuzzleDerivationPruningComputer.getExprs: found unexpected value.");
        return null;
      }
      String curString = ((StringValue) curPair.get(1)).value;
      exprs.add(BooleanExpression.fromString(curString));
    }
    return exprs;
  }

  /**
   * Prune denotations that are sets where every boolean expression equals some value.
   *
   * Usually used with boolExpr as BooleanExpression.TRUE or BooleanExpression.FALSE.
   */
  private boolean pruneAllSame(List<BooleanExpression> exprs, BooleanExpression boolExpr) {
    for (BooleanExpression e: exprs) {
      if (!e.equals(boolExpr)) return false;
    }
    if (DerivationPruner.opts.pruningVerbosity >= 2) {
      LogInfo.logs("PRUNED [allSame == %s]", boolExpr);
    }
    return true;
  }

  private boolean pruneAllFalse(List<BooleanExpression> exprs) {
    return pruneAllSame(exprs, BooleanExpression.FALSE);
  }

  private boolean pruneAllTrue(List<BooleanExpression> exprs) {
    /* Avoid pruning unaries, which are all true but are useful for typing reasons */
    if (exprs.size() > 1) return false;
    return pruneAllSame(exprs, BooleanExpression.TRUE);
  }

  /**
   * Find all count expressions (don't chase nested counts).
   */
  private List<BooleanExpression> getCountExprs(BooleanExpression e) {
    if (e.getType() == BooleanExpression.Type.count) return Collections.singletonList(e);
    List<BooleanExpression> children = e.getChildren();
    if (children.isEmpty()) return Collections.emptyList();
    List<BooleanExpression> answer = new ArrayList<BooleanExpression>();
    for (BooleanExpression child: children) {
      answer.addAll(getCountExprs(child));
    }
    return answer;
  }

  /**
   * Prune denotations where a count is nested within another count
   */
  private boolean pruneNestedCount(List<BooleanExpression> exprs) {
    for (BooleanExpression e: exprs) {
      List<BooleanExpression> counts = getCountExprs(e);
      for (BooleanExpression countExpr: counts) {
        for (BooleanExpression child: countExpr.getChildren()) {
          if (getCountExprs(child).size() > 0) {
            if (DerivationPruner.opts.pruningVerbosity >= 2) {
              LogInfo.logs("PRUNED [nestedCount] %s", exprs);
            }
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Prune statements of the form If P then P.
   */
  private boolean pruneVacuousImplication(List<BooleanExpression> exprs) {
    /* Want to also catch ones nested within and's */
    Stack<BooleanExpression> stack = new Stack<BooleanExpression>();
    stack.addAll(exprs);
    while (!stack.isEmpty()) {
      BooleanExpression e = stack.pop();
      if (e.getType() == BooleanExpression.Type.or) {
        List<BooleanExpression> children = e.getChildren();
        /* Find the single child that is a NOT expression.
         * OR everything else together. */
        boolean isVacuous = false;
        for (int i = 0; i < children.size(); ++i) {
          BooleanExpression child = children.get(i);
          if (child.getType() != BooleanExpression.Type.not) continue;
          ArrayList<BooleanExpression> clone = new ArrayList<BooleanExpression>(children);
          clone.remove(i);
          BooleanExpression otherChild = BooleanExpression.or(clone.toArray(new BooleanExpression[0]));
          if (child.getChildren().get(0).equivHeuristic(otherChild)) {
            isVacuous = true;
            break;
          }
        }
        if (!isVacuous) return false;
      } else if (e.getType() == BooleanExpression.Type.and) {
        stack.addAll(e.getChildren());
      } else {
        return false;
      }
    }
    if (DerivationPruner.opts.pruningVerbosity >= 2) {
      LogInfo.logs("PRUNED [vacuousImplication] %s", exprs);
    }
    return true;
  }
}
