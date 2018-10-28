package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.*;
import edu.stanford.nlp.sempre.freebase.*;
import fig.basic.LispTree;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by joberant on 6/19/14.
 * Given a log file where the target formula is in Pred@0000 we generate a data file
 */
public final class GenerateDataFromLog {
  private GenerateDataFromLog() { }

  public static void main(String[] args) throws IOException {

//    OptionsParser parser = new OptionsParser();
//    SparqlExecutor.MainOptions mainOpts = new SparqlExecutor.MainOptions();
//    parser.registerAll(new Object[]{"SparqlExecutor", SparqlExecutor.opts, "FreebaseInfo", FreebaseInfo.opts, "main", mainOpts});
//    parser.parse(args);
    SparqlExecutor.opts.endpointUrl = "http://jonsson:3093/sparql";
    SparqlExecutor executor = new SparqlExecutor();

    Map<String, Formula> utteranceToFormulaMap = new HashMap<>();
    String utterance = "";
    for (String line : IOUtils.readLines(args[0])) {
      if (line.contains("Example: ")) {
        utterance = line.substring(line.indexOf(':') + 2, line.indexOf('{')).trim();
      } else if (line.contains("Pred@0000")) {
        int formulaIndex = line.indexOf("(formula");
        int valueIndex = line.indexOf("(value");
        String formulaStr = line.substring(formulaIndex + 9, valueIndex - 2).trim();
        Formula f  = Formula.fromString(formulaStr);
        Executor.Response response = executor.execute(f, null);
        Value value = response.value;
        ListValue lValue = (ListValue) value;
        if (lValue.values.size() > 0)
          utteranceToFormulaMap.put(utterance, f);
      }
    }

    PrintWriter writer = IOUtils.getPrintWriter(args[1]);
    for (String u : utteranceToFormulaMap.keySet()) {
      LispTree tree = LispTree.proto.newList();
      tree.addChild("example");

      LispTree utteranceTree = LispTree.proto.newList();
      utteranceTree.addChild("utterance");
      utteranceTree.addChild(u);
      tree.addChild(utteranceTree);

      LispTree targetFormulaTree = LispTree.proto.newList();
      targetFormulaTree.addChild("targetFormula");
      Formula f = utteranceToFormulaMap.get(u);
      targetFormulaTree.addChild(f.toLispTree());
      tree.addChild(targetFormulaTree);
      writer.println(tree);
    }
  }
}
