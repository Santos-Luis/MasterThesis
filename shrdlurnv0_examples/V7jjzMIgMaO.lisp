(example
  (id session:V7jjzMIgMaO)
  (context (date 2018 5 29) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-29T09:58:08.381)
  (NBestInd 2)
  (utterance "njjgnf jdn")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:V7jjzMIgMaO)
  (context (date 2018 5 29) (graph NaiveKnowledgeGraph ((string [[2],[0],[0],[1],[1]]) (name b) (name c))))
  (timeStamp 2018-06-29T10:00:18.778)
  (NBestInd 77)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[2],[0],[0],[],[]]))
)
(example
  (id session:V7jjzMIgMaO)
  (context (date 2018 5 29) (graph NaiveKnowledgeGraph ((string [[1],[3],[1],[0],[0],[1],[0]]) (name b) (name c))))
  (timeStamp 2018-06-29T10:00:30.062)
  (NBestInd 14)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[1],[],[1],[0],[0],[1],[0]]))
)
(example
  (id session:V7jjzMIgMaO)
  (context (date 2018 5 29) (graph NaiveKnowledgeGraph ((string [[3],[3],[1],[0],[1],[2]]) (name b) (name c))))
  (timeStamp 2018-06-29T10:01:30.874)
  (NBestInd 23)
  (utterance "njjgnf jdn")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getTopColor (number 1 COLOR))))))
  (targetValue (string [[3],[3],[],[0],[1],[2]]))
)
(example
  (id session:V7jjzMIgMaO)
  (context (date 2018 5 29) (graph NaiveKnowledgeGraph ((string [[0],[0],[3],[3],[3],[2],[1]]) (name b) (name c))))
  (timeStamp 2018-06-29T10:01:39.151)
  (NBestInd 0)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[0],[0],[],[],[],[2],[1]]))
)
(example
  (id session:V7jjzMIgMaO)
  (context (date 2018 5 29) (graph NaiveKnowledgeGraph ((string [[1],[3],[2],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-29T10:01:48.751)
  (NBestInd 0)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[1],[],[2],[],[]]))
)
(example
  (id session:V7jjzMIgMaO)
  (context (date 2018 5 29) (graph NaiveKnowledgeGraph ((string [[2],[3],[0],[1],[1],[3],[2]]) (name b) (name c))))
  (timeStamp 2018-06-29T10:01:53.665)
  (NBestInd 1)
  (utterance "njjgnf jdn")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getTopColor (number 0 COLOR))))))
  (targetValue (string [[2],[3],[],[1],[1],[3],[2]]))
)
(example
  (id session:V7jjzMIgMaO)
  (context (date 2018 5 29) (graph NaiveKnowledgeGraph ((string [[0],[2],[2],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-06-29T10:01:58.928)
  (NBestInd 13)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[],[2],[2],[2],[2]]))
)
(example
  (id session:V7jjzMIgMaO)
  (context (date 2018 5 29) (graph NaiveKnowledgeGraph ((string [[0],[0],[0],[1]]) (name b) (name c))))
  (timeStamp 2018-06-29T10:02:07.472)
  (NBestInd 3)
  (utterance "adicionar azul direita")
  (targetFormula (call wallToString (call context:stackOnTop (call complement (call context:getTopColor (number 0 COLOR))) (number 0 COLOR))))
  (targetValue (string [[0],[0],[0],[1,0]]))
)
(example
  (id session:V7jjzMIgMaO)
  (context (date 2018 5 29) (graph NaiveKnowledgeGraph ((string [[0],[0],[2],[0],[0],[0]]) (name b) (name c))))
  (timeStamp 2018-06-29T10:02:48.233)
  (NBestInd 2)
  (utterance "adicionar direita")
  (targetFormula (call wallToString (call context:stackOnTop (call rightMost1 (call context:getTopColor (number 0 COLOR))) (number 0 COLOR))))
  (targetValue (string [[0],[0],[2],[0],[0],[0,0]]))
)
(example
  (id session:V7jjzMIgMaO)
  (context (date 2018 5 29) (graph NaiveKnowledgeGraph ((string [[3],[3],[2],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-29T10:02:59.990)
  (NBestInd 0)
  (utterance "adicionar laranja em laranja")
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 3 COLOR)) (number 3 COLOR))))
  (targetValue (string [[3,3],[3,3],[2],[3,3],[3,3],[3,3]]))
)
(example
  (id session:V7jjzMIgMaO)
  (context (date 2018 5 29) (graph NaiveKnowledgeGraph ((string [[0],[2],[2],[2],[0],[0]]) (name b) (name c))))
  (timeStamp 2018-06-29T10:03:06.506)
  (NBestInd 0)
  (utterance "apagar direita")
  (targetFormula (call wallToString (call context:removeTop (call rightMost1 (call context:getTopColor (number 0 COLOR))))))
  (targetValue (string [[0],[2],[2],[2],[0],[]]))
)
(example
  (id session:V7jjzMIgMaO)
  (context (date 2018 5 29) (graph NaiveKnowledgeGraph ((string [[0],[2],[0],[0]]) (name b) (name c))))
  (timeStamp 2018-06-29T10:12:26.059)
  (NBestInd 8)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[2],[0],[0]]))
)
(example
  (id session:V7jjzMIgMaO)
  (context (date 2018 5 29) (graph NaiveKnowledgeGraph ((string [[3],[3],[3],[3],[2],[3]]) (name b) (name c))))
  (timeStamp 2018-06-29T10:12:37.769)
  (NBestInd 5)
  (utterance "adicionar laranja esquerda")
  (targetFormula (call wallToString (call context:stackOnTop (call leftMost1 (call context:getTopColor (number 3 COLOR))) (number 3 COLOR))))
  (targetValue (string [[3,3],[3],[3],[3],[2],[3]]))
)
(example
  (id session:V7jjzMIgMaO)
  (context (date 2018 5 29) (graph NaiveKnowledgeGraph ((string [[1],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-29T10:12:44.834)
  (NBestInd 1)
  (utterance "apagar esquerda")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[3],[3]]))
)