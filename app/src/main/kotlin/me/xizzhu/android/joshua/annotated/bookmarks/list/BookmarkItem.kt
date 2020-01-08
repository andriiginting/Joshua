/*
 * Copyright (C) 2020 Xizhi Zhu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xizzhu.android.joshua.annotated.bookmarks.list

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.createTitleSizeSpan
import me.xizzhu.android.joshua.ui.createTitleStyleSpan
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder
import me.xizzhu.android.joshua.ui.updateSettingsWithPrimaryText

data class BookmarkItem(val verseIndex: VerseIndex, private val bookName: String,
                        private val bookShortName: String, private val verseText: String,
                        @Constants.SortOrder private val sortOrder: Int,
                        val onClick: (VerseIndex) -> Unit)
    : BaseItem(R.layout.item_bookmark, { inflater, parent -> BookmarkItemViewHolder(inflater, parent) }) {
    companion object {
        private val BOOK_NAME_SIZE_SPAN = createTitleSizeSpan()
        private val BOOK_NAME_STYLE_SPAN = createTitleStyleSpan()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    val textForDisplay: CharSequence by lazy {
        SPANNABLE_STRING_BUILDER.clear()
        SPANNABLE_STRING_BUILDER.clearSpans()

        if (sortOrder == Constants.SORT_BY_BOOK) {
            // format:
            // <book short name> <chapter verseIndex>:<verse verseIndex> <verse text>
            SPANNABLE_STRING_BUILDER.append(bookShortName).append(' ')
                    .append((verseIndex.chapterIndex + 1).toString()).append(':').append((verseIndex.verseIndex + 1).toString())
            SPANNABLE_STRING_BUILDER.setSpan(BOOK_NAME_STYLE_SPAN, 0, SPANNABLE_STRING_BUILDER.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            SPANNABLE_STRING_BUILDER.setSpan(BOOK_NAME_SIZE_SPAN, 0, SPANNABLE_STRING_BUILDER.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

            SPANNABLE_STRING_BUILDER.append(' ').append(verseText)
        } else {
            // format:
            // <book name> <chapter verseIndex>:<verse verseIndex>
            // <verse text>
            SPANNABLE_STRING_BUILDER.append(bookName).append(' ')
                    .append((verseIndex.chapterIndex + 1).toString()).append(':').append((verseIndex.verseIndex + 1).toString())
            SPANNABLE_STRING_BUILDER.setSpan(BOOK_NAME_STYLE_SPAN, 0, SPANNABLE_STRING_BUILDER.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            SPANNABLE_STRING_BUILDER.setSpan(BOOK_NAME_SIZE_SPAN, 0, SPANNABLE_STRING_BUILDER.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

            SPANNABLE_STRING_BUILDER.append('\n').append(verseText)
        }

        return@lazy SPANNABLE_STRING_BUILDER.subSequence(0, SPANNABLE_STRING_BUILDER.length)
    }
}

private class BookmarkItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<BookmarkItem>(inflater.inflate(R.layout.item_bookmark, parent, false)) {
    private val text: TextView = itemView.findViewById(R.id.text)

    init {
        itemView.setOnClickListener { item?.let { it.onClick(it.verseIndex) } }
    }

    override fun bind(settings: Settings, item: BookmarkItem, payloads: List<Any>) {
        with(text) {
            updateSettingsWithPrimaryText(settings)
            text = item.textForDisplay
        }
    }
}
