package com.apps.vegas.slt.ktrmbn

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.apps.vegas.slt.ktrmbn.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  WindowCompat.setDecorFitsSystemWindows(window, false)
  window.statusBarColor = Color.TRANSPARENT
  window.navigationBarColor = Color.TRANSPARENT
  WindowInsetsControllerCompat(window, window.decorView).apply {
   isAppearanceLightStatusBars = false
   isAppearanceLightNavigationBars = false
  }
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
   window.attributes = window.attributes.apply {
    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
   }
  }
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
   window.isStatusBarContrastEnforced = false
   window.isNavigationBarContrastEnforced = false
  }
  val binding = ActivityMainBinding.inflate(layoutInflater)
  setContentView(binding.root)
  binding.root.setBackgroundColor(Color.rgb(23, 21, 38))
  if (savedInstanceState == null) {
   supportFragmentManager.beginTransaction().replace(binding.host.id, ClusterFragment()).commit()
  }
 }
}
