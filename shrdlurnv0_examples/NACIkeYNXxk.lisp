(example
  (id session:NACIkeYNXxk)
  (context (date 2018 5 10) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-10T12:33:28.559)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:NACIkeYNXxk)
  (context (date 2018 5 10) (graph NaiveKnowledgeGraph ((string [[2],[3],[1],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-06-10T12:33:36.001)
  (NBestInd 24)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getTopColor (number 3 COLOR))))))
  (targetValue (string [[2],[],[1],[2],[2]]))
)
(example
  (id session:NACIkeYNXxk)
  (context (date 2018 5 10) (graph NaiveKnowledgeGraph ((string [[2],[0],[0],[1],[1]]) (name b) (name c))))
  (timeStamp 2018-06-10T12:33:44.519)
  (NBestInd 18)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[2],[0],[0],[],[]]))
)
(example
  (id session:NACIkeYNXxk)
  (context (date 2018 5 10) (graph NaiveKnowledgeGraph ((string [[1],[3],[1],[0],[0],[1],[0]]) (name b) (name c))))
  (timeStamp 2018-06-10T12:33:49.356)
  (NBestInd 0)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[1],[],[1],[0],[0],[1],[0]]))
)
(example
  (id session:NACIkeYNXxk)
  (context (date 2018 5 10) (graph NaiveKnowledgeGraph ((string [[1],[0],[3],[3],[2],[0]]) (name b) (name c))))
  (timeStamp 2018-06-10T12:33:53.896)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[1],[],[3],[3],[2],[]]))
)
(example
  (id session:NACIkeYNXxk)
  (context (date 2018 5 10) (graph NaiveKnowledgeGraph ((string [[2],[2],[3],[3],[2]]) (name b) (name c))))
  (timeStamp 2018-06-10T12:34:00.671)
  (NBestInd 24)
  (utterance "apagar direita")
  (targetFormula (call wallToString (call context:removeTop (call rightMost1 (call context:getTopColor (number 2 COLOR))))))
  (targetValue (string [[2],[2],[3],[3],[]]))
)
(example
  (id session:NACIkeYNXxk)
  (context (date 2018 5 10) (graph NaiveKnowledgeGraph ((string [[0],[3],[3],[0],[0]]) (name b) (name c))))
  (timeStamp 2018-06-10T12:34:10.023)
  (NBestInd 13)
  (utterance "acrescentar laranja direita")
  (targetFormula (call wallToString (call context:stackOnTop (call rightMost1 (call context:getNonEmpty)) (number 3 COLOR))))
  (targetValue (string [[0],[3],[3],[0],[0,3]]))
)
(example
  (id session:NACIkeYNXxk)
  (context (date 2018 5 10) (graph NaiveKnowledgeGraph ((string [[2],[2],[0],[0],[0]]) (name b) (name c))))
  (timeStamp 2018-06-10T12:34:22.493)
  (NBestInd 1)
  (utterance "acrescentar vermelho direita")
  (targetFormula (call wallToString (call context:stackOnTop (call rightMost1 (call context:getNonEmpty)) (number 2 COLOR))))
  (targetValue (string [[2],[2],[0],[0],[0,2]]))
)
(example
  (id session:NACIkeYNXxk)
  (context (date 2018 5 10) (graph NaiveKnowledgeGraph ((string [[1],[3],[1],[1]]) (name b) (name c))))
  (timeStamp 2018-06-10T12:34:27.774)
  (NBestInd 0)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[],[3],[],[]]))
)
(example
  (id session:NACIkeYNXxk)
  (context (date 2018 5 10) (graph NaiveKnowledgeGraph ((string [[3],[3],[3],[2]]) (name b) (name c))))
  (timeStamp 2018-06-10T12:34:37.764)
  (NBestInd 14)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:stackOnTop (call complement (call context:getTopColor (number 3 COLOR))) (number 3 COLOR))))
  (targetValue (string [[3],[3],[3],[2,3]]))
)
