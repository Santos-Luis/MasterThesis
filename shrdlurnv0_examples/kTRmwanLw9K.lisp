(example
  (id session:kTRmwanLw9K)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-22T15:14:11.596)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:kTRmwanLw9K)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[0],[1],[2],[3]]) (name b) (name c))))
  (timeStamp 2018-06-22T15:14:14.979)
  (NBestInd 0)
  (utterance apagar)
  (targetFormula (call wallToString (call context:removeTop (call rightMost1 (call context:getTopColor (number 0 COLOR))))))
  (targetValue (string [[],[1],[2],[3]]))
)
(example
  (id session:kTRmwanLw9K)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-22T15:17:03.338)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)