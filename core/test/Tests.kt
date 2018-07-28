import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input
import com.badlogic.gdx.backends.headless.mock.input.MockInput
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.learnprogrammingacademy.sampler.common.*
import com.learnprogrammingacademy.sampler.samples.StarfishGame
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class Test {
    private val input = mock<MockInput>()
    private lateinit var sut: LevelScreen

    @Before fun refreshLevel() {
        Gdx.input = input
        StarfishGame.toScreen(Screens.LEVEL)
        Waiter().wait(StarfishGame) { onLevel }
        sut = StarfishGame.screen as LevelScreen
    }

    @Test fun `WHEN collect all THEN win message appears on ui`() {
        with(StarfishGame.screen as LevelScreen) {
            actors += listOf(
                Rock::class to (100f to 150f),
                Rock::class to (200f to 150f),
                Starfish::class to (100f to 100f),
                Starfish::class to (150f to 100f)
            )
            play = true
        }

        Input.Keys.RIGHT.press()

        Waiter().wait(sut.turtle) { x > 150 }
        Waiter().wait(sut.uiStage) { actors.size == 2 }
    }

    @Test fun `WHEN touch shark THEN game over`() {
        with(sut) {
            actors += listOf(Shark::class to (150f to 50f), Starfish::class to (150f to 300f))
            play = true
        }

        Input.Keys.RIGHT.press()

        Waiter().wait(sut.turtle) { !alive }
        Waiter().wait(sut.uiStage) { actors.size == 2 }
    }

    companion object {
        private var app: LwjglApplication? = null
        @BeforeClass @JvmStatic fun setUp() {
            app = LwjglApplication(StarfishGame)
        }

        @AfterClass @JvmStatic fun tearDown() {
            app?.apply { stop(); exit() }
            app = null
        }

    }
}

private fun Int.press() {
    whenever(input.isKeyPressed(this)).thenReturn(true)
}

class Waiter(private val timeout: Long = 5000, private val pollingInterval: Long = 500) {
    fun <T> wait(subject: T, condition: T.() -> Boolean) {
        val start = System.currentTimeMillis()
        while (!isTimeoutExceeded(timeout, start) && !condition(subject)) {
            sleep(pollingInterval)
        }
    }

    private fun isTimeoutExceeded(timeout: Long, start: Long): Boolean {
        return if (System.currentTimeMillis() - start > timeout) throw AssertionError("TIMEOUT EXCEEDED") else false
    }

    private fun sleep(milliseconds: Long) = try {
        Thread.sleep(milliseconds)
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        throw RuntimeException(e)
    }
}
