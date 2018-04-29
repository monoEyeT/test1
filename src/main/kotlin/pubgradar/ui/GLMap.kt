package pubgradar.ui

import com.badlogic.gdx.*
import com.badlogic.gdx.Input.Buttons.*
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
import com.badlogic.gdx.graphics.Texture.TextureFilter.MipMap
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.DEFAULT_CHARS
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line
import com.badlogic.gdx.math.*
import org.lwjgl.opengl.GL11.GL_TEXTURE_BORDER_COLOR
import org.lwjgl.opengl.GL11.glTexParameterfv
import org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER
import pubgradar.*
import pubgradar.Sniffer.Companion.localAddr
import pubgradar.Sniffer.Companion.sniffOption
import pubgradar.deserializer.channel.ActorChannel.Companion.actorHasWeapons
import pubgradar.deserializer.channel.ActorChannel.Companion.actors
import pubgradar.deserializer.channel.ActorChannel.Companion.airDropLocation
import pubgradar.deserializer.channel.ActorChannel.Companion.attacks
import pubgradar.deserializer.channel.ActorChannel.Companion.corpseLocation
import pubgradar.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import pubgradar.deserializer.channel.ActorChannel.Companion.firing
import pubgradar.deserializer.channel.ActorChannel.Companion.playerStateToActor
import pubgradar.deserializer.channel.ActorChannel.Companion.redZoneBombLocation
import pubgradar.deserializer.channel.ActorChannel.Companion.selfID
import pubgradar.deserializer.channel.ActorChannel.Companion.selfStateID
import pubgradar.deserializer.channel.ActorChannel.Companion.teams
import pubgradar.deserializer.channel.ActorChannel.Companion.visualActors
import pubgradar.deserializer.channel.ActorChannel.Companion.weapons
import pubgradar.http.PlayerProfile.Companion.completedPlayerInfo
import pubgradar.http.PlayerProfile.Companion.pendingPlayerInfo
import pubgradar.http.PlayerProfile.Companion.query
import pubgradar.struct.*
import pubgradar.struct.Archetype.*
import pubgradar.struct.Archetype.Plane
import pubgradar.struct.CMD.ActorCMD.actorWithPlayerState
import pubgradar.struct.CMD.CharacterCMD.actorHealth
import pubgradar.struct.CMD.CharacterCMD.spectatedCount
import pubgradar.struct.CMD.GameStateCMD.ElapsedWarningDuration
import pubgradar.struct.CMD.GameStateCMD.MatchElapsedMinutes
import pubgradar.struct.CMD.GameStateCMD.NumAlivePlayers
import pubgradar.struct.CMD.GameStateCMD.NumAliveTeams
import pubgradar.struct.CMD.GameStateCMD.PoisonGasWarningPosition
import pubgradar.struct.CMD.GameStateCMD.PoisonGasWarningRadius
import pubgradar.struct.CMD.GameStateCMD.RedZonePosition
import pubgradar.struct.CMD.GameStateCMD.RedZoneRadius
import pubgradar.struct.CMD.GameStateCMD.RemainingTime
import pubgradar.struct.CMD.GameStateCMD.SafetyZonePosition
import pubgradar.struct.CMD.GameStateCMD.SafetyZoneRadius
import pubgradar.struct.CMD.GameStateCMD.TotalWarningDuration
import pubgradar.struct.CMD.GameStateCMD.isTeamMatch
import pubgradar.struct.CMD.playerNumKills
import pubgradar.struct.CMD.selfCoords
import pubgradar.struct.CMD.selfDirection
import pubgradar.struct.Item.Companion.order
import pubgradar.struct.PlayerState
import pubgradar.struct.Team
import pubgradar.struct.Weapon
import pubgradar.util.debugln
import pubgradar.util.settings.Settings
import pubgradar.util.tuple4
import pubgradar.util.tuple5
import java.lang.Math.toRadians
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.absoluteValue
import kotlin.math.asin
import kotlin.math.pow
import kotlin.reflect.jvm.internal.impl.resolve.constants.NullValue
import javax.swing.Spring.height
import javax.swing.Spring.width
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch



//typealias renderInfo = tuple4<Actor, Float, Float, Float>
typealias renderInfo = tuple5<Actor, Float, Float, Float, Float>

val itemIcons = HashMap<String, AtlasRegion>()
val crateIcons = HashMap<String, AtlasRegion>()
var totRot = 0f
var tempr = 0f
fun Float.d(n: Int) = String.format("%.${n}f", this)
class GLMap(private val jsettings : Settings.jsonsettings) : InputAdapter() , ApplicationListener , GameListener {
    companion object {
        operator fun Vector3.component1(): Float = x
        operator fun Vector3.component2(): Float = y
        operator fun Vector3.component3(): Float = z
        operator fun Vector2.component1(): Float = x
        operator fun Vector2.component2(): Float = y

    }

    init {
        register(this)
    }

    override fun onGameOver() {
        mapCamera.zoom = 1 / 24f
        minimap.zoom = 1 / 24f
        camera.zoom = 1 / 24f
        aimStartTime.clear()
        attackLineStartTime.clear()
        firingStartTime.clear()
        filterEnableItem =0
        screenOffsetX = 0f
        screenOffsetY = 0f
        camera.update()

    }

    fun show() {
        val config = Lwjgl3ApplicationConfiguration()
        config.setTitle("[${localAddr.hostAddress} ${sniffOption.name}] - MONODA v1.2.1")
        config.setWindowIcon(Files.FileType.Internal, "icon.png")
        config.useOpenGL3(false, 2, 1)
        config.setWindowedMode(initialWindowWidth.toInt(), initialWindowWidth.toInt())
        config.setResizable(true)
        config.setBackBufferConfig(8, 8, 8, 8, 16, 1, 2)
        //config.setIdleFPS(60)
        Lwjgl3Application(this, config)
    }
    lateinit var oriMatrixMap:Matrix4
    lateinit var spriteBatch: SpriteBatch
    lateinit var shapeRenderer: ShapeRenderer
    lateinit var mapErangel: Texture
    lateinit var mapMiramar: Texture
    lateinit var mapSavage: Texture
    lateinit var map: Texture
    lateinit var fbo: FrameBuffer
    lateinit var miniMap: TextureRegion
    lateinit var carePackage: TextureRegion
    lateinit var corpseIcon: TextureRegion
    lateinit var vehicleIcons: Map<Archetype, TextureRegion>
    lateinit var grenadeIcons: Map<Archetype, TextureRegion>
    lateinit var redzoneBombIcon: TextureRegion
    lateinit var largeFont: BitmapFont
    lateinit var littleFont: BitmapFont
    lateinit var fontCamera: OrthographicCamera
    lateinit var infoCamera: OrthographicCamera
    lateinit var UICamera: OrthographicCamera
    lateinit var camera: OrthographicCamera
    lateinit var mapCamera: OrthographicCamera
    lateinit var minimap: OrthographicCamera
    lateinit var miniMapCamera: OrthographicCamera
    lateinit var alarmSound: Sound
    lateinit var pawnAtlas: TextureAtlas
    lateinit var itemAtlas: TextureAtlas
    lateinit var crateAtlas: TextureAtlas
    lateinit var markerAtlas: TextureAtlas
    lateinit var markers: Array<TextureRegion>
    private lateinit var parachute: Texture
    private lateinit var teamarrow: Texture
    private lateinit var teamsight: Texture
    private lateinit var arrow: Texture
    private lateinit var arrowsight: Texture
    private lateinit var jetski: Texture
    private lateinit var player: Texture
    private lateinit var playersight: Texture

    private lateinit var hubFont: BitmapFont
    private lateinit var hubFontShadow: BitmapFont
    private lateinit var espFont: BitmapFont
    private lateinit var espFontShadow: BitmapFont
    private lateinit var compaseFont: BitmapFont
    private lateinit var compaseFontShadow: BitmapFont
    private lateinit var littleFontShadow: BitmapFont
    private lateinit var nameFont: BitmapFont
    private lateinit var itemFont: BitmapFont
    private lateinit var hporange: BitmapFont
    private lateinit var hpred: BitmapFont
    private lateinit var hpgreen: BitmapFont
    private lateinit var menuFont: BitmapFont
    private lateinit var menuFontOn: BitmapFont
    private lateinit var menuFontOFF: BitmapFont
    private lateinit var hubpanel: Texture
    private lateinit var hubpanelblank: Texture
    private lateinit var menu: Texture
    private lateinit var bgcompass: Texture
    val firingStartTime = LinkedList<tuple4<Float, Float, Float, Long>>()
    private val layout = GlyphLayout()
    private var windowWidth = initialWindowWidth
    private var windowHeight = initialWindowWidth
    val clipBound = Rectangle()
    private val aimStartTime = HashMap<NetworkGUID, Long>()
    private val attackLineStartTime = LinkedList<Triple<NetworkGUID, NetworkGUID, Long>>()
    private val pinLocation = Vector2()
    lateinit var nameBlueFont: BitmapFont
    lateinit var nameGoldFont: BitmapFont
    lateinit var nameFontShadow: BitmapFont
    // Please change your pre-build settings in Settings.kt
    // You can change them in Settings.json after you run the game once too.
    private var filterEnableItem = jsettings.drawAll
    private var filterEnableRotate = jsettings.enableRoate
    private var filterLvl2 = jsettings.filterLvl2
    private var filterScope = jsettings.filterScope
    private var filterHeals = jsettings.filterHeals
    private var filterAmmo = jsettings.filterAmmo
    private var filterThrow = jsettings.filterThrow
    private var drawcompass = jsettings.drawcompass
    private var drawgrid = jsettings.drawgrid
    private var drawmenu = jsettings.drawmenu
    private var toggleView = jsettings.toggleView
    private var drawDaMap = jsettings.drawDaMap

    // Please change your pre-build settings in Settings.kt
    // You can change them in Settings.json after you run the game once too.

    // private var toggleVehicles = -1
    //  private var toggleVNames = -1

    private var nameToggles = jsettings.nameToggles
    private var VehicleInfoToggles = jsettings.VehicleInfoToggles
    private var ZoomToggles = jsettings.ZoomToggles
    private var scopesToFilter = arrayListOf("")
    private var weaponsToFilter = arrayListOf("")
    private var attachToFilter = arrayListOf("")
    private var level2Filter = arrayListOf("")
    private var level3Filter = arrayListOf("")
    private var level23Filter = arrayListOf("")
    private var level1Filter = arrayListOf("")
    private var equipFilter = arrayListOf("")
    private var healsToFilter = arrayListOf("")
    private var ammoToFilter = arrayListOf("")
    private var throwToFilter = arrayListOf("")
    private var dragging = false
    private var prevScreenX = -1f
    private var prevScreenY = -1f
    private var screenOffsetX = 0f
    private var screenOffsetY = 0f
    private  var preSelf = 0f

    val miniMapWindowWidth = jsettings.miniMapWindowWidth
    val miniMapRadius = jsettings.miniMapRadius
    val playerRadius = jsettings.playerRadius
    val healthBarWidth = jsettings.healthBarWidth
    val healthBarHeight = jsettings.healthBarHeight
    val directionRadius = jsettings.directionRadius
    val fov = jsettings.fov
    val aimLineWidth = jsettings.aimLineWidth
    val aimLineRange = jsettings.aimLineRange
    val aimCircleRadius = jsettings.aimCircleRadius
    val aimTimeThreshold = jsettings.aimTimeThreshold
    val attackLineDuration = jsettings.attackLineDuration
    val attackMeLineDuration = jsettings.attackMeLineDuration
    val firingLineDuration = jsettings.firingLineDuration
    val firingLineLength = jsettings.firingLineLength
    val itemZoomThreshold = jsettings.itemZoomThreshold
    val airDropTextScale = jsettings.airDropTextScale
    val itemScale = jsettings.itemScale
    val staticItemScale = jsettings.staticItemScale
    val mapMarkerScale = jsettings.mapMarkerScale
    val airDropScale = jsettings.airDropScale
    val vehicleScale = jsettings.vehicleScale
    val vehicleScalemini = jsettings.vehicleScale
    val planeScale = jsettings.planeScale
    val grenadeScale = jsettings.grenadeScale
    val corpseScale = jsettings.corpseScale
    val redzoneBombScale = jsettings.redzoneBombScale

