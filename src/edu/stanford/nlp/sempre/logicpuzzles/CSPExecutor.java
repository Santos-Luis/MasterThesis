package edu.stanford.nlp.sempre.logicpuzzles;

import com.google.common.base.Function;
import fig.basic.*;
import java.util.*;

import edu.stanford.nlp.sempre.*;

/**
 * CSPExecutor takes a Lambda-DCS Formula and creates a CSP constraint.
 *
 * We use a "typed" version of Lambda-DCS that associates every expression
 * with a type, and each type has a finite domain.  We depart from
 * vanilla Lambda-DCS in teh following ways:
 *   - NotFormula behaves differently from in standard Lambda-DCS, as it takes the complement
 *     with respect to the domain of the type, rather than the entire universe.
 *     See FiniteUnary/FiniteBinary for more details.
 *   - CountFormula and comparators (&lt;, &ge;, etc.) is only supported in specific uses.
 *     In particular, the only valid way to use them is
 *     (call exists (and (count [unary]) ([comp] (number [int])))).
 *     The denotation of this formula is unary with domain = {(boolean true)}
 *     giving whether the condition is true or not.
 *   - A binary denotation whose range is boolean can be automatically "cast"
 *     to a unary denotation.  This is useful to write down constraints such as
 *     "Each team has at least 3 members".  This would be represented as
 *     (call forall (lambda t (call exists (and (count (assigned (var t))) (&ge; 3))))).
 *     The denotation of the lambda is technicall a binary that maps from teams to booleans,
 *     but it is implicitly converted to a unary before the forall.
 *
 * Responses will be BooleanExpression objects.
 *
 * A couple custom operators are supported, via CallFormula:
 *   - (call exists [unary]): Returns a unary that is true iff argument has at least one element
 *   - (call forall [unary]): Returns a unary that is true iff all elements in argument are present.
 *
 * @author Robin Jia
 */
public class CSPExecutor extends Executor {
  public static class Options {
    @Option(gloss = "Verbosity") public int verbosity = 0;

    @Option(gloss = "Throw Exceptions instead of catching silently, for debugging.")
    public boolean throwExceptions = false;
  }
  public static Options opts = new Options();

  private FiniteUnary valueToUnary(Value value, PuzzleKnowledgeGraph graph) {
    if (value instanceof NameValue) {
      String id = ((NameValue) value).id;
      FiniteUnary unary = graph.getUnary(id);
      if (unary == null) {
        throw new RuntimeException("No unary matching id " + id);
      }
      return unary;
    } else {
      throw new RuntimeException("Unsupported ValueFormula with value " + value + ".");
    }
  }

  private FiniteUnary joinToUnary(JoinFormula join, PuzzleKnowledgeGraph graph,
      Map<String, FiniteUnary> variableValues) {
    Formula relationFormula = join.relation;
    Formula childFormula = join.child;
    FiniteBinary relationBinary = makeFiniteBinary(relationFormula, graph, variableValues);
    FiniteUnary childUnary = makeFiniteUnary(childFormula, graph, variableValues);
    return relationBinary.join(childUnary);
  }

  private FiniteUnary mergeToUnary(MergeFormula merge, PuzzleKnowledgeGraph graph,
      Map<String, FiniteUnary> variableValues) {
    MergeFormula.Mode mode = merge.mode;
    Formula child1 = merge.child1;
    Formula child2 = merge.child2;
    FiniteUnary unary1 = makeFiniteUnary(child1, graph, variableValues);
    FiniteUnary unary2 = makeFiniteUnary(child2, graph, variableValues);
    if (mode == MergeFormula.Mode.and) {
      return FiniteUnary.and(unary1, unary2);
    } else {  // mode == MergeFormula.Mode.or
      return FiniteUnary.or(unary1, unary2);
    }
  }

