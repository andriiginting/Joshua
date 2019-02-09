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

package me.xizzhu.android.joshua.model

import android.database.sqlite.SQLiteDatabase
import androidx.annotation.WorkerThread
import javax.inject.Inject
import javax.inject.Singleton

const val BOOK_COUNT = 66
const val OLD_TESTAMENT_COUNT = 39
const val NEW_TESTAMENT_COUNT = 27
const val TOTAL_CHAPTER_COUNT = 1189
val CHAPTER_COUNT = intArrayOf(50, 40, 27, 36, 34, 24, 21, 4, 31, 24, 22, 25, 29, 36,
        10, 13, 10, 42, 150, 31, 12, 8, 66, 52, 5, 48, 12, 14, 3, 9, 1, 4, 7, 3, 3, 3, 2, 14, 4,
        28, 16, 24, 21, 28, 16, 16, 13, 6, 6, 4, 4, 5, 3, 6, 4, 3, 1, 13, 5, 5, 3, 5, 1, 1, 1, 22)

data class VerseIndex(val bookIndex: Int, val chapterIndex: Int, val verseIndex: Int) {
    companion object {
        val INVALID = VerseIndex(-1, -1, -1)
    }

    fun isValid(): Boolean =
            bookIndex >= 0 && bookIndex < BOOK_COUNT
                    && chapterIndex >= 0 && chapterIndex < CHAPTER_COUNT[bookIndex]
                    && verseIndex >= 0
}

@Singleton
class BibleReadingManager @Inject constructor(private val localStorage: LocalStorage) {
    var currentTranslation: String = ""
        @WorkerThread get() {
            if (field.isEmpty()) {
                field = localStorage.metadataDao.load(MetadataDao.KEY_CURRENT_TRANSLATION, "")
            }
            return field
        }
        @WorkerThread set(value) {
            if (value != field) {
                field = value
                localStorage.metadataDao.save(MetadataDao.KEY_CURRENT_TRANSLATION, value)
            }
        }

    var currentVerseIndex: VerseIndex = VerseIndex.INVALID
        @WorkerThread get() {
            if (!field.isValid()) {
                field = VerseIndex(localStorage.metadataDao.load(MetadataDao.KEY_CURRENT_BOOK_INDEX, "0").toInt(),
                        localStorage.metadataDao.load(MetadataDao.KEY_CURRENT_CHAPTER_INDEX, "0").toInt(),
                        localStorage.metadataDao.load(MetadataDao.KEY_CURRENT_VERSE_INDEX, "0").toInt())
            }
            return field
        }
        @WorkerThread set(value) {
            if (field != value && value.isValid()) {
                field = value

                val entries = ArrayList<Pair<String, String>>(3)
                entries.add(Pair(MetadataDao.KEY_CURRENT_BOOK_INDEX, value.bookIndex.toString()))
                entries.add(Pair(MetadataDao.KEY_CURRENT_CHAPTER_INDEX, value.chapterIndex.toString()))
                entries.add(Pair(MetadataDao.KEY_CURRENT_VERSE_INDEX, value.verseIndex.toString()))
                localStorage.metadataDao.save(entries)
            }
        }

    @WorkerThread
    fun loadBookNames(translationShortName: String): List<String> = localStorage.bookNamesDao.load(translationShortName)
}
