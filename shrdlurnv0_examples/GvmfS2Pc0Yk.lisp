(example
  (id session:GvmfS2Pc0Yk)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[0],[3],[2],[3],[3],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-22T15:09:38.271)
  (NBestInd 2)
  (utterance "apagar laranka")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (id session:GvmfS2Pc0Yk)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[0],[2],[0],[1],[3],[3]]) (name b) (name c))))
  (timeStamp 2018-06-22T15:09:48.712)
  (NBestInd 1)
  (utterance "apagar laranka")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[],[2],[],[1],[3],[3]]))
)
(example
  (id session:GvmfS2Pc0Yk)
  (context (date 2018 5 22) (graph NaiveKnowledgeGraph ((string [[0],[1],[2],[3]]) (name b) (name c))))
  (timeStamp 2018-06-22T15:09:52.794)
  (NBestInd 1)
  (utterance apagar)
  (targetFormula (call wallToString (call context:removeTop (call rightMost1 (call context:getTopColor (number 0 COLOR))))))
  (targetValue (string [[],[1],[2],[3]]))
)