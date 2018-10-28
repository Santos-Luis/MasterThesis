package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;
import fig.basic.LispTree;
import fig.prob.SampleUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class SplitExamples {
  private SplitExamples() { }

  public static void main(String[] args) throws IOException {

    String path = args[0];
    String out = args[1];
    double splitRatio = Double.parseDouble(args[2]);
    List<LispTree> trees = new ArrayList<LispTree>();
    Iterator<LispTree> it = LispTree.proto.parseFromFile(path);
    while (it.hasNext()) {
      LispTree tree = it.next();
      List<LispTree> newChildren = new ArrayList<LispTree>();
      for (LispTree child : tree.children) {
        if (!child.isLeaf() && child.child(0) != null && child.child(0).value.equals("targetFormula"))
          continue;
        newChildren.add(child);
      }
      tree.children = newChildren;
      trees.add(tree);
    }

    int split = (int) (splitRatio * trees.size());
    int[] perm = SampleUtils.samplePermutation(new Random(1), trees.size());
    List<LispTree> train = new ArrayList<LispTree>();
    List<LispTree> test = new ArrayList<LispTree>();
    for (int i = 0; i < split; i++)
      train.add(trees.get(perm[i]));
    for (int i = split; i < trees.size(); i++)
      test.add(trees.get(perm[i]));

    PrintWriter writer = IOUtils.getPrintWriter(out + ".train");
    for (LispTree tree : train) {
      tree.print(writer);
      writer.println();
    }
    writer.close();

    writer = IOUtils.getPrintWriter(out + ".test");
    for (LispTree tree : test) {
      tree.print(writer);
      writer.println();
    }
    writer.close();

  }

}
