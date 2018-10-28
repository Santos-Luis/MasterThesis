package edu.stanford.nlp.sempre.paraphrase.paralex;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.LanguageInfo.LanguageUtils;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;
import fig.basic.LogInfo;
import fig.basic.Option;
import fig.basic.Pair;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Extract phrase table from alignments
 * @author jonathanberant
 *
 */
public class ParaphraseTableExtractor {

  public static class Options {
    @Option(gloss = "Path to derivation file") public String derivationPath = "lib/wordnet-derivations.txt";
    @Option(gloss = "Path to questions file") public String questionsPath = "lib/paralex/questions.retagged.txt";
    @Option(gloss = "Path to alignment file") public String alignmentPath = "lib/paralex/word_alignments.txt";
    @Option(gloss = "Whether to generalize") public boolean generalize = false;
  }
  public static Options opts = new Options();

  public static final int COUNT_THRESHOLD = 2;

  private Set<Pair<String, String>> derivations = new HashSet<>();
  private Map<String, Pair<String, String>> lemmaToPosNerMap = new HashMap<>();

  public ParaphraseTableExtractor() throws IOException {
    if (opts.generalize) {
      uploadDerivations();
      LogInfo.begin_track("uploading lemma to pos map");
      uploadLemmaToPasMap();
      LogInfo.end_track();
    }
  }

  private void uploadLemmaToPasMap() {
    for (String line : IOUtils.readLines(opts.questionsPath)) {
      String[] tokens = line.split("\t");
      if (tokens.length < 4) {
        continue;
      }

      String[] posTags = posCanonicalize(tokens[2].split("\\s+"));
      String[] lemmas = tokens[3].split("\\s+");
      String[] nerTags = tokens[4].split("\\s+");
      if (posTags.length != lemmas.length) {
        continue;
      }
      lemmaToPosNerMap.put(Joiner.on(' ').join(lemmas), Pair.newPair(Joiner.on(' ').join(posTags), Joiner.on(' ').join(nerTags)));
    }
  }

  private String[] posCanonicalize(String[] posArray) {
    String[] res = new String[posArray.length];
    for (int i = 0; i < posArray.length; ++i)
      res[i] = LanguageUtils.getCanonicalPos(posArray[i]);
    return res;
  }

  private void uploadDerivations() {
    for (String line : IOUtils.readLines(opts.derivationPath)) {
      String[] tokens = line.split("\t");
      derivations.add(Pair.newPair(tokens[0], tokens[2]));
    }
  }

  public void extract(String outFile) throws IOException {
    Counter<Pair<String, String>> phraseTable = new ClassicCounter<>();

    int i = 0;
    for (String line : IOUtils.readLines(opts.alignmentPath)) {
      handleLine(line, phraseTable);
      i++;
      if (i % 10000 == 0)
        LogInfo.logs("Lines=%s", i);
    }
    LogInfo.logs("Number of phrases=%s", phraseTable.size());
    Counters.retainAbove(phraseTable, COUNT_THRESHOLD);
    PrintWriter writer = IOUtils.getPrintWriter(outFile);
    for (Pair<String, String> pair: phraseTable.keySet()) {
      writer.println(pair.getFirst() + "\t" + pair.getSecond() + "\t" + phraseTable.getCount(pair));
    }
    writer.close();
  }

