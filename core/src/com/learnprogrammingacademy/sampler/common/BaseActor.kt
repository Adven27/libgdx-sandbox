package com.learnprogrammingacademy.sampler.common

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.*
import com.badlogic.gdx.math.Intersector.MinimumTranslationVector
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.learnprogrammingacademy.sampler.utils.GdxArray
import com.learnprogrammingacademy.sampler.utils.toInternalFile
import kotlin.reflect.KClass

/**
 * Extends functionality of the LibGDX Actor class.
 * by adding support for textures/animation,
 * collision polygons, movement, world boundaries, and camera scrolling.
 * Most game objects should extend this class; lists of extensions can be retrieved by stage and class name.
 *
 * @author Lee Stemkoski
 * @see .Actor
 */
open class BaseActor(x: Float, y: Float, boundarySides: Int = 0) : Actor() {
    constructor(position: Pair<Float, Float>) : this(position.first, position.second)

    private lateinit var animation: Animation<TextureRegion>
    private var elapsedTime: Float = 0f
    private val velocityVec: Vector2

    private val accelerationVec: Vector2
    var animationPaused: Boolean = false
    var acceleration: Float = 0f
    var maxSpeed: Float = 0f
    var deceleration: Float = 0f

    private lateinit var boundaryPolygon: Polygon

    /**
     * Checks if animation is complete: if play mode is normal (not looping)
     * and elapsed time is greater than time corresponding to last frame.
     *
     * @return
     */
    val isAnimationFinished: Boolean get() = animation.isAnimationFinished(elapsedTime)

    /**
     * Calculates the speed of movement (in pixels/second).
     *
     * @return speed of movement (pixels/second)
     */
    /**
     * Set the speed of movement (in pixels/second) in current direction.
     * If current speed is zero (direction is undefined), direction will be set to 0 degrees.
     *
     */
    var speed
        get() = velocityVec.len()
        set(speed) {
            if (velocityVec.len() == 0f)
                velocityVec.set(speed, 0f)
            else
                velocityVec.setLength(speed)
        }

    val isMoving: Boolean get() = speed > 0

    /**
     * Get the angle of motion (in degrees), calculated from the velocity vector.
     * <br></br>
     * To align actor image angle with motion angle, use `setRotation( getMotionAngle() )`.
     *
     * @return angle of motion (degrees)
     */
    /**
     * Sets the angle of motion (in degrees).
     * If current speed is zero, this will have no effect.
     *
     */
    var motionAngle
        get() = velocityVec.angle()
        set(angle) {
            velocityVec.setAngle(angle)
        }

    init {
        this.setPosition(x, y)
        if (boundarySides > 0) setBoundaryPolygon(boundarySides)
        // initialize animation data
        elapsedTime = 0f
        animationPaused = false

        // initialize physics data
        velocityVec = Vector2(0f, 0f)
        accelerationVec = Vector2(0f, 0f)
        acceleration = 0f
        maxSpeed = 1000f
        deceleration = 0f
    }

    /**
     * Align center of actor at given position coordinates.
     *
     * @param x x-coordinate to center at
     * @param y y-coordinate to center at
     */
    fun centerAtPosition(x: Float, y: Float) {
        setPosition(x - width / 2, y - height / 2)
    }

    /**
     * Repositions this BaseActor so its center is aligned
     * with center of other BaseActor. Useful when one BaseActor spawns another.
     *
     * @param other BaseActor to align this BaseActor with
     */
    fun centerAtActor(other: BaseActor) {
        centerAtPosition(other.x + other.width / 2, other.y + other.height / 2)
    }

    /**
     * Sets the animation used when rendering this actor; also sets actor size.
     *
     * @param anim animation that will be drawn when actor is rendered
     */
    private fun setAnimation(anim: Animation<TextureRegion>) {
        animation = anim
        val tr = animation.getKeyFrame(0f)
        val w = tr.regionWidth.toFloat()
        val h = tr.regionHeight.toFloat()
        setSize(w, h)
        setOrigin(w / 2, h / 2)

        setBoundaryRectangle()
    }

