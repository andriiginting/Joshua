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

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.WorkerThread
import me.xizzhu.android.joshua.App
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStorage @Inject constructor(app: App) : SQLiteOpenHelper(app, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "DATABASE_JOSHUA"
        const val DATABASE_VERSION = 1
    }

    val metadataDao = MetadataDao(this)
    val translationInfoDao = TranslationInfoDao(this)
    val translationDao = TranslationDao(this)
    val bookNamesDao = BookNamesDao(this)

    override fun onCreate(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            MetadataDao.createTable(db)
            TranslationInfoDao.createTable(db)
            BookNamesDao.createTable(db)

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // do nothing
    }
}

class BookNamesDao(private val sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_BOOK_NAMES = "bookNames"
        private const val INDEX_BOOK_NAMES = "bookNamesIndex"
        private const val COLUMN_TRANSLATION_SHORT_NAME = "translationShortName"
        private const val COLUMN_BOOK_INDEX = "bookIndex"
        private const val COLUMN_BOOK_NAME = "bookName"

        @WorkerThread
        fun createTable(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_BOOK_NAMES (" +
                    "$COLUMN_TRANSLATION_SHORT_NAME TEXT NOT NULL, $COLUMN_BOOK_INDEX INTEGER NOT NULL, " +
                    "$COLUMN_BOOK_NAME TEXT NOT NULL);")
            db.execSQL("CREATE INDEX $INDEX_BOOK_NAMES ON $TABLE_BOOK_NAMES ($COLUMN_TRANSLATION_SHORT_NAME);")
        }
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun save(translationShortName: String, bookNames: List<String>) {
        val values = ContentValues(3)
        values.put(COLUMN_TRANSLATION_SHORT_NAME, translationShortName)
        for ((bookIndex, bookName) in bookNames.withIndex()) {
            values.put(COLUMN_BOOK_INDEX, bookIndex)
            values.put(COLUMN_BOOK_NAME, bookName)
            db.insertWithOnConflict(TABLE_BOOK_NAMES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }
}

class TranslationDao(private val sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val COLUMN_BOOK_INDEX = "bookIndex"
        private const val COLUMN_CHAPTER_INDEX = "chapterIndex"
        private const val COLUMN_VERSE_INDEX = "verseIndex"
        private const val COLUMN_TEXT = "text"
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun createTable(translationShortName: String) {
        db.execSQL("CREATE TABLE $translationShortName (" +
                "$COLUMN_BOOK_INDEX INTEGER NOT NULL, $COLUMN_CHAPTER_INDEX INTEGER NOT NULL, " +
                "$COLUMN_VERSE_INDEX INTEGER NOT NULL, $COLUMN_TEXT TEXT NOT NULL);")
    }

    @WorkerThread
    fun save(translationShortName: String, bookIndex: Int, chapterIndex: Int, verses: List<String>) {
        val values = ContentValues(4)
        values.put(COLUMN_BOOK_INDEX, bookIndex)
        values.put(COLUMN_CHAPTER_INDEX, chapterIndex)
        for ((verseIndex, verse) in verses.withIndex()) {
            values.put(COLUMN_VERSE_INDEX, verseIndex)
            values.put(COLUMN_TEXT, verse)
            db.insertWithOnConflict(translationShortName, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }
}

class TranslationInfoDao(private val sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_TRANSLATION_INFO = "translationInfo"
        private const val COLUMN_SHORT_NAME = "shortName"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_LANGUAGE = "language"
        private const val COLUMN_SIZE = "size"
        private const val COLUMN_DOWNLOADED = "downloaded"

        @WorkerThread
        fun createTable(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_TRANSLATION_INFO (" +
                    "$COLUMN_SHORT_NAME TEXT PRIMARY KEY, $COLUMN_NAME TEXT NOT NULL, " +
                    " $COLUMN_LANGUAGE TEXT NOT NULL, $COLUMN_SIZE INTEGER NOT NULL, " +
                    " $COLUMN_DOWNLOADED INTEGER NOT NULL);")
        }
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun hasTranslationsInstalled(): Boolean {
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_TRANSLATION_INFO, arrayOf(COLUMN_SHORT_NAME),
                    "$COLUMN_DOWNLOADED = ?", arrayOf("1"), null, null, null, "1")
            return cursor.count > 0
        } finally {
            cursor?.close()
        }
    }

    @WorkerThread
    fun load(): List<TranslationInfo> {
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_TRANSLATION_INFO, null, null, null, null, null, null, null)
            val count = cursor.count
            return if (count == 0) {
                emptyList()
            } else {
                val shortName = cursor.getColumnIndex(COLUMN_SHORT_NAME)
                val name = cursor.getColumnIndex(COLUMN_NAME)
                val language = cursor.getColumnIndex(COLUMN_LANGUAGE)
                val size = cursor.getColumnIndex(COLUMN_SIZE)
                val downloaded = cursor.getColumnIndex(COLUMN_DOWNLOADED)
                val translations = ArrayList<TranslationInfo>(count)
                while (cursor.moveToNext()) {
                    translations.add(TranslationInfo(cursor.getString(shortName),
                            cursor.getString(name), cursor.getString(language),
                            cursor.getLong(size), cursor.getInt(downloaded) == 1))
                }
                translations
            }
        } finally {
            cursor?.close()
        }
    }

    @WorkerThread
    fun replace(translations: List<TranslationInfo>) {
        db.beginTransaction()
        try {
            db.delete(TABLE_TRANSLATION_INFO, null, null)

            val values = ContentValues(5)
            for (t in translations) {
                values.put(COLUMN_SHORT_NAME, t.shortName)
                values.put(COLUMN_NAME, t.name)
                values.put(COLUMN_LANGUAGE, t.language)
                values.put(COLUMN_SIZE, t.size)
                values.put(COLUMN_DOWNLOADED, if (t.downloaded) 1 else 0)
                db.insertWithOnConflict(TABLE_TRANSLATION_INFO, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    @WorkerThread
    fun save(translation: TranslationInfo) {
        val values = ContentValues(5)
        values.put(COLUMN_SHORT_NAME, translation.shortName)
        values.put(COLUMN_NAME, translation.name)
        values.put(COLUMN_LANGUAGE, translation.language)
        values.put(COLUMN_SIZE, translation.size)
        values.put(COLUMN_DOWNLOADED, if (translation.downloaded) 1 else 0)
        db.insertWithOnConflict(TABLE_TRANSLATION_INFO, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
}

class MetadataDao(private val sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_METADATA = "metadata"
        private const val COLUMN_KEY = "key"
        private const val COLUMN_VALUE = "value"

        const val KEY_CURRENT_TRANSLATION = "currentTranslation"

        @WorkerThread
        fun createTable(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_METADATA (" +
                    "$COLUMN_KEY TEXT PRIMARY KEY, $COLUMN_VALUE TEXT NOT NULL);")
        }
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun load(key: String, defaultValue: String): String {
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_METADATA, arrayOf(COLUMN_VALUE),
                    "$COLUMN_KEY = ?", arrayOf(key), null, null, null)
            return if (cursor.count > 0 && cursor.moveToNext()) {
                cursor.getString(0)
            } else {
                defaultValue
            }
        } finally {
            cursor?.close()
        }
    }

    @WorkerThread
    fun save(key: String, value: String) {
        val values = ContentValues(2)
        values.put(COLUMN_KEY, key)
        values.put(COLUMN_VALUE, value)
        db.insertWithOnConflict(TABLE_METADATA, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
}