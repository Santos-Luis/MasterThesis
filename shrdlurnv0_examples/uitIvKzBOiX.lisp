(example
  (id session:uitIvKzBOiX)
  (context (date 2018 5 12) (graph NaiveKnowledgeGraph ((string [[1],[3],[2],[3],[0],[1],[3]]) (name b) (name c))))
  (timeStamp 2018-06-12T10:50:42.225)
  (NBestInd 4)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[1],[3],[2],[3],[],[1],[3]]))
)
(example
  (id session:uitIvKzBOiX)
  (context (date 2018 5 12) (graph NaiveKnowledgeGraph ((string [[1],[3],[2],[1],[1],[0]]) (name b) (name c))))
  (timeStamp 2018-06-12T10:50:49.468)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[1],[3],[2],[1],[1],[]]))
)
(example
  (id session:uitIvKzBOiX)
  (context (date 2018 5 12) (graph NaiveKnowledgeGraph ((string [[3],[2],[2],[0],[3],[2]]) (name b) (name c))))
  (timeStamp 2018-06-12T10:50:56.955)
  (NBestInd 17)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[],[2],[2],[0],[],[2]]))
)
(example
  (id session:uitIvKzBOiX)
  (context (date 2018 5 12) (graph NaiveKnowledgeGraph ((string [[0],[3],[0],[2],[3],[0]]) (name b) (name c))))
  (timeStamp 2018-06-12T10:57:33.038)
  (NBestInd 8)
  (utterance "apaga azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[],[3],[],[2],[3],[]]))
)
(example
  (id session:uitIvKzBOiX)
  (context (date 2018 5 12) (graph NaiveKnowledgeGraph ((string [[3],[2],[2],[0],[1],[3]]) (name b) (name c))))
  (timeStamp 2018-06-12T10:57:51.950)
  (NBestInd 0)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[],[2],[2],[0],[1],[]]))
)
(example
  (id session:uitIvKzBOiX)
  (context (date 2018 5 12) (graph NaiveKnowledgeGraph ((string [[2],[1],[1],[1],[2]]) (name b) (name c))))
  (timeStamp 2018-06-12T10:59:12.724)
  (NBestInd 18)
  (utterance "apagar vermelho")
  (targetFormula (call wallToString (call context:removeTop (call rightMost1 (call context:getTopColor (number 2 COLOR))))))
  (targetValue (string [[2],[1],[1],[1],[]]))
)
(example
  (id session:uitIvKzBOiX)
  (context (date 2018 5 12) (graph NaiveKnowledgeGraph ((string [[3],[1],[3],[3],[3],[1]]) (name b) (name c))))
  (timeStamp 2018-06-12T11:02:50.347)
  (NBestInd 6)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call rightMost1 (call context:getTopColor (number 1 COLOR))))))
  (targetValue (string [[3],[1],[3],[3],[3],[]]))
)
