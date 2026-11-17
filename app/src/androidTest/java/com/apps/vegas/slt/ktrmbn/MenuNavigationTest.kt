package com.apps.vegas.slt.ktrmbn

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MenuNavigationTest {
    private val worlds = listOf("Volcan Games", "Adventure Slots", "Paradise Slots", "Treasure Slots")

    private fun views(view: View): List<View> = listOf(view) +
        if (view is ViewGroup) (0 until view.childCount).flatMap { views(view.getChildAt(it)) } else emptyList()

    private fun text(root: View, value: String) = views(root).filterIsInstance<TextView>().find { it.text.toString() == value }
    private fun description(root: View, value: String) = views(root).find { it.contentDescription?.toString() == value }

    @Test
    fun mainMenuHasTextChoicesWithoutArtwork() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.findViewById<View>(R.id.content)
                assertNotNull(text(root, "Vegas Luck"))
                worlds.forEach { assertTrue(text(root, it)?.isShown == true) }
                assertFalse("Menu options must not obscure the video with artwork", views(root).any { it is ImageView })
            }
        }
    }

    @Test
    fun everyWorldOpensPreviewBeforeSlotsAndBackReturnsThroughBothScreens() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.findViewById<View>(R.id.content)
                worlds.forEach { world ->
                    assertTrue(text(root, world)!!.performClick())
                    assertNotNull(description(root, "Play $world"))
                    assertNull(text(root, "RULES"))
                    assertTrue(description(root, "Back to worlds")!!.performClick())
                    worlds.forEach { assertNotNull(text(root, it)) }
                    assertTrue(text(root, world)!!.performClick())
                    assertTrue(description(root, "Play $world")!!.performClick())
                    assertNotNull(text(root, "RULES"))
                    assertNotNull(description(root, "Auto play off"))
                    assertTrue(description(root, "Back to level preview")!!.performClick())
                    assertNotNull(description(root, "Play $world"))
                    activity.onBackPressedDispatcher.onBackPressed()
                    worlds.forEach { assertNotNull(text(root, it)) }
                }
            }
        }
    }
}
