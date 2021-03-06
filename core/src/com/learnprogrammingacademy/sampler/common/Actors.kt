package com.learnprogrammingacademy.sampler.common

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.learnprogrammingacademy.sampler.utils.pressed

class Turtle(x: Float, y: Float) : BaseActor(x, y) {
    var alive: Boolean = true
        private set

    constructor(position: Pair<Float, Float>) : this(position.first, position.second)

    init {
        val filenames = arrayOf(
            "starfish/turtle-1.png",
            "starfish/turtle-2.png",
            "starfish/turtle-3.png",
            "starfish/turtle-4.png",
            "starfish/turtle-5.png",
            "starfish/turtle-6.png"
        )

        loadAnimationFromFiles(filenames, 0.1f, true)

        acceleration = 40f
        maxSpeed = 200f
        deceleration = 40f

        setBoundaryPolygon(8)
    }

    override fun act(dt: Float) {
        super.act(dt)

        if (alive) {
            if (Keys.LEFT.pressed()) accelerateAtAngle(180f)
            if (Keys.RIGHT.pressed()) accelerateAtAngle(0f)
            if (Keys.UP.pressed()) accelerateAtAngle(90f)
            if (Keys.DOWN.pressed()) accelerateAtAngle(270f)
        }

        applyPhysics(dt)

        animationPaused = !isMoving

        if (speed > 0)
            rotation = motionAngle

        boundToWorld()

        alignCamera()
    }

    fun die() {
        alive = false
        clearActions()
        actions(Actions.fadeOut(1f))
    }
}

class Starfish(x: Float, y: Float, var collected: Boolean = false) : BaseActor(x, y, 8) {
    fun collect() = this.apply {
        collected = true
        clearActions()
        actions(Actions.fadeOut(1f), Actions.after(Actions.removeActor()))
    }

    constructor(position: Pair<Float, Float>) : this(position.first, position.second)

    init {
        loadTexture(STARFISH)
        val spin = Actions.rotateBy(30f, 1f)
        this.addAction(Actions.forever(spin))
    }
}

class Rock(x: Float, y: Float) : BaseActor(x, y, 8) {
    constructor(position: Pair<Float, Float>) : this(position.first, position.second)

    init {
        loadTexture(ROCK)
    }
}

class Shark(x: Float, y: Float) : BaseActor(x, y, 8) {
    constructor(position: Pair<Float, Float>) : this(position.first, position.second)

    init {
        loadTexture(SHARK)
    }
}

sealed class Messages(messages: String) : BaseActor(0f, 0f, 8) {
    init {
        loadTexture(messages)
        centerAtPosition(400f, 300f)
        setOpacity(0f)
        actions(Actions.delay(1f), Actions.after(Actions.fadeIn(1f)))
        name = messages
    }
}

class Win : Messages(MSG_WIN)
class Lose : Messages(MSG_GAME_OVER)

class Whirlpool(x: Float, y: Float) : BaseActor(x, y) {
    constructor(position: Pair<Float, Float>) : this(position.first, position.second)

    init {
        loadAnimationFromSheet(WHIRLPOOL, 2, 5, 0.1f, false)
        setOpacity(0.25f)
    }

    override fun act(dt: Float) {
        super.act(dt)

        if (isAnimationFinished) remove()
    }
}