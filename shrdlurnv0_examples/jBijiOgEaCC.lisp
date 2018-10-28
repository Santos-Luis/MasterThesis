(example
  (id session:jBijiOgEaCC)
  (context (date 2018 5 19) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-19T08:17:37.343)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:jBijiOgEaCC)
  (context (date 2018 5 19) (graph NaiveKnowledgeGraph ((string [[2],[3],[1],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-06-19T08:19:11.846)
  (NBestInd 24)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getTopColor (number 3 COLOR))))))
  (targetValue (string [[2],[],[1],[2],[2]]))
)
(example
  (id session:jBijiOgEaCC)
  (context (date 2018 5 19) (graph NaiveKnowledgeGraph ((string [[0],[1],[2],[3]]) (name b) (name c))))
  (timeStamp 2018-06-19T08:19:16.868)
  (NBestInd 0)
  (utterance azul)
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[1],[2],[3]]))
)
(example
  (id session:jBijiOgEaCC)
  (context (date 2018 5 19) (graph NaiveKnowledgeGraph ((string [[1],[3],[1],[0],[0],[1],[0]]) (name b) (name c))))
  (timeStamp 2018-06-19T08:19:21.558)
  (NBestInd 0)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[1],[],[1],[0],[0],[1],[0]]))
)
(example
  (id session:jBijiOgEaCC)
  (context (date 2018 5 19) (graph NaiveKnowledgeGraph ((string [[1],[0],[3],[3],[2],[0]]) (name b) (name c))))
  (timeStamp 2018-06-19T08:21:37.982)
  (NBestInd 1)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[1],[],[3],[3],[2],[]]))
)
(example
  (id session:jBijiOgEaCC)
  (context (date 2018 5 19) (graph NaiveKnowledgeGraph ((string [[0],[1],[2],[3]]) (name b) (name c))))
  (timeStamp 2018-06-19T08:24:34.633)
  (NBestInd 0)
  (utterance laranja)
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 3 COLOR)) (number 3 COLOR))))
  (targetValue (string [[0],[1],[2],[3,3]]))
)
(example
  (id session:jBijiOgEaCC)
  (context (date 2018 5 19) (graph NaiveKnowledgeGraph ((string [[3],[3],[1],[0],[1],[2]]) (name b) (name c))))
  (timeStamp 2018-06-19T08:24:46.395)
  (NBestInd 2)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[3],[3],[],[0],[],[2]]))
)
(example
  (id session:jBijiOgEaCC)
  (context (date 2018 5 19) (graph NaiveKnowledgeGraph ((string [[1],[3],[1],[3],[1]]) (name b) (name c))))
  (timeStamp 2018-06-19T08:24:52.695)
  (NBestInd 0)
  (utterance "adicionar castanho")
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 1 COLOR)) (number 1 COLOR))))
  (targetValue (string [[1,1],[3],[1,1],[3],[1,1]]))
)