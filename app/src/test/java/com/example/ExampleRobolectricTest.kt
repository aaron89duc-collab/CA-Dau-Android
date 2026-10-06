package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SaveManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Shield Force", appName)
  }

  @Test
  fun `verify SaveManager coins and stats`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val saveManager = SaveManager.getInstance(context)
    assertTrue(saveManager.getCoins() >= 0)
    val stats = saveManager.getCurrentStats()
    assertEquals(100, stats.maxHp)
  }
}
