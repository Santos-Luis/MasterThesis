(example
  (id session:IdCSEWAwToq)
  (context (date 2018 5 28) (graph NaiveKnowledgeGraph ((string [[1],[3],[3],[1],[3],[0],[3]]) (name b) (name c))))
  (timeStamp 2018-06-28T13:23:06.015)
  (NBestInd 7)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[1],[],[],[1],[],[0],[]]))
)
(example
  (id session:IdCSEWAwToq)
  (context (date 2018 5 28) (graph NaiveKnowledgeGraph ((string [[3],[2],[0],[3],[0],[1]]) (name b) (name c))))
  (timeStamp 2018-06-28T13:23:15.116)
  (NBestInd 13)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[3],[2],[],[3],[],[1]]))
)
(example
  (id session:IdCSEWAwToq)
  (context (date 2018 5 28) (graph NaiveKnowledgeGraph ((string [[1],[2],[2],[1],[2],[3]]) (name b) (name c))))
  (timeStamp 2018-06-28T13:23:20.465)
  (NBestInd 0)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[],[2],[2],[],[2],[3]]))
)
