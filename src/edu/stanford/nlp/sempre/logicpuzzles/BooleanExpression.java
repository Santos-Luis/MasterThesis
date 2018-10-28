package edu.stanford.nlp.sempre.logicpuzzles;

import fig.basic.*;
import java.util.*;

/**
 * A boolean expression backed by a LispTree.
 *
 * Grammar: expr goes to one of
 *   (true)
 *   (false)
 *   (var [varname])
 *   (not expr_1)
 *   (and expr_1 ... expr_n)
 *   (or expr_1 ... expr_n)
 *   (count [comparator] [int] expr_1 ... expr_n)
 */
public class BooleanExpression {
  public enum Type { constant, var, not, and, or, count };
  public static final BooleanExpression TRUE = BooleanExpression.fromString("(true)");
  public static final BooleanExpression FALSE = BooleanExpression.fromString("(false)");
  public static final List<String> VALID_COUNT_MODES = Arrays.asList("==", "!=", "<", ">", "<=", ">=");

  /* Present on all BooleanExpression's */
  private Type type;
  private List<BooleanExpression> children = new ArrayList<BooleanExpression>();

  /* Present only when type == constant */
  private boolean value;

  /* Present only when type == var */
  private String varName;

  /* Present only when type == count */
  private String countMode;
  private int countNum;

  /* Static methods to create and combine BooleanExpressions */

  public static BooleanExpression fromString(String str) {
    return fromLispTree(LispTree.proto.parseFromString(str));
  }

  public static BooleanExpression fromLispTree(LispTree tree) {
    BooleanExpression answer = new BooleanExpression();
    if (tree.head().value.equals("true")) {
      answer.type = Type.constant;
      answer.value = true;
      return answer;
    } else if (tree.head().value.equals("false")) {
      answer.type = Type.constant;
      answer.value = false;
      return answer;
    }
    answer.type = Type.valueOf(tree.head().value);
    if (answer.type == Type.constant) {
      throw new RuntimeException("BooleanExpression: unexpected keyword constant, use true or false.");
    } else if (answer.type == Type.var) {
      if (tree.children.size() != 2) {
        throw new RuntimeException("BooleanExpression: var takes precisely 1 argument.");
      }
      answer.varName = tree.children.get(1).value;
    } else if (answer.type == Type.not) {
      if (tree.children.size() != 2) {
        throw new RuntimeException("BooleanExpression: not takes precisely 1 argument.");
      }
      answer.children.add(BooleanExpression.fromLispTree(tree.children.get(1)));
    } else if (answer.type == Type.and || answer.type == Type.or) {
      if (tree.children.size() < 2) {
        throw new RuntimeException("BooleanExpression: and/or take at least 1 argument.");
      }
      for (int i = 1; i < tree.children.size(); ++i) {
        LispTree child = tree.children.get(i);
        answer.children.add(BooleanExpression.fromLispTree(child));
      }
    } else if (answer.type == Type.count) {
      if (tree.children.size() < 4) {
        throw new RuntimeException("BooleanExpression: count takes at least 3 arguments.");
      }
      answer.countMode = tree.children.get(1).value;
      if (!isValidCountMode(answer.countMode)) {
        throw new RuntimeException("Tried to create count expression with invalid mode " + answer.countMode);
      }
      answer.countNum = Integer.parseInt(tree.children.get(2).value);
      for (int i = 3; i < tree.children.size(); ++i) {
        LispTree child = tree.children.get(i);
        answer.children.add(BooleanExpression.fromLispTree(child));
      }
    } else {
      throw new RuntimeException("BooleanExpression: Unknown error in constructor.");
    }
    return answer;
  }

  public static BooleanExpression var(String name) {
    BooleanExpression answer = new BooleanExpression();
    answer.type = Type.var;
    answer.varName = name;
    return answer;
  }

  public static BooleanExpression not(BooleanExpression expr) {
    if (expr.equals(FALSE)) return TRUE;
    if (expr.equals(TRUE)) return FALSE;
    BooleanExpression answer = new BooleanExpression();
    answer.type = Type.not;
    answer.children.add(expr);
    return answer;
  }

  public static BooleanExpression and(BooleanExpression... exprs) {
    BooleanExpression answer = new BooleanExpression();
    answer.type = Type.and;
    for (BooleanExpression e: exprs) {
      if (e.equals(FALSE)) return FALSE;
      if (e.equals(TRUE)) continue;
      if (e.getType() == Type.and) {
        /* Flatten and's */
        for (BooleanExpression child: e.getChildren()) {
          if (answer.children.contains(child)) continue;
          answer.children.add(child);
        }
      } else {
        if (answer.children.contains(e)) continue;
        answer.children.add(e);
      }
    }
    if (answer.children.isEmpty()) return TRUE;
    if (answer.children.size() == 1) return answer.children.get(0);
    return answer;
  }

  public static BooleanExpression or(BooleanExpression... exprs) {
    BooleanExpression answer = new BooleanExpression();
    answer.type = Type.or;
    for (BooleanExpression e: exprs) {
      if (e.equals(TRUE)) return TRUE;
      if (e.equals(FALSE)) continue;
      if (e.getType() == Type.or) {
        /* Flatten or's */
        for (BooleanExpression child: e.getChildren()) {
          if (answer.children.contains(child)) continue;
          answer.children.add(child);
        }
      } else {
        if (answer.children.contains(e)) continue;
        answer.children.add(e);
      }
    }
    if (answer.children.isEmpty()) return FALSE;
    if (answer.children.size() == 1) return answer.children.get(0);
    return answer;
  }

