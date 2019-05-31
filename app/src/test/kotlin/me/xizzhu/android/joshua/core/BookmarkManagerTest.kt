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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.channels.first
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.repository.BookmarkRepository
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals

class BookmarkManagerTest : BaseUnitTest() {
    @Mock
    private lateinit var bookmarkRepository: BookmarkRepository

    private lateinit var bookmarkManager: BookmarkManager

    @Before
    override fun setup() {
        super.setup()

        bookmarkManager = BookmarkManager(bookmarkRepository)
    }

    @Test
    fun testObserveSortOrder() {
        runBlocking {
            `when`(bookmarkRepository.readSortOrder()).thenReturn(Constants.SORT_BY_BOOK)
            assertEquals(Constants.SORT_BY_BOOK, bookmarkManager.observeSortOrder().first())
        }
    }

    @Test
    fun testObserveSortOrderWithException() {
        runBlocking {
            `when`(bookmarkRepository.readSortOrder()).thenThrow(RuntimeException("Random exception"))
            assertEquals(Constants.DEFAULT_SORT_ORDER, bookmarkManager.observeSortOrder().first())
        }
    }
}