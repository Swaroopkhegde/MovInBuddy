package com.example.movinbuddy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movinbuddy.data.InventoryStore
import com.example.movinbuddy.report.HtmlReportGenerator
import com.google.android.material.appbar.MaterialToolbar

class ReviewActivity : AppCompatActivity() {

    private lateinit var store: InventoryStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        store = InventoryStore.getInstance(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val rows = mutableListOf<ReviewRow>()
        store.getSections().forEach { section ->
            rows.add(ReviewRow.SectionHeader(section.displayName))
            section.items.forEach { item -> rows.add(ReviewRow.ItemRow(item)) }
        }

        val recyclerView = findViewById<RecyclerView>(R.id.reviewRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ReviewAdapter(rows)

        findViewById<android.widget.Button>(R.id.openReportButton).setOnClickListener {
            openHtmlReport()
        }
    }

    private fun openHtmlReport() {
        val file = HtmlReportGenerator.generate(this, store)
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/html")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.open_full_report)))
    }
}
