(example
  (id session:3sViiBsXHX2)
  (context (date 2018 5 14) (graph NaiveKnowledgeGraph ((string [[2],[3],[1],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-06-14T11:21:25.272)
  (NBestInd 7)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[2],[],[1],[2],[2]]))
)
(example
  (id session:3sViiBsXHX2)
  (context (date 2018 5 14) (graph NaiveKnowledgeGraph ((string [[2],[3],[0],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-14T11:28:50.794)
  (NBestInd 4)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[2],[3],[],[3],[3]]))
)
(example
  (id session:3sViiBsXHX2)
  (context (date 2018 5 14) (graph NaiveKnowledgeGraph ((string [[3],[2],[3],[0],[2],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-06-14T11:32:00.320)
  (NBestInd 4)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[3],[2],[3],[],[2],[2],[2]]))
)