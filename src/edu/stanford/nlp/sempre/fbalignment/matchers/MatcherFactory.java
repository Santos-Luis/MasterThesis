package edu.stanford.nlp.sempre.fbalignment.matchers;

public final class MatcherFactory {
  private MatcherFactory() { }

  public static Matcher create(String matcherName) {

    if (matcherName.equals("exact-matcher"))
      return new ExactMatcher();
    else if (matcherName.equals("ignore-case-matcher"))
      return new IgnoreCaseMatcher();
    else if (matcherName.equals("symmetric-substring-matcher"))
      return new SymmetricSubstringMatcher();
    else if (matcherName.equals("arg-substring-matcher"))
      return new ArgSubstringMatcher();
    else if (matcherName.equals("time-matcher"))
      return new TimeMatcher();
    else if (matcherName.equals("ignore-case-and-time-matcher"))
      return new IgnoreCaseAndTimeMatcher();
    else
      throw new IllegalArgumentException("Erorr: matcher name is illegal: " + matcherName);
  }

}
