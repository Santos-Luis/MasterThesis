(example
  (id session:TWoV3rEl89v)
  (context (date 2018 5 14) (graph NaiveKnowledgeGraph ((string [[1],[3],[2],[3],[0],[2]]) (name b) (name c))))
  (timeStamp 2018-06-14T23:18:03.210)
  (NBestInd 2)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[0],[2]]))
)
(example
  (id session:TWoV3rEl89v)
  (context (date 2018 5 14) (graph NaiveKnowledgeGraph ((string [[3],[3],[2],[2],[1],[1]]) (name b) (name c))))
  (timeStamp 2018-06-14T23:21:50.871)
  (NBestInd 36)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[],[],[2],[2],[1],[1]]))
)
(example
  (id session:TWoV3rEl89v)
  (context (date 2018 5 14) (graph NaiveKnowledgeGraph ((string [[3],[2],[2],[1],[0],[3],[1]]) (name b) (name c))))
  (timeStamp 2018-06-14T23:23:35.775)
  (NBestInd 7)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[3],[2],[2],[1],[],[3],[1]]))
)
(example
  (id session:TWoV3rEl89v)
  (context (date 2018 5 14) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[0],[3]]) (name b) (name c))))
  (timeStamp 2018-06-14T23:27:23.761)
  (NBestInd 5)
  (utterance "apagar vermelho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 2 COLOR)))))
  (targetValue (string [[0],[3],[],[0],[3]]))
)
(example
  (id session:TWoV3rEl89v)
  (context (date 2018 5 14) (graph NaiveKnowledgeGraph ((string [[0],[1],[3],[3],[1],[1],[0]]) (name b) (name c))))
  (timeStamp 2018-06-14T23:27:58.762)
  (NBestInd 0)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[0],[1],[],[],[1],[1],[0]]))
)