    /**
     * Creates an animation from images stored in separate files.
     *
     * @param files     array of names of files containing animation images
     * @param frameDuration how long each frame should be displayed
     * @param loop          should the animation loop
     * @return animation created (useful for storing multiple animations)
     */
    fun loadAnimationFromFiles(files: Array<String>, frameDuration: Float, loop: Boolean): Animation<TextureRegion> {
        val fileCount = files.size
        val textureArray = GdxArray<TextureRegion>()

        for (n in 0 until fileCount) {
            val fileName = files[n]
            val texture = Texture(fileName.toInternalFile())
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear)
            textureArray.add(TextureRegion(texture))
        }

        val anim = Animation(frameDuration, textureArray).apply {
            setPlayMode(if (loop) Animation.PlayMode.LOOP else Animation.PlayMode.NORMAL)
        }
        setAnimation(anim)
        return anim
    }

    /**
     * Creates an animation from a spritesheet: a rectangular grid of images stored in a single file.
     *
     * @param file      name of file containing spritesheet
     * @param rows          number of rows of images in spritesheet
     * @param cols          number of columns of images in spritesheet
     * @param frameDuration how long each frame should be displayed
     * @param loop          should the animation loop
     * @return animation created (useful for storing multiple animations)
     */
    fun loadAnimationFromSheet(
        file: String,
        rows: Int,
        cols: Int,
        frameDuration: Float,
        loop: Boolean
    ): Animation<TextureRegion> {
        val texture = Texture(file.toInternalFile(), true)
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear)
        val frameWidth = texture.width / cols
        val frameHeight = texture.height / rows

        val temp = TextureRegion.split(texture, frameWidth, frameHeight)

        val textureArray = GdxArray<TextureRegion>()

        for (r in 0 until rows)
            for (c in 0 until cols)
                textureArray.add(temp[r][c])

        val anim = Animation(frameDuration, textureArray).apply {
            setPlayMode(if (loop) Animation.PlayMode.LOOP else Animation.PlayMode.NORMAL)
        }
        setAnimation(anim)
        return anim
    }

    /**
     * Convenience method for creating a 1-frame animation from a single texture.
     *
     * @param fileName names of image file
     * @return animation created (useful for storing multiple animations)
     */
    fun loadTexture(fileName: String) = loadAnimationFromFiles(arrayOf(fileName), 1f, true)


    /**
     * Sets the opacity of this actor.
     *
     * @param opacity value from 0 (transparent) to 1 (opaque)
     */
    fun setOpacity(opacity: Float) {
        this.color.a = opacity
    }


    /**
     * Update accelerate vector by angle and value stored in acceleration field.
     * Acceleration is applied by `applyPhysics` method.
     *
     * @param angle Angle (degrees) in which to accelerate.
     * @see .acceleration
     *
     * @see .applyPhysics
     */
    fun accelerateAtAngle(angle: Float) {
        accelerationVec.add(
            Vector2(acceleration, 0f).setAngle(angle)
        )
    }

    /**
     * Update accelerate vector by current rotation angle and value stored in acceleration field.
     * Acceleration is applied by `applyPhysics` method.
     *
     * @see .acceleration
     *
     * @see .applyPhysics
     */
    fun accelerateForward() {
        accelerateAtAngle(rotation)
    }

    /**
     * Adjust velocity vector based on acceleration vector,
     * then adjust position based on velocity vector. <br></br>
     * If not accelerating, deceleration value is applied. <br></br>
     * Speed is limited by maxSpeed value. <br></br>
     * Acceleration vector reset to (0,0) at end of method. <br></br>
     *
     * @param dt Time elapsed since previous frame (delta time); typically obtained from `act` method.
     * @see .acceleration
     *
     * @see .deceleration
     *
     * @see .maxSpeed
     */
    fun applyPhysics(dt: Float) {
        // apply acceleration
        velocityVec.add(accelerationVec.x * dt, accelerationVec.y * dt)

        var s = speed

        // decrease speed (decelerate) when not accelerating
        if (accelerationVec.len() == 0f)
            s -= deceleration * dt

        // keep speed within set bounds
        s = MathUtils.clamp(s, 0f, maxSpeed)

        // update velocity
        speed = s

        // apply velocity
        moveBy(velocityVec.x * dt, velocityVec.y * dt)

        // reset acceleration
        accelerationVec.set(0f, 0f)
    }

    /**
     * Set rectangular-shaped collision polygon.
     * This method is automatically called when animation is set,
     * provided that the current boundary polygon is null.
     *
     * @see .setAnimation
     */
    private fun setBoundaryRectangle() {
        val w = width
        val h = height

        val vertices = floatArrayOf(0f, 0f, w, 0f, w, h, 0f, h)
        boundaryPolygon = Polygon(vertices)
    }

    /**
     * Replace default (rectangle) collision polygon with an n-sided polygon. <br></br>
     * Vertices of polygon lie on the ellipse contained within bounding rectangle.
     * Note: one vertex will be located at point (0,width);
     * a 4-sided polygon will appear in the orientation of a diamond.
     *
     * @param numSides number of sides of the collision polygon
     */
    fun setBoundaryPolygon(numSides: Int) {
        val w = width
        val h = height

        val vertices = FloatArray(2 * numSides)
        for (i in 0 until numSides) {
            val angle = i * 6.28f / numSides
            // x-coordinate
            vertices[2 * i] = w / 2 * MathUtils.cos(angle) + w / 2
            // y-coordinate
            vertices[2 * i + 1] = h / 2 * MathUtils.sin(angle) + h / 2
        }
        boundaryPolygon = Polygon(vertices)

    }

    /**
     * Returns bounding polygon for this BaseActor, adjusted by Actor's current position and rotation.
     *
     * @return bounding polygon for this BaseActor
     */
    fun getBoundaryPolygon() = boundaryPolygon.apply {
        setPosition(this@BaseActor.x, this@BaseActor.y)
        setOrigin(this@BaseActor.originX, this@BaseActor.originY)
        rotation = this@BaseActor.rotation
        setScale(this@BaseActor.scaleX, this@BaseActor.scaleY)
    }

    /**
     * Determine if this BaseActor overlaps other BaseActor (according to collision polygons).
     *
     * @param other BaseActor to check for overlap
     * @return true if collision polygons of this and other BaseActor overlap
     * @see .setCollisionRectangle
     *
     * @see .setCollisionPolygon
     */
    fun overlaps(other: BaseActor): Boolean {
        val p1 = this.getBoundaryPolygon()
        val p2 = other.getBoundaryPolygon()

        // initial test to improve performance
        return p1.boundingRectangle.overlaps(p2.boundingRectangle) && Intersector.overlapConvexPolygons(p1, p2)
    }

    /**
     * Implement a "solid"-like behavior:
     * when there is overlap, move this BaseActor away from other BaseActor
     * along minimum translation vector until there is no overlap.
     *
     * @param other BaseActor to check for overlap
     * @return direction vector by which actor was translated, null if no overlap
     */
    fun preventOverlap(other: BaseActor): Vector2? {
        val poly1 = this.getBoundaryPolygon()
        val poly2 = other.getBoundaryPolygon()

        // initial test to improve performance
        val mtv = MinimumTranslationVector()
        return if (
            poly1.boundingRectangle.overlaps(poly2.boundingRectangle) &&
            Intersector.overlapConvexPolygons(poly1, poly2, mtv)
        ) {
            moveBy(mtv.normal.x * mtv.depth, mtv.normal.y * mtv.depth)
            mtv.normal
        } else null
    }

    /**
     * If an edge of an object moves past the world bounds,
     * adjust its position to keep it completely on screen.
     */
    fun boundToWorld() {
        if (x < 0) x = 0f
        if (x + width > worldBounds.width) x = worldBounds.width - width
        if (y < 0) y = 0f
        if (y + height > worldBounds.height) y = worldBounds.height - height
    }

    /**
     * Center camera on this object, while keeping camera's range of view
     * (determined by screen size) completely within world bounds.
     */
    fun alignCamera() {
        val cam = this.stage.camera
        val v = this.stage.viewport

        // center camera on actor
        cam.position.set(this.x + this.originX, this.y + this.originY, 0f)

        // bound camera to layout
        cam.position.x =
                MathUtils.clamp(cam.position.x, cam.viewportWidth / 2, worldBounds.width - cam.viewportWidth / 2)
        cam.position.y =
                MathUtils.clamp(cam.position.y, cam.viewportHeight / 2, worldBounds.height - cam.viewportHeight / 2)
        cam.update()
    }

    // ----------------------------------------------
    // Actor methods: act and draw
    // ----------------------------------------------

    /**
     * Processes all Actions and related code for this object;
     * automatically called by act method in Stage class.
     *
     * @param dt elapsed time (second) since last frame (supplied by Stage act method)
     */
    override fun act(dt: Float) {
        super.act(dt)

        if (!animationPaused)
            elapsedTime += dt
    }

    /**
     * Draws current frame of animation; automatically called by draw method in Stage class. <br></br>
     * If color has been set, image will be tinted by that color. <br></br>
     * If no animation has been set or object is invisible, nothing will be drawn.
     *
     * @param batch       (supplied by Stage draw method)
     * @param parentAlpha (supplied by Stage draw method)
     * @see .setColor
     *
     * @see .setVisible
     */
    override fun draw(batch: Batch?, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        // apply color tint effect
        val c = color
        batch!!.setColor(c.r, c.g, c.b, c.a)

        if (isVisible)
            batch.draw(
                animation.getKeyFrame(elapsedTime),
                x, y, originX, originY,
                width, height, scaleX, scaleY, rotation
            )
    }

    companion object {
        // stores size of game world for all actors
        private lateinit var worldBounds: Rectangle

        /**
         * Set world dimensions for use by methods boundToWorld() and scrollTo().
         *
         * @param width  width of world
         * @param height height of world
         */
        fun setWorldBounds(width: Float, height: Float) {
            worldBounds = Rectangle(0f, 0f, width, height)
        }

        /**
         * Set world dimensions for use by methods boundToWorld() and scrollTo().
         *
         * @param ba whose size determines the world bounds (typically a background image)
         */
        fun setWorldBounds(ba: BaseActor) {
            setWorldBounds(ba.width, ba.height)
        }
    }
}

/**
 * Retrieves a list of all instances of the object from the given stage with the given class name
 * or whose class extends the class with the given name.
 * If no instances exist, returns an empty list.
 * Useful when coding interactions between different types of game objects in update method.
 *
 * @param kClass class that extends the BaseActor class
 * @return list of instances of the object in stage which extend with the given class
 */
internal inline fun <reified T : BaseActor> Stage.actors(kClass: KClass<T>) =
    actors.filter { a -> kClass.java.isInstance(a) }.map { it as T }

/**
 * Returns number of instances of a given class (that extends BaseActor).
 *
 * @param kClass class that extends the BaseActor class
 * @return number of instances of the class
 */
internal fun Stage.count(kClass: KClass<out BaseActor>) = actors.count { a -> kClass.java.isInstance(a) }
internal fun Stage.none(kClass: KClass<out BaseActor>) = actors.none { a -> kClass.java.isInstance(a) }

internal fun Actor.actions(vararg actions: Action) = actions.forEach { addAction(it) }