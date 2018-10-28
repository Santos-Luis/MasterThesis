package edu.stanford.nlp.sempre.regex;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import edu.stanford.nlp.sempre.StringValue;
import edu.stanford.nlp.sempre.Value;
import edu.stanford.nlp.sempre.ValueEvaluator;
import fig.basic.LogInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Used to evaluate regular expressions.
 * In regular expressions, all final values are String values,
 * which are compared using DFA equivalence.
 *
 * @author Yushi Wang
 */
public class RegexValueEvaluator implements ValueEvaluator {

  public static final String vowel = "[AEIOUaeiou]";
  public static final String letter = "[A-Za-z]";
  public static final String upper = "[A-Z]";
  public static final String lower = "[a-z]";
  public static final String number = "[0-9]+";
  public static final String word = "[A-Za-z]+";
  public static final Pattern alphaNumericPattern = Pattern.compile("[A-Za-z0-9 ]+");
  public static final Pattern alphaPattern = Pattern.compile("[A-Za-z ]+");
  public static final Pattern numericPattern = Pattern.compile("[0-9]+");

  public double getCompatibility(Value t, Value p) {

    // In regexes, both values should always be strings
    if (t instanceof StringValue && p instanceof StringValue) {
      StringValue target = (StringValue) t;
      StringValue pred = (StringValue) p;
      // TODO(yushi): hack for checking for large numbers
      boolean number1 = pred.value.matches(".*\\{[0-9]{2}.*") || pred.value.matches(".*[0-9]{2}\\}.*") ||
        pred.value.matches(".*[0-9]\\}\\{[0-9].*") ||
        pred.value.matches(".*\\.\\*\\)\\{.*\\}\\.\\*\\)\\{.*\\}\\)\\.\\*");
      boolean number2 = target.value.matches(".*\\{[0-9]{2}.*") || target.value.matches(".*[0-9]{2}\\}.*") ||
        target.value.matches(".*[0-9]\\}\\{[0-9].*") ||
        target.value.matches(".*\\.\\*\\)\\{.*\\}\\.\\*\\)\\{.*\\}\\)\\.\\*");
      if (number1 ^ number2) return 0.0;
      RegExp predRegexp = new RegExp(pred.value);
      // LogInfo.logs(pred.value);
      RegExp targetRegexp = new RegExp(target.value);
      Automaton predDfa = predRegexp.toAutomaton();
      Automaton targetDfa = targetRegexp.toAutomaton();
      return predDfa.equals(targetDfa) ? 1 : 0;
      /*
      LogInfo.logs("looking at %s, %s", t, p);
      if (predDfa.equals(targetDfa)) {
        LogInfo.logs("same");
        return 1;
      } else {
        LogInfo.logs("diff");
        return 0;
      }
      */
    } else {  // This should never happen
      throw new RuntimeException("Illegal value type in RegexValueEvaluator, " +
              "target: " + t.getClass() + ", pred: " + p.getClass());
    }
  }

  //approximately determine if a regex has an empty denotation
  public boolean isApproxEmptyDenotation(String regex) {
    //get the tokens
    List<String> res = new ArrayList<>();
    String[] tokens = regex.split("&|\\(|\\)");
    for (String token: tokens)
      if (token.length() > 0)
        res.add(token);
    //go over adjacent tokens
    for (int i = 0; i < res.size() - 1; ++i) {
      if (isEmptyDenotation(res.get(i), res.get(i + 1)))
        return true;
    }
    return false;
  }

  private static boolean isEmptyDenotation(String token1, String token2) {
    //don't handle certain characters for now
    if (token1.contains(".") || token1.contains("*") || token1.contains("|") || token1.contains("~"))
      return false;
    if (token2.contains(".") || token2.contains("*") || token2.contains("|") || token2.contains("~"))
      return false;

    if (alphaNumericPattern.matcher(token1).matches()) {
      if (alphaNumericPattern.matcher(token2).matches() && !token1.equals(token2))
        return true;
      else
        return handlePatternAndSequence(token2, token1);
    }
    //token1 is not
    else {
      if (alphaNumericPattern.matcher(token2).matches())
        return handlePatternAndSequence(token1, token2);
      else
        return handlePatterns(token1, token2);
    }
  }

  private static boolean handlePatterns(String token1, String token2) {

    if (token1.equals(number)) {
      if (token2.equals(word) ||
              token2.equals(letter) ||
              token2.equals(upper) ||
              token2.equals(lower) ||
              token2.equals(vowel))
        return true;
    }

    if (token2.equals(number)) {
      if (token1.equals(word) ||
              token1.equals(letter) ||
              token1.equals(upper) ||
              token1.equals(lower) ||
              token1.equals(vowel))
        return true;
    }

    if (token1.equals(upper) && token2.equals(lower))
      return true;
    if (token1.equals(lower) && token2.equals(upper))
      return true;
    return false;
  }

  private static boolean handlePatternAndSequence(String pattern, String sequence) {

    if ((pattern.equals(vowel) ||
            pattern.equals(letter) ||
            pattern.equals(upper) ||
            pattern.equals(lower) ||
            pattern.equals(word)) &&
            numericPattern.matcher(sequence).matches())
      return true;

    if (pattern.equals(number) && alphaPattern.matcher(sequence).matches())
      return true;
    return false;
  }


  public static void main(String[] args) {
    Value v1 = new StringValue(args[0]);
    Value v2 = new StringValue(args[1]);
    LogInfo.logs("%s", new RegexValueEvaluator().getCompatibility(v1, v2));
  }
}
