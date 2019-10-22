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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.interactors.BaseSettingsAndLoadingAwareInteractor

class SearchResultInteractor(private val bibleReadingManager: BibleReadingManager,
                             settingsManager: SettingsManager,
                             dispatcher: CoroutineDispatcher = Dispatchers.Default)
    : BaseSettingsAndLoadingAwareInteractor(settingsManager, dispatcher) {
    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val searchRequested: BroadcastChannel<String> = ConflatedBroadcastChannel()

    fun searchRequested(): Flow<String> = searchRequested.asFlow()

    fun requestSearch(query: String) {
        searchRequested.offer(query)
    }

    suspend fun search(query: String): List<Verse> = bibleReadingManager.search(readCurrentTranslation(), query)

    suspend fun readCurrentTranslation(): String = bibleReadingManager.observeCurrentTranslation().first()

    suspend fun readBookNames(translationShortName: String): List<String> =
            bibleReadingManager.readBookNames(translationShortName)

    suspend fun readBookShortNames(translationShortName: String): List<String> =
            bibleReadingManager.readBookShortNames(translationShortName)

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }
}