package ru.hollowhorizon.resourcestages

import net.darkhax.gamestages.data.GameStageSaveHandler
import net.darkhax.gamestages.event.StagesSyncedEvent
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.metadata.pack.PackMetadataSection
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackSource
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AddPackFindersEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.loading.FMLPaths

@Mod(ResourceStages.MODID)
object ResourceStages {
    const val MODID = "resourcestages"
    val MOD_FOLDER = FMLPaths.GAMEDIR.get().resolve("resource_stages").toFile().apply {
        if (!exists()) mkdirs()
    }

    init {
        if (FMLEnvironment.dist.isClient) thedarkcolour.kotlinforforge.forge.MOD_BUS.addListener(::registerPacks)
        MinecraftForge.EVENT_BUS.addListener<StagesSyncedEvent> {
            ResourceStagesPack.close()
            (Minecraft.getInstance().textureManager as ITextureManager).`resourceStages$resetPaths`(ResourceStagesPack.allResources)
            //Minecraft.getInstance().reloadResourcePacks()
        }
    }

    fun registerPacks(event: AddPackFindersEvent) {
        event.addRepositorySource { adder, creator ->
            adder.accept(
                creator.create(
                    ResourceStagesPack.name, Component.literal(ResourceStagesPack.name), true, { ResourceStagesPack },
                    PackMetadataSection(Component.translatable("fml.resources.modresources"), 9),
                    Pack.Position.TOP, PackSource.BUILT_IN, false
                )
            )
        }
    }

    val stages get() = GameStageSaveHandler.getClientData()?.stages?.sorted() ?: emptyList()
}