  /**
   * Handles special case of (call exists (and (count [unary]) ([comp] (number [int])))).
   *
   * Returns null as sentinel that this special case does not apply here.
   */
  private FiniteUnary existsAndCountToUnary(CallFormula call, PuzzleKnowledgeGraph graph,
      Map<String, FiniteUnary> variableValues) {
    String defaultErrorMsg = "Use of AggregateFormula only supported using pattern " +
          "(call exists (and (count [unary]) ([comp] (number [int]))))";
    Formula func = call.func;
    List<Formula> args = call.args;
    if (!func.toString().equals("exists")) return null;
    if (!(args.get(0) instanceof MergeFormula)) return null;
    MergeFormula merge = (MergeFormula) args.get(0);
    if (merge.mode != MergeFormula.Mode.and) return null;
    Formula child1 = merge.child1;
    Formula child2 = merge.child2;
    if (!(child1 instanceof AggregateFormula)) return null;
    AggregateFormula agg = (AggregateFormula) child1;
    if (agg.mode != AggregateFormula.Mode.count) {
      throw new RuntimeException("Received AggregateFormula with unsupported mode " + agg.mode);
    }
    FiniteUnary unary = makeFiniteUnary(agg.child, graph, variableValues);
    if (unary.getDomain().isEmpty()) {
      /* Type-check failure occurred downstream, return the empty unary */
      return unary;
    }
    if (!(child2 instanceof JoinFormula)) {
      throw new RuntimeException(defaultErrorMsg + ", received " + child1 + " as second argument.");
    }
    JoinFormula join = (JoinFormula) child2;
    if (!(join.relation instanceof ValueFormula)) {
      throw new RuntimeException(defaultErrorMsg + ", received bad comparator " + join.relation);
    }
    ValueFormula relation = (ValueFormula) join.relation;
    if (!(relation.value instanceof NameValue)) {
      throw new RuntimeException(defaultErrorMsg + ", received bad comparator " + relation);
    }
    NameValue comparatorValue = (NameValue) relation.value;
    String comparator = comparatorValue.id;

    if (!(join.child instanceof ValueFormula)) {
      throw new RuntimeException(defaultErrorMsg + ", received bad number " + join.child);
    }
    ValueFormula joinChild = (ValueFormula) join.child;
    if (!(joinChild.value instanceof NumberValue)) {
      throw new RuntimeException(defaultErrorMsg + ", received bad number " + joinChild);
    }
    NumberValue joinChildValue = (NumberValue) joinChild.value;
    int num = (int) joinChildValue.value;

    /* Everything looks good, now return a FiniteUnary */
    BooleanValue trueValue = new BooleanValue(true);
    FiniteUnary answer = new FiniteUnary(Collections.singletonList(trueValue));
    List<BooleanExpression> exprs = new ArrayList<BooleanExpression>();
    for (Value v: unary.getDomain()) {
      exprs.add(unary.get(v));
    }
    BooleanExpression[] exprArray = exprs.toArray(new BooleanExpression[0]);
    answer.put(trueValue, BooleanExpression.count(comparator, num, exprArray));
    return answer;
  }


  private FiniteUnary callToUnary(CallFormula call, PuzzleKnowledgeGraph graph,
      Map<String, FiniteUnary> variableValues) {
    Formula func = call.func;
    List<Formula> args = call.args;
    if (func.toString().equals("exists") || func.toString().equals("forall")) {
      if (args.size() != 1) {
        throw new RuntimeException("CallFormula " + func.toString() + " received " + args.size() +
            " arguments, expected 1.");
      }
      /* Check for special case of (call exists (and (count [unary]) ([comp] (number [int])))) */
      FiniteUnary countUnary = existsAndCountToUnary(call, graph, variableValues);
      if (countUnary != null) return countUnary;

      /* Default case */
      FiniteUnary childUnary = makeFiniteUnary(args.get(0), graph, variableValues);

      /* Check if child had type-check failure  */
      if (childUnary.getDomain().size() == 0) {
        return FiniteUnary.EMPTY;
      }

      BooleanValue trueValue = new BooleanValue(true);
      FiniteUnary answer = new FiniteUnary(Collections.singletonList(trueValue));
      List<BooleanExpression> exprs = new ArrayList<BooleanExpression>();
      for (Value v: childUnary.getDomain()) {
        exprs.add(childUnary.get(v));
      }
      BooleanExpression[] exprArray = exprs.toArray(new BooleanExpression[0]);
      if (func.toString().equals("exists")) {
        answer.put(trueValue, BooleanExpression.or(exprArray));
      } else { // forall
        answer.put(trueValue, BooleanExpression.and(exprArray));
      }
      return answer;
    } else {
      throw new RuntimeException("Received CallFormula with unknown function " + func + ".");
    }
  }

