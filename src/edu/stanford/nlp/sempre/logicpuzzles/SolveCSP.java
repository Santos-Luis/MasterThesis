package edu.stanford.nlp.sempre.logicpuzzles;

import fig.basic.*;
import fig.exec.Execution;
import java.util.*;
import org.chocosolver.solver.*;

import edu.stanford.nlp.sempre.*;

/**
 * Solve a CSP defined by the targetFormula's of a Dataset.
 */
public class SolveCSP implements Runnable {
  public void run() {
    Builder builder = new Builder();
    builder.build();

    Dataset dataset = new Dataset();
    dataset.read();
    LogInfo.logs(dataset.groups().toString());

    List<Example> examples = dataset.examples("train");
    Executor executor = builder.executor;
    CSPGenerator gen = new CSPGenerator();
    for (Example e: examples) {
      Formula formula = e.targetFormula;
      ContextValue context = e.context;
      Value value = executor.execute(formula, context).value;
      LogInfo.logs(value.toString());
      gen.addConstraint(CSPEvaluator.getExpr(value));
    }
    Solver solver = gen.createSolver();
    solver.findSolution();
    LogInfo.logs("Feasible: " + solver.isFeasible());
    LogInfo.logs("Solution: " + solver.getSolutionRecorder().getLastSolution());
  }

  public static void main(String[] args) {
    Execution.run(args, "SolveCSP", new SolveCSP(), Master.getOptionsParser());
  }
}
