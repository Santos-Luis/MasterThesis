package edu.stanford.nlp.sempre.fbalignment.bipartite.test;

public class AlignmentExample {

  private String nlNodeDesc;
  private String fbNodeDesc;
  private String type1Desc;
  private String type2Desc;

  protected static final int NL_DESC_INDEX = 0;
  protected static final int TYPE_1_INDEX = 1;
  protected static final int TYPE_2_INDEX = 2;
  protected static final int FB_DESC_INDEX = 3;

  public AlignmentExample(String nlNodeDesc, String fbNodeDesc, String type1Desc, String type2Desc) {
    this.nlNodeDesc = nlNodeDesc;
    this.fbNodeDesc = fbNodeDesc;
    this.type1Desc = type1Desc;
    this.type2Desc = type2Desc;
  }

  public static AlignmentExample fromAnnotationLine(String annoatationLine) {

    String[] tokens = annoatationLine.split("\t");
    return new AlignmentExample(tokens[NL_DESC_INDEX], tokens[FB_DESC_INDEX], tokens[TYPE_1_INDEX], tokens[TYPE_2_INDEX]);
  }

  public String getNlNodeDesc() {
    return nlNodeDesc;
  }

  public String getFbNodeDesc() {
    return fbNodeDesc;
  }

  public String getType1Desc() {
    return type1Desc;
  }

  public String getType2Desc() {
    return type2Desc;
  }


}
