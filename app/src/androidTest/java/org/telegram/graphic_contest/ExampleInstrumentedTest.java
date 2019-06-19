package org.telegram.graphic_contest;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() {

        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("org.telegram.graphic_contest", appContext.getPackageName());
    }
}
