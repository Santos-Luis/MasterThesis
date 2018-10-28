package edu.stanford.nlp.sempre.paraphrase.scripts;

import edu.stanford.nlp.io.IOUtils;
import fig.basic.LispTree;
import fig.basic.LogInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by joberant on 2/26/15.
 * over-sampling superlatives for geo880 training set
 */
public class SuperlativeExampleSampler {

  public static void main(String[] args) {

    List<LispTree> superlatives = new ArrayList<>();
    List<LispTree> comparatives = new ArrayList<>();
    List<LispTree> howMany = new ArrayList<>();
    List<LispTree> other = new ArrayList<>();


    Iterator<LispTree> trees = LispTree.proto.parseFromFile(args[0]);

    while(trees.hasNext()) {

      LispTree tree = trees.next();
      LispTree original = tree.child(2);
      if(!original.child(0).value.equals("original"))
        throw new RuntimeException("Second child is not 'original': " + tree);

      String originalUtterance = original.child(1).value;
      if(originalUtterance.contains("largest") || originalUtterance.contains("smallest")) {
        //tripling the number of superlatives
        superlatives.add(tree);
        //superlatives.add(tree);
        //superlatives.add(tree);
      }
      else if(originalUtterance.contains("larger") || originalUtterance.contains("smaller")
              || originalUtterance.contains("at least")  || originalUtterance.contains("at most")) {
        comparatives.add(tree);
      }
      else
        other.add(tree);
    }
    LogInfo.logs("#Superlatives=%s", superlatives.size());
    LogInfo.logs("#Comparative=%s", comparatives.size());
    LogInfo.logs("#Other=%s", other.size());

    List<LispTree> result = new ArrayList<>();
    result.addAll(superlatives);
    result.addAll(other.subList(0,300));
    Collections.shuffle(result);
    LogInfo.logs("TOTAL sampled=%s", result.size());

    PrintWriter writer = null;
    try {
      writer = IOUtils.getPrintWriter(args[1]);
    } catch (IOException e) {
      e.printStackTrace();
    }
    for(LispTree out: result) {
      out.print(180,180,writer);
      writer.println();
    }
    writer.close();
  }
}
