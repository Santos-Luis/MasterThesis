(example
  (id session:rlyakXGE6AW)
  (context (date 2018 4 20) (graph NaiveKnowledgeGraph ((string [[2],[3],[1],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-05-20T13:40:25.678)
  (NBestInd 1)
  (utterance apagar)
  (targetFormula (call wallToString (call context:removeTop (call complement (call context:getNonEmpty)))))
  (targetValue (string [[2],[3],[1],[2],[2]]))
)
(example
  (id session:rlyakXGE6AW)
  (context (date 2018 4 20) (graph NaiveKnowledgeGraph ((string [[2],[3],[1],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-05-20T13:40:53.494)
  (NBestInd 38)
  (utterance dsfdsfsdf)
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 3 COLOR)) (number 3 COLOR))))
  (targetValue (string [[2],[3,3],[1],[2],[2]]))
)
(example
  (id session:rlyakXGE6AW)
  (context (date 2018 4 21) (graph NaiveKnowledgeGraph ((string [[0],[1],[2],[3]]) (name b) (name c))))
  (timeStamp 2018-05-21T09:50:53.131)
  (NBestInd 0)
  (utterance laranja)
)
(example
  (id session:rlyakXGE6AW)
  (context (date 2018 4 21) (graph NaiveKnowledgeGraph ((string [[2],[3],[1],[2],[2]]) (name b) (name c))))
  (timeStamp 2018-05-21T09:52:11.373)
  (NBestInd 4)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 3 COLOR)))))
  (targetValue (string [[2],[],[1],[2],[2]]))
)