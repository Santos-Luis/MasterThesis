(example
  (id session:5cU1grsDVaB)
  (context (date 2018 5 11) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-11T13:43:20.625)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:5cU1grsDVaB)
  (context (date 2018 5 11) (graph NaiveKnowledgeGraph ((string [[2],[3],[1],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-06-11T13:43:28.258)
  (NBestInd 24)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getTopColor (number 3 COLOR))))))
  (targetValue (string [[2],[],[1],[2],[2]]))
)
(example
  (id session:5cU1grsDVaB)
  (context (date 2018 5 11) (graph NaiveKnowledgeGraph ((string [[2],[0],[0],[1],[1]]) (name b) (name c))))
  (timeStamp 2018-06-11T13:43:37.350)
  (NBestInd 18)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[2],[0],[0],[],[]]))
)
(example
  (id session:5cU1grsDVaB)
  (context (date 2018 5 11) (graph NaiveKnowledgeGraph ((string [[1],[3],[1],[0],[0],[1],[0]]) (name b) (name c))))
  (timeStamp 2018-06-11T13:43:42.548)
  (NBestInd 0)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[1],[],[1],[0],[0],[1],[0]]))
)
(example
  (id session:5cU1grsDVaB)
  (context (date 2018 5 11) (graph NaiveKnowledgeGraph ((string [[1],[0],[3],[3],[2],[0]]) (name b) (name c))))
  (timeStamp 2018-06-11T13:43:53.054)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[1],[],[3],[3],[2],[]]))
)
(example
  (id session:5cU1grsDVaB)
  (context (date 2018 5 11) (graph NaiveKnowledgeGraph ((string [[2],[2],[3],[3],[2]]) (name b) (name c))))
  (timeStamp 2018-06-11T13:44:00.234)
  (NBestInd 24)
  (utterance "apagar direita")
  (targetFormula (call wallToString (call context:removeTop (call rightMost1 (call context:getTopColor (number 2 COLOR))))))
  (targetValue (string [[2],[2],[3],[3],[]]))
)
(example
  (id session:5cU1grsDVaB)
  (context (date 2018 5 11) (graph NaiveKnowledgeGraph ((string [[0],[3],[3],[0],[0]]) (name b) (name c))))
  (timeStamp 2018-06-11T13:44:11.387)
  (NBestInd 13)
  (utterance "acrescentar laranja direita")
  (targetFormula (call wallToString (call context:stackOnTop (call rightMost1 (call context:getNonEmpty)) (number 3 COLOR))))
  (targetValue (string [[0],[3],[3],[0],[0,3]]))
)
(example
  (id session:5cU1grsDVaB)
  (context (date 2018 5 11) (graph NaiveKnowledgeGraph ((string [[0],[1],[2],[3]]) (name b) (name c))))
  (timeStamp 2018-06-11T13:45:00.063)
  (NBestInd 0)
  (utterance vermelho)
)
(example
  (id session:5cU1grsDVaB)
  (context (date 2018 5 11) (graph NaiveKnowledgeGraph ((string [[1],[3],[1],[1]]) (name b) (name c))))
  (timeStamp 2018-06-11T13:45:20.870)
  (NBestInd 0)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[],[3],[],[]]))
)
(example
  (id session:5cU1grsDVaB)
  (context (date 2018 5 11) (graph NaiveKnowledgeGraph ((string [[3],[3],[3],[2]]) (name b) (name c))))
  (timeStamp 2018-06-11T13:46:12.869)
  (NBestInd 0)
  (utterance "acrescentar laranja direita")
  (targetFormula (call wallToString (call context:stackOnTop (call rightMost1 (call context:getNonEmpty)) (number 3 COLOR))))
  (targetValue (string [[3],[3],[3],[2,3]]))
)
