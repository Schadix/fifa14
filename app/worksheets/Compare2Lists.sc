
val list1 = List("word1","word2","word2","word3","word1")
val list2 = List("word1","word4")

val unwanted = list2.toSet
list1.filterNot(unwanted)

list1.filter(e => !list2.contains(e))

list1.diff(list2) ::: list2.diff(list1)


val inGraph = List(1, 2, 3, 4, 5)
val fromFB = List(1,2,6,7)

// have to remove friendship to 3,4,5
inGraph.diff(fromFB)

// and add friendship to 6,7
fromFB.diff(inGraph)