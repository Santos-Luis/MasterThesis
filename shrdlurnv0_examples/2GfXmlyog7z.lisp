(example
  (id session:2GfXmlyog7z)
  (context (date 2018 5 15) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-15T22:53:28.263)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:2GfXmlyog7z)
  (context (date 2018 5 15) (graph NaiveKnowledgeGraph ((string [[1],[3],[1],[0],[0],[1],[0]]) (name b) (name c))))
  (timeStamp 2018-06-15T22:54:51.362)
  (NBestInd 24)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getTopColor (number 3 COLOR))))))
  (targetValue (string [[1],[],[1],[0],[0],[1],[0]]))
)
(example
  (id session:2GfXmlyog7z)
  (context (date 2018 5 15) (graph NaiveKnowledgeGraph ((string [[3],[0],[1],[2],[2],[0]]) (name b) (name c))))
  (timeStamp 2018-06-15T22:56:55.494)
  (NBestInd 1)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[3],[],[1],[2],[2],[]]))
)
