package org.jetbrains.reflekt.plugin.ir

import org.jetbrains.reflekt.plugin.ir.ResultCall.call
import org.gradle.internal.impldep.org.junit.Ignore
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

@Disabled
@Tag("ir")
class IrTransformClassesAndObjectsTest {
    @Test
    @Disabled("Failed since we disabled the Reflekt part")
    fun testClasses() {
        assertEquals(
            setOf("org.jetbrains.reflekt.test.ir.C1", "org.jetbrains.reflekt.test.ir.C2", "org.jetbrains.reflekt.test.ir.C3", "org.jetbrains.reflekt.test.ir.C3.C5"),
            ReflektType.REFLEKT.classesFqNamesCall(Signature("CInterface")).call()
        )
    }


    @Test
    @Disabled("Failed since we disabled the Reflekt part")
    fun testObjects() {
        assertEquals(
            setOf("org.jetbrains.reflekt.test.ir.O1", "org.jetbrains.reflekt.test.ir.O1.O2"),
            ReflektType.REFLEKT.objectsFqNamesCall(Signature("OInterface")).call()
        )
    }
}
