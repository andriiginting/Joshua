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

import android.content.DialogInterface
import android.view.View
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.coroutineScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.*
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.logger.Log
import java.util.*

data class TranslationListViewHolder(
        val swipeRefreshLayout: SwipeRefreshLayout, val translationListView: CommonRecyclerView
) : ViewHolder

class TranslationListPresenter(
        private val translationsActivity: TranslationsActivity, translationsViewModel: TranslationsViewModel,
        lifecycle: Lifecycle, lifecycleScope: LifecycleCoroutineScope = lifecycle.coroutineScope
) : BaseSettingsPresenter<TranslationListViewHolder, TranslationsViewModel>(translationsViewModel, lifecycle, lifecycleScope) {
    private val translationComparator = TranslationInfoComparator(TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_NAME)

    private var downloadingJob: Job? = null
    private var downloadTranslationDialog: ProgressDialog? = null
    private var removingJob: Job? = null
    private var removeTranslationDialog: AlertDialog? = null

    @UiThread
    override fun onBind() {
        super.onBind()

        with(viewHolder.swipeRefreshLayout) {
            setColorSchemeResources(R.color.primary_dark, R.color.primary, R.color.dark_cyan, R.color.dark_lime)
            setOnRefreshListener { loadTranslationList(true) }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        observeSettings()
        loadTranslationList(false)
    }

    private fun observeSettings() {
        viewModel.settings().onEachSuccess { viewHolder.translationListView.setSettings(it) }.launchIn(lifecycleScope)
    }

    private fun loadTranslationList(forceRefresh: Boolean) {
        viewModel.translationList(forceRefresh).onEach(
                onLoading = {
                    with(viewHolder) {
                        swipeRefreshLayout.isRefreshing = true
                        translationListView.visibility = View.GONE
                    }
                },
                onSuccess = { viewData ->
                    with(viewHolder) {
                        swipeRefreshLayout.isRefreshing = false
                        translationListView.setItems(viewData.toItems())
                        translationListView.fadeIn()
                    }
                },
                onError = { _, _ ->
                    viewHolder.swipeRefreshLayout.isRefreshing = false
                    translationsActivity.dialog(false, R.string.dialog_load_translation_list_error,
                            DialogInterface.OnClickListener { _, _ -> loadTranslationList(forceRefresh) },
                            DialogInterface.OnClickListener { _, _ -> translationsActivity.finish() })
                }
        ).launchIn(lifecycleScope)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun TranslationList.toItems(): List<BaseItem> {
        val items: ArrayList<BaseItem> = ArrayList()
        val availableTranslations = availableTranslations.sortedWith(translationComparator)
        val downloadedTranslations = downloadedTranslations.sortedWith(translationComparator)
        items.addAll(downloadedTranslations.toItems(currentTranslation))
        if (availableTranslations.isNotEmpty()) {
            items.add(TitleItem(translationsActivity.getString(R.string.header_available_translations), false))
            items.addAll(availableTranslations.toItems(currentTranslation))
        }
        return items
    }

    private fun List<TranslationInfo>.toItems(currentTranslation: String): List<BaseItem> {
        val items: ArrayList<BaseItem> = ArrayList()
        var currentLanguage = ""
        for (translationInfo in this@toItems) {
            val language = translationInfo.language.split("_")[0]
            if (currentLanguage != language) {
                items.add(TitleItem(Locale(language).displayLanguage, true))
                currentLanguage = language
            }
            items.add(TranslationItem(
                    translationInfo,
                    translationInfo.downloaded && translationInfo.shortName == currentTranslation,
                    this@TranslationListPresenter::onTranslationClicked,
                    this@TranslationListPresenter::onTranslationLongClicked
            ))
        }
        return items
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun onTranslationClicked(translationInfo: TranslationInfo) {
        if (translationInfo.downloaded) {
            updateCurrentTranslationAndFinishActivity(translationInfo.shortName)
        } else {
            downloadTranslation(translationInfo)
        }
    }

    private fun updateCurrentTranslationAndFinishActivity(translationShortName: String) {
        lifecycleScope.launch {
            try {
                viewModel.saveCurrentTranslation(translationShortName)
                translationsActivity.finish()
            } catch (e: Exception) {
                Log.e(tag, "Failed to select translation and close translation management activity", e)
                translationsActivity.dialog(true, R.string.dialog_update_translation_error,
                        DialogInterface.OnClickListener { _, _ -> updateCurrentTranslationAndFinishActivity(translationShortName) })
            }
        }
    }

    private fun downloadTranslation(translationToDownload: TranslationInfo) {
        if (downloadingJob != null || downloadTranslationDialog != null) {
            // just in case the user clicks too fast
            return
        }
        downloadTranslationDialog = translationsActivity.progressDialog(
                R.string.dialog_downloading, 100) { downloadingJob?.cancel() }

        downloadingJob = viewModel.downloadTranslation(translationToDownload).onEach(
                onLoading = {
                    it?.let { progress ->
                        downloadTranslationDialog?.run {
                            if (progress < 100) {
                                setProgress(progress)
                            } else {
                                setTitle(R.string.dialog_installing)
                                setIsIndeterminate(true)
                            }
                        }
                    }
                            ?: throw IllegalStateException("Missing progress data when downloading")
                },
                onSuccess = {
                    translationsActivity.toast(R.string.toast_downloaded)
                    loadTranslationList(false)
                },
                onError = { _, e ->
                    Log.e(tag, "Failed to download translation", e!!)
                    translationsActivity.dialog(true, R.string.dialog_download_error,
                            DialogInterface.OnClickListener { _, _ -> downloadTranslation(translationToDownload) })
                }
        ).onCompletion {
            downloadTranslationDialog?.dismiss()
            downloadTranslationDialog = null
            downloadingJob = null
        }.launchIn(lifecycleScope)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun onTranslationLongClicked(translationInfo: TranslationInfo, isCurrentTranslation: Boolean) {
        if (translationInfo.downloaded) {
            if (!isCurrentTranslation) {
                translationsActivity.dialog(true, R.string.dialog_delete_translation_confirmation,
                        DialogInterface.OnClickListener { _, _ -> removeTranslation(translationInfo) })
            }
        } else {
            downloadTranslation(translationInfo)
        }
    }

    private fun removeTranslation(translationToRemove: TranslationInfo) {
        if (removeTranslationDialog != null) {
            // just in case the user clicks too fast
            return
        }
        removeTranslationDialog = translationsActivity.indeterminateProgressDialog(R.string.dialog_deleting)

        removingJob = viewModel.removeTranslation(translationToRemove).onEach(
                onLoading = { /* do nothing */ },
                onSuccess = {
                    translationsActivity.toast(R.string.toast_deleted)
                    loadTranslationList(false)
                },
                onError = { _, e ->
                    Log.e(tag, "Failed to remove translation", e!!)
                    translationsActivity.dialog(true, R.string.dialog_delete_error,
                            DialogInterface.OnClickListener { _, _ -> removeTranslation(translationToRemove) })
                }
        ).onCompletion {
            removeTranslationDialog?.dismiss()
            removeTranslationDialog = null
            removingJob = null
        }.launchIn(lifecycleScope)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        downloadingJob?.cancel()
        removingJob?.cancel()
    }
}
