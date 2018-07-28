package com.learnprogrammingacademy.sampler.common

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.learnprogrammingacademy.sampler.samples.*

object SampleInfos {
    private val allSamples = arrayListOf(
        SampleInfo(StarfishGame::class.java)
    )

    fun getSampleNames() = allSamples.associateBy {
        it.name
    }.keys.toList().sorted().toTypedArray()

    fun find(name: String) = allSamples.find { it.name == name }
}

class SampleInfo(val clazz: Class<out BaseGame>) {
    val name: String = clazz.simpleName
}

object SampleFactory {
    fun newSample(name: String): BaseGame {
        val info = SampleInfos.find(name)
        return info?.clazz?.getDeclaredField("INSTANCE")?.get(null) as BaseGame
    }
}

abstract class SampleBase : ApplicationAdapter(), InputProcessor {
    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false
    override fun mouseMoved(screenX: Int, screenY: Int) = false
    override fun keyTyped(character: Char) = false
    override fun scrolled(amount: Int) = false
    override fun keyUp(keycode: Int) = false
    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false
    override fun keyDown(keycode: Int) = false
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false
}