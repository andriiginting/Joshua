/*
 * Copyright (C) 2019 Xizhi Zhu
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

package me.xizzhu.android.joshua.search.result

import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.SearchItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import java.util.ArrayList

data class SearchResult(val items: List<BaseItem>, val searchResultCount: Int)

fun List<Verse>.toSearchResult(query: String, onClickListener: (VerseIndex) -> Unit): SearchResult {
    val items = ArrayList<BaseItem>(size + Bible.BOOK_COUNT).apply {
        var currentBookIndex = -1
        this@toSearchResult.forEach { verse ->
            if (currentBookIndex != verse.verseIndex.bookIndex) {
                add(TitleItem(verse.text.bookName, false))
                currentBookIndex = verse.verseIndex.bookIndex
            }
            add(SearchItem(verse.verseIndex, verse.text.bookName,
                    verse.text.text, query, onClickListener))
        }
    }
    return SearchResult(items, size)
}