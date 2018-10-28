(example
  (id session:B8EpGeo7PtO)
  (context (date 2018 5 6) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-06T13:35:41.144)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:B8EpGeo7PtO)
  (context (date 2018 5 6) (graph NaiveKnowledgeGraph ((string [[2],[3],[1],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-06-06T13:35:46.876)
  (NBestInd 24)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getTopColor (number 3 COLOR))))))
  (targetValue (string [[2],[],[1],[2],[2]]))
)
(example
  (id session:B8EpGeo7PtO)
  (context (date 2018 5 6) (graph NaiveKnowledgeGraph ((string [[2],[0],[0],[1],[1]]) (name b) (name c))))
  (timeStamp 2018-06-06T13:35:55.502)
  (NBestInd 18)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[2],[0],[0],[],[]]))
)
(example
  (id session:B8EpGeo7PtO)
  (context (date 2018 5 6) (graph NaiveKnowledgeGraph ((string [[1],[3],[1],[0],[0],[1],[0]]) (name b) (name c))))
  (timeStamp 2018-06-06T13:36:00.565)
  (NBestInd 0)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[1],[],[1],[0],[0],[1],[0]]))
)
(example
  (id session:B8EpGeo7PtO)
  (context (date 2018 5 6) (graph NaiveKnowledgeGraph ((string [[1],[0],[3],[3],[2],[0]]) (name b) (name c))))
  (timeStamp 2018-06-06T13:36:04.814)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[1],[],[3],[3],[2],[]]))
)
(example
  (id session:B8EpGeo7PtO)
  (context (date 2018 5 6) (graph NaiveKnowledgeGraph ((string [[2],[2],[3],[3],[2]]) (name b) (name c))))
  (timeStamp 2018-06-06T13:36:14.147)
  (NBestInd 24)
  (utterance "apagar direita")
  (targetFormula (call wallToString (call context:removeTop (call rightMost1 (call context:getTopColor (number 2 COLOR))))))
  (targetValue (string [[2],[2],[3],[3],[]]))
)
(example
  (id session:B8EpGeo7PtO)
  (context (date 2018 5 6) (graph NaiveKnowledgeGraph ((string [[0],[3],[3],[0],[0]]) (name b) (name c))))
  (timeStamp 2018-06-06T13:37:25.649)
  (NBestInd 13)
  (utterance "acrescentar laranja direita")
  (targetFormula (call wallToString (call context:stackOnTop (call rightMost1 (call context:getNonEmpty)) (number 3 COLOR))))
  (targetValue (string [[0],[3],[3],[0],[0,3]]))
)
(example
  (id session:B8EpGeo7PtO)
  (context (date 2018 5 6) (graph NaiveKnowledgeGraph ((string [[2],[2],[0],[0],[0]]) (name b) (name c))))
  (timeStamp 2018-06-06T13:37:34.896)
  (NBestInd 1)
  (utterance "acrescentar vermelho direita")
  (targetFormula (call wallToString (call context:stackOnTop (call rightMost1 (call context:getNonEmpty)) (number 2 COLOR))))
  (targetValue (string [[2],[2],[0],[0],[0,2]]))
)
