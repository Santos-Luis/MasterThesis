(example
  (id session:LZoZiz04obg)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:11:35.219)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:LZoZiz04obg)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[2],[3],[1],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:11:43.171)
  (NBestInd 24)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getTopColor (number 3 COLOR))))))
  (targetValue (string [[2],[],[1],[2],[2]]))
)
(example
  (id session:LZoZiz04obg)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[2],[0],[0],[1],[1]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:11:50.971)
  (NBestInd 18)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[2],[0],[0],[],[]]))
)
(example
  (id session:LZoZiz04obg)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[3],[0],[1],[0],[3],[1]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:12:43.698)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[3],[],[1],[],[3],[1]]))
)
(example
  (id session:LZoZiz04obg)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[0],[0],[3],[3],[3],[2],[1]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:12:51.077)
  (NBestInd 0)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[0],[0],[],[],[],[2],[1]]))
)
(example
  (id session:LZoZiz04obg)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[2],[2],[2],[0],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:13:01.319)
  (NBestInd 2)
  (utterance "adicionar vermelho direita")
  (targetFormula (call wallToString (call context:stackOnTop (call rightMost1 (call context:getNonEmpty)) (number 2 COLOR))))
  (targetValue (string [[2],[2],[2],[0],[2],[2,2]]))
)
