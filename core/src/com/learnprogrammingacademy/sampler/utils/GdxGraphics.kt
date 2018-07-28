package com.learnprogrammingacademy.sampler.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.utils.viewport.Viewport

/**
 * @author goran on 27/10/2017.
 */
@JvmOverloads
fun clearScreen(color: Color = Color.BLACK) = clearScreen(color.r, color.g, color.b, color.a)

fun clearScreen(r: Float, g: Float, b: Float, a: Float) {
    Gdx.gl.glClearColor(r, g, b, a)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
}

inline fun Batch.use(action: Batch.() -> Unit) {
    begin()
    action()
    end()
}

inline fun ShapeRenderer.draw(action: ShapeRenderer.() -> Unit) {
    begin(ShapeRenderer.ShapeType.Line)
    action()
    end()
}

@JvmOverloads
fun Viewport.drawGrid(renderer: ShapeRenderer, cellSize: Int = 1) {
    val oldColor = renderer.color.cpy()
    val doubleWorldWidth = worldWidth * 2
    val doubleWorldHeight = worldHeight * 2

    renderer.draw {
        color = Color.WHITE

        // draw vertical lines
        var x = -doubleWorldWidth
        while (x < doubleWorldWidth) {
            line(x, -doubleWorldHeight, x, doubleWorldHeight)
            x += cellSize
        }

        // draw horizontal lines
        var y = -doubleWorldHeight
        while (y < doubleWorldHeight) {
            line(-doubleWorldWidth, y, doubleWorldWidth, y)
            y += cellSize
        }

        // draw 0/0 lines
        color = Color.RED
        line(0f, -doubleWorldHeight, 0f, doubleWorldHeight)
        line(-doubleWorldWidth, 0f, doubleWorldWidth, 0f)

        // draw world bounds
        color = Color.GREEN
        line(0f, worldHeight, worldWidth, worldHeight)
        line(worldWidth, 0f, worldWidth, worldHeight)
    }

    renderer.color = oldColor
}

@JvmOverloads
fun ShapeRenderer.circle(c: Circle, segments: Int = 30) = circle(c.x, c.y, c.radius, segments)