package com.example.movinbuddy

import android.graphics.Bitmap
import android.graphics.BitmapFactory

object BitmapUtils {

    fun decodeSampledBitmap(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, boundsOptions)
        if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return null

        var inSampleSize = 1
        var halfHeight = boundsOptions.outHeight / 2
        var halfWidth = boundsOptions.outWidth / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
        return BitmapFactory.decodeFile(path, decodeOptions)
    }
}
