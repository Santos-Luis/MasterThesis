(example
  (id session:58MCQcchmCV)
  (context (date 2018 5 14) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-14T23:04:42.464)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:58MCQcchmCV)
  (context (date 2018 5 14) (graph NaiveKnowledgeGraph ((string [[2],[0],[0],[1],[1]]) (name b) (name c))))
  (timeStamp 2018-06-14T23:09:37.636)
  (NBestInd 34)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[2],[0],[0],[],[]]))
)
(example
  (id session:58MCQcchmCV)
  (context (date 2018 5 14) (graph NaiveKnowledgeGraph ((string [[1],[0],[3],[3],[2],[0]]) (name b) (name c))))
  (timeStamp 2018-06-14T23:13:21.160)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[1],[],[3],[3],[2],[]]))
)
(example
  (id session:58MCQcchmCV)
  (context (date 2018 5 14) (graph NaiveKnowledgeGraph ((string [[3],[3],[1],[0],[1],[2]]) (name b) (name c))))
  (timeStamp 2018-06-14T23:17:10.291)
  (NBestInd 0)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[3],[3],[],[0],[],[2]]))
)
(example
  (id session:58MCQcchmCV)
  (context (date 2018 5 14) (graph NaiveKnowledgeGraph ((string [[0],[1],[2],[3]]) (name b) (name c))))
  (timeStamp 2018-06-14T23:17:15.427)
  (NBestInd 16)
  (utterance castanho)
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 2 COLOR)) (number 1 COLOR))))
  (targetValue (string [[0],[1],[2,1],[3]]))
)
(example
  (id session:58MCQcchmCV)
  (context (date 2018 5 14) (graph NaiveKnowledgeGraph ((string [[0],[1],[2],[3]]) (name b) (name c))))
  (timeStamp 2018-06-14T23:17:19.650)
  (NBestInd 48)
  (utterance castanho)
  (targetFormula (call wallToString (call context:stackOnTop (call leftMost1 (call context:getTopColor (number 2 COLOR))) (number 3 COLOR))))
  (targetValue (string [[0],[1],[2,3],[3]]))
)
