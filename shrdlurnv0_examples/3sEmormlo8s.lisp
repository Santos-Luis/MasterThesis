(example
  (id session:3sEmormlo8s)
  (context (date 2018 5 15) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-15T22:48:51.391)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:3sEmormlo8s)
  (context (date 2018 5 15) (graph NaiveKnowledgeGraph ((string [[2],[0],[0],[1],[1]]) (name b) (name c))))
  (timeStamp 2018-06-15T22:50:11.179)
  (NBestInd 34)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[2],[0],[0],[],[]]))
)
(example
  (id session:3sEmormlo8s)
  (context (date 2018 5 15) (graph NaiveKnowledgeGraph ((string [[3],[0],[1],[2],[2],[0]]) (name b) (name c))))
  (timeStamp 2018-06-15T22:50:55.652)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[3],[],[1],[2],[2],[]]))
)
(example
  (id session:3sEmormlo8s)
  (context (date 2018 5 15) (graph NaiveKnowledgeGraph ((string [[3],[2],[3],[0],[2],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-06-15T22:51:25.418)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[3],[2],[3],[],[2],[2],[2]]))
)
(example
  (id session:3sEmormlo8s)
  (context (date 2018 5 15) (graph NaiveKnowledgeGraph ((string [[0],[1],[3],[1],[2],[3]]) (name b) (name c))))
  (timeStamp 2018-06-15T22:51:55.770)
  (NBestInd 0)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[0],[],[3],[],[2],[3]]))
)