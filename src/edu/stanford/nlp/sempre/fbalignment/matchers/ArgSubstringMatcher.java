package edu.stanford.nlp.sempre.fbalignment.matchers;

public class ArgSubstringMatcher implements Matcher {

  @Override
  public boolean matchArgument2Entity(String predicate, String relation) {
    return relation.toLowerCase().contains(predicate.toLowerCase());
  }

}