    // Please change your pre-build settings in Settings.kt
    // You can change them in Settings.json after you run the game once too.


    private fun windowToMap(x: Float, y: Float) =
            Vector2(
                    selfCoords.x + (x - windowWidth / 2.0f) * mapCamera.zoom * windowToMapUnit + screenOffsetX,
                    selfCoords.y + (y - windowHeight / 2.0f) * mapCamera.zoom * windowToMapUnit + screenOffsetY
            )

    private fun mapToWindow(x: Float, y: Float) =
            Vector2(
                    (x - selfCoords.x - screenOffsetX) / (mapCamera.zoom * windowToMapUnit) + windowWidth / 2.0f,
                    (y - selfCoords.y - screenOffsetY) / (mapCamera.zoom * windowToMapUnit) + windowHeight / 2.0f
            )


    fun Vector2.windowToMap() = windowToMap(x, y)
    fun Vector2.mapToWindow() = mapToWindow(x, y)
    fun windowToMap(length: Float) = length * mapCamera.zoom * windowToMapUnit
    fun mapToWindow(length: Float) = length / (mapCamera.zoom * windowToMapUnit)


    override fun scrolled(amount: Int): Boolean {

        if (mapCamera.zoom >= 0.01f && mapCamera.zoom <= 1f) {
            mapCamera.zoom *= 1.05f.pow(amount)


        } else {
            if (mapCamera.zoom < 0.01f) {
                mapCamera.zoom = 0.01f
                println("Max Zoom")
            }
            if (mapCamera.zoom > 1f) {
                mapCamera.zoom = 1f
                println("Min Zoom")
            }
        }
        minimap.zoom = mapCamera.zoom
        //miniMapCamera.zoom = if (mapCamera.zoom > 1 / 12f) 1 / 24f else 1 / 20f


        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        when (button) {
            RIGHT -> {
                pinLocation.set(pinLocation.set(screenX.toFloat(), screenY.toFloat()).windowToMap())
                camera.update()
                //println(pinLocation)
                return true
            }
            LEFT -> {
                dragging = true
                prevScreenX = screenX.toFloat()
                prevScreenY = screenY.toFloat()
                return true
            }
            MIDDLE -> {
                screenOffsetX = 0f
                screenOffsetY = 0f
            }
        }
        return false
    }

    override fun keyDown(keycode: Int): Boolean {

        when (keycode) {


        // Change Player Info
            Input.Keys.valueOf(jsettings.nameToogle_Key) -> {
                if (nameToggles < 5) {
                    nameToggles += 1
                }
                if (nameToggles == 5) {
                    nameToggles = 0
                }
            }

            Input.Keys.valueOf(jsettings.VehicleInfoToggles_Key) -> {
                if (VehicleInfoToggles <= 4) {
                    VehicleInfoToggles += 1
                }
                if (VehicleInfoToggles == 4) {
                    VehicleInfoToggles = 1
                }
            }
        // Zoom (Loot, Combat, Scout)
            Input.Keys.valueOf(jsettings.ZoomToggles_Key) -> {
                if (ZoomToggles <= 4) {
                    ZoomToggles += 1
                }
                if (ZoomToggles == 4) {
                    ZoomToggles = 1
                }
                if (ZoomToggles == 1) {
                    mapCamera.zoom = 1 / 8f
                    camera.zoom = 1 / 48f

                }
                if (ZoomToggles == 2) {
                    mapCamera.zoom = 1 / 16f
                    camera.zoom = 1 / 48f
                }
                if (ZoomToggles == 3) {
                    mapCamera.zoom = 1 / 36f
                    camera.zoom = 1 / 8f
                }
                minimap.zoom = mapCamera.zoom
            }

        // Level 1 and 2 item filters
            Input.Keys.valueOf(jsettings.filterAttach_Key) -> {
                if (filterLvl2 < 5) {
                    filterLvl2 += 1
                }
            }

        // Please Change Your Settings in Util/Settings.kt
        // Other Filter Keybinds
            Input.Keys.valueOf(jsettings.drawcompass_Key) -> drawcompass = drawcompass * -1
            Input.Keys.valueOf(jsettings.drawgrid_Key) -> drawgrid = drawgrid * -1
        // Toggle View Line
            Input.Keys.valueOf(jsettings.toggleView_Key) -> toggleView = toggleView * -1
        // Toggle Da Minimap
            Input.Keys.valueOf(jsettings.drawDaMap_Key) -> drawDaMap = drawDaMap * -1
        // Toggle Menu
            Input.Keys.valueOf(jsettings.drawmenu_Key) -> drawmenu = drawmenu * -1
        // Icon Filter Keybinds
            Input.Keys.valueOf(jsettings.enableAll_Key) -> filterEnableItem = filterEnableItem * -1
            Input.Keys.valueOf(jsettings.enableRoate_Key) -> filterEnableRotate = filterEnableRotate * -1
            Input.Keys.valueOf(jsettings.filterPrintLOC) -> {
                println("x ${selfCoords.x},y ${selfCoords.y},z ${selfCoords.z}  ")

            }
            Input.Keys.valueOf(jsettings.filterThrow_Key) -> filterThrow = filterThrow * -1
            Input.Keys.valueOf(jsettings.filterScope_Key) -> filterScope = filterScope * -1
            Input.Keys.valueOf(jsettings.filterAmmo_Key) -> filterAmmo = filterAmmo * -1
        // Zoom In/Out || Overrides Max/Min Zoom
            Input.Keys.valueOf(jsettings.camera_zoom_Minus_Key) -> mapCamera.zoom = mapCamera.zoom + 0.00525f
            Input.Keys.valueOf(jsettings.camera_zoom_Plus_Key) -> mapCamera.zoom = mapCamera.zoom - 0.00525f
        // Please Change Your Settings in Util/Settings.kt
        }
        minimap.zoom = mapCamera.zoom
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (!dragging) return false
        with(camera) {
            screenOffsetX += (prevScreenX - screenX.toFloat()) * camera.zoom * 500
            screenOffsetY += (prevScreenY - screenY.toFloat()) * camera.zoom * 500
            prevScreenX = screenX.toFloat()
            prevScreenY = screenY.toFloat()
        }
        return true
    }

