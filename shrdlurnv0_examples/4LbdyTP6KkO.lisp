(example
  (id session:4LbdyTP6KkO)
  (context (date 2018 5 21) (graph NaiveKnowledgeGraph ((string [[1],[0],[3],[3],[2],[0]]) (name b) (name c))))
  (timeStamp 2018-06-21T16:16:17.002)
  (NBestInd 4)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[1],[],[3],[3],[2],[]]))
)
(example
  (id session:4LbdyTP6KkO)
  (context (date 2018 5 21) (graph NaiveKnowledgeGraph ((string [[3],[0],[1],[2],[2],[0]]) (name b) (name c))))
  (timeStamp 2018-06-21T16:16:22.346)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[3],[],[1],[2],[2],[]]))
)
