package edu.stanford.nlp.sempre.fbalignment.bipartite.test;

public class AnnotatedAlignmentExample extends AlignmentExample implements HasLabel {

  private boolean label;
  private static final int LABEL_INDEX = 11;

  public AnnotatedAlignmentExample(String nlNodeDesc, String fbNodeDesc,
                                   String type1Desc, String type2Desc, boolean label) {
    super(nlNodeDesc, fbNodeDesc, type1Desc, type2Desc);
    this.label = label;
  }

  public boolean isPositive() {
    return label;
  }

  public static AnnotatedAlignmentExample fromAnnotationLine(String annoatationLine) {

    String[] tokens = annoatationLine.split("\t");
    boolean goldLabel = tokens[LABEL_INDEX].equals("1") ? true : false;
    return new AnnotatedAlignmentExample(
        tokens[NL_DESC_INDEX], tokens[FB_DESC_INDEX], tokens[TYPE_1_INDEX], tokens[TYPE_2_INDEX],
        goldLabel);
  }


}