    fun printText(spriteBatch1: SpriteBatch,
                  bitmapFont: BitmapFont, posX: Float, posY: Float, angle: Float,
                  text: String) {
        spriteBatch1.end()
        val oldTransformMatrix = spriteBatch1.transformMatrix.cpy()

        val mx4Font = Matrix4()
        mx4Font.rotate(Vector3(0f, 0f, 1f), angle)
        mx4Font.trn(posX, posY, 0f)
        spriteBatch1.transformMatrix = mx4Font

        spriteBatch1.begin()
        bitmapFont.setColor(1.0f, 1.0f, 1.0f, 1.0f)

        bitmapFont.draw(spriteBatch1, text, 0f, 0f)

        spriteBatch1.end()

        spriteBatch1.transformMatrix = oldTransformMatrix
        spriteBatch1.begin()
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button == LEFT) {
            dragging = false
            return true
        }
        return false
    }


    override fun create() {
        spriteBatch = SpriteBatch()

        shapeRenderer = ShapeRenderer()
        Gdx.input.inputProcessor = this
        mapCamera = OrthographicCamera(windowWidth, windowHeight)
        minimap = OrthographicCamera(windowWidth, windowHeight)
        miniMapCamera = OrthographicCamera()
        with(mapCamera) {
            setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
            zoom = 1 / 24f
            update()
            position.set(mapWidth / 2, mapWidth / 2, 0f)
            update()
        }
        with(minimap) {
            setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
            zoom = 1 / 24f
            update()
            position.set(mapWidth / 2, mapWidth / 2, 0f)
            update()
        }
        with(miniMapCamera) {
            val z = 1 / 8f
            setToOrtho(true, miniMapRadius * 2 / z, miniMapRadius * 2 / z)
            zoom = z
            update()
        }
        camera = mapCamera
        fontCamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
        infoCamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
        UICamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
        alarmSound = Gdx.audio.newSound(Gdx.files.internal("Alarm.wav"))
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, floatArrayOf(bgColor.r, bgColor.g, bgColor.b, bgColor.a))
        mapErangel = Texture(Gdx.files.internal("maps/Erangel_Minimap.png"), null, true).apply {
            setFilter(MipMap, Linear)
            Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER.toFloat())
            Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER.toFloat())
        }
        mapMiramar = Texture(Gdx.files.internal("maps/Miramar_Minimap.png"), null, true).apply {
            setFilter(MipMap, Linear)
            Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER.toFloat())
            Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER.toFloat())
        }
        mapSavage = Texture(Gdx.files.internal("maps/Savage_Minimap.png"), null, true).apply {
            setFilter(MipMap, Linear)
            Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER.toFloat())
            Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER.toFloat())
        }
        map = mapErangel
        fbo = FrameBuffer(RGBA8888, miniMapWindowWidth.toInt(), miniMapWindowWidth.toInt(), false)
        miniMap = TextureRegion(fbo.colorBufferTexture)

        hubpanel = Texture(Gdx.files.internal("images/hub_panel.png"))
        menu = Texture(Gdx.files.internal("images/menu.png"))
        bgcompass = Texture(Gdx.files.internal("images/bg_compass.png"))
        arrow = Texture(Gdx.files.internal("images/arrow.png"))
        player = Texture(Gdx.files.internal("images/player.png"))
        playersight = Texture(Gdx.files.internal("images/green_view_line.png"))
        teamsight = Texture(Gdx.files.internal("images/teamsight.png"))
        arrowsight = Texture(Gdx.files.internal("images/red_view_line.png"))
        teamarrow = Texture(Gdx.files.internal("images/team.png"))
        parachute = Texture(Gdx.files.internal("images/parachute.png"))

        parachute = Texture(Gdx.files.internal("images/parachute.png"))

        itemAtlas = TextureAtlas(Gdx.files.internal("icons/itemIcons.txt"))
        for (region in itemAtlas.regions)
            itemIcons[region.name] = region.apply { flip(false, true) }


        crateAtlas = TextureAtlas(Gdx.files.internal("icons/crateIcons.txt"))
        for (region in crateAtlas.regions)
            crateIcons[region.name] = region.apply { flip(false, true) }

        pawnAtlas = TextureAtlas(Gdx.files.internal("icons/APawnIcons.txt"))
        for (region in pawnAtlas.regions)
            region.flip(false, true)

        carePackage = pawnAtlas.findRegion("CarePackage")
        corpseIcon = pawnAtlas.findRegion("corpse")
        redzoneBombIcon = pawnAtlas.findRegion("redzoneBomb")
        vehicleIcons = mapOf(
                TwoSeatBoat to pawnAtlas.findRegion("AquaRail"),
                SixSeatBoat to pawnAtlas.findRegion("boat"),
                Dacia to pawnAtlas.findRegion("dacia"),
                Uaz to pawnAtlas.findRegion("uaz"),
                Pickup to pawnAtlas.findRegion("pickup"),
                Buggy to pawnAtlas.findRegion("buggy"),
                Bike to pawnAtlas.findRegion("bike"),
                SideCar to pawnAtlas.findRegion("bike"),
                Bus to pawnAtlas.findRegion("bus"),
                Plane to pawnAtlas.findRegion("plane")
        )
        grenadeIcons = mapOf(
                SmokeBomb to pawnAtlas.findRegion("smokebomb"),
                Molotov to pawnAtlas.findRegion("molotov"),
                Grenade to pawnAtlas.findRegion("fragbomb"),
                FlashBang to pawnAtlas.findRegion("flashbang")
        )


        markerAtlas = TextureAtlas(Gdx.files.internal("icons/Markers.txt"))
        for (region in markerAtlas.regions)
            region.flip(false, true)


        markers = arrayOf(
                markerAtlas.findRegion("marker1"), markerAtlas.findRegion("marker2"),
                markerAtlas.findRegion("marker3"), markerAtlas.findRegion("marker4"),
                markerAtlas.findRegion("marker5"), markerAtlas.findRegion("marker6"),
                markerAtlas.findRegion("marker7"), markerAtlas.findRegion("marker8"),
                markerAtlas.findRegion("marker8"), markerAtlas.findRegion("marker8")
        )

        val generatorHub = FreeTypeFontGenerator(Gdx.files.internal("font/AGENCYFB.TTF"))
        val paramHub = FreeTypeFontParameter()
        paramHub.characters = DEFAULT_CHARS
        paramHub.size = jsettings.hubFont_size
        paramHub.color = jsettings.hubFont_color
        hubFont = generatorHub.generateFont(paramHub)
        paramHub.color = jsettings.hubFontShadow_color
        hubFontShadow = generatorHub.generateFont(paramHub)
        paramHub.size = jsettings.espFont_size
        paramHub.color = jsettings.espFont_color
        espFont = generatorHub.generateFont(paramHub)
        paramHub.color = jsettings.espFontShadow_color
        espFontShadow = generatorHub.generateFont(paramHub)
        val generatorNumber = FreeTypeFontGenerator(Gdx.files.internal("font/NUMBER.TTF"))
        val paramNumber = FreeTypeFontParameter()
        paramNumber.characters = DEFAULT_CHARS
        paramNumber.size = jsettings.largeFont_size
        paramNumber.color = jsettings.largeFont_color
        largeFont = generatorNumber.generateFont(paramNumber)
        val generator = FreeTypeFontGenerator(Gdx.files.internal("font/GOTHICB.TTF"))
        val param = FreeTypeFontParameter()
        param.characters = DEFAULT_CHARS
        param.size = jsettings.largeFont_size2
        param.color = jsettings.largeFont_color2
        largeFont = generator.generateFont(param)
        param.size = jsettings.littleFont_size
        param.color = jsettings.littleFont_color
        littleFont = generator.generateFont(param)
        param.color = jsettings.itemFont_color
        param.size = jsettings.itemFont_size
        itemFont = generator.generateFont(param)
        param.color = jsettings.compaseFont_color
        param.size = jsettings.compaseFont_size
        compaseFont = generator.generateFont(param)
        param.color = jsettings.compaseFontShadow_color
        compaseFontShadow = generator.generateFont(param)
        param.characters = DEFAULT_CHARS
        param.size = jsettings.littleFont_size2
        param.color = jsettings.littleFont_color2
        littleFont = generator.generateFont(param)
        param.color = jsettings.littleFontShadow_color
        littleFontShadow = generator.generateFont(param)
        param.color = jsettings.menuFont_color
        param.size = jsettings.menuFont_size
        menuFont = generator.generateFont(param)
        param.color = jsettings.menuFontOn_color
        param.size = jsettings.menuFontOn_size
        menuFontOn = generator.generateFont(param)
        param.color = jsettings.menuFontOFF_color
        param.size = jsettings.menuFontOFF_size
        menuFontOFF = generator.generateFont(param)
        param.color = jsettings.hporange_color
        param.size = jsettings.hporange_size
        hporange = generator.generateFont(param)
        param.borderColor = Color.BLACK
        param.borderWidth = 0.5f
        param.color = jsettings.hpgreen_color
        param.size = jsettings.hpgreen_size
        param.borderColor = Color.BLACK
        param.borderWidth = 0.5f
        hpgreen = generator.generateFont(param)
        param.color = jsettings.hpred_color
        param.size = jsettings.hpred_size
        param.borderColor = Color.BLACK
        param.borderWidth = 0.5f
        hpred = generator.generateFont(param)
        param.color = jsettings.nameFont_color
        param.size = jsettings.nameFont_size
        param.borderColor = Color.BLACK
        param.borderWidth = 0.5f
        nameFont = generator.generateFont(param)
        param.characters = DEFAULT_CHARS
        param.color = Color(0.3f, 0.9f, 1f, 1f)
        param.borderColor = Color.BLACK
        param.borderWidth = 0.5f
        param.size = jsettings.nameFont_size
        nameBlueFont = generator.generateFont(param)
        param.color = Color(0f, 0f, 0f, 0.5f)
        param.size = jsettings.nameFont_size
        param.borderColor = Color.BLACK
        param.borderWidth = 0.5f
        nameFontShadow = generator.generateFont(param)
        param.color = Color(1f, 0.8f, 1f, 1f)
        param.size = jsettings.nameFont_size
        param.borderColor = Color.BLACK
        param.borderWidth = 0.5f
        nameGoldFont = generator.generateFont(param)
        generatorHub.dispose()
        generatorNumber.dispose()
        generator.dispose()

    }

    private val dirUnitVector = Vector2(1f, 0f)

    override fun render() {
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        if (gameStarted) {
            when(isErangel)
            {
                1   ->{
                    map = mapErangel
                }

                2   ->{
                    map = mapMiramar
                }

                3   ->{
                    map = mapSavage
                }
            }
            // map = if (isErangel) mapErangel else mapMiramar
        }
        else
            return
        actors[selfID]?.apply {
            actors[attachParent ?: return@apply]?.apply {
                selfCoords.set(location.x, location.y, location.z)
                selfDirection = rotation.y
            }
        }
        val (selfX, selfY) = selfCoords


        var diff = 0f


        //move camera
        mapCamera.position.set(selfCoords.x + screenOffsetX, selfCoords.y + screenOffsetY, 0f)
        minimap.position.set(selfCoords.x + screenOffsetX, selfCoords.y + screenOffsetY, 0f)

        mapCamera.update()
        minimap.update()
        if (filterEnableRotate == 1){
            if (preSelf != selfDirection) {
                //mapCamera.rotate(-tempr)
                //90' N
                //tempr = 450 - selfDirection
                tempr = 90 + selfDirection
                if (tempr < 0) tempr += 360f
                if (tempr >= 360) tempr -= 360f
                diff = tempr - totRot
                tempr = diff
                totRot += diff
                if (totRot < 0) totRot += 360f
                if (totRot >= 360) totRot -= 360f
                // println("1.self:"+ selfDirection+" totDirect:"+totRot +"diff:"+tempr)
                preSelf = selfDirection
                mapCamera.rotate(tempr)
            } else {
                diff = 0f
                tempr = 0f
            }
        }
        else{
            miniMapCamera.rotate(-totRot)
            fontCamera.rotate(-totRot)
            mapCamera.rotate(-totRot)
            totRot = 0f
            diff = 0f
            tempr = 0f
        }
        val mapRegion = Rectangle().apply {
            setPosition(windowToMap(0f, 0f))
            width = windowToMap(windowWidth)
            height = windowToMap(windowHeight)
        }
        val miniMapRegion = Rectangle().apply {
            x = selfCoords.x - miniMapRadius
            y = selfCoords.y - miniMapRadius
            width = miniMapRadius * 2
            height = miniMapRadius * 2
        }

        var parachutes: ArrayList<renderInfo>? = null
        var players: ArrayList<renderInfo>? = null
        var vehicles: ArrayList<renderInfo>? = null
        var grenades: ArrayList<renderInfo>? = null

        for ((_, actor) in visualActors) {
            val (x, y, z) = actor.location
            if (!mapRegion.contains(x, y) && !miniMapRegion.contains(x, y)) continue
            //val visualActor = tuple4(actor, x, y, actor.rotation.y)
            val visualActor = tuple5(actor, x, y, z, actor.rotation.y)
            val list = when (actor.type) {
                Parachute -> {
                    parachutes = parachutes ?: ArrayList()
                    parachutes
                }
                Player -> {
                    players = players ?: ArrayList()
                    players
                }
                TwoSeatBoat, SixSeatBoat, Dacia, Uaz, Pickup, Buggy,
                Bike, SideCar, Bus, Plane -> {
                    vehicles = vehicles ?: ArrayList()
                    actor as Vehicle
                    actor.apply {
                        var driver: Actor? = null
                        for (child in attachChildren) {
                            driver = actors[child] ?: continue
                            break
                        }
                        if (driver == null && driverPlayerState.isValid()) {
                            val driverID = playerStateToActor[driverPlayerState]
                            driver = if (driverID != null) actors[driverID] else null
                        }
                        if (driver == null) return@apply
                        val _players = players ?: ArrayList()
                        _players.add(visualActor.copy(_1 = driver))
                        players = _players
                    }
                    vehicles
                }
                SmokeBomb, Molotov, Grenade, FlashBang -> {
                    grenades = grenades ?: ArrayList()
                    grenades
                }
                else -> null
            }
            list?.add(visualActor)
        }
        clipBound.set(mapRegion)
        camera = mapCamera
        camera.update()
        minimap.update()
        //draw map
        paint(camera.combined) {
            draw(
                    map, 0f, 0f, mapWidth, mapWidth,
                    0, 0, map.width, map.height,
                    false, true
            )
            drawRedZoneBomb()
            drawMapMarkers()
            drawVehicles(vehicles)
            drawCorpse()
            drawItem()
            drawGrenades(grenades)
            drawAirDrop()
        }




        fontCamera.rotate(tempr)
        fontCamera.update()
        val numKills = playerNumKills[selfStateID] ?: 0
        val zero = numKills.toString()
        val numeyes = spectatedCount[selfID] ?: 0
        val eyes = numeyes.toString()
        paint(fontCamera.combined) {
            if (drawcompass == 1)
            {

                spriteBatch.draw(bgcompass, windowWidth / 2 - 168f, windowHeight / 2 - 168f)

                layout.setText(compaseFont, "0")
                compaseFont.draw(spriteBatch, "0", windowWidth / 2 - layout.width / 2, windowHeight / 2 + layout.height + 150)                  // N
                layout.setText(compaseFont, "45")
                compaseFont.draw(spriteBatch, "45", windowWidth / 2 - layout.width / 2 + 104, windowHeight / 2 + layout.height / 2 + 104)          // NE
                layout.setText(compaseFont, "90")
                compaseFont.draw(spriteBatch, "90", windowWidth / 2 - layout.width / 2 + 147, windowHeight / 2 + layout.height / 2)                // E
                layout.setText(compaseFont, "135")
                compaseFont.draw(spriteBatch, "135", windowWidth / 2 - layout.width / 2 + 106, windowHeight / 2 + layout.height / 2 - 106)          // SE
                layout.setText(compaseFont, "180")
                compaseFont.draw(spriteBatch, "180", windowWidth / 2 - layout.width / 2, windowHeight / 2 + layout.height / 2 - 151)                // S
                layout.setText(compaseFont, "225")
                compaseFont.draw(spriteBatch, "225", windowWidth / 2 - layout.width / 2 - 109, windowHeight / 2 + layout.height / 2 - 109)          // SW
                layout.setText(compaseFont, "270")
                compaseFont.draw(spriteBatch, "270", windowWidth / 2 - layout.width / 2 - 153, windowHeight / 2 + layout.height / 2)                // W
                layout.setText(compaseFont, "315")
                compaseFont.draw(spriteBatch, "315", windowWidth / 2 - layout.width / 2 - 106, windowHeight / 2 + layout.height / 2 + 106)          // NW
            }
            //safeZoneHint()
            drawPlayerSprites(parachutes, players)
            drawPlayerInfos(players)
            drawItemText()

        }
        paint(UICamera.combined) {
            val timeHints = if (RemainingTime > 0) "${RemainingTime}s"
            else "${MatchElapsedMinutes}min"

            // NUMBER PANEL
            val numText = "$NumAlivePlayers"
            layout.setText(hubFont, numText)
            spriteBatch.draw(hubpanel, windowWidth - 130f, windowHeight - 60f)
            hubFontShadow.draw(spriteBatch, "ALIVE", windowWidth - 85f, windowHeight - 29f)
            hubFont.draw(spriteBatch, "$NumAlivePlayers", windowWidth - 110f - layout.width / 2, windowHeight - 29f)
            val teamText = "$NumAliveTeams"


            if (isTeamMatch) {
                layout.setText(hubFont, teamText)
                spriteBatch.draw(hubpanel, windowWidth - 260f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "TEAM", windowWidth - 215f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$NumAliveTeams", windowWidth - 240f - layout.width / 2, windowHeight - 29f)
            }
            if (isTeamMatch) {

                layout.setText(hubFont, zero)
                spriteBatch.draw(hubpanel, windowWidth - 390f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "KILLS", windowWidth - 345f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$zero", windowWidth - 370f - layout.width / 2, windowHeight - 29f)
            } else {
                spriteBatch.draw(hubpanel, windowWidth - 390f + 130f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "KILLS", windowWidth - 345f + 128f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$zero", windowWidth - 370f + 128f - layout.width / 2, windowHeight - 29f)

            }

            if (isTeamMatch) {
                layout.setText(hubFont, eyes)
                spriteBatch.draw(hubpanel, windowWidth - 520f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "EYES", windowWidth - 475, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$eyes", windowWidth - 500 - layout.width / 2, windowHeight - 29f)
            } else {
                spriteBatch.draw(hubpanel, windowWidth - 520f + 130f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "EYES", windowWidth - 475 + 128f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$eyes", windowWidth - 500 + 128f - layout.width / 2, windowHeight - 29f)

            }
            val timeText = "${TotalWarningDuration.toInt() - ElapsedWarningDuration.toInt()}"

            layout.setText(hubFont, timeText)

            var offset = 0f
            if (isTeamMatch) {
                spriteBatch.draw(hubpanel, windowWidth - 650 , windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "SECS", windowWidth - 605 , windowHeight - 29f)
                hubFont.draw(spriteBatch, "$timeText", windowWidth - 630  - layout.width / 2, windowHeight - 29f)
            }else {
                spriteBatch.draw(hubpanel, windowWidth - 650 + 130f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "SECS", windowWidth - 605 + 128f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$timeText", windowWidth - 630 + 128f - layout.width / 2, windowHeight - 29f)
            }
            // ITEM ESP FILTER PANEL
            //  spriteBatch.draw(hubpanelblank, 30f, windowHeight - 60f)

            // This is what you were trying to do
            if (filterEnableItem == 1)
                espFont.draw(spriteBatch, "DrawAll", 40f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "DrawAll", 39f, windowHeight - 25f)

            if (filterEnableRotate == 1)
                espFont.draw(spriteBatch, "RORATE", 40f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "RORATE", 40f, windowHeight - 42f)

            if (filterLvl2 != 0)
                espFont.draw(spriteBatch, "EQUIP", 100f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "EQUIP", 100f, windowHeight - 25f)

            if (filterScope == 1)
                espFont.draw(spriteBatch, "SCOPE", 98f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "SCOPE", 98f, windowHeight - 42f)
            //monoeyeT
            if (filterHeals == 1)
                espFont.draw(spriteBatch, "MEDS", 150f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "MEDS", 150f, windowHeight - 25f)

            if (filterAmmo == 1)
                espFont.draw(spriteBatch, "AMMO", 150f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "AMMO", 150f, windowHeight - 42f)
            if (drawcompass == 1)
                espFont.draw(spriteBatch, "COMPASS", 200f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "COMPASS", 200f, windowHeight - 42f)
            if (drawgrid == 1)
                espFont.draw(spriteBatch, "GRID", 270f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "GRID", 270f, windowHeight - 42f)
            if (filterThrow == 1)
                espFont.draw(spriteBatch, "THROW", 200f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "THROW", 200f, windowHeight - 25f)

            if (drawmenu == 1)
                espFont.draw(spriteBatch, "[" + jsettings.drawmenu_Key + "] Menu ON", 270f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "[" + jsettings.drawmenu_Key + "] Menu OFF", 270f, windowHeight - 25f)

            val num = nameToggles
            espFontShadow.draw(spriteBatch, "[" + jsettings.nameToogle_Key + "] Player Info: $num", 270f, windowHeight - 42f)

            val znum = ZoomToggles
            espFontShadow.draw(spriteBatch, "[" + jsettings.ZoomToggles_Key + "] Zoom Toggle: $znum", 40f, windowHeight - 68f)

            val vnum = VehicleInfoToggles
            espFontShadow.draw(spriteBatch, "[" + jsettings.VehicleInfoToggles_Key + "] Vehicle Toggles: $vnum", 40f, windowHeight - 85f)


            val pinDistance = (pinLocation.cpy().sub(selfCoords.x, selfCoords.y).len() / 100).toInt()
            val (x, y) = pinLocation.mapToWindow()


            //safeZoneHint()


            val camnum = camera.zoom

            if (drawmenu == 1) {
                spriteBatch.draw(menu, 20f, windowHeight / 2 - 200f)

                //
                menuFont.draw(spriteBatch, jsettings.enableAll_Key, 120f, windowHeight / 2 + 121f)
                menuFont.draw(spriteBatch, jsettings.enableRoate_Key, 120f, windowHeight / 2 + 103f)
                menuFont.draw(spriteBatch, jsettings.filterPrintLOC, 120f, windowHeight / 2 + 85f)
                menuFont.draw(spriteBatch, jsettings.filterThrow_Key, 120f, windowHeight / 2 + 67f)
                menuFont.draw(spriteBatch, jsettings.filterAttach_Key, 120f, windowHeight / 2 + 49f)
                menuFont.draw(spriteBatch, jsettings.filterScope_Key, 120f, windowHeight / 2 + 31f)
                menuFont.draw(spriteBatch, jsettings.filterAmmo_Key, 120f, windowHeight / 2 + 13f)

                // Filters
                if (filterEnableItem == 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 121f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 121f)

                if (filterEnableRotate == 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 103f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 103f)

                if (filterLvl2 == 2)
                    menuFontOn.draw(spriteBatch, "Level 2", 187f, windowHeight / 2 + 103f)

                if (filterLvl2 == 3)
                    menuFontOn.draw(spriteBatch, "Level 3", 187f, windowHeight / 2 + 103f)

                if (filterLvl2 == 4)
                    menuFontOn.draw(spriteBatch, "Level 2/3", 187f, windowHeight / 2 + 103f)


                if (filterLvl2 == 0)
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 103f)

                if (filterHeals == 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 85f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 85f)

                if (filterThrow == 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 67f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 67f)

                if (filterLvl2 == 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 49f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 49f)

                if (filterScope == 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 31f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 31f)

                if (filterAmmo == 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 13f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 13f)



                menuFont.draw(spriteBatch, jsettings.camera_zoom_Plus_Key, 120f, windowHeight / 2 - 27f)
                menuFont.draw(spriteBatch, jsettings.camera_zoom_Minus_Key, 120f, windowHeight / 2 - 45f)

                val camvalue = camera.zoom
                when
                {
                    camvalue <= 0.0100f -> menuFontOFF.draw(spriteBatch, "Max Zoom", 187f, windowHeight / 2 + -27f)
                    camvalue >= 1f -> menuFontOFF.draw(spriteBatch, "Min Zoom", 187f, windowHeight / 2 + -27f)
                    camvalue == 0.2500f -> menuFont.draw(spriteBatch, "Default", 187f, windowHeight / 2 + -27f)
                    camvalue == 0.1250f -> menuFont.draw(spriteBatch, "Scouting", 187f, windowHeight / 2 + -27f)
                    camvalue >= 0.0833f -> menuFont.draw(spriteBatch, "Combat", 187f, windowHeight / 2 + -27f)
                    camvalue <= 0.0417f -> menuFont.draw(spriteBatch, "Looting", 187f, windowHeight / 2 + -27f)

                    else -> menuFont.draw(spriteBatch, ("%.4f").format(camnum), 187f, windowHeight / 2 + -27f)
                }
                menuFont.draw(spriteBatch, ("%.4f").format(camnum), 257f, windowHeight / 2 + -27f)
                menuFont.draw(spriteBatch, jsettings.nameToogle_Key, 120f, windowHeight / 2 - 71f)
                menuFont.draw(spriteBatch, jsettings.drawcompass_Key, 120f, windowHeight / 2 - 89f)
                menuFont.draw(spriteBatch, jsettings.drawDaMap_Key, 120f, windowHeight / 2 - 107f)
                menuFont.draw(spriteBatch, jsettings.toggleView_Key, 120f, windowHeight / 2 - 125f)
                menuFont.draw(spriteBatch, jsettings.VehicleInfoToggles_Key, 120f, windowHeight / 2 - 143f)
                menuFont.draw(spriteBatch, jsettings.drawmenu_Key, 120f, windowHeight / 2 - 161f)
                menuFont.draw(spriteBatch, jsettings.drawgrid_Key, 120f, windowHeight / 2 - 182f)
                // Name Toggles
                val togs = nameToggles
                if (nameToggles >= 1)

                    menuFontOn.draw(spriteBatch, "Enabled: $togs", 187f, windowHeight / 2 + -71f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -71f)


                // Compass
                if (drawcompass != 1)
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -89f)
                else
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -89f)


                if (drawDaMap == 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -107f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -107f)


                if (toggleView == 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -125f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -125f)


                if (VehicleInfoToggles < 3)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -143f)
                if (VehicleInfoToggles == 3)
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -143f)

                // DrawMenu == 1 already
                menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -161f)
                // Compass
                if (drawgrid != 1)
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -182f)
                else
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -182f)
            }
            // DrawMenu == 0 (Disabled)



            littleFont.draw(spriteBatch, "$pinDistance", x, windowHeight - y)
        }


        Gdx.gl.glEnable(GL20.GL_BLEND)

        shapeRenderer.projectionMatrix = camera.combined
        draw(Line) {
            players?.forEach {
                aimAtMe(it)
            }
            drawCircles()
            drawAttackLine()
            drawAirDropLine()

        }
        if(drawgrid==1) {
            if(isErangel == 3) drawGrid4k()
            else drawGrid8k()
        }
        draw(Filled) {
            color = redZoneColor
            circle(RedZonePosition, RedZoneRadius, 100)

            color = visionColor
            circle(selfX, selfY, visionRadius, 100)

            drawPlayersH(players)

        }
        Gdx.gl.glDisable(GL20.GL_BLEND)
        clipBound.set(miniMapRegion)
        camera = miniMapCamera
        miniMapCamera.rotate(tempr)
        miniMapCamera.update()
        if (drawDaMap == 1)
        {
            drawMiniMap(parachutes, players, vehicles)
        }
    }

    private fun ShapeRenderer.drawPlayersH(players : ArrayList<renderInfo>?)
    {
        //draw self
        // drawAllPlayerHealth(selfColor , tuple4(actors[selfID] ?: return , selfCoords.x , selfCoords.y , selfDirection))
        players?.forEach {
            drawAllPlayerHealth(playerColor, it)

        }
        drawAllPlayerHealth(selfColor, tuple5(actors[selfID] ?: return, selfCoords.x, selfCoords.y,selfCoords.z, selfDirection))
    }

    private fun ShapeRenderer.DrawMyselfH(){

        drawAllPlayerHealth(selfColor , tuple5(actors[selfID] ?: return , selfCoords.x , selfCoords.y , selfCoords.z,selfDirection))
    }

    private fun ShapeRenderer.drawPlayersMini(parachutes : ArrayList<renderInfo>?, players : ArrayList<renderInfo>?)
    {
        parachutes?.forEach {
            drawPlayer(parachuteColor, it)
        }
        //draw self
        drawPlayer(selfColor, tuple5(actors[selfID] ?: return, selfCoords.x, selfCoords.y,selfCoords.z, selfDirection))
        players?.forEach {
            drawPlayer(playerColor, it)
        }
    }


    private fun drawPlayerSprites(parachutes : ArrayList<renderInfo>?, players : ArrayList<renderInfo>?)
    {

        parachutes?.forEach {
            val (actor, x, y, z, dir) = it
            val (sx, sy) = Vector2(x, y).mapToWindow()
            spriteBatch.draw(
                    parachute,
                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 8f, 8f,
                    dir * -1, 0, 0, 128, 128, true, false
            )

            val attach = actor.attachChildren.firstOrNull()
            val teamId = isTeamMate(actor)
            if (teamId > 0)
            {
                // Can't wait for the "Omg Players don't draw issues
                spriteBatch.draw(
                        teamarrow,
                        sx, windowHeight - sy - 2, 4.toFloat() / 2,
                        4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                        dir * -1, 0, 0, 64, 64, true, false
                )

                if (toggleView == 1)
                {
                    spriteBatch.draw(
                            teamsight,
                            sx + 1, windowHeight - sy - 2,
                            2.toFloat() / 2,
                            2.toFloat() / 2,
                            12.toFloat(), 2.toFloat(),
                            10f, 10f,
                            dir * -1, 0, 0, 512, 64, true, false
                    )
                }

            }
        }

        players?.forEach {

            val (actor, x, y, z ,  dir) = it
            val (sx, sy) = Vector2(x, y).mapToWindow()
            val playerStateGUID = actorWithPlayerState[actor.netGUID] ?: return@forEach
            val PlayerState = actors[playerStateGUID] as? PlayerState ?: return@forEach
            val selfStateGUID = actorWithPlayerState[selfID] ?: return@forEach
            val selfState = actors[selfStateGUID] as? PlayerState ?: return@forEach


            // val teamId = isTeamMate(actor)
            //println(teamId)
            // if (teamId > 0) {
            //teamId = isTeamMate(actors[attach])
            if (PlayerState.teamNumber == selfState.teamNumber)
            {
                // Can't wait for the "Omg Players don't draw issues
                spriteBatch.draw(
                        teamarrow,
                        sx, windowHeight - sy - 2, 4.toFloat() / 2,
                        4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                        dir * -1, 0, 0, 64, 64, true, false
                )

                if (toggleView == 1)
                {
                    spriteBatch.draw(
                            teamsight,
                            sx + 1, windowHeight - sy - 2,
                            2.toFloat() / 2,
                            2.toFloat() / 2,
                            12.toFloat(), 2.toFloat(),
                            10f, 10f,
                            dir * -1, 0, 0, 512, 64, true, false
                    )
                }

            }

            if (PlayerState.teamNumber != selfState.teamNumber) {

                spriteBatch.draw(
                        arrow,
                        sx, windowHeight - sy - 2, 4.toFloat() / 2,
                        4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                        dir * -1, 0, 0, 64, 64, true, false
                )

                if (toggleView == 1)
                {
                    spriteBatch.draw(
                            arrowsight,
                            sx + 1, windowHeight - sy - 2,
                            2.toFloat() / 2,
                            2.toFloat() / 2,
                            12.toFloat(), 2.toFloat(),
                            10f, 10f,
                            dir * -1, 0, 0, 512, 64, true, false
                    )
                }
            }

        }
        //draw self
        drawMyself(tuple5(actors[selfID] ?: return, selfCoords.x, selfCoords.y, selfCoords.z, selfDirection))
    }

    private fun drawMyself(actorInfo : renderInfo)
    {
        val (actor, x, y,z, dir) = actorInfo
        val (sx, sy) = Vector2(x, y).mapToWindow()
        if (toggleView == 1)
        {
            // Just draw them both at the same time to avoid player not drawing ¯\_(ツ)_/¯
            spriteBatch.draw(
                    player,
                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                    dir * -1, 0, 0, 64, 64, true, false
            )

            spriteBatch.draw(
                    playersight,
                    sx + 1, windowHeight - sy - 2,
                    2.toFloat() / 2,
                    2.toFloat() / 2,
                    12.toFloat(), 2.toFloat(),
                    10f, 10f,
                    dir * -1, 0, 0, 512, 64, true, false
            )
        }
        else
        {

            spriteBatch.draw(
                    player,
                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                    dir * -1, 0, 0, 64, 64, true, false
            )
        }
    }

    private fun SpriteBatch.drawMapMarkers()
    {
        for (team in teams.values)
        {
            if (team.showMapMarker)
            {
                val icon = markers[team.memberNumber]
                val (x, y) = team.mapMarkerPosition
                draw(icon, x, y, 0f, mapMarkerScale, false)
            }
        }
    }


    fun ShapeRenderer.drawPlayer(pColor : Color?, actorInfo : renderInfo)
    {
        val (actor, x, y, z, dir) = actorInfo
        if (!clipBound.contains(x, y)) return
        val zoom = camera.zoom
        val backgroundRadius = (playerRadius + 2000f) * zoom
        val playerRadius = playerRadius * zoom
        val directionRadius = directionRadius * zoom

        color = BLACK
        circle(x, y, backgroundRadius, 10)

        val attach = actor.attachChildren.firstOrNull()
        val teamId = isTeamMate(actor)
        color = when
        {
            teamId >= 0 -> teamColor[teamId]
            attach == null -> pColor
            attach == selfID -> selfColor
            else             ->
            {
                val teamId = isTeamMate(actors[attach])
                if (teamId >= 0)
                    GREEN//teamColor[teamId]
                else
                    pColor
            }
        }
        if (actor is Character)
            color = when
            {
                actor.isGroggying ->
                {
                    GRAY
                }
                actor.isReviving  ->
                {
                    WHITE
                }
                else -> color
            }
        circle(x, y, playerRadius, 10)

        color = sightColor
        arc(x, y, directionRadius, dir - fov / 2, fov, 10)

        if (actor is Character)
        {//draw health
            val health = if (actor.health <= 0f) actor.groggyHealth else actor.health
            val width = healthBarWidth * zoom
            val height = healthBarHeight * zoom

            val healthWidth = (health / 100.0 * width).toFloat()
            color = when {
                health > 84f -> Color(0.00f, 0.93f, 0.93f, 1f)
                health > 57f -> Color(0.16f, 0.86f, 0.16f, 1f)    // 1 headshot by kar98
                health > 38f -> YELLOW                            // 3 bodyshots
                health > 19f -> ORANGE                            // 2 bodyshots
                health > 0f -> RED                               // 1 bodyshot
                else -> GRAY
            }
            if(filterEnableRotate ==1 ) {
                var cosd = MathUtils.cos(Math.toRadians(totRot.toDouble()).toFloat())
                var sind = MathUtils.sin(Math.toRadians(totRot.toDouble()).toFloat())
                var fx = healthWidth * cosd // offset
                var fy = healthWidth * sind //offset
                val offsetX = (width / 2) * (-1) // 헬스 -좌표 x1
                val offsetY = backgroundRadius + height / 2 // 헬스 y좌표 y1
                var offx = offsetX * cosd - offsetY*sind//position
                var offy = offsetY*cosd + offsetX * sind //position

                rectLine(x + offx, y + offy, x + offx + fx, y + fy + offy, height)
            }
            else
            {
                rectLine(x - width / 2, y, x - width / 2 + healthWidth, y, height)

            }

            //rectLine(x - width / 2, y, x - width / 2 + healthWidth, y, height)


        }
    }


    private fun drawMiniMap(parachutes : ArrayList<renderInfo>?, players : ArrayList<renderInfo>?, vehicles : ArrayList<renderInfo>?)
    {

        fbo.begin()
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        val (selfX, selfY) = selfCoords

        miniMapCamera.apply {
            position.set(selfX, selfY, 0f)
            update()
        }

        camera.zoom = camera.zoom * 2
        spriteBatch.projectionMatrix = miniMapCamera.combined
        paint {
            draw(
                    map, 0f, 0f, mapWidth, mapWidth,
                    0, 0, map.width, map.height,
                    false, true
            )
            drawVehicles(vehicles)
            drawAirDrop()
            drawMapMarkers()
            drawItem()
        }
        shapeRenderer.projectionMatrix = miniMapCamera.combined
        Gdx.gl.glEnable(GL20.GL_BLEND)
        draw(Filled) {
            drawPlayersH(players)
            drawPlayersMini(parachutes, players)
        }
        draw(Line) {
            players?.forEach {
                aimAtMe(it)
            }
            drawCircles()
            drawAttackLine()
            drawAirDropLine()
        }
        Gdx.gl.glDisable(GL20.GL_BLEND)
        camera.zoom = camera.zoom / 2
        fbo.end()

        val miniMapWidth = windowToMap(miniMapWindowWidth)
        val (rx, ry) = windowToMap(windowWidth, windowHeight).sub(miniMapWidth, miniMapWidth)
        spriteBatch.projectionMatrix = minimap.combined

        paint {

            draw(miniMap, rx, ry, miniMapWidth, miniMapWidth)
        }
        shapeRenderer.projectionMatrix = minimap.combined
        Gdx.gl.glLineWidth(2f)
        draw(Line) {
            color = BLACK
            rect(rx, ry, miniMapWidth, miniMapWidth)
        }
        Gdx.gl.glLineWidth(1f)
    }

    private fun SpriteBatch.drawVehicles(vehicles : ArrayList<renderInfo>?)
    {
        vehicles?.forEach { (actor, x, y, z, dir) ->
            if (!clipBound.contains(x, y)) return@forEach
            val icon = vehicleIcons[actor.type] ?: return

            if (actor.type == Plane)
                draw(icon, x, y, dir, planeScale, false)
            else
            {
                val zoom = !(actor as Vehicle).driverPlayerState.isValid()

                val scale = vehicleScale
                draw(icon, x, y, dir, scale, zoom)
            }
        }
    }

    private fun SpriteBatch.drawGrenades(grenades: ArrayList<renderInfo>?) {
        grenades?.forEach { (actor, x, y, z, dir) ->
            if (!clipBound.contains(x, y)) return@forEach
            val icon = grenadeIcons[actor.type] ?: return@forEach
            draw(icon, x, y, dir, grenadeScale, true)
        }
    }


    private fun ShapeRenderer.drawAttackLine()
    {
        val currentTime = System.currentTimeMillis()
        run {
            while (attacks.isNotEmpty())
            {
                val (A, B) = attacks.poll()
                attackLineStartTime.add(Triple(A, B, currentTime))
            }
            if (attackLineStartTime.isEmpty()) return@run
            val iter = attackLineStartTime.iterator()
            while (iter.hasNext())
            {
                val (A, B, st) = iter.next()
                if (A == selfStateID || B == selfStateID)
                {
                    if (A != B)
                    {
                        val otherGUID = playerStateToActor[if (A == selfStateID) B else A]
                        if (otherGUID == null)
                        {
                            iter.remove()
                            continue
                        }
                        val other = actors[otherGUID]
                        if (other == null || currentTime - st > attackMeLineDuration)
                        {
                            iter.remove()
                            continue
                        }
                        color = attackLineColor
                        val (xA, yA) = other.location
                        val (xB, yB) = selfCoords
                        line(xA, yA, xB, yB)
                    }
                }
                else
                {
                    val actorAID = playerStateToActor[A]
                    val actorBID = playerStateToActor[B]
                    if (actorAID == null || actorBID == null)
                    {
                        iter.remove()
                        continue
                    }
                    val actorA = actors[actorAID]
                    val actorB = actors[actorBID]
                    if (actorA == null || actorB == null || currentTime - st > attackLineDuration)
                    {
                        iter.remove()
                        continue
                    }
                    color = attackLineColor
                    val (xA, yA) = actorA.location
                    val (xB, yB) = actorB.location
                    line(xA, yA, xB, yB)
                }
            }
        }
        run {
            while (firing.isNotEmpty())
            {
                val (A, st) = firing.poll()
                actors[A]?.apply {
                    firingStartTime.add(tuple4(location.x, location.y, rotation.y, st))
                }
            }
            if (firingStartTime.isEmpty()) return@run
            val iter = firingStartTime.iterator()
            while (iter.hasNext())
            {
                val (x, y, yaw, st) = iter.next()
                if (currentTime - st > firingLineDuration)
                {
                    iter.remove()
                    continue
                }
                color = firingLineColor
                val (xB, yB) = dirUnitVector.cpy().rotate(yaw).scl(firingLineLength).add(x, y)
                line(x, y, xB, yB)
            }
        }
    }

    private fun ShapeRenderer.drawCircles()
    {
        Gdx.gl.glLineWidth(2f)
        //vision circle
        color = safeZoneColor
        circle(PoisonGasWarningPosition, PoisonGasWarningRadius, 100)

        color = BLUE
        circle(SafetyZonePosition, SafetyZoneRadius, 100)

        if (PoisonGasWarningPosition.len() > 0) {
            color = safeDirectionColor
            line(Vector2(selfCoords.x, selfCoords.y), PoisonGasWarningPosition)
        }
        Gdx.gl.glLineWidth(1f)


    }

    private fun ShapeRenderer.drawAirDropLine()
    {
        airDropLocation.values.forEach {
            val (x, y) = it
            val airdropcoords = (Vector2(x, y))
            color = YELLOW
            line(Vector2(selfCoords.x, selfCoords.y), airdropcoords)
        }
    }

    private fun SpriteBatch.drawCorpse()
    {
        corpseLocation.values.forEach {
            if (airDropLocation.values.contains(it))
            {
                debugln { ("Ignored corpse locations in airdrop locations") }
            }
            else
            {
                val (x, y, z) = it
                if (!clipBound.contains(x, y)) return@forEach
                draw(corpseIcon, x, y, 0f, corpseScale, true)
            }
        }
    }

    private fun SpriteBatch.drawAirDrop()
    {
        airDropLocation.values.forEach {
            if (corpseLocation.contains(it))
            {
                debugln { ("Ignored airdrop locations in corpse locations") }
            }
            else
            {
                val (x, y) = it
                if (!clipBound.contains(x, y)) return@forEach

                draw(carePackage, x, y, -90f, airDropScale, false)
            }
        }
    }

    private fun SpriteBatch.drawRedZoneBomb()
    {
        val currentTime = System.currentTimeMillis()
        val iter = redZoneBombLocation.entries.iterator()
        while (iter.hasNext())
        {
            val (loc, time) = iter.next().value
            val (x, y) = loc
            if (currentTime - time > redzongBombShowDuration)
                iter.remove()
            else if (clipBound.contains(x, y))
                draw(redzoneBombIcon, x, y, 0f, redzoneBombScale, true)
        }
    }

    val itemSetting: Map<String, Boolean> = hashMapOf(
            // Armor Etc
            "Item_Armor_E_01_Lv1_C"	to	jsettings.Level1Armor	,
            "Item_Head_E_01_Lv1_C"	to	jsettings.Level1Head	,
            "Item_Head_E_02_Lv1_C"	to	jsettings.Level1Head1	,
            "Item_Back_E_01_Lv1_C"	to	jsettings.Level1Back	,
            "Item_Back_E_02_Lv1_C"	to	jsettings.Level1Back1	,
            "Item_Armor_D_01_Lv2_C"	to	jsettings.Level2Armor	,
            "Item_Head_F_02_Lv2_C"	to	jsettings.Level2Head	,
            "Item_Head_F_01_Lv2_C"	to	jsettings.Level2Head1	,
            "Item_Back_F_01_Lv2_C"	to	jsettings.Level2Back	,
            "Item_Back_F_02_Lv2_C"	to	jsettings.Level2Back1	,
            "Item_Armor_C_01_Lv3_C"	to	jsettings.Level3Armor	,
            "Item_Head_G_01_Lv3_C"	to	jsettings.Level3Head	,
            "Item_Back_C_02_Lv3_C"	to	jsettings.Level3Back	,
            "Item_Back_C_01_Lv3_C"	to	jsettings.Level3Back1	,

            // Pistols
            "Item_Weapon_G18_C"	to	jsettings.G18	,
            "Item_Weapon_Rhino_C"	to	jsettings.Rhino45	,
            "Item_Weapon_M1911_C"	to	jsettings.M1911	,
            "Item_Weapon_NagantM1895_C"	to	jsettings.R1895	,
            "Item_Weapon_M9_C"	to	jsettings.M9	,
            "Item_Weapon_SawenOff_C"	to	jsettings.SawenOff	,
            "Item_Weapon_FlareGun_C"	to	jsettings.FlareGun	,
            // Meds
            "Item_Heal_Bandage_C"	to	jsettings.Bandage	,
            "Item_Heal_MedKit_C"	to	jsettings.MedKit	,
            "Item_Heal_FirstAid_C"	to	jsettings.FirstAid	,
            "Item_Boost_PainKiller_C"	to	jsettings.PainKiller	,
            "Item_Boost_EnergyDrink_C"	to	jsettings.EnergyDrink	,
            "Item_Boost_AdrenalineSyringe_C"	to	jsettings.Syringe	,
            "Item_JerryCan_C" to jsettings.JerryCan,
            // Attachments
            "Item_Attach_Weapon_Lower_AngledForeGrip_C"	to	jsettings.AngledForegrip	,
            "Item_Attach_Weapon_Lower_Foregrip_C"	to	jsettings.Foregrip	,
            "Item_Attach_Weapon_Magazine_Extended_Large_C"	to	jsettings.ExAR	,
            "Item_Attach_Weapon_Magazine_Extended_Medium_C"	to	jsettings.ExSMG	,
            "Item_Attach_Weapon_Magazine_Extended_SniperRifle_C"	to	jsettings.ExSR	,
            "Item_Attach_Weapon_Magazine_ExtendedQuickDraw_Large_C"	to	jsettings.ExQuickAR	,
            "Item_Attach_Weapon_Magazine_ExtendedQuickDraw_Medium_C"	to	jsettings.ExtQuickSMG	,
            "Item_Attach_Weapon_Magazine_ExtendedQuickDraw_SniperRifle_C"	to	jsettings.QDSnipe	,
            "Item_Attach_Weapon_Muzzle_Compensator_Large_C" to jsettings.CompensatorAR,
            "Item_Attach_Weapon_Muzzle_Compensator_Medium_C"	to	jsettings.CompensatorSMG	,
            "Item_Attach_Weapon_Muzzle_Compensator_SniperRifle_C" to jsettings.CompensatorSR,
            "Item_Attach_Weapon_Muzzle_FlashHider_Large_C"	to	jsettings.FlashHiderAR	,
            "Item_Attach_Weapon_Muzzle_FlashHider_Medium_C"	to	jsettings.FlashHiderSMG	,
            "Item_Attach_Weapon_Muzzle_FlashHider_SniperRifle_C" to jsettings.FlashHiderSR,
            "Item_Attach_Weapon_Muzzle_Suppressor_Large_C"	to	jsettings.SuppressorAR	,
            "Item_Attach_Weapon_Muzzle_Suppressor_Medium_C"	to	jsettings.SuppressorSMG	,
            "Item_Attach_Weapon_Muzzle_Suppressor_SniperRifle_C"	to	jsettings.SuppressorSR	,
            "Item_Attach_Weapon_Stock_AR_Composite_C"	to	jsettings.StockAR	,
            "Item_Attach_Weapon_Stock_SniperRifle_CheekPad_C"	to	jsettings.CheekSR	,
            "Item_Attach_Weapon_Stock_SniperRifle_BulletLoops_C"	to	jsettings.LoopsSR	,
            "Item_Attach_Weapon_Upper_PM2_01_C" to jsettings.PM2,
            // "Item_Attach_Weapon_Muzzle_Duckbill_C" to jsettings.Duckbill,
            "Item_Attach_Weapon_Lower_ThumbGrip_C" to jsettings.ThumbGrip,
            "Item_Attach_Weapon_Lower_LightweightForeGrip_C" to jsettings.LightWeightForeGrip,
            "Item_Attach_Weapon_Lower_HalfGrip_C" to jsettings.HalfGrip,
            //sight
            "Item_Attach_Weapon_Upper_Holosight_C"	to	jsettings.Holosight	,
            "Item_Attach_Weapon_Upper_DotSight_01_C"	to	jsettings.DotSight	,
            "Item_Attach_Weapon_Upper_Aimpoint_C"	to	jsettings.Aimpoint	,
            "Item_Attach_Weapon_Upper_CQBSS_C"	to	jsettings.CQBSS	,
            "Item_Attach_Weapon_Upper_ACOG_01_C"	to	jsettings.ACOG	,
            "Item_Attach_Weapon_Upper_Scope3x_C"  to jsettings.SC3x,
            "Item_Attach_Weapon_Upper_Scope6x_C"  to jsettings.SC6x,
            // Decent Weapons
            "Item_Weapon_AK47_C"	to	jsettings.AK47	,
            "Item_Weapon_AUG_C"	to	jsettings.AUG	,
            "Item_Weapon_AWM_C"	to	jsettings.AWM	,
            "Item_Weapon_Berreta686_C"	to	jsettings.Berreta686	,
            "Item_Weapon_DP28_C"	to	jsettings.DP28	,
            "Item_Weapon_Groza_C"	to	jsettings.Groza	,
            "Item_Weapon_HK416_C"	to	jsettings.HK416	,
            "Item_Weapon_Kar98k_C"	to	jsettings.Kar98k	,
            "Item_Weapon_M16A4_C"	to	jsettings.M16A4	,
            "Item_Weapon_M24_C"	to	jsettings.M24	,
            "Item_Weapon_M249_C"	to	jsettings.M249	,
            "Item_Weapon_Mini14_C"	to	jsettings.Mini14	,
            "Item_Weapon_Mk14_C"	to	jsettings.MK14	,
            "Item_Weapon_Saiga12_C"	to	jsettings.Saiga12	,
            "Item_Weapon_SCAR-L_C"	to	jsettings.SCARL	,
            "Item_Weapon_SKS_C"	to	jsettings.SKS	,
            "Item_Weapon_Thompson_C"	to	jsettings.Thompson	,
            "Item_Weapon_UMP_C"	to	jsettings.UMP	,
            "Item_Weapon_UZI_C"	to	jsettings.UZI	,
            "Item_Weapon_Vector_C"	to	jsettings.Vector	,
            "Item_Weapon_VSS_C"	to	jsettings.VSS	,
            "Item_Weapon_Win94_C"	to	jsettings.Win94	,
            "Item_Weapon_Winchester_C"	to	jsettings.Winchester	,
            "Item_Weapon_FNFal_C"	to	jsettings.FNFal ,
            "Item_Weapon_Pan_C"	to	jsettings.Weapon_Pan	,

            "Item_Ammo_12Guage_C"	to	jsettings.Ammo_12Guage,
            "Item_Ammo_300Magnum_C"	to	jsettings.Ammo_300Magnum	,
            "Item_Ammo_45ACP_C"	to	jsettings.Ammo_45ACP	,
            "Item_Ammo_556mm_C"	to	jsettings.Ammo_556mm	,
            "Item_Ammo_762mm_C"	to	jsettings.Ammo_762mm	,
            "Item_Ammo_9mm_C"	to	jsettings.Ammo_9mm	,
            "Item_Ammo_Flare_C"	to	jsettings.Ammo_Flare	,

            "Item_Weapon_Grenade_C" to	jsettings.Grenade,
            "Item_Weapon_FlashBang_C" to	jsettings.FlashBang,
            "Item_Weapon_SmokeBomb_C" to	jsettings.SmokeBomb,
            "Item_Weapon_Molotov_C" to	jsettings.Molotov,
            "Item_Ghillie_01_C" to jsettings.Ghillie,
            "Item_Ghillie_02_C" to jsettings.Ghillie

    )

    private fun SpriteBatch.drawItem()
    {
        val sorted = ArrayList(droppedItemLocation.values)
        sorted.sortBy {
            order[it._2]
        }
        var strtemp = ""
        sorted.forEach{
            if (it._3 && camera.zoom > itemZoomThreshold) return@forEach
            val (x, y, itemHeight) = it._1
            val items = it._2
            val icon = itemIcons[items] !!
            val scale = if (it._3) itemScale else staticItemScale
            val (sx , sy) = mapToWindow(x , y)
            if(itemSetting.contains(items) == false) println(items+"Setting is NULL")
            if((filterEnableItem ==1)|| itemSetting.contains(items)&&itemSetting[items]==true) {

                when {
                    itemHeight*100 > (selfCoords.z + 200)-> strtemp = "∧"
                    itemHeight*100 < (selfCoords.z - 100) -> strtemp = "∨"
                    else -> strtemp = "="

                }
                //println(items +" $itemHeight "+strtemp)

                if (items in crateIcons) {

                    val adt = crateIcons[items]!!
                    draw(adt, x + 50, y, totRot, airDropTextScale, it._3)

                } else {

                    draw(icon, x, y, totRot, scale, it._3)

                }
                //itemFont.draw(spriteBatch,strtemp,x+50,y)
                //printText(spriteBatch,itemFont,x,y,totRot,strtemp)
            }
        }
    }
    private fun drawItemText()
    {
        val sorted = ArrayList(droppedItemLocation.values)
        sorted.sortBy {
            order[it._2]
        }
        sorted.forEach{
            if (it._3 && camera.zoom > itemZoomThreshold) return@forEach
            val (x, y, itemHeight) = it._1
            val items = it._2
            val (sx , sy) = mapToWindow(x , y)
            if(itemSetting.contains(items) == false) println(items+"Setting is NULL")
            if((filterEnableItem ==1)|| itemSetting.contains(items)&&itemSetting[items]==true) {
                val strtemp =
                        when {
                            itemHeight > (selfCoords.z + 200)-> "^"
                            itemHeight < (selfCoords.z - 100) -> "v"
                            else -> "o"

                        }
                printText(spriteBatch,itemFont,sx, windowHeight-sy, -totRot,strtemp)
            }
        }
    }
    fun checkPlayerLOC(players : MutableList<renderInfo>?) {

        players?.forEach {
            val (actor, x, y, z,dir) = it
            println("s,o = ${selfCoords.x},${selfCoords.y},${selfCoords.z}\n ${x},${y},${z}")
        }
    }
    fun drawPlayerInfos(players : MutableList<renderInfo>?)
    {
        var cosd = MathUtils.cos(Math.toRadians((360-totRot).toDouble()).toFloat())
        var sind = MathUtils.sin(Math.toRadians((360-totRot).toDouble()).toFloat())
        var fx = 20 * cosd - 20*sind
        var fy = 20 * cosd + 20*sind

        players?.forEach {
            val (actor , x , y , z, dir1) = it
            if (! clipBound.contains(x , y)) return@forEach
            val dir = Vector2(x - selfCoords.x , y - selfCoords.y)
            val distance = (dir.len() / 100).toInt()

            val angle = ((dir.angle() + 90) % 360).toInt()
            val (sx , sy) = mapToWindow(x , y)
            val playerStateGUID = (actor as? Character)?.playerStateID ?: return@forEach
            val playerState = actors[playerStateGUID] as? PlayerState ?: return@forEach
            val name = playerState.name
            val health = actorHealth[actor.netGUID] ?: 100f
            val teamNumber = playerState.teamNumber
            val numKills = playerState.numKills
            val equippedWeapons = actorHasWeapons[actor.netGUID]
            val df = DecimalFormat("###.#")
            var weapon = ""
            var zDiff = ""
//            if(spectatedCount[actor.netGUID] != null &&  spectatedCount[actor.netGUID] != 0 )
//                println("spectated: "+spectatedCount[actor.netGUID] +" N:"+name)
            if (equippedWeapons != null)
            {
                for (w in equippedWeapons)
                {
                    val weap = weapons[w ?: continue] as? Weapon ?: continue
                    val result = weap.typeName.split("_")
                    weapon += "${result[2].substring(4)}-->${weap.currentAmmoInClip}\n"
                }
            }
            var items = ""
            for (element in playerState.equipableItems)
            {
                if (element == null||element._1.isBlank()) continue
                items += "${element._1}->${element._2.toInt()}\n"
            }
            for (element in playerState.castableItems)
            {
                if (element == null||element._1.isBlank()) continue
                items += "${element._1}->${element._2}\n"
            }

            if (name != "")
                query(name, 1000)
            var bIshacker = 0
            var bIsGoodPlayer = 0
            var hackerInfo = " "
            var goodinfo = " "
            if (completedPlayerInfo.containsKey(name)) {
                val info = completedPlayerInfo[name]!!
                if (info.kill_max>7 ||info.killDeathRatio > 2.5f || info.headshotKillRatio > 0.22f||info.longest_kill_max > 250f) {
                    bIsGoodPlayer = 1
                    goodinfo = "\n\n\nH:${(info.headshotKillRatio * 100).d(0)}% M:${(info.kill_max).d(0)} K:${(info.killDeathRatio).d(1)} L:${(info.longest_kill_max).d(1)}"

                    //val hackerInfoa = "  ${(info.headshotKillRatio * 100).d(0)}% ${info.killDeathRatio.d(1)}"
                }
                if (info.kill_max > 15 ||info.killDeathRatio > 10f || info.headshotKillRatio > 0.3f||info.longest_kill_max > 450f) {
                    bIshacker = 1
                    hackerInfo = "\n\n\nH:${(info.headshotKillRatio * 100).d(0)}% M:${(info.kill_max).d(0)} K:${(info.killDeathRatio).d(1)} L:${(info.longest_kill_max).d(1)}"

                }
            }


            when (nameToggles)
            {

                0 ->
                {

                }

                1 ->
                {
                    var str1 = ("$angle°${distance}m\n" +
                            "|N: $name\n" +
                            "|H: \n" +
                            "|K: ($numKills)\nTN.($teamNumber)\n" +
                            "|S: \n" +
                            "|W: $weapon" +
                            "|I: $items")


                    if(filterEnableRotate == 1) {
                        printText(spriteBatch, nameFont, sx + 20, windowHeight - sy + 20, -totRot, str1)
                    }
                    else
                    {
                        nameFont.draw(spriteBatch , str1, sx + 20 , windowHeight - sy + 20)
                    }
                    val healthText = health
                    var strHealth = ("\n\n      "+"${df.format(health)}")
                    if(filterEnableRotate == 1)
                    {
                        when {
                            healthText > 80f -> printText(spriteBatch, hpgreen, sx + 20, windowHeight - sy + 20,- totRot, strHealth)
                            healthText > 33f -> printText(spriteBatch, hporange, sx + 20, windowHeight - sy + 20,- totRot, strHealth)
                            else -> printText(spriteBatch, hpred, sx + 20, windowHeight - sy + 20, -totRot, strHealth)

                        }
                    }
                    else
                    {

                        when {
                            healthText > 80f -> hpgreen.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 9)
                            healthText > 33f -> hporange.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 9)
                            else -> hpred.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 9)
                        }
                    }


                    if (actor is Character) {
                        var fonttemp = hpred
                        var strTemp ="temp"
                        if (actor is Character)
                            when
                            {
                                actor.isGroggying -> {
                                    fonttemp = hpred
                                    strTemp = "DOWNED"
                                }
                                actor.isReviving -> {
                                    fonttemp = hporange
                                    strTemp = "GETTING REVIVED"
                                }
                                else -> {

                                    fonttemp = hpgreen
                                    if(health <= 0)
                                        strTemp = "DEAD"
                                    else
                                        strTemp = "ALIVE"
                                }

                            }
                        var strState = ("\n\n\n\n\n      "+strTemp)
                        if (filterEnableRotate == 1) {
                            printText(spriteBatch, fonttemp, sx + 20, windowHeight - sy + 20, -totRot, strState)
                        } else {
                            fonttemp.draw(spriteBatch, strTemp, sx + 40, windowHeight - sy + -42)
                        }
                    }
                }
                2 ->
                {
                    var str1 ="${distance}m\n" +
                            "|N: $name\n" +
                            "|H: ${df.format(health)}\n" +
                            "|W: $weapon"

                    if(filterEnableRotate == 1) {
                        printText(spriteBatch, nameFont, sx + 20, windowHeight - sy + 20, -totRot, str1)
                    }
                    else
                    {
                        nameFont.draw(spriteBatch , str1, sx + 20 , windowHeight - sy + 20)
                    }

                }
                3 -> {


                    if(z >= (selfCoords.z + 400))
                        zDiff ="^^"
                    else if(z < (selfCoords.z + 400) && z > (selfCoords.z + 200))
                        zDiff ="^"
                    else if(z > (selfCoords.z - 300) && z < v




                    (selfCoords.z - 100))
                        zDiff = "v"
                    else if(z <= (selfCoords.z - 300))
                        zDiff = "vv"
                    else
                        zDiff = "o"

                    //println("s,o = ${selfCoords.x},${selfCoords.y},${selfCoords.z}\n ${x},${y},${z}")
                    //checkPlayerLOC(players)
                    var str1 = "${zDiff}, ${distance}m, ${zDiff}"


                    if (filterEnableRotate == 1) {
                        //printText(spriteBatch, nameFont, sx + 20, windowHeight - sy + 20, -totRot, str1)
                        printText(spriteBatch, nameFont, sx + fx, windowHeight - sy + fy, -totRot, str1)
                    } else {
                        nameFont.draw(spriteBatch, str1, sx + 20, windowHeight - sy + 20)
                    }
                    val healthText = health
                    var strHealth = ("\nH: ${df.format(health)}")
                    if (filterEnableRotate == 1) {
                        when {
                            healthText > 80f -> printText(spriteBatch, hpgreen, sx + fx, windowHeight - sy + fy, -totRot, strHealth)
                            healthText > 33f -> printText(spriteBatch, hporange, sx + fx, windowHeight - sy + fy, -totRot, strHealth)
                            else -> printText(spriteBatch, hpred, sx + fx, windowHeight - sy + fy, -totRot, strHealth)

                        }
                    } else {

                        when {
                            healthText > 80f -> hpgreen.draw(spriteBatch, "\n${df.format(health)}", sx + 20, windowHeight - sy + 20)
                            healthText > 33f -> hporange.draw(spriteBatch, "\n${df.format(health)}", sx + 20, windowHeight - sy + 20)
                            else -> hpred.draw(spriteBatch, "\n${df.format(health)}", sx + 20, windowHeight - sy +20)
                        }
                    }

                    if (actor is Character) {
                        var fonttemp = hpred
                        var strTemp = "temp"
                        if (actor is Character)
                            when {
                                actor.isGroggying -> {
                                    fonttemp = hpred
                                    strTemp = "DOWNED"
                                }
                                actor.isReviving -> {
                                    fonttemp = hporange
                                    strTemp = "GETTING REVIVED"
                                }
                                else -> {
                                    fonttemp = hpgreen
                                    strTemp = "ALIVE"
                                }

                            }
                        var strState = ("\n\nS:" + strTemp)
                        if (filterEnableRotate == 1) {
//                            printText(spriteBatch, fonttemp, sx + 20, windowHeight - sy + 20, -totRot, strState)
                            printText(spriteBatch, fonttemp, sx + fx, windowHeight - sy + fy, -totRot, strState)
                        } else {
                            fonttemp.draw(spriteBatch, strTemp, sx + 20, windowHeight - sy + 20)
                        }


                        if (bIsGoodPlayer ==1) {

                            if (filterEnableRotate == 1) {
                                printText(spriteBatch, nameBlueFont, sx + fx, windowHeight - sy + fy, -totRot, goodinfo)
                            } else {
                                nameBlueFont.draw(spriteBatch, goodinfo, sx + 20, windowHeight - sy + 20)
                            }

                        }
                        if (bIshacker ==1) {

                            if (filterEnableRotate == 1) {
                                printText(spriteBatch, nameGoldFont, sx + fx, windowHeight - sy + fy, -totRot, hackerInfo)
                            } else {
                                nameGoldFont.draw(spriteBatch, hackerInfo, sx + 20, windowHeight - sy + 20)
                            }

                        }

                    }
                }
                4 ->
                {
                    var str1 ="|N: $name\n|D: ${distance}m $angle°\n" +
                            "|H:\n" +
                            "|S:\n" +
                            "|W: $weapon"
                    if(filterEnableRotate == 1) {
                        printText(spriteBatch, nameFont, sx + 20, windowHeight - sy + 20, -totRot, str1)
                    }
                    else
                    {
                        nameFont.draw(spriteBatch , str1, sx + 20 , windowHeight - sy + 20)
                    }
                    // Change color of hp
                    val healthText = health
                    var strHealth = ("\n\n      "+"${df.format(health)}")
                    if(filterEnableRotate == 1)
                    {
                        when {
                            healthText > 80f -> printText(spriteBatch, hpgreen, sx + 20, windowHeight - sy + 20,- totRot, strHealth)
                            healthText > 33f -> printText(spriteBatch, hporange, sx + 20, windowHeight - sy + 20,- totRot, strHealth)
                            else -> printText(spriteBatch, hpred, sx + 20, windowHeight - sy + 20, -totRot, strHealth)

                        }
                    }
                    else
                    {

                        when {
                            healthText > 80f -> hpgreen.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 9)
                            healthText > 33f -> hporange.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 9)
                            else -> hpred.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 9)
                        }
                    }

                    if (actor is Character) {
                        var fonttemp = hpred
                        var strTemp ="temp"
                        if (actor is Character)
                            when
                            {
                                actor.isGroggying -> {
                                    fonttemp = hpred
                                    strTemp = "DOWNED"
                                }
                                actor.isReviving -> {
                                    fonttemp = hporange
                                    strTemp = "GETTING REVIVED"
                                }
                                else -> {
                                    fonttemp = hpgreen
                                    strTemp = "ALIVE"
                                }

                            }
                        var strState = ("\n\n\n      "+strTemp)
                        if (filterEnableRotate == 1) {
                            printText(spriteBatch, fonttemp, sx + 20, windowHeight - sy + 20, -totRot, strState)
                        } else {
                            fonttemp.draw(spriteBatch, strTemp, sx + 40, windowHeight - sy + -42)
                        }
                    }
                }
            }
        }
    }
    private fun drawGrid8k() {
        draw(Filled) {
            var sector =8
            val unit = gridWidth / 8
            val unit2 = unit / 10
            color = GRAY
            //thin grid
            for (i in 0..7)
                for (j in 0..9) {
                    rectLine(0f, i * unit + j * unit2, gridWidth, i * unit + j * unit2, 100f)
                    rectLine(i * unit + j * unit2, 0f, i * unit + j * unit2, gridWidth, 100f)
                }
            color = BLACK
            //thick grid
            for (i in 0..7) {
                rectLine(0f, i * unit, gridWidth, i * unit, 150f)
                rectLine(i * unit, 0f, i * unit, gridWidth, 150f)
            }
        }
    }
    private fun drawGrid4k() {
        draw(Filled) {
            var sector =4
            val unit = gridWidth / 4
            val unit2 = unit / 10
            color = GRAY
            //thin grid
            for (i in 0..3)
                for (j in 0..9) {
                    rectLine(0f, i * unit + j * unit2, gridWidth, i * unit + j * unit2, 100f)
                    rectLine(i * unit + j * unit2, 0f, i * unit + j * unit2, gridWidth, 100f)
                }
            color = BLACK
            //thick grid
            for (i in 0..3) {
                rectLine(0f, i * unit, gridWidth, i * unit, 150f)
                rectLine(i * unit, 0f, i * unit, gridWidth, 150f)
            }
        }
    }
    var lastPlayTime = System.currentTimeMillis()
    fun safeZoneHint()
    {
        if (PoisonGasWarningPosition.len() > 0)
        {
            val dir = PoisonGasWarningPosition.cpy().sub(Vector2(selfCoords.x, selfCoords.y))
            val road = dir.len() - PoisonGasWarningRadius
            if (road > 0)
            {
                val runningTime = (road / runSpeed).toInt()
                val (x, y) = dir.nor().scl(road).add(Vector2(selfCoords.x, selfCoords.y)).mapToWindow()
                littleFont.draw(spriteBatch , "$runningTime" , x , windowHeight - y)
                val remainingTime = (TotalWarningDuration - ElapsedWarningDuration).toInt()
                if (remainingTime == 60&&runningTime > remainingTime)
                {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastPlayTime > 10000)
                    {
                        lastPlayTime = currentTime
                        alarmSound.play()
                    }
                }
            }
        }
    }

    fun SpriteBatch.draw(texture : TextureRegion , x : Float , y : Float , yaw : Float , scale : Float , zoom : Boolean = true)
    {
        val w = texture.regionWidth.toFloat()
        val h = texture.regionHeight.toFloat()
        val scale = if (zoom) scale  else scale * camera.zoom
        draw(
                texture , x - w / 2 ,
                y - h / 2 ,
                w / 2 , h / 2 ,
                w , h ,
                scale , scale ,
                yaw
        )
    }

    inline fun draw(type : ShapeType , draw : ShapeRenderer.() -> Unit)
    {
        shapeRenderer.apply {
            begin(type)
            draw()
            end()
        }
    }

    inline fun paint(matrix : Matrix4? = null , paint : SpriteBatch.() -> Unit)
    {
        spriteBatch.apply {
            if (matrix != null) projectionMatrix = matrix
            begin()
            paint()
            end()
        }
    }

    fun ShapeRenderer.circle(loc : Vector2 , radius : Float , segments : Int)
    {
        circle(loc.x , loc.y , radius , segments)
    }

    fun ShapeRenderer.aimAtMe(it : renderInfo)
    {
        val currentTime = System.currentTimeMillis()
        val (selfX , selfY) = selfCoords
        val zoom = camera.zoom
        //draw aim line
        val (actor , x , y , z, dir) = it
        if (isTeamMate(actor) >= 0) return
        val actorID = actor.netGUID
        val dirVec = dirUnitVector.cpy().rotate(dir)
        val focus = Vector2(selfX - x , selfY - y)
        val distance = focus.len()
        var aim = false
        if (distance < aimLineRange&&distance > aimCircleRadius)
        {
            val aimAngle = focus.angle(dirVec)
            if (aimAngle.absoluteValue < asin(aimCircleRadius / distance) * MathUtils.radiansToDegrees)
            {//aim
                aim = true
                aimStartTime.compute(actorID) { _ , startTime ->
                    if (startTime == null) currentTime
                    else
                    {
                        if (currentTime - startTime > aimTimeThreshold)
                        {
                            color = aimLineColor
                            rectLine(x , y , selfX , selfY , aimLineWidth * zoom)
                        }
                        startTime
                    }
                }
            }
        }
        if (! aim)
            aimStartTime.remove(actorID)
    }

    fun ShapeRenderer.drawAllPlayerHealth(pColor : Color? , actorInfo : renderInfo)
    {
        val (actor , x , y ,z, dir) = actorInfo
        if (! clipBound.contains(x , y)) return
        val zoom = camera.zoom
        val backgroundRadius = (playerRadius + 2000f) * zoom

//        val attach = actor.attachChildren.firstOrNull()
//        val teamId = isTeamMate(actor)
//        color = when {
//            teamId >= 0 -> teamColor[teamId]
//            attach == null -> pColor
//            attach == selfID -> selfColor
//            else -> {
//                val teamId = isTeamMate(actors[attach])
//                if (teamId >= 0)
//                    teamColor[teamId]
//                else
//                    pColor
//            }
//        }
//        if (actor is Character)
//            color = when {
//                actor.isGroggying -> {
//                    GRAY
//                }
//                actor.isReviving -> {
//                    WHITE
//                }
//                else -> color
//            }

        if (actor is Character)
        {//draw health
            var health = if (actor.health <= 0f) actor.groggyHealth else actor.health
            if(health == null)
                health = 0f
            val width = healthBarWidth * zoom
            val height = healthBarHeight * zoom
            //val y = y + backgroundRadius + height / 2
            val healthWidth = (health / 100.0 * width).toFloat()

            color = when {
                health > 84f -> Color(0.00f, 0.93f, 0.93f, 1f)
                health > 57f -> Color(0.16f, 0.86f, 0.16f, 1f)    // 1 headshot by kar98
                health > 38f -> YELLOW                            // 3 bodyshots
                health > 19f -> ORANGE                            // 2 bodyshots
                health > 0f -> RED                               // 1 bodyshot
                else -> GRAY
            }
            if(filterEnableRotate ==1 ) {
                var cosd = MathUtils.cos(Math.toRadians(totRot.toDouble()).toFloat())
                var sind = MathUtils.sin(Math.toRadians(totRot.toDouble()).toFloat())
                var fx = healthWidth * cosd // offset
                var fy = healthWidth * sind //offset
                val offsetX = (width / 2) * (-1) // 헬스 -좌표 x1
                val offsetY = backgroundRadius + height / 2 // 헬스 y좌표 y1
                var offx = offsetX * cosd - offsetY*sind//position
                var offy = offsetY*cosd + offsetX * sind //position
                rectLine(x + offx, y + offy, x + offx + fx, y + fy + offy, height)

                //               rectLine(x + offx, y + offy, x + offx + fx, y + fy + offy, height)

            }
            else
            {
                rectLine(x - width / 2, y, x - width / 2 + healthWidth, y, height)

            }
        }
    }

    private fun isTeamMate(actor : Actor?) : Int
    {
        val teamID = (actor as? Character)?.teamID ?: return - 1
        val team = actors[teamID] as? Team ?: return - 1
        return team.memberNumber
    }

    override fun resize(width : Int , height : Int)
    {
        windowWidth = width.toFloat()
        windowHeight = height.toFloat()
        totRot = 0f
        tempr = 0f
        preSelf = 0f

        // mapCamera.rotate(-totRot)
        // miniMapCamera.rotate(-totRot)
        mapCamera.setToOrtho(true , windowWidth * windowToMapUnit , windowHeight * windowToMapUnit)
        minimap.setToOrtho(true , windowWidth * windowToMapUnit , windowHeight * windowToMapUnit)
        miniMapCamera.setToOrtho(true, miniMapRadius * 2 * 8, miniMapRadius * 2 * 8)
        fontCamera.setToOrtho(false , windowWidth , windowHeight)
        infoCamera.setToOrtho(false , windowWidth , windowHeight)
        UICamera.setToOrtho(false , windowWidth , windowHeight)
        // println("self:"+ selfDirection+" totDirect:"+totRot +"diff:"+tempr)
    }

    override fun pause()
    {
    }

    override fun resume()
    {
    }

    override fun dispose()
    {
        deregister(this)
        alarmSound.dispose()
        largeFont.dispose()
        littleFont.dispose()
        mapErangel.dispose()
        mapMiramar.dispose()
        mapSavage.dispose()
        nameBlueFont.dispose()
        nameFontShadow.dispose()
        carePackage.texture.dispose()
        itemAtlas.dispose()
        crateAtlas.dispose()
        pawnAtlas.dispose()
        spriteBatch.dispose()
        shapeRenderer.dispose()
        fbo.dispose()
    }

}