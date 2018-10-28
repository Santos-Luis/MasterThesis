package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.Formula;
import edu.stanford.nlp.sempre.Json;
import edu.stanford.nlp.sempre.freebase.lexicons.EntrySource;
import edu.stanford.nlp.sempre.freebase.lexicons.LexicalEntry;
import edu.stanford.nlp.sempre.freebase.lexicons.normalizers.BinaryNormalizer;
import edu.stanford.nlp.sempre.fbalignment.scripts.AlignmentBinaryLexicon.AlignmentEntry;
import edu.stanford.nlp.sempre.freebase.utils.FileUtils;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.Pair;
import fig.basic.LogInfo;
import fig.basic.Option;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Takes a property info file that has fields (mid, id, popularity, name, expected_type1, expected_type2,
 * and adds a set of alignment scores for the mapping from name to formula
 *
 * @author jonathanberant
 */
public class AddAlignmentScoresToPropertyInfoFile {

  public static class Options {
    @Option(gloss = "Path to alignment file") public String alignmentFilePath;
    @Option(gloss = "Path to binary properties popularity file")
    public String binaryPopularityFilePath;
    @Option(gloss = "Path to binary string match file")
    public String binaryStringMatchFilePath;
    @Option(gloss = "Path to out lexicon file")
    public String outAlignmentLexiconFilePath;

  }

  public static Options opts = new Options();

  /**
   * Algorithm proceeds in two steps: 1. Go over the property info file. For
   * each line check if it exists in the Alignment lexicon. if it exists - add
   * the alignment scores and record that the pair was already written 2. Go
   * over all entries - for every entry that was not already in the property
   * lexicon add a new entry
   *
   * @param args 0 - old property info file - this file has a different row for
   *             every different name and formula 1 - new property info file 2 -
   *             alignment scores file 3 - property popularity file
   * @throws IOException
   */
  public void run() throws IOException {

    LogInfo.log("loading alignment scores");
    AlignmentBinaryLexicon alignmentLexicon = AlignmentBinaryLexicon.fromFourPartiteGraphFile(opts.alignmentFilePath);
    LogInfo.log("loading popularity scores");
    Counter<String> popularityCounter = FileUtils.loadStringCounter(opts.binaryPopularityFilePath);
    LogInfo.log("loading id-to-descriptions map");
    Map<String, List<String>> idToDescriptionsMap = loadIdToDescriptionsMap(opts.binaryStringMatchFilePath);
    LogInfo.log("Adding alignment scores to old entries");
    PrintWriter writer = IOUtils.getPrintWriter(opts.outAlignmentLexiconFilePath);
    Set<Pair<String, String>> oldEntries = addAlignmentScoreToOldEntries(opts.binaryStringMatchFilePath, alignmentLexicon, writer, idToDescriptionsMap);
    LogInfo.log("Adding new entries");
    alignmentLexicon.writeAlignmentEntriesAsLexicalEntries(oldEntries, popularityCounter, writer, idToDescriptionsMap);
    writer.close();
  }

  private static Map<String, List<String>> loadIdToDescriptionsMap(String oldFile) {

    Map<String, List<String>> res = new HashMap<String, List<String>>();
    for (String line : IOUtils.readLines(oldFile)) {

      String[] tokens = line.split("\t");
      if (!tokens[1].startsWith("!")) {
        List<String> descriptions = res.get(tokens[1]);
        if (descriptions == null) {
          descriptions = new LinkedList<String>();
          res.put(tokens[1], descriptions);
        }
        descriptions.add(tokens[3].toLowerCase());
      }
    }
    return res;
  }

  /**
   * Goes over the old entries, checks if they exist in the alignment lexicon
   * and adds scores
   *
   * @throws IOException
   */
  private static Set<Pair<String, String>> addAlignmentScoreToOldEntries(String infile,
                                                                         AlignmentBinaryLexicon alignmentLexicon, PrintWriter outWriter, Map<String, List<String>> idToDescriptionsMap) throws IOException {

    Set<Pair<String, String>> oldEntries = new HashSet<Pair<String, String>>();
    BinaryNormalizer normalizer = new BinaryNormalizer();
    List<String> scoreDescs = alignmentLexicon.getAlignmentScoreDescriptions();

    for (String line : IOUtils.readLines(infile)) {

      String[] tokens = line.split("\t");
      if (tokens.length < 6) {
        LogInfo.log("not enough tokens: " + line);
        continue;
      }
      // all fields
      String formula = tokens[1];
      String name = tokens[3];

      // fields to add and change
      String normalized = normalizer.normalize(name); // this tries to simulate the lemmatizer - probably will fail most of the time but this is not too important

      Pair<String, String> oldEntry = new Pair<String, String>(normalized, formula);
      oldEntries.add(oldEntry); // check if this entry exists already

      AlignmentEntry alignmentEntry = alignmentLexicon.getEntry(oldEntry.first, oldEntry.second);
      Map<String, Double> scoreMap = new TreeMap<String, Double>();

      if (alignmentEntry == null) {
        for (int i = 0; i < scoreDescs.size(); ++i) {
          scoreMap.put(scoreDescs.get(i), 0.0);
        }
      } else {
        scoreMap = alignmentEntry.scores;
      }

      LexicalEntry.LexiconValue lv = new LexicalEntry.LexiconValue(normalized, Formula.fromString(formula), EntrySource.STRING_MATCH.toString(), scoreMap);
      String serialized = Json.writeValueAsStringHard(lv);
      outWriter.println(serialized);
    }
    outWriter.flush();
    return oldEntries;
  }
}
