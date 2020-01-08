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

package me.xizzhu.android.joshua.strongnumber

import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Test
import kotlin.test.assertEquals

class VerseStrongNumberItemTest : BaseUnitTest() {
    @Test
    fun testItemViewType() {
        assertEquals(R.layout.item_verse_strong_number, VerseStrongNumberItem(VerseIndex.INVALID, "", "", {}).viewType)
    }

    @Test
    fun testTextForDisplay() {
        assertEquals(
                "Gen. 1:1 In the beginning God created the heaven and the earth.",
                VerseStrongNumberItem(
                        MockContents.kjvVerses[0].verseIndex, MockContents.kjvBookShortNames[0], MockContents.kjvVerses[0].text.text, {}
                ).textForDisplay.toString()
        )
    }
}
