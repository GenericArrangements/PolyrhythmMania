package polyrhythmmania.world.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import io.github.chrislo27.paintbox.registry.AssetRegistry
import io.github.chrislo27.paintbox.util.gdxutils.disposeQuietly
import net.beadsproject.beads.ugens.SamplePlayer
import polyrhythmmania.PRManiaGame
import polyrhythmmania.PRManiaScreen
import polyrhythmmania.soundsystem.SimpleTimingProvider
import polyrhythmmania.soundsystem.TimingProvider
import polyrhythmmania.engine.Engine
import polyrhythmmania.engine.Event
import polyrhythmmania.engine.input.EventLockInputs
import polyrhythmmania.engine.input.InputType
import polyrhythmmania.engine.tempo.TempoChange
import polyrhythmmania.soundsystem.BeadsMusic
import polyrhythmmania.soundsystem.SoundSystem
import polyrhythmmania.soundsystem.sample.GdxAudioReader
import polyrhythmmania.soundsystem.sample.MusicSamplePlayer
import polyrhythmmania.soundsystem.sample.PlayerLike
import polyrhythmmania.util.DecimalFormats
import polyrhythmmania.world.*
import kotlin.system.measureNanoTime


class TestWorldRenderScreen(main: PRManiaGame) : PRManiaScreen(main) {

    companion object {
        private val music: BeadsMusic = GdxAudioReader.newMusic(Gdx.files.internal("debugetc/Polyrhythm.ogg"))
    }

    val world: World = World()
    val soundSystem: SoundSystem = SoundSystem.createDefaultSoundSystem()
    val timing: TimingProvider = soundSystem
    val engine: Engine = Engine(timing, world, soundSystem)
    val renderer: WorldRenderer by lazy {
        WorldRenderer(world, GBATileset(AssetRegistry["tileset_gba"]))
    }

    private val player: MusicSamplePlayer = music.createPlayer(soundSystem.audioContext).apply {
        this.gain = 0.75f
//        this.loopStartMs = 3725f
        this.loopEndMs = 40928f //33482f
        this.loopType = SamplePlayer.LoopType.LOOP_FORWARDS
        this.prepareStartBuffer()
    }

    init {
        soundSystem.audioContext.out.addInput(player)
        soundSystem.startRealtime()

        engine.tempos.addTempoChange(TempoChange(0f, 129f))
//        engine.tempos.addTempoChange(TempoChange(88f, 148.5f))

        engine.inputter.areInputsLocked = false
        
        addEvents()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val batch = main.batch

        renderer.render(batch, engine)

        super.render(delta)
    }

    private var nanoTime = System.nanoTime()

