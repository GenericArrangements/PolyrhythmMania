package polyrhythmmania.editor.pane

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import paintbox.binding.Var
import paintbox.packing.PackedSheet
import paintbox.registry.AssetRegistry
import paintbox.ui.Anchor
import paintbox.ui.ImageIcon
import paintbox.ui.ImageNode
import paintbox.ui.Pane
import paintbox.ui.area.Insets
import paintbox.ui.border.SolidBorder
import paintbox.ui.control.Button
import paintbox.ui.element.RectElement
import paintbox.ui.layout.HBox
import polyrhythmmania.Localization
import polyrhythmmania.editor.PlayState
import polyrhythmmania.editor.Tool
import polyrhythmmania.editor.pane.dialog.ResultsTextDialog


class Toolbar(val upperPane: UpperPane) : Pane() {

    val editorPane: EditorPane = upperPane.editorPane

    val previewSection: Pane
    val mainSection: Pane

    val tilesetButton: Button
    val playtestButton: Button

    val tapalongPane: TapalongPane

    init {
        this.border.set(Insets(2f, 2f, 0f, 0f))
        this.borderStyle.set(SolidBorder().apply { this.color.bind { editorPane.palette.upperPaneBorder.use() } })
        this.padding.set(Insets(2f))


        // Preview section
        previewSection = Pane().apply {
            Anchor.TopLeft.configure(this)
            this.bounds.width.bind { upperPane.previewPane.contentZone.width.useF() - 2f }
            this.border.set(Insets(0f, 0f, 0f, 2f))
            this.borderStyle.set(SolidBorder().apply { this.color.bind { editorPane.palette.previewPaneSeparator.use() } })
            this.padding.set(Insets(0f, 0f, 2f, 4f))
        }
        this += previewSection


        val leftPreviewHbox = HBox().apply {
            Anchor.TopLeft.configure(this)
            this.spacing.set(4f)
            this.bounds.width.set(32f * 3 + this.spacing.get() * 2)
            this.align.set(HBox.Align.LEFT)
        }
        leftPreviewHbox.temporarilyDisableLayouts { 
            
        }
        previewSection += leftPreviewHbox
        val rightPreviewHbox = HBox().apply {
            Anchor.TopRight.configure(this)
            this.spacing.set(4f)
            this.bounds.width.set(32f * 3 + this.spacing.get() * 2)
            this.align.set(HBox.Align.RIGHT)
        }
        previewSection += rightPreviewHbox
        
        tilesetButton = Button("").apply {
            this.padding.set(Insets.ZERO)
            this.bounds.width.set(32f)
            this.skinID.set(EditorSkins.BUTTON)
            this += ImageNode(TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_tileset"]))
            this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.button.tileset")))
            this.setOnAction {
                editorPane.editor.attemptOpenTilesetEditDialog()
            }
        }
        playtestButton = Button("").apply {
            this.padding.set(Insets(2f))
            this.bounds.width.set(32f)
            this.skinID.set(EditorSkins.BUTTON)
            val open: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_clapboard_open"])
            val shut: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_clapboard_shut"])
            this += ImageNode(null).apply {
                this.textureRegion.bind {
                    if (pressedState.use().pressed) shut else open
                }
            }
            this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.button.playtest")))
            this.setOnAction {
                editorPane.editor.attemptStartPlaytest()
            }
        }
        rightPreviewHbox.temporarilyDisableLayouts {
            rightPreviewHbox += tilesetButton
            rightPreviewHbox += playtestButton
        }
        
        val playbackButtonPane = HBox().apply {
            Anchor.Centre.configure(this)
            this.spacing.set(4f)
            this.bounds.width.set(32f * 3 + this.spacing.get() * 2)
            this.align.set(HBox.Align.CENTRE)
        }
        previewSection += playbackButtonPane
        val pauseButton = Button("").apply {
            this.padding.set(Insets.ZERO)
            this.bounds.width.set(32f)
            this.skinID.set(EditorSkins.BUTTON)
            val active: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_pause_color"])
            val inactive: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_pause_white"])
            this += ImageNode(null).apply {
                this.textureRegion.bind {
                    if (apparentDisabledState.use()) inactive else active
                }
                this.tint.bind {
                    if (apparentDisabledState.use()) Color.GRAY else Color.WHITE
                }
            }
            this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.button.pause")))
            this.disabled.bind { editorPane.editor.playState.use() != PlayState.PLAYING }
            this.setOnAction {
                editorPane.editor.changePlayState(PlayState.PAUSED)
            }
        }
        val playButton = Button("").apply {
            this.padding.set(Insets.ZERO)
            this.bounds.width.set(32f)
            this.skinID.set(EditorSkins.BUTTON)
            val active: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_play_color"])
            val inactive: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_play_white"])
            this += ImageNode(null).apply {
                this.textureRegion.bind {
                    if (apparentDisabledState.use()) inactive else active
                }
                this.tint.bind {
                    if (apparentDisabledState.use()) Color.GRAY else Color.WHITE
                }
            }
            this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.button.play")))
            this.disabled.bind { editorPane.editor.playState.use() == PlayState.PLAYING }
            this.setOnAction {
                editorPane.editor.changePlayState(PlayState.PLAYING)
            }
        }
        val stopButton = Button("").apply {
            this.padding.set(Insets.ZERO)
            this.bounds.width.set(32f)
            this.skinID.set(EditorSkins.BUTTON)
            val active: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_stop_color"])
            val inactive: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_stop_white"])
            this += ImageNode(null).apply {
                this.textureRegion.bind {
                    if (apparentDisabledState.use()) inactive else active
                }
                this.tint.bind {
                    if (apparentDisabledState.use()) Color.GRAY else Color.WHITE
                }
            }
            this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.button.stop")))
            this.disabled.bind { editorPane.editor.playState.use() == PlayState.STOPPED }
            this.setOnAction {
                editorPane.editor.changePlayState(PlayState.STOPPED)
            }
        }
        playbackButtonPane.temporarilyDisableLayouts {
            playbackButtonPane += pauseButton
            playbackButtonPane += playButton
            playbackButtonPane += stopButton
        }
        previewSection += createPlaybackButtonSet().apply { 
            Anchor.Centre.configure(this)
        }


        // Main section
        mainSection = Pane().apply {
            Anchor.TopRight.configure(this)
            this.bounds.width.bind {
                (parent.use()?.contentZone?.width?.useF() ?: 0f) - previewSection.bounds.width.useF()
            }
            this.margin.set(Insets(0f, 0f, 4f, 4f))
        }
        this += mainSection

        val tools = Tool.VALUES
        val toolsPane = HBox().apply {
            Anchor.TopRight.configure(this)
            this.align.set(HBox.Align.RIGHT)
            this.spacing.set(4f)
            this.bounds.width.set((32f + this.spacing.get()) * tools.size)
        }
        mainSection += toolsPane
        toolsPane.temporarilyDisableLayouts {
            tools.forEachIndexed { index, thisTool ->
                toolsPane.addChild(IndentedButton("").apply {
                    this.bounds.width.set(32f)
                    this.skinID.set(EditorSkins.BUTTON)
                    this.padding.set(Insets.ZERO)
                    this += ImageNode(TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_tool")[thisTool.textureKey]))
                    editorPane.styleIndentedButton(this)
                    this.selectedState.bind { editorPane.editor.tool.use() == thisTool }
                    this.setOnAction {
                        editorPane.editor.changeTool(thisTool)
                    }
                    val tooltipStr = Localization.getVar("tool.tooltip", Var.bind {
                        listOf(Localization.getVar(thisTool.localizationKey).use(), "${index + 1}")
                    })
                    this.tooltipElement.set(editorPane.createDefaultTooltip(tooltipStr))
                })
            }
        }


        tapalongPane = TapalongPane(this).apply {
            this.bounds.width.set(300f)
        }
        val leftControlPane = HBox().apply {
            Anchor.TopLeft.configure(this)
            this.align.set(HBox.Align.LEFT)
            this.spacing.set(4f)
            this.bounds.width.set((32f + this.spacing.get()) * 3 + tapalongPane.bounds.width.get())
        }
        mainSection += leftControlPane


        leftControlPane.temporarilyDisableLayouts {
            leftControlPane.addChild(Button("").apply {
                this.bounds.width.set(32f)
                this.skinID.set(EditorSkins.BUTTON)
                this.padding.set(Insets.ZERO)
                this += ImageNode(TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_results"]))
                this.setOnAction {
                    if (editorPane.editor.allowedToEdit.getOrCompute()) {
                        editorPane.openDialog(ResultsTextDialog(editorPane))
                    }
                }
                this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.button.results")))
            })
            leftControlPane.addChild(RectElement(binding = {editorPane.palette.previewPaneSeparator.use()}).apply { 
                this.bounds.width.set(2f)
                this.margin.set(Insets(2f, 2f, 0f, 0f))
            })
            leftControlPane.addChild(Button("").apply {
                this.bounds.width.set(32f)
                this.skinID.set(EditorSkins.BUTTON)
                this.padding.set(Insets.ZERO)
                this += ImageNode(TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_music"]))
                this.setOnAction {
                    if (editorPane.editor.allowedToEdit.getOrCompute()) {
                        editorPane.openDialog(editorPane.musicDialog.prepareShow())
                    }
                }
                this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.button.music")))
            })
            leftControlPane.addChild(IndentedButton("").apply {
                editorPane.styleIndentedButton(this)
                this.bounds.width.set(32f)
                this.skinID.set(EditorSkins.BUTTON)
                this.padding.set(Insets.ZERO)
                this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.button.metronome")))
                this.selectedState.addListener {
                    editorPane.editor.metronomeEnabled.set(it.getOrCompute())
                }
                val inactive = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_metronome"])
                val active = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_metronome_active"])
                this += ImageNode(null).apply {
                    this.textureRegion.bind {
                        val isActive = selectedState.use()
                        if (isActive) active else inactive
                    }
                }
            })
            leftControlPane.addChild(IndentedButton("").apply {
                editorPane.styleIndentedButton(this)
                this.bounds.width.set(32f)
                this.skinID.set(EditorSkins.BUTTON)
                this.padding.set(Insets.ZERO)
                val inactiveTooltip = Localization.getVar("editor.button.tapalong")
                val activeTooltip = Localization.getVar("editor.button.tapalong.active")
                this.tooltipElement.set(editorPane.createDefaultTooltip {
                    if (this@apply.selectedState.use()) activeTooltip.use() else inactiveTooltip.use()
                })
                tapalongPane.visible.set(this.selectedState.getOrCompute())
                selectedState.addListener {
                    val state = it.getOrCompute()

                    if (state) {
                        editorPane.enqueueAnimation(tapalongPane.opacity, 0f, 1f, 0.125f).apply { 
                            onStart = {
                                tapalongPane.visible.set(true)
                            }
                        }
                    } else {
                        editorPane.enqueueAnimation(tapalongPane.opacity, 1f, 0f, 0.125f).apply {
                            onComplete = {
                                tapalongPane.visible.set(false)
                            }
                        }
                    }
                }
                this += ImageNode(TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_tapalong"]))
            })
            leftControlPane.addChild(tapalongPane)
        }
    }
    
    fun createPlaybackButtonSet(): Pane {
        val playbackButtonPane = HBox().apply {
            this.spacing.set(4f)
            this.bounds.width.set(32f * 3 + this.spacing.get() * 2)
            this.align.set(HBox.Align.CENTRE)
        }
        val pauseButton = Button("").apply {
            this.padding.set(Insets.ZERO)
            this.bounds.width.set(32f)
            this.skinID.set(EditorSkins.BUTTON)
            val active: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_pause_color"])
            val inactive: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_pause_white"])
            this += ImageNode(null).apply {
                this.textureRegion.bind {
                    if (apparentDisabledState.use()) inactive else active
                }
                this.tint.bind {
                    if (apparentDisabledState.use()) Color.GRAY else Color.WHITE
                }
            }
            this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.button.pause")))
            this.disabled.bind { editorPane.editor.playState.use() != PlayState.PLAYING }
            this.setOnAction {
                editorPane.editor.changePlayState(PlayState.PAUSED)
            }
        }
        val playButton = Button("").apply {
            this.padding.set(Insets.ZERO)
            this.bounds.width.set(32f)
            this.skinID.set(EditorSkins.BUTTON)
            val active: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_play_color"])
            val inactive: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_play_white"])
            this += ImageNode(null).apply {
                this.textureRegion.bind {
                    if (apparentDisabledState.use()) inactive else active
                }
                this.tint.bind {
                    if (apparentDisabledState.use()) Color.GRAY else Color.WHITE
                }
            }
            this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.button.play")))
            this.disabled.bind { editorPane.editor.playState.use() == PlayState.PLAYING }
            this.setOnAction {
                editorPane.editor.changePlayState(PlayState.PLAYING)
            }
        }
        val stopButton = Button("").apply {
            this.padding.set(Insets.ZERO)
            this.bounds.width.set(32f)
            this.skinID.set(EditorSkins.BUTTON)
            val active: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_stop_color"])
            val inactive: TextureRegion = TextureRegion(AssetRegistry.get<PackedSheet>("ui_icon_editor")["toolbar_stop_white"])
            this += ImageNode(null).apply {
                this.textureRegion.bind {
                    if (apparentDisabledState.use()) inactive else active
                }
                this.tint.bind {
                    if (apparentDisabledState.use()) Color.GRAY else Color.WHITE
                }
            }
            this.tooltipElement.set(editorPane.createDefaultTooltip(Localization.getVar("editor.button.stop")))
            this.disabled.bind { editorPane.editor.playState.use() == PlayState.STOPPED }
            this.setOnAction {
                editorPane.editor.changePlayState(PlayState.STOPPED)
            }
        }
        playbackButtonPane.temporarilyDisableLayouts {
            playbackButtonPane += pauseButton
            playbackButtonPane += playButton
            playbackButtonPane += stopButton
        }
        return playbackButtonPane
    }

}