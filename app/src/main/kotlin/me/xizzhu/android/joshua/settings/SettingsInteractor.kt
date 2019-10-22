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

package me.xizzhu.android.joshua.settings

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.arch.Interactor
import me.xizzhu.android.joshua.infra.arch.ViewData
import java.io.InputStream
import java.io.OutputStream

class SettingsInteractor(private val settingsManager: SettingsManager,
                         private val backupManager: BackupManager,
                         dispatcher: CoroutineDispatcher = Dispatchers.Default) : Interactor(dispatcher) {
    fun settings(): Flow<ViewData<Settings>> = settingsManager.observeSettings().map { ViewData.success(it) }

    suspend fun saveFontSizeScale(fontSizeScale: Int) {
        settingsManager.saveFontSizeScale(fontSizeScale)
    }

    suspend fun saveKeepScreenOn(keepScreenOn: Boolean) {
        settingsManager.saveKeepScreenOn(keepScreenOn)
    }

    suspend fun saveNightModeOn(nightModeOn: Boolean) {
        settingsManager.saveNightModeOn(nightModeOn)
    }

    suspend fun saveSimpleReadingModeOn(simpleReadingModeOn: Boolean) {
        settingsManager.saveSimpleReadingModeOn(simpleReadingModeOn)
    }

    suspend fun backup(to: OutputStream) {
        to.write(backupManager.prepareForBackup().toByteArray(Charsets.UTF_8))
    }

    suspend fun restore(from: InputStream) {
        backupManager.restore(String(from.readBytes(), Charsets.UTF_8))
    }
}