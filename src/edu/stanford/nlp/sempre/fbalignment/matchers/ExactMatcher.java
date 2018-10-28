package edu.stanford.nlp.sempre.fbalignment.matchers;


public class ExactMatcher implements Matcher {

  @Override
  public boolean matchArgument2Entity(String str1, String str2) {

    return str1.equals(str2);
  }

}
