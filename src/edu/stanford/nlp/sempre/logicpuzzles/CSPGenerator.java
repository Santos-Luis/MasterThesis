package edu.stanford.nlp.sempre.logicpuzzles;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.util.*;
import org.chocosolver.solver.*;
import org.chocosolver.solver.constraints.*;
import org.chocosolver.solver.search.strategy.*;
import org.chocosolver.solver.search.strategy.strategy.*;
import org.chocosolver.solver.variables.*;
import org.slf4j.LoggerFactory;

/**
 * CSPGenerator generates CSPs from BooleanExpressions.
 *
 * @author Robin Jia
 */
public class CSPGenerator {
  private List<BooleanExpression> exprs = new ArrayList<BooleanExpression>();
  private Set<String> variables = new HashSet<String>();

  /**
   * Turn off DEBUG-level logging for choco
   */
  static {
    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.INFO);
  }

  /**
   * Deal with the issue that choco expects "=" instead of "==".
   */
  public static String toChocoSumOperator(String s) {
    if (!BooleanExpression.isValidCountMode(s)) return null;
    if (s.equals("==")) return "=";
    return s;
  }

  public void addConstraint(BooleanExpression e) {
    Set<String> varNames = e.getVariableNames();
    variables.addAll(varNames);
    exprs.add(e);
  }

  private BoolVar createReifiedVariable(Solver solver, Map<String, BoolVar> nameToVar, BooleanExpression e) {
    BooleanExpression.Type type = e.getType();
    if (type == BooleanExpression.Type.constant) {
      if (e.getValue()) {
        return VariableFactory.one(solver);
      } else {
        return VariableFactory.zero(solver);
      }
    } else if (type == BooleanExpression.Type.var) {
      return nameToVar.get(e.getVar());
    } else if (type == BooleanExpression.Type.not) {
      BooleanExpression child = e.getChildren().get(0);
      BoolVar childVar = createReifiedVariable(solver, nameToVar, child);
      BoolVar retVar = VariableFactory.bool(null, solver);
      solver.post(IntConstraintFactory.sum(new BoolVar[] {childVar, retVar},
          VariableFactory.fixed(1, solver)));
      return retVar;
    } else if (type == BooleanExpression.Type.and || type == BooleanExpression.Type.or) {
      List<BooleanExpression> children = e.getChildren();
      List<BoolVar> vars = new ArrayList<BoolVar>();
      for (BooleanExpression child: children) {
        vars.add(createReifiedVariable(solver, nameToVar, child));
      }
      Constraint constraint;
      if (type == BooleanExpression.Type.and) {
        constraint = LogicalConstraintFactory.and(vars.toArray(new BoolVar[0]));
      } else {  // == BooleanExpression.Type.or
        constraint = LogicalConstraintFactory.or(vars.toArray(new BoolVar[0]));
      }
      return constraint.reif();
    } else if (type == BooleanExpression.Type.count) {
      List<BooleanExpression> children = e.getChildren();
      List<BoolVar> vars = new ArrayList<BoolVar>();
      for (BooleanExpression child: children) {
        vars.add(createReifiedVariable(solver, nameToVar, child));
      }
      String operator = toChocoSumOperator(e.getCountMode());
      IntVar num = VariableFactory.fixed(e.getCountNum(), solver);
      Constraint constraint = IntConstraintFactory.sum(vars.toArray(new BoolVar[0]), operator, num);
      return constraint.reif();
    } else {
      throw new RuntimeException("Encountered unexpected BooleanExpression type " + type + ".");
    }
  }

  private void addConstraintToSolver(Solver solver, Map<String, BoolVar> nameToVar, BooleanExpression e) {
    BoolVar var = createReifiedVariable(solver, nameToVar, e);
    SatFactory.addTrue(var);
  }

  public Solver createSolver() {
    Solver solver = new Solver();
    Map<String, BoolVar> nameToVar = new HashMap<String, BoolVar>();
    for (String name: variables) {
      BoolVar var = VariableFactory.bool(name, solver);
      nameToVar.put(name, var);
    }
    for (BooleanExpression e: exprs) {
      addConstraintToSolver(solver, nameToVar, e);
    }

    /* CSP Strategy magic:
     * impact seemed to work the best out of the available strategies.  domOverWDeg was second.
     * Pairing with lastConflict gives additional small performance boost.  */
    AbstractStrategy impact = IntStrategyFactory.impact(nameToVar.values().toArray(new IntVar[0]), 0L);
    solver.set(IntStrategyFactory.lastConflict(solver, impact));
    return solver;
  }
}
