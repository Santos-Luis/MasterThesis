(example
  (id session:0KEQH3e0ogO)
  (context (date 2018 5 15) (graph NaiveKnowledgeGraph ((string [[2],[0],[1],[0],[1],[2],[1]]) (name b) (name c))))
  (timeStamp 2018-06-15T08:56:00.506)
  (NBestInd 4)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[2],[],[1],[],[1],[2],[1]]))
)
(example
  (id session:0KEQH3e0ogO)
  (context (date 2018 5 15) (graph NaiveKnowledgeGraph ((string [[0],[1],[2],[3]]) (name b) (name c))))
  (timeStamp 2018-06-15T08:56:05.407)
  (NBestInd 3)
  (utterance castanho)
  (targetFormula (call wallToString (call context:stackOnTop (call complement (call leftMost1 (call context:getNonEmpty))) (number 1 COLOR))))
  (targetValue (string [[0],[1,1],[2,1],[3,1]]))
)
