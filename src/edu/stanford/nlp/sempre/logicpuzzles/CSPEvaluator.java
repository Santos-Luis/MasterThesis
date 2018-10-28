package edu.stanford.nlp.sempre.logicpuzzles;

import fig.basic.*;
import java.util.*;
import org.chocosolver.solver.*;
import org.chocosolver.solver.search.loop.monitors.*;

import edu.stanford.nlp.sempre.*;

/**
 * CSPEvaluator evaluates BooleanExpressions for equivalence.
 *
 * Each BooleanExpression represents a CSP constraint.
 * Constraints c1 and c2 are equivalent iff the following both hold:
 *   - (and (not c1) c2) is not satisfiable (means c2 implies c1)
 *   - (and (not c2) c1) is not satisfiable (means c1 implies c2)
 *
 *  An alterantive supervision mode also supported by this class is to use
 *  lists of assignments that are supposed to either satisfy or not satisfy the
 *  current rule.  These are sourced from mturk.  For these, the target value
 *  looks like the following:
 *    ROOT: (mturk (yes S_1 ... S_n) (no S_1 ... S_n'))
 *    S: (A_1 ... A_m)
 *    A: (relation_name value_1 ... value_k)
 *
 * @author Robin Jia
 */
public class CSPEvaluator implements ValueEvaluator {
  public static class Options {
    @Option(gloss = "Solver time limit (ms)") public int solverTimeLimit = 1000;
    @Option(gloss = "Verbosity") public int verbosity = 0;
  }
  public static Options opts = new Options();

  public static class TimeLimitExceededException extends RuntimeException {
    public TimeLimitExceededException(String s) {
      super(s);
    }
  }

  /* Keep track of what targetValue's have been seen already */
  private static Set<Value> seenTargetValues = new HashSet<Value>();

  public double getCompatibility(Value target, Value pred) {
    if (target instanceof ErrorValue) {
      throw new RuntimeException("Received invalid target value " + target);
    }
    if (target instanceof StringValue) {
      return getCompatibilityTurkSupervision(target, pred);
    } else {
      return getCompatibilityCloseSupervision(target, pred);
    }
  }

  private boolean evaluateExprOnAssignment(BooleanExpression expr, LispTree tree) {
    Set<String> trueRecordNames = new HashSet<String>();
    for (LispTree child: tree.children) {
      String relationName = child.head().value;
      List<String> entities = new ArrayList<String>();
      for (LispTree subchild: child.tail().children) {
        entities.add(subchild.value.toLowerCase());
      }
      String varName = PuzzleTypeSystem.createRecordDescription(
          relationName, entities);
      trueRecordNames.add(varName);
    }
    return expr.eval(trueRecordNames);
  }

  public double getCompatibilityTurkSupervision(Value target, Value pred) {
    BooleanExpression predExpr = getExpr(pred);
    LispTree targetTree = LispTree.proto.parseFromString(((StringValue) target).value);
    if (!targetTree.child(0).value.equals("mturk")) {
      throw new RuntimeException("Received invalid target StringValue "
          + target + ", expected to start with mturk.");
    }
    List<LispTree> yesTrees = targetTree.child(1).tail().children;
    List<LispTree> noTrees = targetTree.child(2).tail().children;
    for (LispTree tree: yesTrees) {
      if (!evaluateExprOnAssignment(predExpr, tree)) return 0;
    }
    for (LispTree tree: noTrees) {
      if (evaluateExprOnAssignment(predExpr, tree)) return 0;
    }
    return 1;
  }

  public double getCompatibilityCloseSupervision(Value target, Value pred) {
    boolean log = opts.verbosity >= 2 || opts.verbosity >= 1 && !seenTargetValues.contains(target);
    if (log) {
      LogInfo.begin_track("CSPEvaluator.getCompatibilityCloseSupervision()");
      if (!seenTargetValues.contains(target)) {
        LogInfo.logs("Received new target value: " + target);
        seenTargetValues.add(target);
      } else if (opts.verbosity >= 2) {
        LogInfo.logs("Target value = " + target);
      }
      if (opts.verbosity >= 2) {
        LogInfo.logs("Predicted value = " + pred);
      }
    }

    BooleanExpression targetExpr = getExpr(target);
    BooleanExpression predExpr = getExpr(pred);
    if (opts.verbosity >= 2) {
      LogInfo.logs("Target expr = " + targetExpr);
      LogInfo.logs("Predicted expr = " + predExpr);
    }

    if (!isValid(targetExpr)) {
      throw new RuntimeException("Received invalid target value " + target);
    }
    if (!isValid(predExpr)) {
      throw new RuntimeException("Received invalid prediction value " + pred);
    }

    double answer = exprsEqual(targetExpr, predExpr) ? 1 : 0;
    if (log) {
      LogInfo.end_track();
    }
    return answer;
  }

  /**
   * Checks if this defines a reasonable constraint
   */
  public static boolean isValid(BooleanExpression e) {
    return e.getType() != BooleanExpression.Type.constant;
  }

  public static BooleanExpression getExpr(Value v) {
    if (!(v instanceof ListValue)) {
      throw new RuntimeException("CSPEvaluator: Received non-ListValue " + v);
    }
    List<Value> values = ((ListValue) v).values;
    if (values.size() != 1) {
      throw new RuntimeException("CSPEvaluator: value must be list of size 1 (received " +
          values.size() + ").");
    }
    ListValue pair = (ListValue) values.get(0);
    if (pair.values.size() != 2) {
      throw new RuntimeException("CSPEvaluator: requires pairs.");
    }
    Value trueValue = pair.values.get(0);
    if (!trueValue.equals(new BooleanValue(true))) {
      throw new RuntimeException("CSPEvaluator: value must contain (boolean true).");
    }
    StringValue stringValue = (StringValue) pair.values.get(1);
    return BooleanExpression.fromString(stringValue.value);
  }

  /**
   * Return true iff e1 implies e2, equivalent to NOT(e1 AND NOT e2).
   *
   * Throws TimeLimitExceededException if solver takes too long.
   */
  private boolean checkImplies(BooleanExpression e1, BooleanExpression e2) {
    CSPGenerator gen = new CSPGenerator();
    gen.addConstraint(e1);
    gen.addConstraint(BooleanExpression.not(e2));
    Solver solver = gen.createSolver();
    SearchMonitorFactory.limitTime(solver, opts.solverTimeLimit);
    boolean answer = !solver.findSolution();
    if (solver.hasReachedLimit()) {
      throw new TimeLimitExceededException("Reached Solver's time limit (= "
          + opts.solverTimeLimit + " ms).");
    }
    return answer;
  }

  private boolean exprsEqual(BooleanExpression e1, BooleanExpression e2) {
    boolean answer;

    try {
      /* Check using CSP solver */
      answer = checkImplies(e1, e2) && checkImplies(e2, e1);
    } catch (TimeLimitExceededException e) {
      /* Solver took too long, return false */
      if (opts.verbosity >= 1) {
        LogInfo.logs("CSPEvaluator: time limit exceeded in solver");
      }
      answer = false;
    }

    /* Log results */
    if (opts.verbosity >= 2) {
      LogInfo.logs("Equal: " + Boolean.toString(answer));
    }
    return answer;
  }
}
