package ru.hollowhorizon.resourcestages

import net.minecraft.resources.ResourceLocation

interface ITextureManager {
    fun `resourceStages$resetPaths`(resources: Collection<ResourceLocation>)
}