  private FiniteBinary lambdaToBinary(LambdaFormula lambda, PuzzleKnowledgeGraph graph,
      Map<String, FiniteUnary> variableValues) {
    String var = lambda.var;
    Formula body = lambda.body;
    List<Value> domain = inferVariableType(body, graph, variableValues, var);
    List<FiniteUnary> unaries = new ArrayList<FiniteUnary>();
    for (Value d: domain) {
      FiniteUnary varUnary = FiniteUnary.makeSingleton(domain, d);
      variableValues.put(var, varUnary);
      unaries.add(makeFiniteUnary(body, graph, variableValues));
      variableValues.remove(var);
    }
    return FiniteBinary.fromUnaries(domain, unaries);
  }

  private FiniteBinary valueToBinary(Value value, PuzzleKnowledgeGraph graph) {
    if (value instanceof NameValue) {
      String id = ((NameValue) value).id;
      return graph.getBinary(id);
    } else {
      throw new RuntimeException("Unsupported ValueFormula with value " + value + ".");
    }
  }

  public FiniteUnary makeFiniteUnary(Formula formula, PuzzleKnowledgeGraph graph,
      Map<String, FiniteUnary> variableValues) {
    try {
      if (formula instanceof ValueFormula) {
        return valueToUnary(((ValueFormula<?>) formula).value, graph);
      } else if (formula instanceof JoinFormula) {
        return joinToUnary((JoinFormula) formula, graph, variableValues);
      } else if (formula instanceof VariableFormula) {
        String var = ((VariableFormula) formula).name;
        return variableValues.get(var);
      } else if (formula instanceof MergeFormula) {
        return mergeToUnary((MergeFormula) formula, graph, variableValues);
      } else if (formula instanceof NotFormula) {
        Formula childFormula = ((NotFormula) formula).child;
        FiniteUnary childUnary = makeFiniteUnary(childFormula, graph, variableValues);
        return FiniteUnary.not(childUnary);
      } else if (formula instanceof CallFormula) {
        return callToUnary((CallFormula) formula, graph, variableValues);
      } else if (formula instanceof ArithmeticFormula) {
        throw new RuntimeException("ArithmeticFormula not supported.");
      } else if (formula instanceof MarkFormula) {
        throw new RuntimeException("MarkFormula not supported.");
      } else if (formula instanceof SuperlativeFormula) {
        throw new RuntimeException("SuperlativeFormula not supported.");
      } else if (formula instanceof AggregateFormula) {
        throw new RuntimeException("AggregateFormula not supported except in special pattern " +
            "(call exists (and (count [unary]) ([comp] (number [int])))).");
      } else {
        throw new RuntimeException("Received unexpected unary formula " + formula + ".");
      }
    } catch (Exception e1) {
      /* See if we can evaluate the expression as a binary whose range is boolean */
      try {
        FiniteBinary binary = makeFiniteBinary(formula, graph, variableValues);
        if (binary.getRange().size() == 0) {
          /* This was a binary, but it had a type-check failure. */
          return FiniteUnary.EMPTY;
        } else if (binary.getRange().size() != 1) {
          throw e1;
        }
        Value rangeObject = binary.getRange().get(0);
        if (!(rangeObject instanceof BooleanValue) || !((BooleanValue) rangeObject).value) {
          throw e1;
        }
        FiniteUnary unary = new FiniteUnary(binary.getDomain());
        for (Value v: binary.getDomain()) {
          unary.put(v, binary.get(v, rangeObject));
        }
        return unary;
      } catch (Exception e2) {
        if (opts.verbosity >= 1) {
          LogInfo.logs("Tried to convert " + formula + " to binary, got error: " + e2.getMessage());
        }
        throw e1;
      }
    }
  }

