package com.example.movinbuddy

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movinbuddy.data.InventoryStore
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class ItemActivity : AppCompatActivity() {

    private lateinit var store: InventoryStore
    private lateinit var sectionId: String
    private lateinit var itemId: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View
    private lateinit var commentEditText: TextInputEditText
    private var pendingPhotoFile: File? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = pendingPhotoFile
        if (success && file != null) {
            store.addPhoto(sectionId, itemId, file.absolutePath)
            refreshPhotos()
        }
        pendingPhotoFile = null
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera()
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) importPickedImage(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        sectionId = intent.getStringExtra(EXTRA_SECTION_ID) ?: run { finish(); return }
        itemId = intent.getStringExtra(EXTRA_ITEM_ID) ?: run { finish(); return }
        store = InventoryStore.getInstance(this)
        val item = store.getItem(sectionId, itemId) ?: run { finish(); return }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = item.name
        toolbar.setNavigationOnClickListener { finish() }

        commentEditText = findViewById(R.id.commentEditText)
        commentEditText.setText(item.comment)

        recyclerView = findViewById(R.id.photoRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        emptyState = findViewById(R.id.emptyStateTextView)

        findViewById<FloatingActionButton>(R.id.addPhotoFab).setOnClickListener {
            requestPhotoCapture()
        }
        findViewById<FloatingActionButton>(R.id.galleryPhotoFab).setOnClickListener {
            pickImageLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        refreshPhotos()
    }

    override fun onPause() {
        super.onPause()
        store.updateComment(sectionId, itemId, commentEditText.text.toString())
    }

    private fun refreshPhotos() {
        val item = store.getItem(sectionId, itemId) ?: return
        val paths = item.photos.map { it.filePath }
        emptyState.visibility = if (paths.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.adapter = PhotoAdapter(paths) { path ->
            confirmDeletePhoto(path)
        }
    }

    private fun confirmDeletePhoto(path: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_photo)
            .setMessage(R.string.delete_photo_confirm)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                store.removePhotoByPath(sectionId, itemId, path)
                refreshPhotos()
            }
            .show()
    }

    private fun requestPhotoCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val photoDir = File(filesDir, "item_photos").apply { mkdirs() }
        val file = File(photoDir, "${sectionId}_${itemId}_${System.currentTimeMillis()}.jpg")
        pendingPhotoFile = file
        val uri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        takePictureLauncher.launch(uri)
    }

    private fun importPickedImage(sourceUri: Uri) {
        val photoDir = File(filesDir, "item_photos").apply { mkdirs() }
        val file = File(photoDir, "${sectionId}_${itemId}_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(sourceUri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        if (file.exists() && file.length() > 0) {
            store.addPhoto(sectionId, itemId, file.absolutePath)
            refreshPhotos()
        }
    }

    companion object {
        const val EXTRA_SECTION_ID = "extra_section_id"
        const val EXTRA_ITEM_ID = "extra_item_id"
    }
}
