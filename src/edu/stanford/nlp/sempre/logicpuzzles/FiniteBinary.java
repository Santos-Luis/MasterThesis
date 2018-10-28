package edu.stanford.nlp.sempre.logicpuzzles;

import fig.basic.*;
import java.util.*;

import edu.stanford.nlp.sempre.*;

/**
 * Finite binary with type checking and support for unknowns.
 *
 * Follows same paradigm as FiniteUnary.
 * The domain is the input space (join with unaries that have the same domain).
 * The range is the output space.
 *
 * @author Robin Jia
 */
public class FiniteBinary {
  private final Map<Value, Map<Value, BooleanExpression>> values;

  private final List<Value> domain;
  private final List<Value> range;

  public FiniteBinary(List<? extends Value> domain, List<? extends Value> range) {
    values = new HashMap<Value, Map<Value, BooleanExpression>>();
    this.domain = Collections.unmodifiableList(domain);
    this.range = Collections.unmodifiableList(range);
    for (Value d: domain) {
      Map<Value, BooleanExpression> curMap = new HashMap<Value, BooleanExpression>();
      for (Value r: range) {
        curMap.put(r, BooleanExpression.FALSE);
      }
      values.put(d, curMap);
    }
  }

  /**
   * Create a FiniteBinary where each item in values maps to the corresponding unary
   */
  public static FiniteBinary fromUnaries(List<Value> values, List<FiniteUnary> unaries) {
    FiniteBinary binary = new FiniteBinary(values, unaries.get(0).getDomain());
    for (int i = 0; i < values.size(); ++i) {
      Value value = values.get(i);
      FiniteUnary unary = unaries.get(i);
      for (Value r: unary.getDomain()) {
        binary.put(value, r, unary.get(r));
      }
    }
    return binary;
  }

  public static FiniteBinary reverse(FiniteBinary in) {
    FiniteBinary answer = new FiniteBinary(in.range, in.domain);
    for (Value d: in.domain) {
      for (Value r: in.range) {
        answer.put(r, d, in.get(d, r));
      }
    }
    return answer;
  }

  public List<Value> getDomain() { return domain; }
  public List<Value> getRange() { return range; }

  public void put(Value v1, Value v2, BooleanExpression expr) {
    if (!domain.contains(v1)) {
      throw new RuntimeException("FiniteBinary.put: value " + v1 + " not in domain = " + domain + ".");
    }
    if (!range.contains(v2)) {
      throw new RuntimeException("FiniteBinary.put: value " + v2 + " not in range = " + range + ".");
    }
    values.get(v1).put(v2, expr);
  }

  public BooleanExpression get(Value v1, Value v2) {
    return values.get(v1).get(v2);
  }

  public void log() {
    for (Value d: domain) {
      for (Value r: range) {
        LogInfo.logs("(%s, %s): %s", d, r, get(d, r));
      }
    }
  }

  public FiniteUnary join(FiniteUnary unary) {
    if (!domain.equals(unary.getDomain())) {
      // Type-check failed; return an empty FiniteUnary.
      if (CSPExecutor.opts.verbosity >= 2) {
        LogInfo.logs("FiniteBinary.join: Type-check failed: %s vs. %s.", domain, unary.getDomain());
      }
      return new FiniteUnary(Collections.emptyList());
    }
    FiniteUnary answer = new FiniteUnary(range);
    for (Value r: range) {
      ArrayList<BooleanExpression> exprs = new ArrayList<BooleanExpression>();
      for (Value d: domain) {
        exprs.add(BooleanExpression.and(get(d, r), unary.get(d)));
      }
      answer.put(r, BooleanExpression.or(exprs.toArray(new BooleanExpression[0])));
    }
    return answer;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FiniteBinary that = (FiniteBinary) o;
    return values.equals(that.values);
  }

  @Override public int hashCode() { return values.hashCode(); }
}
