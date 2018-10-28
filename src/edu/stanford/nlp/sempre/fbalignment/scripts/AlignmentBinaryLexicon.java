package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.Formula;
import edu.stanford.nlp.sempre.Json;
import edu.stanford.nlp.sempre.freebase.lexicons.EntrySource;
import edu.stanford.nlp.sempre.freebase.lexicons.LexicalEntry;
import edu.stanford.nlp.sempre.freebase.utils.FormatConverter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.Pair;
import fig.basic.LogInfo;
import fig.basic.MapUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Lexicon conatining alignment information. mapping name to logical form,
 * expected types and set of alignment scores
 *
 * @author jonathanberant
 */
public final class AlignmentBinaryLexicon {

  public static final String TYPED_INTERSECTION_SIZE = "Intersection_size_typed";
  public static final String TYPED_NL_SIZE = "NL_typed_size";
  public static final String TYPED_FB_SIZE = "FB_typed_size";
  public static final String NL_SIZE = "NL-size";

  private Map<String, Map<String, AlignmentEntry>> nameToFormulaToEntryMap;
  private int numOfAlignmentScores;

  private AlignmentBinaryLexicon(Map<String, Map<String, AlignmentEntry>> nameToFormulaToEntryMap, int numOfAlignmentScores) {
    this.nameToFormulaToEntryMap = nameToFormulaToEntryMap;
    this.numOfAlignmentScores = numOfAlignmentScores;
  }

  public static AlignmentBinaryLexicon fromFourPartiteGraphFile(String file) throws IOException {

    LogInfo.begin_track("Loading alignment lexicon");
    Map<String, Map<String, AlignmentEntry>> res = new HashMap<String, Map<String, AlignmentEntry>>();

    BufferedReader reader = IOUtils.getBufferedFileReader(file);
    Map<String, Integer> titleToIndex = getTitleToIndex(reader.readLine());
    String line;

    while ((line = reader.readLine()) != null) {
      AlignmentEntry entry = generateEntryFromFourpartiteLine(line, titleToIndex);
      Map<String, AlignmentEntry> nameEntries = res.get(entry.name);
      if (nameEntries == null) {
        nameEntries = new HashMap<String, AlignmentBinaryLexicon.AlignmentEntry>();
        res.put(entry.name, nameEntries);
      }
      if (nameEntries.get(entry.formula) != null) {
        throw new RuntimeException("Trying to insert entry: " + entry + " but duplicate entry exists: " + nameEntries.get(entry.formula));
      }
      nameEntries.put(entry.formula, entry);
    }
    reader.close();
    LogInfo.end_track("Loading alignment lexicon");
    return new AlignmentBinaryLexicon(res, 4);
  }

  private static Map<String, Integer> getTitleToIndex(String line) {

    Map<String, Integer> titleToIndex = new HashMap<String, Integer>();
    String[] tokens = line.split("\t");
    for (int i = 0; i < tokens.length; ++i) {
      titleToIndex.put(tokens[i], i);
    }
    return titleToIndex;
  }

  private static AlignmentEntry generateEntryFromFourpartiteLine(String line, Map<String, Integer> titleToIndex) {

    String[] tokens = line.split("\t");
    String name = tokens[0];
    String expectedType1 = FormatConverter.fromSlashToDot(tokens[1], false);
    String expectedType2 = FormatConverter.fromSlashToDot(tokens[2], false);
    String formula = FormatConverter.fromCvtBinaryToLispTree(tokens[3]);
    Map<String, Double> scores = new TreeMap<String, Double>();
    scores.put(TYPED_INTERSECTION_SIZE, Double.parseDouble(tokens[titleToIndex.get(TYPED_INTERSECTION_SIZE)]));
    scores.put(TYPED_NL_SIZE, Double.parseDouble(tokens[titleToIndex.get(TYPED_NL_SIZE)]));
    scores.put(TYPED_FB_SIZE, Double.parseDouble(tokens[titleToIndex.get(TYPED_FB_SIZE)]));
    scores.put(NL_SIZE, Double.parseDouble(tokens[titleToIndex.get(NL_SIZE)]));
    return new AlignmentEntry(name, expectedType1, expectedType2, formula, scores);
  }

  public Map<String, AlignmentEntry> getNameEntries(String name) {
    return nameToFormulaToEntryMap.get(name);
  }

