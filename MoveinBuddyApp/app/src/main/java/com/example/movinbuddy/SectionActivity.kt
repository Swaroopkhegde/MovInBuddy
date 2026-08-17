package com.example.movinbuddy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movinbuddy.data.InventoryStore
import com.google.android.material.appbar.MaterialToolbar

class SectionActivity : AppCompatActivity() {

    private lateinit var store: InventoryStore
    private lateinit var sectionId: String
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_section)

        sectionId = intent.getStringExtra(EXTRA_SECTION_ID) ?: run { finish(); return }
        store = InventoryStore.getInstance(this)
        val section = store.getSection(sectionId) ?: run { finish(); return }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = section.displayName
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.itemRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        val section = store.getSection(sectionId) ?: return
        recyclerView.adapter = ItemRowAdapter(section.items) { item ->
            val intent = Intent(this, ItemActivity::class.java)
            intent.putExtra(ItemActivity.EXTRA_SECTION_ID, sectionId)
            intent.putExtra(ItemActivity.EXTRA_ITEM_ID, item.id)
            startActivity(intent)
        }
    }

    companion object {
        const val EXTRA_SECTION_ID = "extra_section_id"
    }
}
