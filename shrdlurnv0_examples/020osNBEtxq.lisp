(example
  (id session:020osNBEtxq)
  (context (date 2018 5 21) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-21T15:49:22.098)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:020osNBEtxq)
  (context (date 2018 5 21) (graph NaiveKnowledgeGraph ((string [[2],[3],[1],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-06-21T15:49:29.634)
  (NBestInd 24)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getTopColor (number 3 COLOR))))))
  (targetValue (string [[2],[],[1],[2],[2]]))
)
(example
  (id session:020osNBEtxq)
  (context (date 2018 5 21) (graph NaiveKnowledgeGraph ((string [[3],[0],[1],[2],[2],[0]]) (name b) (name c))))
  (timeStamp 2018-06-21T16:14:44.548)
  (NBestInd 4)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[3],[],[1],[2],[2],[]]))
)
(example
  (id session:020osNBEtxq)
  (context (date 2018 5 21) (graph NaiveKnowledgeGraph ((string [[3],[3],[1],[0],[1],[2]]) (name b) (name c))))
  (timeStamp 2018-06-21T16:14:54.670)
  (NBestInd 13)
  (utterance "apagar castanhi")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[3],[3],[],[0],[],[2]]))
)
