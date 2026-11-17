package com.apps.vegas.slt.ktrmbn
import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import org.json.JSONArray
import kotlin.random.Random

data class ClusterSession(val coins:Long=5400,val stake:Int=50,val claimedAt:Long=0,val board:IntArray=IntArray(16){it%9},val prize:Long=0)
class Vault(context:Context){
 private val prefs=context.getSharedPreferences("cluster_vault",0)
 fun read():ClusterSession=runCatching{val j=JSONObject(prefs.getString("record","")!!);val a=j.getJSONArray("board");ClusterSession(j.getLong("coins"),j.getInt("stake"),j.getLong("claimedAt"),IntArray(16){a.getInt(it)},j.optLong("prize"))}.getOrDefault(ClusterSession())
 fun write(s:ClusterSession):Boolean=prefs.edit().putString("record",JSONObject().put("coins",s.coins).put("stake",s.stake).put("claimedAt",s.claimedAt).put("prize",s.prize).put("board",JSONArray(s.board.toList())).toString()).commit()
}
interface ClusterDisplay {fun render();fun animateGrid(done:()->Unit)}
class ClusterPresenter(private val vault:Vault,private val display:ClusterDisplay, private val winDelay:()->Long={1600L}){
 var session=vault.read();private set
 var busy=false;private set
 var automatic=false;private set
 private val clock=Handler(Looper.getMainLooper())
 private var active=true
 private fun commit(next:ClusterSession){if(vault.write(next))session=next}
 fun wager(delta:Int){if(!busy){commit(session.copy(stake=(session.stake+delta).coerceIn(50,500)));display.render()}}
 fun claim(){if(System.currentTimeMillis()-session.claimedAt>=86400000L){commit(session.copy(coins=session.coins+1000,claimedAt=System.currentTimeMillis()));display.render()}}
 fun spin(){if(!active||busy)return;if(session.coins<session.stake){automatic=false;display.render();return}
  val board=IntArray(16){Random.nextInt(9)};val prize=ClusterRules.reward(board,session.stake);val next=session.copy(coins=session.coins-session.stake+prize,board=board,prize=prize)
  if(!vault.write(next))return;session=next;busy=true;display.render();display.animateGrid{if(active){busy=false;display.render();if(automatic)clock.postDelayed({if(automatic&&active)spin()},winDelay())}}
 }
 fun toggle(){automatic=!automatic;display.render();if(automatic&&!busy)spin()}
 fun stop(){automatic=false;busy=false;clock.removeCallbacksAndMessages(null)}
 fun pause(){active=false;stop()}
 fun resume(){active=true}
}