  private void handleLine(String line,
                          Counter<Pair<String, String>> phraseTable) {

    String[] tokens = line.split("\t");
    if (tokens.length != 3) return;

    String[] source = tokens[0].split("\\s+");
    String[] target = tokens[1].split("\\s+");
    String[] alignments = tokens[2].split("\\s+");

    List<Set<Integer>> sourceAlignment = Lists.newArrayListWithCapacity(source.length);
    for (int i = 0; i < source.length; ++i)
      sourceAlignment.add(new HashSet<>());
    List<Set<Integer>> targetAlignment = Lists.newArrayListWithCapacity(target.length);
    for (int i = 0; i < target.length; ++i)
      targetAlignment.add(new HashSet<>());
    generateAlignments(sourceAlignment, targetAlignment, alignments);

    for (int i = 0; i < source.length; ++i) {
      for (int span1 = 1; span1 <= 5 && i + span1 <= source.length; ++span1) {
        String sourcePhrase = Joiner.on(' ').join(Arrays.asList(source).subList(i, i + span1));
        for (int j = 0; j < target.length; ++j) {
          for (int span2 = 1; span2 <= 5 && j + span2 <= target.length; ++span2) {
            String targetPhrase = Joiner.on(' ').join(Arrays.asList(target).subList(j, j + span2));
            if (targetPhrase.equals(sourcePhrase)) continue;

            if (validAlignment(i, i + span1, j, j + span2, sourceAlignment, targetAlignment)) {
              if (opts.generalize) {
                generalizePhrases(source, target, i, i + span1, j, j + span2, sourceAlignment, phraseTable);
              } else { // then just regular phrases
                phraseTable.incrementCount(Pair.newPair(sourcePhrase, targetPhrase));
                phraseTable.incrementCount(Pair.newPair(targetPhrase, sourcePhrase));
              }
            }
          }
        }
      }
    }
  }

  private void generalizePhrases(String[] source, String[] target, int sourceStart,
                                 int sourceEnd, int targetStart, int targetEnd, List<Set<Integer>> sourceAlignment, Counter<Pair<String, String>> phraseTable) {

    int numOfGeneralizations = 0;
    String sourceUtterance = Joiner.on(' ').join(source);
    String targetUtterance = Joiner.on(' ').join(target);
    if (!lemmaToPosNerMap.containsKey(sourceUtterance) || !lemmaToPosNerMap.containsKey(targetUtterance))
      return;
    String[] sourcePos = lemmaToPosNerMap.get(sourceUtterance).getFirst().split("\\s+");
    String[] targetPos = lemmaToPosNerMap.get(targetUtterance).getFirst().split("\\s+");
    String[] sourceNer = lemmaToPosNerMap.get(sourceUtterance).getSecond().split("\\s+");
    String[] targetNer = lemmaToPosNerMap.get(targetUtterance).getSecond().split("\\s+");


    String[] generalizedSourceArray = new String[sourceEnd - sourceStart];
    String[] generalizedTargetArray = new String[targetEnd - targetStart];
    for (int currIndex = sourceStart; currIndex < sourceEnd; currIndex++) {
      generalizedSourceArray[currIndex - sourceStart] = source[currIndex] + "||" + sourcePos[currIndex] + "||" + sourceNer[currIndex]; // init with same word
      for (Integer aligned : sourceAlignment.get(currIndex)) {
        String sourceToken = source[currIndex];
        String targetToken = target[aligned];
        if (sourceToken.equals(targetToken)) {
          generalizedSourceArray[currIndex - sourceStart] = sourcePos[currIndex] + "_" + numOfGeneralizations;
          generalizedTargetArray[aligned - targetStart] = targetPos[aligned] + "_" + numOfGeneralizations++;
          break;
        } else if (derivations.contains(Pair.newPair(sourceToken, targetToken))) {
          generalizedSourceArray[currIndex - sourceStart] = "DER_" + sourcePos[currIndex] + "_" + numOfGeneralizations;
          generalizedTargetArray[aligned - targetStart] = "DER_" + targetPos[aligned] + "_" + numOfGeneralizations++;
          break;
        }
      }
    }
    if (numOfGeneralizations > 0) {
      for (int currIndex = targetStart; currIndex < targetEnd; ++currIndex) {
        if (generalizedTargetArray[currIndex - targetStart] == null)
          generalizedTargetArray[currIndex - targetStart] = target[currIndex] + "||" + targetPos[currIndex] + "||" + targetNer[currIndex];
      }
      String generalizedSource = Joiner.on(' ').join(generalizedSourceArray);
      String generalizedTarget = Joiner.on(' ').join(generalizedTargetArray);
      if (!generalizedSource.equals(generalizedTarget)) {
        phraseTable.incrementCount(Pair.newPair(generalizedSource, generalizedTarget));
        phraseTable.incrementCount(Pair.newPair(generalizedTarget, generalizedSource));
      }
    }
  }

