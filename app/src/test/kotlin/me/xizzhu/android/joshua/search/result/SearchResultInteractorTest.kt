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

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchResultInteractorTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var searchResultInteractor: SearchResultInteractor

    @BeforeTest
    override fun setup() {
        super.setup()

        searchResultInteractor = SearchResultInteractor(bibleReadingManager, settingsManager, testDispatcher)
    }

    @Test
    fun testSearchRequest() = testDispatcher.runBlockingTest {
        val requestSearchAsync = async { searchResultInteractor.searchRequested().take(3).toList() }

        val queries = listOf("query1", "query2", "query3")
        queries.forEach { searchResultInteractor.requestSearch(it) }

        assertEquals(queries, requestSearchAsync.await())
    }
}
