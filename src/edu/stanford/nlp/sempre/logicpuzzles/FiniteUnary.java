package edu.stanford.nlp.sempre.logicpuzzles;

import fig.basic.*;
import java.util.*;

import edu.stanford.nlp.sempre.*;

/**
 * Finite unary with type checking and support for unknowns.
 *
 * Instead of representing sets of objects, a FiniteUnary represents
 * a dict from objects to boolean expressions.  An object is "in" the FiniteUnary
 * iff its associated boolean expression evaluates to true.  This representation is
 * useful when the world's state depends on unknown values (e.g. logicpuzzles).
 *
 * We let the set of keys in the dict be the "domain" of the FiniteUnary.
 * The domain contains all objects of the correct type, which gives us a sort of
 * typed Lambda-DCS.
 *
 * @author Robin Jia
 */
public class FiniteUnary {
  public static final FiniteUnary EMPTY = new FiniteUnary(Collections.emptyList());

  private final Map<Value, BooleanExpression> values;
  private final List<Value> domain;

  public FiniteUnary(List<? extends Value> domain) {
    values = new HashMap<Value, BooleanExpression>();
    this.domain = Collections.unmodifiableList(domain);
    for (Value entity: domain) {
      values.put(entity, BooleanExpression.FALSE);
    }
  }

  public static FiniteUnary makeSingleton(List<? extends Value> domain, Value entity) {
    FiniteUnary unary = new FiniteUnary(domain);
    unary.put(entity, BooleanExpression.TRUE);
    return unary;
  }

  public static FiniteUnary makeAll(List<? extends Value> domain) {
    FiniteUnary unary = new FiniteUnary(domain);
    for (Value entity: domain) {
      unary.put(entity, BooleanExpression.TRUE);
    }
    return unary;
  }

  public static FiniteUnary makeSubset(List<? extends Value> domain, List<? extends Value> subset) {
    FiniteUnary unary = new FiniteUnary(domain);
    for (Value entity: subset) {
      unary.values.put(entity, BooleanExpression.TRUE);
    }
    return unary;
  }

  public static FiniteUnary not(FiniteUnary in) {
    FiniteUnary answer = new FiniteUnary(in.domain);
    for (Value d: in.domain) {
      answer.put(d, BooleanExpression.not(in.get(d)));
    }
    return answer;
  }

  public static FiniteUnary and(FiniteUnary a, FiniteUnary b) {
    if (!a.getDomain().equals(b.getDomain())) {
      // Type-check failed; return an empty FiniteUnary.
      if (CSPExecutor.opts.verbosity >= 2) {
        LogInfo.logs("FiniteUnary.and: Type-check failed: %s vs. %s.", a.getDomain(), b.getDomain());
      }
      return new FiniteUnary(Collections.emptyList());
    }
    FiniteUnary answer = new FiniteUnary(a.domain);
    for (Value d: a.domain) {
      answer.put(d, BooleanExpression.and(a.get(d), b.get(d)));
    }
    return answer;
  }

  public static FiniteUnary or(FiniteUnary a, FiniteUnary b) {
    if (!a.getDomain().equals(b.getDomain())) {
      // Type-check failed; return an empty FiniteUnary.
      if (CSPExecutor.opts.verbosity >= 2) {
        LogInfo.logs("FiniteUnary.or: Type-check failed: %s vs. %s.", a.getDomain(), b.getDomain());
      }
      return new FiniteUnary(Collections.emptyList());
    }
    FiniteUnary answer = new FiniteUnary(a.domain);
    for (Value d: a.domain) {
      answer.put(d, BooleanExpression.or(a.get(d), b.get(d)));
    }
    return answer;
  }

  /**
   * Returns list of list values, where each inner list is (value, string) pair.
   */
  public Value toValue() {
    List<Value> values = new ArrayList<Value>();
    for (Value v: domain) {
      List<Value> curPair = new ArrayList<Value>();
      curPair.add(v);
      curPair.add(new StringValue(get(v).toString()));
      values.add(new ListValue(curPair));
    }
    return new ListValue(values);
  }

  public LispTree toLispTree() {
    LispTree tree = LispTree.proto.newList();
    for (Value d: domain) {
      LispTree child = LispTree.proto.newList();
      child.addChild(d.toLispTree());
      child.addChild(get(d).toLispTree());
      tree.addChild(child);
    }
    return tree;
  }

  public List<Value> getDomain() { return domain; }

  public void put(Value v, BooleanExpression expr) {
    if (!domain.contains(v)) {
      throw new RuntimeException("FiniteUnary.put: value " + v + " not in domain = " + domain + ".");
    }
    values.put(v, expr);
  }

  public BooleanExpression get(Value v) {
    return values.get(v);
  }

  public void log() {
    for (Value v: domain) {
      LogInfo.logs("%s: %s", v, get(v));
    }
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FiniteUnary that = (FiniteUnary) o;
    return values.equals(that.values);
  }

  @Override public int hashCode() { return values.hashCode(); }
  @Override public String toString() { return this.toLispTree().toString(); }
}
