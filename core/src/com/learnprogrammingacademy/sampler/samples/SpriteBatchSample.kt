package com.learnprogrammingacademy.sampler.samples

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.learnprogrammingacademy.sampler.common.SampleBase
import com.learnprogrammingacademy.sampler.utils.clearScreen
import com.learnprogrammingacademy.sampler.utils.logger
import com.learnprogrammingacademy.sampler.utils.toInternalFile
import com.learnprogrammingacademy.sampler.utils.use

/**
 * @author goran on 26/10/2017.
 */
class SpriteBatchSample : SampleBase() {

    companion object {
        @JvmStatic
        private val log = logger<SpriteBatchSample>()
    }

    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: Viewport
    private lateinit var batch: SpriteBatch
    private lateinit var texture: Texture

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        log.debug("create()")

        camera = OrthographicCamera()
        viewport = FitViewport(10.8f, 7.2f, camera)
        batch = SpriteBatch()
        texture = Texture("raw/character.png".toInternalFile())
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render() {
        clearScreen()
        batch.projectionMatrix = camera.combined

        batch.use {
            val width = 1f // world units
            val height = 1f // world units
            draw(
                texture,
                1f, 1f,
                width / 2f, height / 2f,
                width, height,
                1f, 1f,
                0f,
                texture.width / 2, texture.height / 2,
                texture.width, texture.height,
                false, false
            )
            draw(
                texture,
                4f, 2f,
                width / 2f, height / 2f,
                width, height,
                2f, 2f,
                0f,
                0, 0,
                texture.width, texture.height,
                false, false
            )
            val oldColor = color
            color = Color.GREEN
            draw(
                texture,
                8f, 1f,
                width / 2f, height / 2f,
                width, height,
                1f, 1f,
                0f,
                0, 0,
                texture.width, texture.height,
                false, true
            )
            color = oldColor
        }
    }

    override fun dispose() {
        log.debug("dispose")
        batch.dispose()
        texture.dispose()
    }
}