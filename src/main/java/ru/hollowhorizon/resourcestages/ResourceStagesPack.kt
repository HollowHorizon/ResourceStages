package ru.hollowhorizon.resourcestages

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.metadata.MetadataSectionSerializer
import net.minecraft.server.packs.resources.IoSupplier
import net.minecraftforge.fml.ModList
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

object ResourceStagesPack : PackResources {
    private val resourcesFolder
        get() = ResourceStages.MOD_FOLDER.resolve(
            ResourceStages.stages.lastOrNull() ?: "default"
        )
    val resourceMap = ConcurrentHashMap<ResourceLocation, IResourceStreamSupplier?>()

    val allResources
        get() = resourcesFolder.parentFile.walk()
            .filter { it.isFile }
            .map {
                var path = it.canonicalPath.substringAfter(resourcesFolder.parentFile.canonicalPath).replace('\\', '/')
                if (path.startsWith('/')) path = path.substring(1)

                path = path.substringAfter('/')
                path = path.replaceFirst('/', ':')

                ResourceLocation(path)
            }.distinct().toList()

    override fun getResource(pType: PackType, pLocation: ResourceLocation): IoSupplier<InputStream>? {
        return resourceMap.computeIfAbsent(pLocation) {
            val path = resourcesFolder.resolve(it.namespace).resolve(it.path)

            IResourceStreamSupplier.create(path)
        }
    }

    override fun listResources(
        pPackType: PackType,
        pNamespace: String,
        pPath: String,
        pResourceOutput: PackResources.ResourceOutput,
    ) {
        resourcesFolder.resolve(pNamespace).resolve(pPath).walk().forEach {
            if (it.isFile) {
                var path = it.canonicalPath.substringAfter(resourcesFolder.canonicalPath).replace('\\', '/')
                if (path.startsWith('/')) path = path.substring(1)

                path = path.replaceFirst('/', ':')
                pResourceOutput.accept(ResourceLocation(path), IResourceStreamSupplier.create(it))
            }
        }
    }

    override fun getNamespaces(pType: PackType): MutableSet<String> =
        ModList.get().mods.map { it.modId }.toMutableSet()

    override fun <T> getMetadataSection(pDeserializer: MetadataSectionSerializer<T>): T? {
        if (pDeserializer.metadataSectionName.equals("pack")) {
            //var - java 16 feature
            val obj = JsonObject()
            val supportedFormats = JsonArray()
            (6..9).forEach(supportedFormats::add) // 1.16.2 - 1.19.3
            obj.addProperty("pack_format", 9)
            obj.add("supported_formats", supportedFormats)
            obj.addProperty("description", "Generated resources for HollowCore")
            return pDeserializer.fromJson(obj)
        }
        return null
    }

    override fun packId() = "Resource Stages Data"

    override fun close() {
        resourceMap.clear()
    }

    override fun getRootResource(vararg pElements: String): IoSupplier<InputStream> {
        throw FileNotFoundException(pElements.joinToString())
    }
}

interface IResourceStreamSupplier : IoSupplier<InputStream> {
    @Throws(IOException::class)
    override fun get(): InputStream

    companion object {
        fun create(file: File): IResourceStreamSupplier? = if (file.exists()) create { file.inputStream() } else null

        fun create(streamable: () -> InputStream): IResourceStreamSupplier {
            return object : IResourceStreamSupplier {

                @Throws(IOException::class)
                override fun get() = streamable()
            }
        }
    }
}
