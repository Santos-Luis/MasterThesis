(example
  (id session:YULoLKHdc8j)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-22T15:44:32.650)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:YULoLKHdc8j)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[2],[3],[1],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-06-22T15:44:40.257)
  (NBestInd 24)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getTopColor (number 3 COLOR))))))
  (targetValue (string [[2],[],[1],[2],[2]]))
)
(example
  (id session:YULoLKHdc8j)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[2],[0],[0],[1],[1]]) (name b) (name c))))
  (timeStamp 2018-06-22T15:44:47.983)
  (NBestInd 18)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[2],[0],[0],[],[]]))
)
(example
  (id session:YULoLKHdc8j)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[1],[0],[3],[3],[2],[0]]) (name b) (name c))))
  (timeStamp 2018-06-22T15:44:58.502)
  (NBestInd 7)
  (utterance azul)
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[1],[],[3],[3],[2],[]]))
)
(example
  (id session:YULoLKHdc8j)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[3],[0],[1],[0],[3],[1]]) (name b) (name c))))
  (timeStamp 2018-06-22T15:45:05.099)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[3],[],[1],[],[3],[1]]))
)
(example
  (id session:YULoLKHdc8j)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[2],[3],[2],[2],[2],[1],[3]]) (name b) (name c))))
  (timeStamp 2018-06-22T15:45:12.184)
  (NBestInd 0)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[2],[3],[2],[2],[2],[],[3]]))
)
(example
  (id session:YULoLKHdc8j)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[2],[0],[1],[1],[2],[3],[0]]) (name b) (name c))))
  (timeStamp 2018-06-22T15:45:23.419)
  (NBestInd 0)
  (utterance laranja)
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 3 COLOR)) (number 3 COLOR))))
  (targetValue (string [[2],[0],[1],[1],[2],[3,3],[0]]))
)
(example
  (id session:YULoLKHdc8j)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[0],[2],[0],[2]]) (name b) (name c))))
  (timeStamp 2018-06-22T15:45:30.996)
  (NBestInd 21)
  (utterance "apagar direita")
  (targetFormula (call wallToString (call context:removeTop (call rightMost1 (call context:getTopColor (number 2 COLOR))))))
  (targetValue (string [[0],[2],[0],[]]))
)
(example
  (id session:YULoLKHdc8j)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[1],[1],[2],[1],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-06-22T15:45:43.298)
  (NBestInd 11)
  (utterance "adicionar vermelho direita")
  (targetFormula (call wallToString (call context:stackOnTop (call leftMost1 (call context:getNonEmpty)) (number 2 COLOR))))
  (targetValue (string [[1,2],[1],[2],[1],[2],[2]]))
)
