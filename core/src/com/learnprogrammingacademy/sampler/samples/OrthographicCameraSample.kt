package com.learnprogrammingacademy.sampler.samples

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.learnprogrammingacademy.sampler.common.SampleBase
import com.learnprogrammingacademy.sampler.utils.*

/**
 * @author goran on 26/10/2017.
 */
class OrthographicCameraSample : SampleBase() {

    companion object {
        @JvmStatic
        private val log = logger<OrthographicCameraSample>()

        private const val WORLD_WIDTH = 10.8f // world units
        private const val WORLD_HEIGHT = 7.2f // world units
        private const val CAMERA_SPEED = 2.0f // world units
        private const val CAMERA_ZOOM_SPEED = 2.0f // world units
    }


    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: Viewport
    private lateinit var batch: SpriteBatch
    private lateinit var texture: Texture


    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        log.debug("create()")

        camera = OrthographicCamera()
//        camera.setToOrtho(true)
        viewport = FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera)
        batch = SpriteBatch()
        texture = Texture("raw/level-bg.png".toInternalFile())
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render() {
        clearScreen()
        queryInput()

        batch.projectionMatrix = camera.combined

        batch.use {
            draw(texture, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
        }
    }

    private fun queryInput() {
        val deltaTime = Gdx.graphics.deltaTime


        when {
            Input.Keys.LEFT.pressed() -> camera.position.x -= CAMERA_SPEED * deltaTime
            Input.Keys.RIGHT.pressed() -> camera.position.x += CAMERA_SPEED * deltaTime
            Input.Keys.UP.pressed() -> camera.position.y += CAMERA_SPEED * deltaTime
            Input.Keys.DOWN.pressed() -> camera.position.y -= CAMERA_SPEED * deltaTime
            Input.Keys.PAGE_UP.pressed() -> camera.zoom -= CAMERA_ZOOM_SPEED * deltaTime
            Input.Keys.PAGE_DOWN.pressed() -> camera.zoom += CAMERA_ZOOM_SPEED * deltaTime
            Input.Keys.ENTER.pressed() -> {
                log.debug("position= ${camera.position}")
                log.debug("zoom= ${camera.zoom}")
            }
//            else -> log.debug("no keys pressed")
        }

        camera.update()
    }

    override fun dispose() {
        log.debug("dispose")
        batch.dispose()
        texture.dispose()
    }
}