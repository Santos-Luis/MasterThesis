(example
  (id session:s0ntjtEzX0a)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-22T01:00:18.879)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:s0ntjtEzX0a)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[0],[2],[0],[1],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-22T01:00:53.181)
  (NBestInd 1)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[],[2],[],[1],[3],[3]]))
)
(example
  (id session:s0ntjtEzX0a)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-22T01:03:35.544)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:s0ntjtEzX0a)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[0],[2],[0],[1],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-22T01:03:43.790)
  (NBestInd 1)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[],[2],[],[1],[3],[3]]))
)
