(example
  (id session:w5HhDDelSfp)
  (context (date 2018 5 28) (graph NaiveKnowledgeGraph ((string [[0],[3],[0],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-28T13:31:39.664)
  (NBestInd 4)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[],[3],[],[3],[3]]))
)
(example
  (id session:w5HhDDelSfp)
  (context (date 2018 5 28) (graph NaiveKnowledgeGraph ((string [[2],[1],[2],[1],[2],[2],[1]]) (name b) (name c))))
  (timeStamp 2018-06-28T13:31:52.976)
  (NBestInd 21)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call complement (call context:getTopColor (number 2 COLOR))))))
  (targetValue (string [[2],[],[2],[],[2],[2],[]]))
)
(example
  (id session:w5HhDDelSfp)
  (context (date 2018 5 28) (graph NaiveKnowledgeGraph ((string [[3],[2],[2],[3],[1]]) (name b) (name c))))
  (timeStamp 2018-06-28T13:31:59.937)
  (NBestInd 1)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[3],[2],[2],[3],[]]))
)