  /**
   * Creates an expression comparing a count to a number.
   *
   * mode should be one of VALID_COUNT_MODES.
   */
  public static BooleanExpression count(String mode, int num, BooleanExpression... exprs) {
    BooleanExpression answer = new BooleanExpression();
    answer.type = Type.count;
    if (!isValidCountMode(mode)) {
      throw new RuntimeException("Tried to create count expression with invalid mode " + mode);
    }
    answer.countMode = mode;
    answer.countNum = num;
    for (BooleanExpression e: exprs) {
      if (e.equals(FALSE)) continue;
      if (answer.children.contains(e)) continue;
      answer.children.add(e);
    }
    if (answer.children.isEmpty()) {
      /* Counting over empty set, can evaluate directly to TRUE or FALSE */
      if (num == 0) {
        if (mode.equals("==") || mode.equals("<=") || mode.equals(">=")) return TRUE;
        else return FALSE;
      } else {
        if (mode.equals("!=") || mode.equals("<=") || mode.equals("<")) return TRUE;
        else return FALSE;
      }
    }
    return answer;
  }

  /* Other static methods */

  public static boolean isValidCountMode(String s) {
    return VALID_COUNT_MODES.contains(s);
  }

  /* Instance methods */

  public Type getType() {
    return type;
  }

  public List<BooleanExpression> getChildren() {
    return children;
  }

  /** Only guaranteed to be meaningful if type == Type.constant */
  public boolean getValue() {
    return value;
  }

  /** Only guaranteed to be meaningful if type == Type.var */
  public String getVar() {
    return varName;
  }

  /** Only guaranteed to be meaningful if type == Type.count */
  public String getCountMode() {
    return countMode;
  }

  /** Only guaranteed to be meaningful if type == Type.count */
  public int getCountNum() {
    return countNum;
  }

  public LispTree toLispTree() {
    LispTree tree = LispTree.proto.newList();
    if (type == Type.constant) {
      if (value) {
        tree.addChild("true");
      } else {
        tree.addChild("false");
      }
      return tree;
    }
    tree.addChild(type.toString());
    if (type == Type.var) {
      tree.addChild(varName);
    } else if (type == Type.count) {
      tree.addChild(countMode);
      tree.addChild(Integer.toString(countNum));
    }
    for (BooleanExpression child: children) {
      tree.addChild(child.toLispTree());
    }
    return tree;
  }

  /** Evaluate the expression where the given variables are True, all else False */
  public boolean eval(Set<String> trueVars) {
    if (type == Type.constant) return value;
    if (type == Type.var) return trueVars.contains(varName);
    if (type == Type.not) return !children.get(0).eval(trueVars);
    if (type == Type.and) {
      for (BooleanExpression child: children) {
        if (!child.eval(trueVars)) return false;
      }
      return true;
    }
    if (type == Type.or) {
      for (BooleanExpression child: children) {
        if (child.eval(trueVars)) return true;
      }
      return false;
    }
    if (type == Type.count) {
      int numTrue = 0;
      for (BooleanExpression child: children) {
        if (child.eval(trueVars)) numTrue++;
      }
      if (countMode.equals("==")) return numTrue == countNum;
      if (countMode.equals("!=")) return numTrue != countNum;
      if (countMode.equals("<")) return numTrue < countNum;
      if (countMode.equals(">")) return numTrue > countNum;
      if (countMode.equals("<=")) return numTrue <= countNum;
      if (countMode.equals(">=")) return numTrue >= countNum;
    }
    throw new RuntimeException("BooleanExpression.eval(): Failed to evaluate");
  }

  /** Get set of all variable names used in this expression */
  public Set<String> getVariableNames() {
    if (type == Type.var) {
      return Collections.singleton(varName);
    }
    Set<String> names = new HashSet<String>();
    for (BooleanExpression child: children) {
      names.addAll(child.getVariableNames());
    }
    return names;
  }

  private boolean hasMatchesHeuristic(List<BooleanExpression> first, List<BooleanExpression> second) {
    for (BooleanExpression c1: first) {
      boolean foundMatch = false;
      for (BooleanExpression c2: second) {
        if (c1.equivHeuristic(c2)) {
          foundMatch = true;
          break;
        }
      }
      if (!foundMatch) return false;
    }
    return true;
  }

  /**
   * Uses some heuristics to check if two expressions are equal.
   */
  public boolean equivHeuristic(BooleanExpression other) {
    if (type != other.getType()) return false;
    if (type == Type.constant || type == Type.var) {
      return this.equals(other);
    }
    if (type == Type.count) {
      if (!this.countMode.equals(other.getCountMode())) return false;
      if (this.countNum != other.getCountNum()) return false;
    }

    /* Make sure every child of this has a matching expression in other, and vice versa */
    if (!hasMatchesHeuristic(children, other.getChildren())) return false;
    if (!hasMatchesHeuristic(other.getChildren(), children)) return false;

    return true;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BooleanExpression that = (BooleanExpression) o;
    return this.toString().equals(that.toString());
  }

  @Override public String toString() { return toLispTree().toString(); }
  @Override public int hashCode() { return toString().hashCode(); }
}
