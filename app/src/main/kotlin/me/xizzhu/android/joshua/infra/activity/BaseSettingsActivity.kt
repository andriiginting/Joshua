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

package me.xizzhu.android.joshua.infra.activity

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import dagger.android.AndroidInjection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.onEachSuccess
import me.xizzhu.android.joshua.ui.getBackgroundColor

// TODO switch to inherit from BaseViewModel
abstract class BaseSettingsViewModel(protected val settingsManager: SettingsManager) : ViewModel() {
    protected val tag: String = javaClass.simpleName

    fun settings(): Flow<ViewData<Settings>> = settingsManager.settings().map { ViewData.success(it) }
}

// TODO switch to inherit from ViewPresenter
abstract class BaseSettingsPresenter<VH : ViewHolder, VM : BaseSettingsViewModel>(
        protected val viewModel: VM, private val lifecycle: Lifecycle,
        protected val lifecycleScope: LifecycleCoroutineScope
) : LifecycleObserver {
    protected val tag: String = javaClass.simpleName

    protected lateinit var viewHolder: VH

    @UiThread
    fun bind(viewHolder: VH) {
        this.viewHolder = viewHolder
        onBind()
        lifecycle.addObserver(this)
    }

    @UiThread
    @CallSuper
    open fun onBind() {
    }
}

// TODO switch to inherit from BaseActivity
abstract class BaseSettingsActivity : AppCompatActivity() {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        getBaseSettingsViewModel().settings().onEachSuccess { settings ->
            with(window.decorView) {
                keepScreenOn = settings.keepScreenOn
                setBackgroundColor(settings.getBackgroundColor())
            }
        }.launchIn(lifecycleScope)
    }

    abstract fun getBaseSettingsViewModel(): BaseSettingsViewModel
}
