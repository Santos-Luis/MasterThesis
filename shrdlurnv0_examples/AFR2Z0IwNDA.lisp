(example
  (id session:AFR2Z0IwNDA)
  (context (date 2018 5 13) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-13T23:03:36.510)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:AFR2Z0IwNDA)
  (context (date 2018 5 13) (graph NaiveKnowledgeGraph ((string [[1],[3],[1],[0],[0],[1],[0]]) (name b) (name c))))
  (timeStamp 2018-06-13T23:04:56.722)
  (NBestInd 24)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getTopColor (number 3 COLOR))))))
  (targetValue (string [[1],[],[1],[0],[0],[1],[0]]))
)