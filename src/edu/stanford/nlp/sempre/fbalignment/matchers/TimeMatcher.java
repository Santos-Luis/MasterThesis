package edu.stanford.nlp.sempre.fbalignment.matchers;

import edu.stanford.nlp.sempre.freebase.utils.FreebaseUtils;
import edu.stanford.nlp.util.Triple;

public class TimeMatcher implements Matcher {

  @Override
  public boolean matchArgument2Entity(String timeArgument, String timeEntity) {

    if (!FreebaseUtils.isDate(timeEntity))
      return false;

    Triple<String, String, String> argTriple = getYearMonthDay(timeArgument);
    Triple<String, String, String> entityTriple = getYearMonthDay(timeEntity);
    if (argTriple.first().equals(entityTriple.first())) {
      return true;
    }
    return false;
  }

  private Triple<String, String, String> getYearMonthDay(String timeExp) {

    boolean neg = false;
    if (timeExp.startsWith("-")) {
      neg = true;
      timeExp = timeExp.substring(1);
    }

    String year = "", month = "", day = "";
    if (timeExp.length() >= 4) {
      year = timeExp.substring(0, 4);
    }
    if (timeExp.length() >= 7) {
      month = timeExp.substring(5, 7);
    }
    if (timeExp.length() >= 10) {
      day = timeExp.substring(8, 10);
    }
    return neg ? new Triple<String, String, String>("-" + year, month, day) : new Triple<String, String, String>(year, month, day);
  }
}
