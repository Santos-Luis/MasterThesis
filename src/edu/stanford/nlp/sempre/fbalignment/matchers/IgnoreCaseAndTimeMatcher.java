package edu.stanford.nlp.sempre.fbalignment.matchers;

import edu.stanford.nlp.sempre.freebase.utils.LinkedExtractionFileUtils;

public class IgnoreCaseAndTimeMatcher implements Matcher {

  private IgnoreCaseMatcher ignoreCaseMatcher;
  private TimeMatcher timeMatcher;

  public IgnoreCaseAndTimeMatcher() {
    timeMatcher = new TimeMatcher();
    ignoreCaseMatcher = new IgnoreCaseMatcher();
  }

  @Override
  public boolean matchArgument2Entity(String argument, String entity) {

    if (LinkedExtractionFileUtils.isTimeArg(argument)) {
      return timeMatcher.matchArgument2Entity(LinkedExtractionFileUtils.extractTime(argument), entity);
    } else {
      return ignoreCaseMatcher.matchArgument2Entity(argument, entity);
    }
  }
}
