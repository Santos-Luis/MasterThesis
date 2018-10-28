package edu.stanford.nlp.sempre.fbalignment.fbgraph;

import com.google.common.collect.BiMap;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.FbBipartiteNode;
import edu.stanford.nlp.util.Pair;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FbPropertiesExpectedTypes implements Serializable {

  private static final long serialVersionUID = -2144428085120449706L;
  private Map<String, Pair<String, String>> fbPropertiesExpectedTypesMap;

  public FbPropertiesExpectedTypes(String file) throws IOException {

    fbPropertiesExpectedTypesMap = new HashMap<String, Pair<String, String>>();
    for (String line : IOUtils.readLines(file)) {
      String[] tokens = line.split("\t");

      fbPropertiesExpectedTypesMap.put(tokens[0], new Pair<String, String>(tokens[1], tokens[2]));
      /*if(!fbPropertiesExpectedTypesMap.containsKey(FormatConverter.fromDotToSlash(tokens[0])))
        fbPropertiesExpectedTypesMap.put(FormatConverter.fromDotToSlash(tokens[0]),
            new Pair<String, String>(FormatConverter.fromDotToSlash(tokens[1]),
                FormatConverter.fromDotToSlash(tokens[2])));*/
    }
  }

  public Pair<String, String> getExpectedTypes(FbBipartiteNode fbNode) {

    List<String> compositePredicate = fbNode.getCompositePredicate();


    String type1 = fbPropertiesExpectedTypesMap.get(compositePredicate.get(0)).first;
    String type2 = fbPropertiesExpectedTypesMap.get(compositePredicate.get(compositePredicate.size() - 1)).second;
    if (type1 == null || type2 == null)
      throw new IllegalStateException("Missing expected type for FB node: " + fbNode.getDescription());
    /*else if (type2 == null) {
      LogInfo.logs("Missing expected type2 for FB node: " + fbNode.getDescription());
      type2 = "/base";
    }*/

    if (fbNode.isReversed()) {
      return new Pair<String, String>(type2, type1);
    }
    return new Pair<String, String>(type1, type2);
  }

  public Pair<Integer, Integer> getExpectedTypeIds(FbBipartiteNode fbTypedNode, BiMap<String, Integer> type2IdMap) {

    Pair<String, String> typePair = getExpectedTypes(fbTypedNode);

    Integer typeId1 = type2IdMap.get(typePair.first);
    Integer typeId2 = type2IdMap.get(typePair.second);

    if (typeId1 == null || typeId2 == null)
      throw new IllegalStateException("Missing typed id for expected type for FB node: " + fbTypedNode.getDescription());

    return new Pair<Integer, Integer>(typeId1, typeId2);
  }
}
