package polyrhythmmania.sidemodes

import net.beadsproject.beads.ugens.SamplePlayer
import polyrhythmmania.PRManiaGame
import polyrhythmmania.container.GlobalContainerSettings
import polyrhythmmania.container.TexturePackSource
import polyrhythmmania.editor.block.Block
import polyrhythmmania.engine.Engine
import polyrhythmmania.engine.Event
import polyrhythmmania.engine.tempo.TempoChange
import polyrhythmmania.soundsystem.BeadsMusic
import polyrhythmmania.soundsystem.sample.LoopParams
import polyrhythmmania.world.DunkWorldBackground
import polyrhythmmania.world.EntityRodDunk
import polyrhythmmania.world.WorldMode
import polyrhythmmania.world.WorldType
import polyrhythmmania.world.render.ForceTexturePack
import polyrhythmmania.world.render.ForceTilesetPalette
import polyrhythmmania.world.tileset.StockTexturePacks
import polyrhythmmania.world.tileset.TilesetPalette


class DunkMode(main: PRManiaGame, prevHighScore: EndlessModeScore)
    : AbstractEndlessMode(main, prevHighScore) {
    
    init {
        container.world.worldMode = WorldMode(WorldType.DUNK, true)
        container.engine.inputter.endlessScore.maxLives.set(5)
        container.renderer.worldBackground = DunkWorldBackground
        container.texturePackSource.set(TexturePackSource.STOCK_GBA)
        TilesetPalette.createGBA1TilesetPalette().applyTo(container.renderer.tileset)
    }
    
    override fun createGlobalContainerSettings(): GlobalContainerSettings {
        return super.createGlobalContainerSettings().copy(forceTexturePack = ForceTexturePack.FORCE_GBA, forceTilesetPalette = ForceTilesetPalette.NO_FORCE)
    }
    
    override fun initialize() {
        engine.tempos.addTempoChange(TempoChange(0f, 129f))

        val music: BeadsMusic = SidemodeAssets.practiceTheme
        val musicData = engine.musicData
        musicData.musicSyncPointBeat = 0f
        musicData.loopParams = LoopParams(SamplePlayer.LoopType.LOOP_FORWARDS, 0.0, music.musicSample.lengthMs)
        musicData.beadsMusic = music
        musicData.update()
        
        addInitialBlocks()
    }
    
    private fun addInitialBlocks() {
        val blocks = mutableListOf<Block>()
        blocks += ResetMusicVolumeBlock(engine).apply { 
            this.beat = 0f
        }
        val loop = LoopingEventBlock(engine, 4f, { engine ->
            engine.inputter.endlessScore.lives.get() > 0
        }) { engine, startBeat ->
            engine.addEvent(EventDeployRodDunk(engine, startBeat))
        }


        blocks += loop.apply {
            this.beat = 0f
        }

        container.addBlocks(blocks)
    }
}

class EventDeployRodDunk(engine: Engine, startBeat: Float) : Event(engine) {
    init {
        this.beat = startBeat
    }

    override fun onStart(currentBeat: Float) {
        super.onStart(currentBeat)
        engine.world.addEntity(EntityRodDunk(engine.world, this.beat))
    }
}
