package edu.stanford.nlp.sempre.fbalignment.matchers;

public class SymmetricSubstringMatcher implements Matcher {

  @Override
  public boolean matchArgument2Entity(String str1, String str2) {

    return str1.toLowerCase().contains(str2.toLowerCase()) ||
        str2.toLowerCase().contains(str1.toLowerCase());
  }

}
