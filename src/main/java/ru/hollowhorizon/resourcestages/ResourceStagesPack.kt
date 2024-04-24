package ru.hollowhorizon.resourcestages

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.metadata.MetadataSectionSerializer
import net.minecraftforge.fml.ModList
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

object ResourceStagesPack : PackResources {
    private val resourcesFolder
        get() = ResourceStages.MOD_FOLDER.resolve(
            ResourceStages.stages.lastOrNull() ?: "default"
        )
    val resourceMap = ConcurrentHashMap<ResourceLocation, IResourceStreamSupplier>()

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

    override fun getRootResource(pFileName: String): InputStream {
        throw FileNotFoundException(pFileName)
    }

    override fun getResource(pType: PackType, pLocation: ResourceLocation): InputStream {
        return resourceMap.computeIfAbsent(pLocation) {
            val path = resourcesFolder.resolve(it.namespace).resolve(it.path)
            IResourceStreamSupplier.create(path::exists, path::inputStream)
        }.create()
    }

    override fun getResources(
        pType: PackType,
        pNamespace: String,
        pPath: String,
        pFilter: Predicate<ResourceLocation>,
    ): MutableCollection<ResourceLocation> {
        return resourcesFolder.walk()
            .filter { it.isFile }
            .map {
                var path = it.canonicalPath.substringAfter(resourcesFolder.canonicalPath).replace('\\', '/')
                if (path.startsWith('/')) path = path.substring(1)

                path = path.replaceFirst('/', ':')

                ResourceLocation(path)
            }
            .filter { it.path.startsWith(pNamespace) }
            .filter { pFilter.test(it) }.toMutableSet()
    }

    override fun hasResource(pType: PackType, pLocation: ResourceLocation): Boolean {
        val path = resourcesFolder.resolve(pLocation.namespace).resolve(pLocation.path)
        return path.exists()
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

    override fun getName() = "Resource Stages Data"

    override fun close() {
        resourceMap.clear()
    }
}

interface IResourceStreamSupplier {
    fun exists(): Boolean

    @Throws(IOException::class)
    fun create(): InputStream

    companion object {
        fun create(exists: () -> Boolean, streamable: () -> InputStream): IResourceStreamSupplier {
            return object : IResourceStreamSupplier {
                override fun exists() = exists()

                @Throws(IOException::class)
                override fun create() = streamable()
            }
        }
    }
}
