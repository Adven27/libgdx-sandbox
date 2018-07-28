package com.learnprogrammingacademy.sampler.common

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.learnprogrammingacademy.sampler.samples.StarfishGame
import com.learnprogrammingacademy.sampler.utils.pressed
import java.lang.UnsupportedOperationException
import java.util.*
import kotlin.reflect.KClass

class MenuScreen : BaseScreen() {
    init {
        mainStage.apply {
            addActor(BaseActor(0f, 0f).apply {
                loadTexture(WATER)
                setSize(800f, 600f)
            })
            addActor(BaseActor(0f, 0f).apply {
                loadTexture(TITLE)
                centerAtPosition(400f, 300f)
                moveBy(0f, 100f)
            })
            addActor(BaseActor(0f, 0f).apply {
                loadTexture(MSG_START)
                centerAtPosition(400f, 300f)
                moveBy(0f, -100f)
            })
        }
    }

    override fun update(dt: Float) {
        if (Keys.S.pressed()) StarfishGame.screen = LevelScreen()
    }
}

class LevelScreen : BaseScreen() {
    private val statLabel = Label("LOG:", StarfishGame.labelStyle).apply {
        color = Color.RED
        setPosition(0f, 50f)
        name = "Stat"
    }
    var turtle: Turtle private set
    var win: Boolean = false
        private set

    val actors: Queue<Pair<KClass<out BaseActor>, Pair<Float, Float>>> =
        ArrayDeque<Pair<KClass<out BaseActor>, Pair<Float, Float>>>()

    init {
        turtle = Turtle(20f, 20f)
        mainStage.apply {
            addActor(BaseActor(0f, 0f).apply {
                loadTexture(WATER_BORDER)
                setSize(1200f, 900f)
                BaseActor.setWorldBounds(this)
            })
            addActor(turtle)
        }
        uiStage.addActor(statLabel)
        win = false
    }

    override fun update(dt: Float) {
        actors.populate(mainStage)

        mainStage.applyPhysics()

        when {
            win() -> setWinState()
            lose() -> setLoseState()
        }
        stat()
    }

    private fun Stage.applyPhysics() {
        collect(Starfish::class, turtle) { collect(it) }
        overlap(Shark::class, turtle) { turtle.die() }
        solid(Rock::class, turtle)
    }

    private fun collect(starfish: Starfish) = mainStage.addActor(Whirlpool(0f, 0f).apply {
        centerAtActor(starfish.collect())
    })

    private fun setWinState() {
        win = true
        play = false
        uiStage.addActor(Win())
    }

    private fun setLoseState() {
        win = false
        play = false
        uiStage.addActor(Lose())
    }

    private fun lose() = play && !turtle.alive
    private fun win() = play && mainStage.none(Starfish::class)

    private fun stat() = statLabel.setText(
        """
        |T: ${turtle.alive}
        |R: ${mainStage.count(Rock::class)}
        |St: ${mainStage.count(Starfish::class)}
        |Sh: ${mainStage.count(Shark::class)}
        |AQ: $actors
        |MSA: ${mainStage.actors}
        |USA: ${uiStage.actors.map { it.name }}
        """.trimMargin()
    )

    private fun Queue<Pair<KClass<out BaseActor>, Pair<Float, Float>>>.populate(stage: Stage) {
        while (!isEmpty()) {
            val (actor, position) = poll()
            stage.addActor(
                when (actor) {
                    Rock::class -> Rock(position)
                    Shark::class -> Shark(position)
                    Turtle::class -> Turtle(position)
                    Starfish::class -> Starfish(position)
                    else -> throw UnsupportedOperationException("${poll().first}")
                }
            )
        }
    }
}