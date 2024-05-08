package ru.hollowhorizon.resourcestages

import net.darkhax.gamestages.data.GameStageSaveHandler
import net.darkhax.gamestages.event.StagesSyncedEvent
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackSource
import net.minecraftforge.client.event.RenderGuiOverlayEvent
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AddPackFindersEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.loading.FMLPaths
import ru.hollowhorizon.resourcestages.util.IReloadNoScreen
import ru.hollowhorizon.resourcestages.util.ReloadOverlay

@Mod(ResourceStages.MODID)
object ResourceStages {
    const val MODID = "resourcestages"
    val MOD_FOLDER = FMLPaths.GAMEDIR.get().resolve("resource_stages").toFile().apply {
        if (!exists()) mkdirs()
    }

    init {
        if (FMLEnvironment.dist.isClient) thedarkcolour.kotlinforforge.forge.MOD_BUS.addListener(::registerPacks)
        MinecraftForge.EVENT_BUS.addListener<StagesSyncedEvent> {
            (Minecraft.getInstance() as IReloadNoScreen).reloadNoScreen(false)
            //ResourceStagesPack.close()
            //(Minecraft.getInstance().textureManager as ITextureManager).`resourceStages$resetPaths`(ResourceStagesPack.allResources)
        }

        MinecraftForge.EVENT_BUS.addListener(::renderOverlay)
    }

    fun registerPacks(event: AddPackFindersEvent) {
        event.addRepositorySource { packs ->
            packs.accept(
                Pack.create(
                    ResourceStagesPack.packId(),
                    Component.literal(ResourceStagesPack.packId()),
                    true,
                    { ResourceStagesPack },
                    Pack.readPackInfo(ResourceStagesPack.packId()) { ResourceStagesPack }
                        ?: throw NullPointerException("No pack info available"),
                    PackType.CLIENT_RESOURCES,
                    Pack.Position.TOP, true, PackSource.BUILT_IN
                )
            )
        }
    }

    @JvmStatic
    fun renderOverlay(event: RenderGuiOverlayEvent.Post) {
        if (event.overlay != VanillaGuiOverlay.HOTBAR.type()) return

        val window = event.window
        val width = window.guiScaledWidth
        val height = window.guiScaledHeight
        ReloadOverlay.render(event.guiGraphics, width, height)
    }

    val stages get() = GameStageSaveHandler.getClientData()?.stages?.sorted() ?: emptyList()
}