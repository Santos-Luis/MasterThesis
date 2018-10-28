(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:15:23.846)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[2],[3],[1],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:15:30.658)
  (NBestInd 24)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getTopColor (number 3 COLOR))))))
  (targetValue (string [[2],[],[1],[2],[2]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[2],[0],[0],[1],[1]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:15:39.982)
  (NBestInd 18)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[2],[0],[0],[],[]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[3],[3],[1],[0],[1],[2]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:15:49.526)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[3],[3],[1],[],[1],[2]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[3],[0],[1],[0],[3],[1]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:15:55.875)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[3],[],[1],[],[3],[1]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[2],[3],[2],[2],[2],[1],[3]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:16:01.179)
  (NBestInd 0)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[2],[3],[2],[2],[2],[],[3]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[2],[0],[1],[1],[2],[3],[0]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:16:03.962)
  (NBestInd 0)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[2],[0],[1],[1],[2],[],[0]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[0],[2],[0],[2]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:16:18.301)
  (NBestInd 21)
  (utterance "apagar direita")
  (targetFormula (call wallToString (call context:removeTop (call rightMost1 (call context:getTopColor (number 2 COLOR))))))
  (targetValue (string [[0],[2],[0],[]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[1],[1],[2],[1],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:16:30.754)
  (NBestInd 14)
  (utterance "adicionar vermelho esquerda")
  (targetFormula (call wallToString (call context:stackOnTop (call leftMost1 (call context:getTopColor (number 1 COLOR))) (number 2 COLOR))))
  (targetValue (string [[1,2],[1],[2],[1],[2],[2]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[0],[3],[1],[0],[2]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:16:48.042)
  (NBestInd 9)
  (utterance "adicionar esquerda")
  (targetFormula (call wallToString (call context:stackOnTop (call leftMost1 (call context:getNonEmpty)) (number 3 COLOR))))
  (targetValue (string [[0,3],[3],[1],[0],[2]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[3],[0],[0],[0]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:17:21.074)
  (NBestInd 1)
  (utterance "adicionar azul em laranja")
  (targetFormula (call wallToString (call context:stackOnTop (call leftMost1 (call context:getNonEmpty)) (number 0 COLOR))))
  (targetValue (string [[3,0],[0],[0],[0]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[0],[2],[2],[2],[0],[0]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:17:27.796)
  (NBestInd 0)
  (utterance "apagar direita")
  (targetFormula (call wallToString (call context:removeTop (call rightMost1 (call context:getNonEmpty)))))
  (targetValue (string [[0],[2],[2],[2],[0],[]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[0],[2],[0],[0]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:17:48.039)
  (NBestInd 0)
  (utterance "apagar esquerda")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[2],[0],[0]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[3,3],[3,2],[3,3],[3,2]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:18:00.174)
  (NBestInd 1)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[3],[3,2],[3],[3,2]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[1,1],[1,3],[1,1],[1,3],[1,1]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:18:21.024)
  (NBestInd 40)
  (utterance "adicionar vermelho em laranja")
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 3 COLOR)) (number 2 COLOR))))
  (targetValue (string [[1,1],[1,3,2],[1,1],[1,3,2],[1,1]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[2,2],[2,0],[2,2],[2,0]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:18:40.853)
  (NBestInd 0)
  (utterance "adicionar azul em azul")
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 0 COLOR)) (number 0 COLOR))))
  (targetValue (string [[2,2],[2,0,0],[2,2],[2,0,0]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[2,3],[3,2],[2,3],[3,2],[2,3],[3,2]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:19:09.349)
  (NBestInd 4)
  (utterance "adicionar laranja em vermelho")
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 2 COLOR)) (number 3 COLOR))))
  (targetValue (string [[2,3],[3,2,3],[2,3],[3,2,3],[2,3],[3,2,3]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[0,2],[2,0],[0,2],[2,0]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:19:18.765)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[0,2],[2],[0,2],[2]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[0,2],[2]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:19:36.418)
  (NBestInd 0)
  (utterance "apagar direita")
  (targetFormula (call wallToString (call context:removeTop (call rightMost1 (call context:getNonEmpty)))))
  (targetValue (string [[0,2],[]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[3,1],[3,3],[1]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:19:57.951)
  (NBestInd 22)
  (utterance "acrescentar castanho")
  (targetFormula (call wallToString (call context:stackOnTop (call complement (call context:getTopColor (number 2 COLOR))) (number 1 COLOR))))
  (targetValue (string [[3,1,1],[3,3,1],[1,1]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[1],[1],[1,3],[1]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:20:39.251)
  (NBestInd 8)
  (utterance "apagar tudo menos esqueda")
  (targetFormula (call wallToString (call context:removeTop (call complement (call leftMost1 (call context:getNonEmpty))))))
  (targetValue (string [[1],[],[1],[]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[3,1],[3],[0,3]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:21:36.190)
  (NBestInd 0)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[3],[3],[0,3]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[0],[2],[0],[3,0]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:21:59.033)
  (NBestInd 0)
  (utterance "adicionar azul esquerda")
  (targetFormula (call wallToString (call context:stackOnTop (call leftMost1 (call context:getNonEmpty)) (number 0 COLOR))))
  (targetValue (string [[0,0],[2],[0],[3,0]]))
)
(example
  (id session:QSC89PgeOVq)
  (context (date 2018 6 4) (graph NaiveKnowledgeGraph ((string [[0,0],[0,1,1],[0,1,1],[0,1],[0,0]]) (name b) (name c))))
  (timeStamp 2018-07-04T11:23:18.622)
  (NBestInd 1)
  (utterance "adicionar castanho meio")
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 1 COLOR)) (number 1 COLOR))))
  (targetValue (string [[0,0],[0,1,1,1],[0,1,1,1],[0,1,1],[0,0]]))
)
