(example
  (id session:S5CNgTDSL7S)
  (context (date 2018 5 28) (graph NaiveKnowledgeGraph ((string [[0],[0],[2],[2],[3],[2],[0]]) (name b) (name c))))
  (timeStamp 2018-06-28T13:26:03.591)
  (NBestInd 4)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[],[],[2],[2],[3],[2],[]]))
)
(example
  (id session:S5CNgTDSL7S)
  (context (date 2018 5 28) (graph NaiveKnowledgeGraph ((string [[2],[3],[2],[0],[1]]) (name b) (name c))))
  (timeStamp 2018-06-28T13:26:07.395)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[2],[3],[2],[],[1]]))
)
(example
  (id session:S5CNgTDSL7S)
  (context (date 2018 5 28) (graph NaiveKnowledgeGraph ((string [[3],[2],[2],[3],[1],[1],[0]]) (name b) (name c))))
  (timeStamp 2018-06-28T13:26:13.799)
  (NBestInd 7)
  (utterance "apagar vermelho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 2 COLOR)))))
  (targetValue (string [[3],[],[],[3],[1],[1],[0]]))
)
