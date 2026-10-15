package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PreferencesManager
import com.example.data.model.UnitCategory
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Hermes CX", appName)
  }

  @Test
  fun `test unit conversion pair recalled between sessions`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = PreferencesManager(context)

    // Session 1: User selects TEMPERATURE, Celsius to Fahrenheit
    prefs.setUnitPair(UnitCategory.TEMPERATURE, "c", "f")

    // Session 2: A new PreferencesManager instance reading stored preferences
    val newSessionPrefs = PreferencesManager(context)
    assertEquals(UnitCategory.TEMPERATURE, newSessionPrefs.getUnitCategory())
    assertEquals("c", newSessionPrefs.getUnitFrom(UnitCategory.TEMPERATURE))
    assertEquals("f", newSessionPrefs.getUnitTo(UnitCategory.TEMPERATURE))

    // User switches to MASS, Kilogram to Pound
    newSessionPrefs.setUnitPair(UnitCategory.MASS, "kg", "lb")

    // Session 3: Check that MASS selection is recalled
    val thirdSessionPrefs = PreferencesManager(context)
    assertEquals(UnitCategory.MASS, thirdSessionPrefs.getUnitCategory())
    assertEquals("kg", thirdSessionPrefs.getUnitFrom(UnitCategory.MASS))
    assertEquals("lb", thirdSessionPrefs.getUnitTo(UnitCategory.MASS))

    // Also verify previous category's pair was preserved
    assertEquals("c", thirdSessionPrefs.getUnitFrom(UnitCategory.TEMPERATURE))
    assertEquals("f", thirdSessionPrefs.getUnitTo(UnitCategory.TEMPERATURE))
  }
}
