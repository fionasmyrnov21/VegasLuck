package com.apps.vegas.slt.ktrmbn

object ClusterRules {
 fun components(board:IntArray):List<List<Int>>{
  require(board.size==16);val seen=BooleanArray(16);val groups=mutableListOf<List<Int>>()
  for(start in board.indices){if(seen[start])continue;val queue=java.util.ArrayDeque<Int>();val found=mutableListOf<Int>();queue.add(start);seen[start]=true
   while(queue.isNotEmpty()){val i=queue.removeFirst();found.add(i);val r=i/4;val c=i%4
    val neighbors=mutableListOf<Int>();if(r>0)neighbors.add(i-4);if(r<3)neighbors.add(i+4);if(c>0)neighbors.add(i-1);if(c<3)neighbors.add(i+1)
    for(next in neighbors)if(!seen[next]&&board[next]==board[start]){seen[next]=true;queue.add(next)}
   };groups.add(found)
  };return groups
 }
 fun multiplier(size:Int):Int=when(size){4->8;5->10;6->20;7->35;in 8..16->60;else->0}
 fun reward(board:IntArray,stake:Int):Long=components(board).sumOf{multiplier(it.size)*stake.toLong()}
}
