package edu.stanford.nlp.sempre.fbalignment.bipartite.filters;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteNode;

public class FbRelationFilter implements BipartiteNodeFilter {

  private static final String USER_FB_NODE_PREFIX = "/user/";
  private static final String BASE_FB_NODE_PREFIX = "/base/";
  private static final String DATA_WORLD_FB_NODE_PREFIX = "/dataworld/";
  private static final String COMMON_NODE_PREFIX = "/common/";
  private static final String FREEBASE_NODE_PREFIX = "/freebase/";

  @Override
  public boolean filterNode(BipartiteNode node) {

    boolean toFilter = false;
    toFilter |= node.getDescription().contains("characters_of_this_gender");
    toFilter |= node.getDescription().contains(USER_FB_NODE_PREFIX);
    toFilter |= node.getDescription().contains(BASE_FB_NODE_PREFIX);
    toFilter |= node.getDescription().contains(DATA_WORLD_FB_NODE_PREFIX);
    toFilter |= node.getDescription().contains(COMMON_NODE_PREFIX);
    toFilter |= node.getDescription().contains(FREEBASE_NODE_PREFIX);

    return toFilter;
  }

}
