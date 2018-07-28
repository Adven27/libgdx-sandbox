package com.learnprogrammingacademy.sampler.common

import com.badlogic.gdx.Game
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.learnprogrammingacademy.sampler.utils.clearScreen

abstract class BaseGame : Game() {
    lateinit var labelStyle: Label.LabelStyle
    var destScreen: Screens? = null
    val onLevel: Boolean get() = screen is LevelScreen && destScreen == null
    val onMenu: Boolean get() = screen is MenuScreen && destScreen == null

    fun toScreen(s: Screens) {
        destScreen = s
    }

    override fun create() {
        labelStyle = Label.LabelStyle().apply { font = BitmapFont() }
    }

    override fun render() {
        super.render()

        destScreen?.let {
            screen = when (destScreen) {
                Screens.LEVEL -> LevelScreen()
                Screens.MENU -> MenuScreen()
                else -> screen
            }
            destScreen = null
        }
    }
}

enum class Screens { MENU, LEVEL }

abstract class BaseScreen : Screen {
    protected var mainStage: Stage = Stage()
    var uiStage: Stage = Stage()

    var play = false

    abstract fun update(dt: Float)

    override fun render(dt: Float) {
        uiStage.act(dt)
        mainStage.act(dt)

        update(dt)

        clearScreen()
        mainStage.draw()
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {}

    override fun pause() {}

    override fun resume() {}

    override fun dispose() {}

    override fun show() {}

    override fun hide() {}
}