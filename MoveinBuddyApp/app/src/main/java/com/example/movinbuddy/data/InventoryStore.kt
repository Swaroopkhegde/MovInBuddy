package com.example.movinbuddy.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class InventoryStore private constructor(context: Context) {

    private val storageFile = File(context.filesDir, "inventory.json")
    private val sections: MutableList<Section>
    val homeInfo: HomeInfo

    init {
        val loaded = load()
        sections = loaded?.first ?: seedDefaultSections()
        homeInfo = loaded?.second ?: HomeInfo()
        if (loaded == null) persist()
    }

    fun getSections(): List<Section> = sections

    fun getSection(sectionId: String): Section? = sections.find { it.id == sectionId }

    fun getItem(sectionId: String, itemId: String): Item? =
        getSection(sectionId)?.items?.find { it.id == itemId }

    fun addSection(sectionType: String): Section {
        val template = SectionTemplates.ITEM_TEMPLATES[sectionType] ?: emptyList()
        val existingCount = sections.count { it.sectionType == sectionType }
        val displayName = if (existingCount == 0) sectionType else "$sectionType ${existingCount + 1}"
        val section = Section(
            id = "sec_${System.currentTimeMillis()}_${sections.size}",
            sectionType = sectionType,
            displayName = displayName,
            items = template.mapIndexed { index, name ->
                Item(id = "item_${index}_${System.nanoTime()}", name = name)
            }.toMutableList()
        )
        sections.add(section)
        persist()
        return section
    }

    fun deleteSection(sectionId: String) {
        val section = getSection(sectionId) ?: return
        section.items.forEach { item -> item.photos.forEach { File(it.filePath).delete() } }
        sections.remove(section)
        persist()
    }

    fun updateComment(sectionId: String, itemId: String, comment: String) {
        getItem(sectionId, itemId)?.comment = comment
        persist()
    }

    fun addPhoto(sectionId: String, itemId: String, filePath: String) {
        getItem(sectionId, itemId)?.photos?.add(Photo(id = "photo_${System.nanoTime()}", filePath = filePath))
        persist()
    }

    fun removePhotoByPath(sectionId: String, itemId: String, filePath: String) {
        val item = getItem(sectionId, itemId) ?: return
        val photo = item.photos.find { it.filePath == filePath } ?: return
        File(photo.filePath).let { if (it.exists()) it.delete() }
        item.photos.remove(photo)
        persist()
    }

    fun save() = persist()

    fun updateHomeInfo(address: String, tenantNames: String, landlordName: String) {
        homeInfo.address = address
        homeInfo.tenantNames = tenantNames
        homeInfo.landlordName = landlordName
        persist()
    }

    fun clearAll() {
        sections.forEach { section ->
            section.items.forEach { item -> item.photos.forEach { File(it.filePath).delete() } }
        }
        sections.clear()
        sections.addAll(seedDefaultSections())
        homeInfo.address = ""
        homeInfo.tenantNames = ""
        homeInfo.landlordName = ""
        persist()
    }

    private fun seedDefaultSections(): MutableList<Section> {
        val result = mutableListOf<Section>()
        SectionTemplates.DEFAULT_SEED_ORDER.forEachIndexed { sectionIndex, type ->
            val template = SectionTemplates.ITEM_TEMPLATES[type] ?: emptyList()
            result.add(
                Section(
                    id = "sec_seed_$sectionIndex",
                    sectionType = type,
                    displayName = type,
                    items = template.mapIndexed { itemIndex, name ->
                        Item(id = "item_seed_${sectionIndex}_$itemIndex", name = name)
                    }.toMutableList()
                )
            )
        }
        return result
    }

    private fun load(): Pair<MutableList<Section>, HomeInfo>? {
        if (!storageFile.exists()) return null
        return try {
            val root = JSONObject(storageFile.readText())
            val info = root.optJSONObject("homeInfo")
            val homeInfo = HomeInfo(
                address = info?.optString("address").orEmpty(),
                tenantNames = info?.optString("tenantNames").orEmpty(),
                landlordName = info?.optString("landlordName").orEmpty()
            )
            val sectionsJson = root.getJSONArray("sections")
            val result = mutableListOf<Section>()
            for (s in 0 until sectionsJson.length()) {
                val sObj = sectionsJson.getJSONObject(s)
                val itemsJson = sObj.getJSONArray("items")
                val items = mutableListOf<Item>()
                for (i in 0 until itemsJson.length()) {
                    val iObj = itemsJson.getJSONObject(i)
                    val photosJson = iObj.getJSONArray("photos")
                    val photos = mutableListOf<Photo>()
                    for (p in 0 until photosJson.length()) {
                        val pObj = photosJson.getJSONObject(p)
                        photos.add(Photo(id = pObj.getString("id"), filePath = pObj.getString("filePath")))
                    }
                    items.add(
                        Item(
                            id = iObj.getString("id"),
                            name = iObj.getString("name"),
                            comment = iObj.optString("comment").orEmpty(),
                            photos = photos
                        )
                    )
                }
                result.add(
                    Section(
                        id = sObj.getString("id"),
                        sectionType = sObj.getString("sectionType"),
                        displayName = sObj.getString("displayName"),
                        items = items
                    )
                )
            }
            result to homeInfo
        } catch (e: Exception) {
            null
        }
    }

    private fun persist() {
        val root = JSONObject()
        val info = JSONObject()
        info.put("address", homeInfo.address)
        info.put("tenantNames", homeInfo.tenantNames)
        info.put("landlordName", homeInfo.landlordName)
        root.put("homeInfo", info)

        val sectionsJson = JSONArray()
        sections.forEach { section ->
            val sObj = JSONObject()
            sObj.put("id", section.id)
            sObj.put("sectionType", section.sectionType)
            sObj.put("displayName", section.displayName)
            val itemsJson = JSONArray()
            section.items.forEach { item ->
                val iObj = JSONObject()
                iObj.put("id", item.id)
                iObj.put("name", item.name)
                iObj.put("comment", item.comment)
                val photosJson = JSONArray()
                item.photos.forEach { photo ->
                    val pObj = JSONObject()
                    pObj.put("id", photo.id)
                    pObj.put("filePath", photo.filePath)
                    photosJson.put(pObj)
                }
                iObj.put("photos", photosJson)
                itemsJson.put(iObj)
            }
            sObj.put("items", itemsJson)
            sectionsJson.put(sObj)
        }
        root.put("sections", sectionsJson)
        storageFile.writeText(root.toString())
    }

    companion object {
        @Volatile
        private var instance: InventoryStore? = null

        fun getInstance(context: Context): InventoryStore =
            instance ?: synchronized(this) {
                instance ?: InventoryStore(context.applicationContext).also { instance = it }
            }
    }
}