    override fun renderUpdate() {
        super.renderUpdate()

        if (timing is SimpleTimingProvider) {
            timing.seconds += Gdx.graphics.deltaTime
        }

//        val realtimeMsDelta = (System.nanoTime() - nanoTime) / 1000000.0
//        nanoTime = System.nanoTime()
//        val deltaMs = Gdx.graphics.deltaTime.toDouble() * 1000.0
//        println("$deltaMs \t $realtimeMsDelta \t ${deltaMs - realtimeMsDelta}")

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            Gdx.app.postRunnable {
                val timeToStop = measureNanoTime {
                    this.soundSystem.stopRealtime()
                }
//                println(timeToStop / 1000000.0)
                this.dispose()
                main.screen = TestWorldRenderScreen(main)
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            soundSystem.setPaused(!soundSystem.isPaused)
        }
        
        // Inputs
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            val atSeconds = engine.seconds
            engine.postRunnable {
                engine.inputter.onInput(InputType.DPAD, atSeconds)
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            val atSeconds = engine.seconds
            engine.postRunnable {
                engine.inputter.onInput(InputType.A, atSeconds)
            }
        }

        val camera = renderer.camera
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.position.y += Gdx.graphics.deltaTime * +4f
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.position.y += Gdx.graphics.deltaTime * -4f
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.position.x += Gdx.graphics.deltaTime * +4f
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.position.x += Gdx.graphics.deltaTime * -4f
        }
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            camera.zoom += Gdx.graphics.deltaTime * -1f
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom += Gdx.graphics.deltaTime * +1f
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            camera.setToOrtho(false, 5 * (16f / 9f), 5f)
            camera.zoom = 1f
            camera.position.set(camera.zoom * camera.viewportWidth / 2.0f, camera.zoom * camera.viewportHeight / 2.0f, 0f)
        }
    }

    override fun getDebugString(): String {
        return """player pos: ${DecimalFormats.format("0.000", player.position / 1000f)}
---
${engine.getDebugString()}
---
${renderer.getDebugString()}
"""

    }

    override fun dispose() {
        soundSystem.disposeQuietly()
    }

    private fun addEvents() {
        val events: MutableList<Event> = when (1) {
            0 -> addPr1Patterns()
            1 -> addInputTestPatterns()
            2 -> addTestPatterns()
            else -> addTestPatterns()
        }.toMutableList()

        // FIXME debug
        if (!engine.inputter.areInputsLocked) {
            events.removeIf { e ->
                e is EventRowBlockExtend
            }
        }
        
        engine.addEvents(events)
    }

    private fun addTestPatterns(): List<Event> {
        val events = mutableListOf<Event>()
        events += EventRowBlockSpawn(engine, world.rowA, 0, EntityRowBlock.Type.PISTON_A, 8f)
        events += EventRowBlockSpawn(engine, world.rowA, 4, EntityRowBlock.Type.PISTON_A, 10f)
        events += EventRowBlockSpawn(engine, world.rowA, 8, EntityRowBlock.Type.PLATFORM, 12f, true)

        // Explode test
        events += EventRowBlockSpawn(engine, world.rowDpad, 0, EntityRowBlock.Type.PISTON_DPAD, 5f)
        events += EventRowBlockSpawn(engine, world.rowDpad, 1, EntityRowBlock.Type.PLATFORM, 5.5f)
        events += EventRowBlockSpawn(engine, world.rowDpad, 2, EntityRowBlock.Type.PISTON_DPAD, 6f)
        events += EventRowBlockSpawn(engine, world.rowDpad, 7, EntityRowBlock.Type.PLATFORM, 7f)
        events += EventRowBlockExtend(engine, world.rowDpad, 2, 11f)
        events += EventRowBlockExtend(engine, world.rowDpad, 0, 13f)
        events += EventDeployRod(engine, world.rowDpad, 5f)
        events += EventDeployRod(engine, world.rowDpad, 8f)
        events += EventDeployRod(engine, world.rowDpad, 12f)
        events += EventRowBlockDespawn(engine, world.rowDpad, -1, 23f)

        events += EventDeployRod(engine, world.rowA, 12f - 4)
        events += EventRowBlockExtend(engine, world.rowA, 0, 12f)
        events += EventRowBlockExtend(engine, world.rowA, 4, 14f)
        events += EventRowBlockRetract(engine, world.rowA, -1, 15.5f)

        events += EventDeployRod(engine, world.rowA, 16f - 4)
        events += EventRowBlockExtend(engine, world.rowA, 0, 16f)
//        events += EventRowBlockExtend(engine, world.rowA, 4, 18f)
        events += EventRowBlockRetract(engine, world.rowA, -1, 22f)

        events += EventRowBlockDespawn(engine, world.rowA, -1, 23f)



        events += EventDeployRod(engine, world.rowA, 28f - 4)
        events += EventRowBlockSpawn(engine, world.rowA, 0, EntityRowBlock.Type.PISTON_A, 24f)
        events += EventRowBlockSpawn(engine, world.rowA, 2, EntityRowBlock.Type.PISTON_A, 25f)
        events += EventRowBlockSpawn(engine, world.rowA, 4, EntityRowBlock.Type.PISTON_A, 26f)
        events += EventRowBlockSpawn(engine, world.rowA, 6, EntityRowBlock.Type.PISTON_A, 27f)
        events += EventRowBlockSpawn(engine, world.rowA, 8, EntityRowBlock.Type.PLATFORM, 28f, true)
        events += EventRowBlockExtend(engine, world.rowA, 0, 28f)
        events += EventRowBlockExtend(engine, world.rowA, 2, 29f)
        events += EventRowBlockExtend(engine, world.rowA, 4, 30f)
        events += EventRowBlockExtend(engine, world.rowA, 6, 31f)
        events += EventRowBlockRetract(engine, world.rowA, -1, 31.5f)

        events += EventDeployRod(engine, world.rowA, 32f - 4)
        events += EventRowBlockExtend(engine, world.rowA, 0, 32f)
        events += EventRowBlockExtend(engine, world.rowA, 2, 33f)
        events += EventRowBlockExtend(engine, world.rowA, 4, 34f)
        events += EventRowBlockExtend(engine, world.rowA, 6, 35f)
        events += EventRowBlockRetract(engine, world.rowA, -1, 38f)

        events += EventRowBlockDespawn(engine, world.rowA, -1, 39f)



        events += EventDeployRod(engine, world.rowA, 44f - 4)
        events += EventDeployRod(engine, world.rowDpad, 44f - 4)
        events += EventRowBlockSpawn(engine, world.rowA, 0, EntityRowBlock.Type.PISTON_A, 40f)
        events += EventRowBlockSpawn(engine, world.rowA, 2, EntityRowBlock.Type.PISTON_A, 41f)
        events += EventRowBlockSpawn(engine, world.rowDpad, 0, EntityRowBlock.Type.PLATFORM, 41f)
        events += EventRowBlockSpawn(engine, world.rowDpad, 1, EntityRowBlock.Type.PLATFORM, 41f)
        events += EventRowBlockSpawn(engine, world.rowDpad, 2, EntityRowBlock.Type.PISTON_DPAD, 41f)
        events += EventRowBlockSpawn(engine, world.rowA, 4, EntityRowBlock.Type.PISTON_A, 42f)
        events += EventRowBlockSpawn(engine, world.rowA, 6, EntityRowBlock.Type.PISTON_A, 43f)
        events += EventRowBlockSpawn(engine, world.rowDpad, 6, EntityRowBlock.Type.PISTON_DPAD, 43f)
        events += EventRowBlockSpawn(engine, world.rowA, 8, EntityRowBlock.Type.PLATFORM, 44f, true)
        events += EventRowBlockSpawn(engine, world.rowDpad, 10, EntityRowBlock.Type.PLATFORM, 44f, true)
        events += EventRowBlockExtend(engine, world.rowA, 0, 44f)
        events += EventRowBlockExtend(engine, world.rowA, 2, 45f)
        events += EventRowBlockExtend(engine, world.rowDpad, 2, 45f)
        events += EventRowBlockExtend(engine, world.rowA, 4, 46f)
        events += EventRowBlockExtend(engine, world.rowA, 6, 47f)
        events += EventRowBlockExtend(engine, world.rowDpad, 6, 47f)
        events += EventRowBlockRetract(engine, world.rowA, -1, 47.5f)
        events += EventRowBlockRetract(engine, world.rowDpad, -1, 47.5f)

        events += EventDeployRod(engine, world.rowA, 48f - 4)
        events += EventDeployRod(engine, world.rowDpad, 48f - 4)
        events += EventRowBlockExtend(engine, world.rowA, 0, 48f)
        events += EventRowBlockExtend(engine, world.rowA, 2, 49f)
        events += EventRowBlockExtend(engine, world.rowDpad, 2, 49f)
        events += EventRowBlockExtend(engine, world.rowA, 4, 50f)
        events += EventRowBlockExtend(engine, world.rowA, 6, 51f)
        events += EventRowBlockExtend(engine, world.rowDpad, 6, 51f)
        events += EventRowBlockRetract(engine, world.rowA, -1, 54f)
        events += EventRowBlockRetract(engine, world.rowDpad, -1, 54f)

        events += EventRowBlockDespawn(engine, world.rowA, -1, 55f)
        events += EventRowBlockDespawn(engine, world.rowDpad, -1, 55f)

        return events
    }

    data class Spawn(val index: Int, val type: EntityRowBlock.Type, val beat: Float, val forward: Boolean = false)

    fun addPattern(events: MutableList<Event>, startBeat: Float, rowA: List<Spawn>, rowD: List<Spawn>) {
        fun doIt(row: Row, list: List<Spawn>) {
            list.forEach { s ->
                events += EventRowBlockSpawn(engine, row, s.index, s.type, startBeat + s.beat, s.forward)
                events += EventRowBlockExtend(engine, row, s.index, startBeat + (s.index * 0.5f) + 4f, false)
                events += EventRowBlockExtend(engine, row, s.index, startBeat + (s.index * 0.5f) + 8f, false)
            }

            events += EventLockInputs(engine, false, startBeat + 2f)
            events += EventDeployRod(engine, row, startBeat)
            events += EventDeployRod(engine, row, startBeat + 4)
            events += EventRowBlockRetract(engine, row, -1, startBeat + 7.5f)
            events += EventRowBlockRetract(engine, row, -1, startBeat + 7.85f)
            events += EventLockInputs(engine, true, startBeat + 13.75f)
            events += EventRowBlockRetract(engine, row, -1, startBeat + 14f)
            events += EventRowBlockDespawn(engine, row, -1, startBeat + 15f)
        }

        if (rowA.isNotEmpty()) {
            doIt(world.rowA, rowA)
        }
        if (rowD.isNotEmpty()) {
            doIt(world.rowDpad, rowD)
        }
    }

    private fun addPr1Patterns(): List<Event> {
        val events = mutableListOf<Event>()

        addPattern(events, 0 * 16 + 8f, listOf(
                Spawn(0, EntityRowBlock.Type.PISTON_A, 0f),
                Spawn(4, EntityRowBlock.Type.PISTON_A, 2f),
                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true),
        ), emptyList())
        addPattern(events, 1 * 16 + 8f, listOf(
                Spawn(0, EntityRowBlock.Type.PISTON_A, 0f),
                Spawn(4, EntityRowBlock.Type.PISTON_A, 2f),
                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true)
        ), emptyList())

        addPattern(events, 2 * 16 + 8f, listOf(
                Spawn(0, EntityRowBlock.Type.PISTON_A, 0f),
                Spawn(2, EntityRowBlock.Type.PISTON_A, 1f),
                Spawn(4, EntityRowBlock.Type.PISTON_A, 2f),
                Spawn(6, EntityRowBlock.Type.PISTON_A, 3f),
                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true),
        ), emptyList())
        addPattern(events, 3 * 16 + 8f, listOf(
                Spawn(0, EntityRowBlock.Type.PISTON_A, 0f),
                Spawn(2, EntityRowBlock.Type.PISTON_A, 1f),
                Spawn(4, EntityRowBlock.Type.PISTON_A, 2f),
                Spawn(6, EntityRowBlock.Type.PISTON_A, 3f),
                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true),
        ), emptyList())

        addPattern(events, 4 * 16 + 8f, emptyList(), listOf(
                Spawn(0, EntityRowBlock.Type.PISTON_DPAD, 0f),
                Spawn(2, EntityRowBlock.Type.PISTON_DPAD, 1f),
                Spawn(4, EntityRowBlock.Type.PISTON_DPAD, 2f),
                Spawn(6, EntityRowBlock.Type.PISTON_DPAD, 3f),
                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true),
        ))
        addPattern(events, 5 * 16 + 8f, emptyList(), listOf(
                Spawn(0, EntityRowBlock.Type.PISTON_DPAD, 0f),
                Spawn(2, EntityRowBlock.Type.PISTON_DPAD, 1f),
                Spawn(4, EntityRowBlock.Type.PISTON_DPAD, 2f),
                Spawn(6, EntityRowBlock.Type.PISTON_DPAD, 3f),
                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true),
        ))

        addPattern(events, 6 * 16 + 8f, listOf(
                Spawn(0, EntityRowBlock.Type.PISTON_A, 0f),
                Spawn(4, EntityRowBlock.Type.PISTON_A, 2f),
                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true),
        ), listOf(
                Spawn(0, EntityRowBlock.Type.PLATFORM, 1f),
                Spawn(1, EntityRowBlock.Type.PLATFORM, 1f),
                Spawn(2, EntityRowBlock.Type.PISTON_DPAD, 1f),
                Spawn(6, EntityRowBlock.Type.PISTON_DPAD, 3f),
                Spawn(10, EntityRowBlock.Type.PLATFORM, 5f, true),
        ))
        addPattern(events, 7 * 16 + 8f, listOf(
                Spawn(0, EntityRowBlock.Type.PISTON_A, 0f),
                Spawn(4, EntityRowBlock.Type.PISTON_A, 2f),
                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true),
        ), listOf(
                Spawn(0, EntityRowBlock.Type.PLATFORM, 1f),
                Spawn(1, EntityRowBlock.Type.PLATFORM, 1f),
                Spawn(2, EntityRowBlock.Type.PISTON_DPAD, 1f),
                Spawn(6, EntityRowBlock.Type.PISTON_DPAD, 3f),
                Spawn(10, EntityRowBlock.Type.PLATFORM, 5f, true),
        ))

        addPattern(events, 8 * 16 + 8f, listOf(
                Spawn(0, EntityRowBlock.Type.PISTON_A, 0f),
                Spawn(2, EntityRowBlock.Type.PISTON_A, 1f),
                Spawn(4, EntityRowBlock.Type.PISTON_A, 2f),
                Spawn(6, EntityRowBlock.Type.PISTON_A, 3f),
                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true),
        ), listOf(
                Spawn(0, EntityRowBlock.Type.PISTON_DPAD, 0f),
                Spawn(2, EntityRowBlock.Type.PISTON_DPAD, 1f),
                Spawn(4, EntityRowBlock.Type.PISTON_DPAD, 2f),
                Spawn(6, EntityRowBlock.Type.PISTON_DPAD, 3f),
                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true),
        ))
        addPattern(events, 9 * 16 + 8f, listOf(
                Spawn(0, EntityRowBlock.Type.PISTON_A, 0f),
                Spawn(2, EntityRowBlock.Type.PISTON_A, 1f),
                Spawn(4, EntityRowBlock.Type.PISTON_A, 2f),
                Spawn(6, EntityRowBlock.Type.PISTON_A, 3f),
                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true),
        ), listOf(
                Spawn(0, EntityRowBlock.Type.PISTON_DPAD, 0f),
                Spawn(2, EntityRowBlock.Type.PISTON_DPAD, 1f),
                Spawn(4, EntityRowBlock.Type.PISTON_DPAD, 2f),
                Spawn(6, EntityRowBlock.Type.PISTON_DPAD, 3f),
                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true),
        ))

        addPattern(events, 10 * 16 + 8f, listOf(
                Spawn(0, EntityRowBlock.Type.PISTON_A, 0f),
                Spawn(2, EntityRowBlock.Type.PISTON_A, 1f),
                Spawn(4, EntityRowBlock.Type.PISTON_A, 2f),
                Spawn(6, EntityRowBlock.Type.PISTON_A, 3f),
                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true),
        ), listOf(
                Spawn(0, EntityRowBlock.Type.PLATFORM, 1f),
                Spawn(1, EntityRowBlock.Type.PLATFORM, 1f),
                Spawn(2, EntityRowBlock.Type.PISTON_DPAD, 1f),
                Spawn(6, EntityRowBlock.Type.PISTON_DPAD, 3f),
                Spawn(10, EntityRowBlock.Type.PLATFORM, 5f, true),
        ))
        addPattern(events, 11 * 16 + 8f, listOf(
                Spawn(0, EntityRowBlock.Type.PISTON_A, 0f),
                Spawn(2, EntityRowBlock.Type.PISTON_A, 1f),
                Spawn(4, EntityRowBlock.Type.PISTON_A, 2f),
                Spawn(6, EntityRowBlock.Type.PISTON_A, 3f),
                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true),
        ), listOf(
                Spawn(0, EntityRowBlock.Type.PLATFORM, 1f),
                Spawn(1, EntityRowBlock.Type.PLATFORM, 1f),
                Spawn(2, EntityRowBlock.Type.PISTON_DPAD, 1f),
                Spawn(6, EntityRowBlock.Type.PISTON_DPAD, 3f),
                Spawn(10, EntityRowBlock.Type.PLATFORM, 5f, true),
        ))

        return events
    }

    private fun addInputTestPatterns(): List<Event> {
        val events = mutableListOf<Event>()

        var patternIndex = 0
//        addPattern(events, patternIndex++ * 16 + 8f, listOf(
//                Spawn(0, EntityRowBlock.Type.PISTON_A, 0f),
//                Spawn(4, EntityRowBlock.Type.PISTON_A, 2f),
//                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true),
//        ), listOf(
//                Spawn(0, EntityRowBlock.Type.PLATFORM, 1f),
//                Spawn(1, EntityRowBlock.Type.PLATFORM, 1f),
//                Spawn(2, EntityRowBlock.Type.PISTON_DPAD, 1f),
//                Spawn(6, EntityRowBlock.Type.PISTON_DPAD, 3f),
//                Spawn(10, EntityRowBlock.Type.PLATFORM, 5f, true),
//        ))
        addPattern(events, patternIndex++ * 16 + 8f, listOf(
                Spawn(0, EntityRowBlock.Type.PISTON_A, 0f),
                Spawn(1, EntityRowBlock.Type.PISTON_A, 1 * 0.5f),
                Spawn(2, EntityRowBlock.Type.PISTON_A, 2 * 0.5f),
                Spawn(3, EntityRowBlock.Type.PISTON_A, 3 * 0.5f),
                Spawn(4, EntityRowBlock.Type.PISTON_A, 4 * 0.5f),
                Spawn(5, EntityRowBlock.Type.PISTON_A, 5 * 0.5f),
                Spawn(6, EntityRowBlock.Type.PISTON_A, 6 * 0.5f),
                Spawn(7, EntityRowBlock.Type.PISTON_A, 7 * 0.5f),
                Spawn(8, EntityRowBlock.Type.PLATFORM, 4f, true),
        ), emptyList())
        
        return events
    }
}