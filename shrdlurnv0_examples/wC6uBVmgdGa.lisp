(example
  (learningMode Implicit)
  (id session:wC6uBVmgdGa)
  (timeStamp 2018-09-14T10:20:39.597)
  (NBestInd 2)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getNonEmpty)))))
  (targetValue (string [[],[3],[2],[3],[3],[3],[3]]))
)
(example
  (learningMode Implicit)
  (id session:wC6uBVmgdGa)
  (timeStamp 2018-09-14T10:20:44.453)
  (NBestInd 24)
  (utterance "apagar laranja")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getTopColor (number 3 COLOR))))))
  (targetValue (string [[2],[],[1],[2],[2]]))
)
(example
  (learningMode Implicit)
  (id session:wC6uBVmgdGa)
  (timeStamp 2018-09-14T10:20:51.223)
  (NBestInd 18)
  (utterance "apagar castanho")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 1 COLOR)))))
  (targetValue (string [[2],[0],[0],[],[]]))
)
(example
  (learningMode Active)
  (id session:wC6uBVmgdGa)
  (timeStamp 2018-09-14T10:20:53.339)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[3],[2],[1],[1],[1],[1],[]]))
)
(example
  (learningMode Implicit)
  (id session:wC6uBVmgdGa)
  (timeStamp 2018-09-14T10:21:01.962)
  (NBestInd 17)
  (utterance "adicionar laranja azul")
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 0 COLOR)) (number 3 COLOR))))
  (targetValue (string [[0,3],[0,3],[3],[0,3],[3],[3]]))
)
(example
  (learningMode Implicit)
  (id session:wC6uBVmgdGa)
  (timeStamp 2018-09-14T10:21:08.793)
  (NBestInd 0)
  (utterance "adicionar castanho castanho")
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 1 COLOR)) (number 1 COLOR))))
  (targetValue (string [[1,1],[1,1],[1,1],[1,1]]))
)
(example
  (learningMode Implicit)
  (id session:wC6uBVmgdGa)
  (timeStamp 2018-09-14T10:21:20.018)
  (NBestInd 7)
  (utterance "adicionar vermelho laranja")
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 3 COLOR)) (number 2 COLOR))))
  (targetValue (string [[3,2],[2],[2],[3,2],[2]]))
)
(example
  (learningMode Implicit)
  (id session:wC6uBVmgdGa)
  (timeStamp 2018-09-14T10:21:43.255)
  (NBestInd 29)
  (utterance "adicionar laranja direita")
  (targetFormula (call wallToString (call context:stackOnTop (call rightMost1 (call context:getTopColor (number 2 COLOR))) (number 3 COLOR))))
  (targetValue (string [[2],[2],[3],[2],[2],[2,3]]))
)
(example
  (learningMode Implicit)
  (id session:wC6uBVmgdGa)
  (timeStamp 2018-09-14T10:21:49.936)
  (NBestInd 6)
  (utterance "apagar esquerda")
  (targetFormula (call wallToString (call context:removeTop (call leftMost1 (call context:getTopColor (number 0 COLOR))))))
  (targetValue (string [[],[0],[3],[0],[3]]))
)
(example
  (learningMode Implicit)
  (id session:wC6uBVmgdGa)
  (timeStamp 2018-09-14T10:22:09.172)
  (NBestInd 2)
  (utterance "adicionar laranja vermelho")
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 2 COLOR)) (number 3 COLOR))))
  (targetValue (string [[2,2,3],[2,3],[2,2,3],[2,3],[2,2,3]]))
)
(example
  (learningMode Implicit)
  (id session:wC6uBVmgdGa)
  (timeStamp 2018-09-14T10:22:15.801)
  (NBestInd 0)
  (utterance "apagar azul")
  (targetFormula (call wallToString (call context:removeTop (call context:getTopColor (number 0 COLOR)))))
  (targetValue (string [[3,3],[3],[3,3],[3]]))
)
(example
  (learningMode Implicit)
  (id session:wC6uBVmgdGa)
  (timeStamp 2018-09-14T10:22:21.110)
  (NBestInd 3)
  (utterance "adicionar laranja")
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 3 COLOR)) (number 3 COLOR))))
  (targetValue (string [[3,3,3],[3,3],[3,3,3],[3,3]]))
)
(example
  (learningMode Implicit)
  (id session:wC6uBVmgdGa)
  (timeStamp 2018-09-14T10:22:32.797)
  (NBestInd 4)
  (utterance "adicionar azul")
  (targetFormula (call wallToString (call context:stackOnTop (call context:getTopColor (number 0 COLOR)) (number 0 COLOR))))
  (targetValue (string [[2],[0,0],[1],[1],[3],[0,0],[3]]))
)
