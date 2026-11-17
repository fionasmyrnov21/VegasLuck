package com.apps.vegas.slt.ktrmbn
import org.junit.Assert.*
import org.junit.Test
class ClusterRulesTest {
 @Test fun noWin(){assertEquals(0L,ClusterRules.reward(IntArray(16){it},50))}
 @Test fun tiers(){for((size,mult)in listOf(4 to 8,5 to 10,6 to 20,7 to 35,8 to 60,16 to 60)){val b=IntArray(16){it+1};repeat(size){b[it]=0};assertEquals(mult*50L,ClusterRules.reward(b,50))}}
 @Test fun diagonalsDoNotConnect(){val b=IntArray(16){it+1};listOf(0,5,10,15).forEach{b[it]=0};assertEquals(0L,ClusterRules.reward(b,50))}
 @Test fun separateGroups(){val b=IntArray(16){it+1};listOf(0,1,4,5,10,11,14,15).forEach{b[it]=0};assertEquals(800L,ClusterRules.reward(b,50))}
}
