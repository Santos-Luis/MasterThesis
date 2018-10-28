package edu.stanford.nlp.sempre.clojure;

import edu.stanford.nlp.sempre.*;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

import fig.basic.*;

/**
 * Concatenate and convert string into clojure code and execute code
 *
 * @author Alex Ratner
 */
public final class ClojureExecute {
  private ClojureExecute() { }

  public static class Options {
    @Option(gloss = "The path of the clojure code to wrap the parsed form in for execution")
    public String executorPath = "clojure/executors/clojure_executor_default.clj";
  }

  public static Options opts = new Options();

  // TODO(alex): clean this all up
  public static String execute(StringValue codeVal) {
    String code = codeVal.value;

    // load the execution wrapper file
    String exec;
    try {
     // byte[] encoded = Files.readAllBytes(Paths.get(opts.executorPath));
      byte[] encoded = Files.readAllBytes(Paths.get(opts.executorPath));
      exec = new String(encoded, StandardCharsets.UTF_8);
    } catch (IOException e) {
      System.out.println("IOException: Failed to read execution loop file for clojure");
      e.printStackTrace();
      return "ERR";
    }

    // form final code using execution wrapper
    String toExecute = exec.replaceAll("\"\\s*\\(var body\\)\\s*\"", Matcher.quoteReplacement(" " + code + " "));

    // wrap parse string in execution loop and execute
    IFn loadString = Clojure.var("clojure.core", "load-string");
    loadString.invoke(toExecute);
    IFn f = Clojure.var("clojure.core", "execute");
    IFn str = Clojure.var("clojure.core", "str");
    return (String) str.invoke(f.invoke());
  }
}
