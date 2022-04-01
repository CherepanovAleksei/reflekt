package org.jetbrains.reflekt.plugin.ic.data.base_test

import org.jetbrains.reflekt.Reflekt

fun main() {
    val objects = Reflekt.objects().withAnnotations<B>(A::class).toList().map { it.javaClass.simpleName }
    println(objects.toString())
}
