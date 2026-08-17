package com.example.movinbuddy.data

data class Photo(
    val id: String,
    val filePath: String
)

data class Item(
    val id: String,
    val name: String,
    var comment: String = "",
    val photos: MutableList<Photo> = mutableListOf()
)

data class Section(
    val id: String,
    val sectionType: String,
    var displayName: String,
    val items: MutableList<Item>
)

data class HomeInfo(
    var address: String = "",
    var tenantNames: String = "",
    var landlordName: String = ""
)
