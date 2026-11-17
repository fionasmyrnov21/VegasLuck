package com.apps.vegas.slt.ktrmbn
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import androidx.fragment.app.Fragment
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.apps.vegas.slt.ktrmbn.databinding.FragmentClusterBinding

class ClusterFragment:Fragment(),ClusterDisplay {
 private var viewBinding:FragmentClusterBinding?=null
 private val ui get()=viewBinding!!
 private lateinit var art:MediaLibrary
 private lateinit var presenter:ClusterPresenter
 private var movie:MenuMovie?=null
 private var level=-1
 private var playing=false
 private val animator=Handler(Looper.getMainLooper())
 private val tiles=mutableListOf<ImageView>()
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 override fun onCreateView(i:LayoutInflater,p:ViewGroup?,s:Bundle?):View{
  viewBinding=FragmentClusterBinding.inflate(i,p,false)
  val content=ui.content
  val left=content.paddingLeft;val top=content.paddingTop;val right=content.paddingRight;val bottom=content.paddingBottom
  // Keep controls inside the safe area while scenery and video fill the window.
  ViewCompat.setOnApplyWindowInsetsListener(content){view,insets->
   val safeArea=insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
   view.setPadding(left+safeArea.left,top+safeArea.top,right+safeArea.right,bottom+safeArea.bottom)
   insets
  }
  return ui.root
 }
 override fun onViewCreated(v:View,s:Bundle?){art=MediaLibrary(requireContext());presenter=ClusterPresenter(Vault(requireContext()),this){art.timing(level.coerceAtLeast(0),"winMs")};movie=MenuMovie(requireContext(),ui.movie,unavailable={if(viewBinding!=null)ui.menuLoading.visibility=View.GONE}){visible->if(viewBinding!=null&&level<0)ui.menuLoading.visibility=if(visible)View.GONE else View.VISIBLE};requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object:OnBackPressedCallback(true){override fun handleOnBackPressed(){if(level>=0){presenter.stop();animator.removeCallbacksAndMessages(null);if(playing)playing=false else level=-1;render()}}});render()}
 private fun label(s:String,size:Float=18f):TextView=TextView(requireContext()).apply{text=s;textSize=size;setTextColor(art.text);typeface=Typeface.DEFAULT_BOLD;gravity=Gravity.CENTER;setPadding(4,dp(6),4,dp(6));setShadowLayer(dp(2).toFloat(),0f,dp(1).toFloat(),0xAA000000.toInt())}
 private fun plate(color:Int=art.panel):GradientDrawable=GradientDrawable().apply{setColor(color);cornerRadius=dp(18).toFloat();setStroke(dp(1),art.accent)}
 private fun action(s:String,callback:()->Unit)=Button(requireContext()).apply{text=s;setTextColor(art.text);backgroundTintList=android.content.res.ColorStateList.valueOf(art.panel);setOnClickListener{callback()}}
 private fun imageButton(path:String,callback:()->Unit)=ImageButton(requireContext()).apply{setImageBitmap(art.image(path));scaleType=ImageView.ScaleType.FIT_CENTER;setBackgroundColor(Color.TRANSPARENT);contentDescription=path.substringAfterLast('/');setOnClickListener{callback()}}
 private fun renderMenu(root:LinearLayout){
  val options=LinearLayout(requireContext()).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER}
  val area=FrameLayout(requireContext())
  root.addView(area,LinearLayout.LayoutParams(-1,0,1f))
  area.addView(options,FrameLayout.LayoutParams(-1,-2,Gravity.CENTER))
  art.themes.forEachIndexed{index,theme->
   val option=action(theme.title){presenter.stop();level=index;playing=false;render()}.apply{
    isAllCaps=false;textSize=20f;typeface=Typeface.DEFAULT_BOLD
    backgroundTintList=null;background=plate(0x33171526)
   }
   options.addView(option,LinearLayout.LayoutParams(-1,dp(60)).apply{setMargins(dp(12),dp(8),dp(12),dp(8))})
  }
 }
 private fun renderPreview(root:LinearLayout){
  val toolbar=FrameLayout(requireContext())
  root.addView(toolbar,LinearLayout.LayoutParams(-1,dp(76)))
  val heading=LinearLayout(requireContext()).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER}
  heading.addView(label(getString(R.string.app_name),24f))
  heading.addView(label("${presenter.session.coins} COINS",13f))
  toolbar.addView(heading,FrameLayout.LayoutParams(-1,-1).apply{leftMargin=dp(60);rightMargin=dp(60)})
  val back=imageButton("common/buttons/back.png"){level=-1;render()}.apply{
   contentDescription="Back to worlds"
   setPadding(dp(6),dp(6),dp(6),dp(6))
  }
  toolbar.addView(back,FrameLayout.LayoutParams(dp(52),dp(52),Gravity.START or Gravity.CENTER_VERTICAL))
  val area=FrameLayout(requireContext())
  root.addView(area,LinearLayout.LayoutParams(-1,0,1f))
  area.addView(label(art.themes[level].title,28f),FrameLayout.LayoutParams(-1,-2,Gravity.TOP or Gravity.CENTER_HORIZONTAL).apply{topMargin=dp(16)})
  val play=imageButton("${art.base(level)}/graphics/play.png"){playing=true;render()}.apply{
   contentDescription="Play ${art.themes[level].title}"
  }
  area.addView(play,FrameLayout.LayoutParams(dp(220),dp(76),Gravity.CENTER))
 }
 override fun render(){if(viewBinding==null)return;val root=ui.content;root.removeAllViews();tiles.clear();val state=presenter.session
  ui.menuLoading.visibility=if(level<0)View.VISIBLE else View.GONE
  ui.shade.setBackgroundColor(if(level<0)0x10000000 else 0x18000000)
  if(level>=0&&!playing){movie?.pause();ui.movie.visibility=View.GONE;ui.scenery.setImageBitmap(art.background(level));renderPreview(root);return}
  root.addView(label(getString(R.string.app_name),if(level<0)32f else 26f))
  root.addView(label("${state.coins} COINS",15f))
  if(level<0){ui.movie.visibility=View.VISIBLE;ui.scenery.setImageBitmap(art.image("menu/graphics/fallback.jpg"));if(lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED))movie?.resume()
   renderMenu(root)
   val ready=System.currentTimeMillis()-state.claimedAt>=86400000L
   root.addView(action(if(ready)"COLLECT DAILY GIFT +1000" else "DAILY GIFT · AVAILABLE IN ${((86400000L-System.currentTimeMillis()+state.claimedAt)/3600000)+1}h"){presenter.claim()}.apply{isEnabled=ready;backgroundTintList=android.content.res.ColorStateList.valueOf(0x33171526)})
  }else{movie?.pause();ui.movie.visibility=View.GONE;ui.scenery.setImageBitmap(art.background(level))
   val nav=LinearLayout(requireContext());nav.addView(imageButton("common/buttons/back.png"){presenter.stop();animator.removeCallbacksAndMessages(null);playing=false;render()}.apply{contentDescription="Back to level preview"},LinearLayout.LayoutParams(0,dp(52),1f));nav.addView(action("RULES"){AlertDialog.Builder(requireContext()).setTitle("Connected wins").setMessage("Match identical symbols connected through shared edges. Diagonals do not connect.\n\n4 = 8×\n5 = 10×\n6 = 20×\n7 = 35×\n8–16 = 60× bet\n\nEach separate group pays once. All wins add up. No cascades. Virtual coins only.").setPositiveButton("GOT IT",null).show()},LinearLayout.LayoutParams(0,dp(52),1f));root.addView(nav)
   root.addView(label(art.themes[level].title,25f))
   val board=LinearLayout(requireContext()).apply{orientation=LinearLayout.VERTICAL;background=plate(0x553C344A);setPadding(dp(6),dp(6),dp(6),dp(6))};root.addView(board,LinearLayout.LayoutParams(-1,0,1f))
   val wins=if(!presenter.busy)ClusterRules.components(state.board).filter{it.size>=4}.flatten().toSet() else emptySet()
   repeat(4){row->val strip=LinearLayout(requireContext());board.addView(strip,LinearLayout.LayoutParams(-1,0,1f));repeat(4){col->val index=row*4+col;val tile=ImageView(requireContext()).apply{setImageBitmap(art.symbol(level,state.board[index]));scaleType=ImageView.ScaleType.FIT_CENTER;setPadding(dp(3),dp(4),dp(3),dp(4));if(index in wins)setBackgroundColor(0x66FFD16A)};tiles.add(tile);strip.addView(tile,LinearLayout.LayoutParams(0,-1,1f))}}
   root.addView(label(if(presenter.busy)"SHUFFLING..." else if(state.prize>0)"GROUP WIN +${state.prize}" else "CONNECT 4 OR MORE",20f))
   val stakes=LinearLayout(requireContext());stakes.gravity=Gravity.CENTER
   stakes.addView(imageButton("common/buttons/minus.png"){presenter.wager(-50)}.apply{isEnabled=!presenter.busy},LinearLayout.LayoutParams(dp(55),dp(48)));stakes.addView(label("BET ${state.stake}",22f),LinearLayout.LayoutParams(0,dp(48),1f));stakes.addView(imageButton("common/buttons/plus.png"){presenter.wager(50)}.apply{isEnabled=!presenter.busy},LinearLayout.LayoutParams(dp(55),dp(48)));root.addView(stakes)
   val controls=LinearLayout(requireContext());controls.addView(imageButton("common/buttons/auto.png"){presenter.toggle()}.apply{contentDescription=if(presenter.automatic)"Auto play on" else "Auto play off";isSelected=presenter.automatic;if(presenter.automatic)background=plate()},LinearLayout.LayoutParams(0,dp(65),1f));controls.addView(imageButton("${art.base(level)}/graphics/play.png"){presenter.spin()}.apply{isEnabled=!presenter.busy&&!presenter.automatic&&state.coins>=state.stake;alpha=if(isEnabled)1f else .45f},LinearLayout.LayoutParams(0,dp(65),1f));root.addView(controls)
   if(state.coins<state.stake)root.addView(label("Lower bet or claim daily gift in Worlds",12f))
  }
 }
 override fun animateGrid(done:()->Unit){val start=android.os.SystemClock.uptimeMillis();val chosen=level;val run=object:Runnable{override fun run(){if(viewBinding==null||level!=chosen)return;val elapsed=android.os.SystemClock.uptimeMillis()-start;val duration=art.timing(chosen,"spinMs");if(elapsed<duration){tiles.forEachIndexed{i,t->t.setImageBitmap(art.symbol(chosen,((elapsed/65+i)%9).toInt()))};animator.postDelayed(this,55)}else done()}};animator.post(run)}
 override fun onResume(){super.onResume();if(::presenter.isInitialized){presenter.resume();render();if(level<0)movie?.resume()}}
 override fun onPause(){presenter.pause();movie?.pause();animator.removeCallbacksAndMessages(null);super.onPause()}
 override fun onDestroyView(){animator.removeCallbacksAndMessages(null);movie?.pause();movie=null;viewBinding=null;super.onDestroyView()}
}