  public AlignmentEntry getEntry(String name, String formula) {

    if (nameToFormulaToEntryMap.get(name) == null)
      return null;
    return nameToFormulaToEntryMap.get(name).get(formula);
  }

  public int numOfAlignmentScores() {
    return numOfAlignmentScores;
  }

  public List<String> getAlignmentScoreDescriptions() {
    AlignmentEntry firstEntry = nameToFormulaToEntryMap.entrySet().iterator().next().getValue().entrySet().iterator().next().getValue();
    return firstEntry.getSortedScoresDesc();
  }

  /**
   * writes the entries in the property info file format
   *
   * @param notToAddEntries - (name, formula) pairs not to be added
   */
  public void writeAlignmentEntriesAsLexicalEntries(Set<Pair<String, String>> notToAddEntries, Counter<String> popularityCounter, PrintWriter writer, Map<String, List<String>> idToDescriptionsMap) {


    for (String name : nameToFormulaToEntryMap.keySet()) {
      Map<String, AlignmentEntry> formulaToEntries = nameToFormulaToEntryMap.get(name);
      for (String formula : formulaToEntries.keySet()) {
        // check if need to add entry
        if (!notToAddEntries.contains(new Pair<String, String>(name, formula))) {

          AlignmentEntry entry = formulaToEntries.get(formula);
          LexicalEntry.LexiconValue lv = new LexicalEntry.LexiconValue(name, Formula.fromString(formula), EntrySource.ALIGNMENT.toString(), entry.scores);
          String serialized = Json.writeValueAsStringHard(lv);
          writer.println(serialized);
        } else {
          LogInfo.log("Entry already exists: " + "(" + name + "," + formula + ")");
        }

      }
    }
  }

  public static String getBinaryDescriptions(String formula,
                                             Map<String, List<String>> idToDescriptionsMap) {

    if (formula.startsWith("!")) {
      formula = formula.substring(1);
    }
    List<String> descriptions = idToDescriptionsMap.get(formula);
    StringBuilder sb = new StringBuilder();
    if (descriptions == null)
      throw new RuntimeException("All formulas must have a description: " + formula);
    for (String description : descriptions) {
      sb.append(description.toLowerCase() + "###");
    }
    sb.delete(sb.length() - 3, sb.length());
    return sb.toString().trim();

  }


  public static class AlignmentEntry {

    public String name;
    public String expectedType1;
    public String expectedType2;
    public String formula;
    public Map<String, Double> scores;

    public AlignmentEntry(String name, String expectedType1, String expectedTyp2,
                          String formula, Map<String, Double> scores) {
      this.name = name;
      this.expectedType1 = expectedType1;
      this.expectedType2 = expectedTyp2;
      this.formula = formula;
      this.scores = scores;
    }

    public double getScore(String scoreName) {
      return MapUtils.getDouble(scores, scoreName, 0.0);
    }

    public Set<String> getScoresDescs() {
      return scores.keySet();
    }

    public List<String> getSortedScoresDesc() {

      List<String> res = new LinkedList<String>(scores.keySet());
      Collections.sort(res);
      return res;
    }

    public String toString() {

      StringBuilder sb = new StringBuilder();
      sb.append(name + "\t" + formula + "\t" + expectedType1 + "\t" + expectedType2);
      List<String> scoreList = new LinkedList<String>(scores.keySet());
      Collections.sort(scoreList);
      for (String scoreItem : scoreList) {
        sb.append("\t" + MapUtils.getDouble(scores, scoreItem, 0.0));
      }
      return sb.toString();
    }
    /**
     * Return a comma-separated list of feature descriptions, then '\t', then
     * the corresponding feature values
     */
    public String getFeatureDesc() {

      List<String> sortedDescriptions = getSortedScoresDesc();
      StringBuilder sb1 = new StringBuilder();
      StringBuilder sb2 = new StringBuilder();
      for (String desc : sortedDescriptions) {
        sb1.append(desc + ",");
        sb2.append(MapUtils.getDouble(scores, desc, 0.0));
      }
      sb1.deleteCharAt(sb1.length() - 1);
      sb2.deleteCharAt(sb2.length() - 1);
      return sb1.toString() + "\t" + sb2.toString();
    }
  }
}
