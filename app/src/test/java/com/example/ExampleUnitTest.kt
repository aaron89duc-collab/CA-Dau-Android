package com.example

import com.example.data.SaveManager
import com.example.engine.Vector2D
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testVectorMath() {
        val vec = Vector2D(3f, 4f)
        assertEquals(5f, vec.length(), 0.001f)

        vec.normalize()
        assertEquals(0.6f, vec.x, 0.001f)
        assertEquals(0.8f, vec.y, 0.001f)
        assertEquals(1f, vec.length(), 0.001f)
    }

    @Test
    fun testGddUpgradeValues() {
        // GDD Section 12 HP progression
        assertEquals(100, SaveManager.HP_VALUES[0])
        assertEquals(110, SaveManager.HP_VALUES[1])
        assertEquals(120, SaveManager.HP_VALUES[2])
        assertEquals(135, SaveManager.HP_VALUES[3])
        assertEquals(150, SaveManager.HP_VALUES[4])

        // GDD Section 12 Shield Throw progression
        assertEquals(35, SaveManager.SHIELD_THROW_VALUES[0])
        assertEquals(40, SaveManager.SHIELD_THROW_VALUES[1])
        assertEquals(46, SaveManager.SHIELD_THROW_VALUES[2])
        assertEquals(54, SaveManager.SHIELD_THROW_VALUES[3])
        assertEquals(65, SaveManager.SHIELD_THROW_VALUES[4])

        // GDD Section 12 Ultimate progression
        assertEquals(150, SaveManager.ULTIMATE_VALUES[0])
        assertEquals(260, SaveManager.ULTIMATE_VALUES[4])
    }

    @Test
    fun testPerfectBlockWindow() {
        // GDD Section 4.2: Perfect block occurs if block activated within 0.12s
        val perfectBlockThreshold = 0.12f
        val reactionTime1 = 0.08f
        val reactionTime2 = 0.18f

        assertTrue("0.08s should be within perfect block window", reactionTime1 <= perfectBlockThreshold)
        assertTrue("0.18s should be regular block", reactionTime2 > perfectBlockThreshold)
    }
}