  private static boolean validAlignment(int sourceStart, int sourceEnd, int targetStart, int targetEnd,
                                        List<Set<Integer>> sourceAlignment, List<Set<Integer>> targetAlignment) {

    // check there is some alignment
    boolean isAligned = false;
    for (int i = sourceStart; i < sourceEnd; ++i) {
      for (int alignedToSource : sourceAlignment.get(i)) {
        if (alignedToSource >= targetStart && alignedToSource < targetEnd) {
          isAligned = true;
        }
      }
    }

    if (!isAligned)
      return false;

    //single word alignments are always fine if there is alignmen
    if (sourceEnd - sourceStart == 1 && targetEnd - targetStart == 1)
      return true;

    // check no source index is aligned out of the target
    for (int i = sourceStart; i < sourceEnd; ++i) {
      for (int alignedToSource : sourceAlignment.get(i)) {
        if (alignedToSource < targetStart || alignedToSource >= targetEnd) {
          return false;
        }
      }
    }

    for (int i = targetStart; i < targetEnd; ++i) {
      for (int alignedToTarget : targetAlignment.get(i)) {
        if (alignedToTarget < sourceStart || alignedToTarget >= sourceEnd) {
          return false;
        }
      }
    }
    return true;
  }

  private static void generateAlignments(List<Set<Integer>> sourceAlignment,
                                         List<Set<Integer>> targetAlignment, String[] alignments) {
    for (String alignment : alignments) {
      String[] parts = alignment.split("-");
      assert parts.length == 2;
      int sourceIndex = Integer.parseInt(parts[0]);
      int targetIndex = Integer.parseInt(parts[1]);

      sourceAlignment.get(sourceIndex).add(targetIndex);
      targetAlignment.get(targetIndex).add(sourceIndex);
    }
  }

  public static void addPhraseCountsToPhraseTable(String phraseTableFile, String outFile) throws IOException {
    Set<String> phrases = new HashSet<String>();
    Counter<String> phraseCounts = new ClassicCounter<>();
    // get phrases
    for (String line:IOUtils.readLines(phraseTableFile)) {
      String[] tokens = line.split("\t");
      phrases.add(tokens[0]);
      phrases.add(tokens[1]);
    }
    // get counts
    LogInfo.logs("Number of phrases=%s", phrases.size());
    Set<String> utterances = new HashSet<>();
    for (String line : IOUtils.readLines(opts.alignmentPath)) {
      String[] tokens = line.split("\t");
      utterances.add(tokens[0]);
      utterances.add(tokens[1]);
    }

    int utteranceNum = 0;
    for (String utterance : utterances) {
      String[] lemmas = utterance.split("\\s+");
      for (int i = 0; i < lemmas.length; ++i) {
        for (int j = i + 1; j <= i + 5 && j <= lemmas.length; ++j) {
          String phrase = Joiner.on(' ').join(Arrays.copyOfRange(lemmas, i, j));
          phraseCounts.incrementCount(phrase);
        }
      }
      if (++utteranceNum % 100000 == 0)
        LogInfo.logs("Number of utterances=%s", utteranceNum);
    }
    LogInfo.logs("Number of counts=%s", phraseCounts.size());
    // write file
    PrintWriter writer = IOUtils.getPrintWriter(outFile);
    for (String line:IOUtils.readLines(phraseTableFile)) {
      String[] tokens = line.split("\t");
      double count1 = phraseCounts.getCount(tokens[0]);
      double count2 = phraseCounts.getCount(tokens[1]);
      writer.println(line + "\t" + count1 + "\t" + count2);
    }
    writer.close();
  }

  public static void main(String[] args) throws IOException {
    ParaphraseTableExtractor extractor = new ParaphraseTableExtractor();
    opts.alignmentPath = args[0];
    extractor.extract(args[1]);
    //addPhraseCountsToPhraseTable(args[0], args[1]);
  }
}
