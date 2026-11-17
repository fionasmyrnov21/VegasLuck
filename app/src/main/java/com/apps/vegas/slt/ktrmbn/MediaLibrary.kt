package com.apps.vegas.slt.ktrmbn
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.view.TextureView
import android.view.Surface
import org.json.JSONArray
import org.json.JSONObject

class MediaLibrary(private val context: Context) {
 data class Theme(val folder:String,val title:String)
 val themes:List<Theme> = JSONArray(read("levels.json")).let { a -> (0 until a.length()).map { val j=a.getJSONObject(it); Theme(j.getString("folder"),j.getString("title")) } }
 private val cache=mutableMapOf<String,Bitmap>()
 private val colors=JSONObject(read("theme.json"))
 val accent:Int get()=Color.parseColor(colors.getString("accent"))
 val panel:Int get()=Color.parseColor(colors.getString("panel"))
 val text:Int get()=Color.parseColor(colors.getString("text"))
 fun read(file:String)=context.assets.open(file).bufferedReader().use{it.readText()}
 fun image(file:String):Bitmap=cache.getOrPut(file){context.assets.open(file).use{BitmapFactory.decodeStream(it) ?: error("Invalid image: $file")}}
 fun base(index:Int)="levels/${themes[index].folder}"
 fun background(index:Int)=image("${base(index)}/graphics/background.jpg")
 fun symbol(index:Int,id:Int)=image("${base(index)}/symbols/${(id+1).toString().padStart(2,'0')}.png")
 fun timing(index:Int,key:String):Long=JSONObject(read("${base(index)}/animations/settings.json")).optLong(key,1500).coerceIn(50,10000)
}

/** Reveal the video only after its final crop and first decoded frame are ready. */
class MenuMovie(
 private val context:Context,
 private val surface:TextureView,
 private val unavailable:()->Unit={},
 private val videoVisible:(Boolean)->Unit={}
):TextureView.SurfaceTextureListener {
 private var player:MediaPlayer?=null
 private var target:Surface?=null
 private var active=false
 private var prepared=false
 private var rendering=false
 private var visible=false
 private var revealQueued=false
 private var videoWidth=0
 private var videoHeight=0
 private var viewportWidth=0
 private var viewportHeight=0
 private val reveal=Runnable {
  revealQueued=false
  if(active&&prepared&&rendering&&viewportWidth>0&&viewportHeight>0&&surface.width==viewportWidth&&surface.height==viewportHeight){
   visible=true;surface.alpha=1f;videoVisible(true)
  }
 }
 init {
  surface.alpha=0f
  surface.surfaceTextureListener=this
  surface.addOnLayoutChangeListener{_,left,top,right,bottom,oldLeft,oldTop,oldRight,oldBottom->
   if(right-left!=oldRight-oldLeft||bottom-top!=oldBottom-oldTop){
    hideVideo()
    if(active){if(player==null)start() else configureSurface()}
   }
  }
 }
 fun resume(){active=true;if(surface.isAvailable)start();videoVisible(visible)}
 fun pause(){active=false;releasePlayer()}
 private fun hideVideo(){
  surface.removeCallbacks(reveal);revealQueued=false;visible=false;surface.alpha=0f;videoVisible(false)
 }
 private fun releasePlayer(){
  hideVideo();prepared=false;rendering=false;videoWidth=0;videoHeight=0;viewportWidth=0;viewportHeight=0
  val old=player;player=null;old?.release();target?.release();target=null
 }
 private fun fail(){pause();unavailable()}
 private fun configureSurface():Boolean {
  val width=surface.width;val height=surface.height
  if(width<=0||height<=0||videoWidth<=0||videoHeight<=0||!surface.isAvailable)return false
  surface.surfaceTexture?.setDefaultBufferSize(videoWidth,videoHeight)
  val scale=maxOf(width.toFloat()/videoWidth,height.toFloat()/videoHeight)
  val matrix=android.graphics.Matrix()
  matrix.setScale(videoWidth*scale/width,videoHeight*scale/height,width/2f,height/2f)
  surface.setTransform(matrix)
  viewportWidth=width;viewportHeight=height
  return true
 }
 private fun start(){
  if(!active||player!=null||!surface.isAvailable||surface.width<=0||surface.height<=0)return
  val texture=surface.surfaceTexture?:return
  hideVideo()
  try {
   target=Surface(texture)
   val p=MediaPlayer();player=p
   context.assets.openFd("menu/video/menu_bg.mp4").use{p.setDataSource(it.fileDescriptor,it.startOffset,it.length)}
   p.setSurface(target);p.isLooping=true;p.setVolume(0f,0f)
   p.setOnVideoSizeChangedListener{_,width,height->
    if(player===p&&active&&width>0&&height>0&&(width!=videoWidth||height!=videoHeight)){hideVideo();videoWidth=width;videoHeight=height;configureSurface()}
   }
   p.setOnPreparedListener{
    if(player===p&&active){prepared=true;if(p.videoWidth>0&&p.videoHeight>0){videoWidth=p.videoWidth;videoHeight=p.videoHeight};configureSurface();p.start()}
   }
   p.setOnInfoListener{_,what,_->if(player===p&&active&&what==MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START)rendering=true;false}
   p.setOnErrorListener{_,_,_->if(player===p)fail();true}
   p.prepareAsync()
  }catch(_:Exception){fail()}
 }
 override fun onSurfaceTextureAvailable(t:android.graphics.SurfaceTexture,w:Int,h:Int){if(active)start()}
 override fun onSurfaceTextureSizeChanged(t:android.graphics.SurfaceTexture,w:Int,h:Int){hideVideo();configureSurface()}
 override fun onSurfaceTextureUpdated(t:android.graphics.SurfaceTexture){
  if(active&&prepared&&rendering&&!visible&&!revealQueued&&viewportWidth>0&&viewportHeight>0){revealQueued=true;surface.postOnAnimation(reveal)}
 }
 override fun onSurfaceTextureDestroyed(t:android.graphics.SurfaceTexture):Boolean{releasePlayer();return true}
}
