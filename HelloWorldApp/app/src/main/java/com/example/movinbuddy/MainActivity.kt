package com.example.movinbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movinbuddy.data.InventoryStore
import com.example.movinbuddy.data.SectionTemplates
import com.example.movinbuddy.pdf.PdfGenerator

class MainActivity : AppCompatActivity() {

    private lateinit var store: InventoryStore
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        store = InventoryStore.getInstance(this)

        findViewById<android.widget.Button>(R.id.propertyDetailsButton).setOnClickListener {
            showHomeInfoDialog()
        }
        findViewById<android.widget.Button>(R.id.clearAllButton).setOnClickListener {
            confirmClearAll()
        }

        recyclerView = findViewById(R.id.sectionRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<android.widget.Button>(R.id.addSectionButton).setOnClickListener {
            showAddSectionDialog()
        }
        findViewById<android.widget.Button>(R.id.reviewButton).setOnClickListener {
            startActivity(Intent(this, ReviewActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.finishButton).setOnClickListener {
            generatePdf()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        recyclerView.adapter = SectionAdapter(
            store.getSections(),
            onClick = { section ->
                val intent = Intent(this, SectionActivity::class.java)
                intent.putExtra(SectionActivity.EXTRA_SECTION_ID, section.id)
                startActivity(intent)
            },
            onLongClick = { section -> confirmDeleteSection(section.id) }
        )
    }

    private fun confirmDeleteSection(sectionId: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_section)
            .setMessage(R.string.delete_section_confirm)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                store.deleteSection(sectionId)
                refreshList()
            }
            .show()
    }

    private fun confirmClearAll() {
        AlertDialog.Builder(this)
            .setTitle(R.string.clear_all)
            .setMessage(R.string.clear_all_confirm)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.clear_all) { _, _ ->
                store.clearAll()
                refreshList()
                Toast.makeText(this, R.string.clear_all_done, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showAddSectionDialog() {
        val types = SectionTemplates.DUPLICABLE_TYPES.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(R.string.add_section_title)
            .setItems(types) { _, which ->
                store.addSection(types[which])
                refreshList()
            }
            .show()
    }

    private fun showHomeInfoDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_home_info, null)
        val addressEditText = view.findViewById<EditText>(R.id.addressEditText)
        val tenantEditText = view.findViewById<EditText>(R.id.tenantEditText)
        val landlordEditText = view.findViewById<EditText>(R.id.landlordEditText)

        addressEditText.setText(store.homeInfo.address)
        tenantEditText.setText(store.homeInfo.tenantNames)
        landlordEditText.setText(store.homeInfo.landlordName)

        AlertDialog.Builder(this)
            .setTitle(R.string.property_details)
            .setView(view)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                store.updateHomeInfo(
                    addressEditText.text.toString(),
                    tenantEditText.text.toString(),
                    landlordEditText.text.toString()
                )
            }
            .show()
    }

    private fun generatePdf() {
        val result = PdfGenerator.generate(this, store)
        if (result == null) {
            Toast.makeText(this, R.string.pdf_generation_failed, Toast.LENGTH_LONG).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.pdf_ready_title)
            .setMessage(R.string.pdf_generated)
            .setNegativeButton(R.string.close, null)
            .setNeutralButton(R.string.share_pdf_action) { _, _ -> sharePdf(result) }
            .setPositiveButton(R.string.view_pdf) { _, _ -> viewPdf(result) }
            .show()
    }

    private fun viewPdf(uri: android.net.Uri) {
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(viewIntent)
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_pdf_viewer, Toast.LENGTH_LONG).show()
        }
    }

    private fun sharePdf(uri: android.net.Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_pdf)))
    }
}