  public FiniteBinary makeFiniteBinary(Formula formula, PuzzleKnowledgeGraph graph,
      Map<String, FiniteUnary> variableValues) {
    if (formula instanceof LambdaFormula) {
      return lambdaToBinary((LambdaFormula) formula, graph, variableValues);
    } else if (formula instanceof ValueFormula) {
      return valueToBinary(((ValueFormula) formula).value, graph);
    } else if (formula instanceof ReverseFormula) {
      Formula child = ((ReverseFormula) formula).child;
      FiniteBinary childBinary = makeFiniteBinary(child, graph, variableValues);
      return FiniteBinary.reverse(childBinary);
    } else {
      throw new RuntimeException("Received unexpected binary formula " + formula + ".");
    }
  }

  /**
   * Takes the body of a lambda, infers the domain (type) of the given variable.
   *
   * Currently, infers types when the variable x is used in one of these patterns:
   *   - (binary (var x)): use the domain of the binary
   */
  public List<Value> inferVariableType(Formula formula, PuzzleKnowledgeGraph graph,
      Map<String, FiniteUnary> variableValues, String varName) {
    List<Formula> list = formula.mapToList(
        new Function<Formula, List<Formula>>() {
          public List<Formula> apply(Formula formula) {
            if (formula instanceof LambdaFormula) {
              // If this is a nested lambda with the same variable name, throw an error.
              String curVar = ((LambdaFormula) formula).var;
              if (curVar.equals(varName)) {
                throw new RuntimeException("Nested lambdas use same variable " + curVar + ".");
              }
            } else if (formula instanceof JoinFormula) {
              Formula relation = ((JoinFormula) formula).relation;
              Formula child = ((JoinFormula) formula).child;
              if (child instanceof VariableFormula) {
                String curVar = ((VariableFormula) child).name;
                if (curVar.equals(varName)) {
                  List<Value> domain = makeFiniteBinary(relation, graph, variableValues).getDomain();
                  List<Formula> ret = new ArrayList<Formula>();
                  ret.add(new ValueFormula<ListValue>(new ListValue(domain)));
                  return ret;
                }
              }
            }
            return new ArrayList<Formula>();
          }
        }, false);
    if (list.isEmpty()) {
      throw new RuntimeException("Could not infer type of " + varName + " within " + formula + ".");
    }
    // TODO(robinjia): decide on better behavior if list.size() > 1.
    // Currently just choose any of the types.
    return ((ValueFormula<ListValue>) list.get(0)).value.values;
  }

  @Override public Response execute(Formula formula, ContextValue context) {
    KnowledgeGraph graph = context.graph;
    if (graph == null || !(graph instanceof PuzzleKnowledgeGraph)) {
      throw new RuntimeException("CSPExecutor requires that context contains PuzzleKnowledgeGraph");
    }

    formula = Formulas.betaReduction(formula);
    if (opts.verbosity >= 3) {
      LogInfo.logs("CSPExecutor.execute: After Formulas.betaReduction(): %s", formula);
    }
    Value value;
    try {
      FiniteUnary unary = makeFiniteUnary(formula, (PuzzleKnowledgeGraph) graph,
          new HashMap<String, FiniteUnary>());
      value = unary.toValue();
    } catch (Exception e) {
      if (opts.throwExceptions) {
        throw e;
      }
      value = new ErrorValue(e.toString());
    }
    return new Response(value);
  }
}
