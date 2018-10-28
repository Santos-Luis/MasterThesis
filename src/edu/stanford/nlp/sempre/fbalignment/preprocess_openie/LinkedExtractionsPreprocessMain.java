package edu.stanford.nlp.sempre.fbalignment.preprocess_openie;

import fig.basic.Option;
import fig.exec.Execution;

import java.io.IOException;

public class LinkedExtractionsPreprocessMain implements Runnable {

  @Option(gloss = "Whether to apply unary or binary preprocessing")
  public String mode;

  @Override
  public void run() {

    try {
      System.out.println("mode: " + mode);
      if (mode.equals("binary"))
        binaryPreprocess();
      else if (mode.equals("unary"))
        unaryPreprocess();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void unaryPreprocess() throws IOException {

    UnaryExtractionsPreprocessor unaryPreprocessor = new UnaryExtractionsPreprocessor();
    unaryPreprocessor.preprocess();

  }

  private void binaryPreprocess() throws IOException {
    BinaryExtractionsPreprocessor binaryPreprocessor = new BinaryExtractionsPreprocessor();
    binaryPreprocessor.preprocess();
  }

  public static void main(String[] args) {
    Execution.run(
        args,
        "LinkedExtractionsPreprocessMain", new LinkedExtractionsPreprocessMain(),
        "ExtractionsPreprocessor", ExtractionsPreprocessor.opts);

  }

}
