package com.example.movinbuddy

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat

object CommentTextUtils {

    fun buildSummary(context: Context, comment: String, photoCount: Int): CharSequence {
        val photoSuffix = context.getString(R.string.photo_count_suffix, photoCount)
        if (comment.isBlank()) {
            return photoSuffix.trimStart(' ', '•').trim()
        }
        val builder = SpannableStringBuilder(comment)
        builder.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.comment_blue)),
            0, comment.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        builder.append(photoSuffix)
        return builder
    }
}
