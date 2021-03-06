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

package me.xizzhu.android.joshua.translations

import kotlinx.coroutines.flow.*
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.infra.activity.BaseSettingsViewModel

data class TranslationList(val currentTranslation: String,
                           val availableTranslations: List<TranslationInfo>,
                           val downloadedTranslations: List<TranslationInfo>)

class TranslationsViewModel(private val bibleReadingManager: BibleReadingManager,
                            private val translationManager: TranslationManager,
                            settingsManager: SettingsManager) : BaseSettingsViewModel(settingsManager) {
    fun translationList(forceRefresh: Boolean): Flow<TranslationList> = flow {
        translationManager.reload(forceRefresh)
        emit(TranslationList(
                bibleReadingManager.currentTranslation().first(),
                translationManager.availableTranslations().first(),
                translationManager.downloadedTranslations().first()
        ))
    }

    suspend fun saveCurrentTranslation(translationShortName: String) {
        bibleReadingManager.saveCurrentTranslation(translationShortName)
    }

    fun downloadTranslation(translationToDownload: TranslationInfo): Flow<Int> =
            translationManager.downloadTranslation(translationToDownload)

    fun removeTranslation(translationToRemove: TranslationInfo): Flow<Unit> = flow {
        translationManager.removeTranslation(translationToRemove)
    }
}
