/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.cooeeui.brand.zenlauncher.apps;

import android.content.ContentValues;
import android.content.Intent;

import com.cooeeui.brand.zenlauncher.LauncherSettings;

/**
 * Represents an item in the launcher.
 */
public class ItemInfo {

    /**
     *
     */

    public static final int NO_ID = -1;

    /**
     * The id in the settings database for this item
     */
    public long id = NO_ID;

    /**
     * Title of the item
     */
    public CharSequence title;

    /**
     * The intent used to start the application.
     */
    public Intent intent;

    /**
     * One of {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION} , {@link
     * LauncherSettings.BaseLauncherColumns#ITEM_TYPE_SHORTCUT}
     */
    public int itemType;

    public ItemInfo() {
        title = null;
        itemType = LauncherSettings.ITEM_TYPE_NONE;
        intent = null;
    }

    ItemInfo(ItemInfo info) {
        id = info.id;
        title = info.title.toString();
        itemType = info.itemType;
        intent = new Intent(info.intent);
    }

    public Intent getIntent() {
        return intent;
    }

    /**
     * Write the fields of this item to the DB
     */
    public void onAddToDatabase(ContentValues values) {
        //对浏览器进行特殊处理
        String uri = intent != null ? intent.toUri(0) : "*BROWSER*";
        values.put(LauncherSettings.BaseLauncherColumns.INTENT, uri);
    }

    /**
     * It is very important that sub-classes implement this if they contain any references to the
     * activity (anything in the view hierarchy etc.). If not, leaks can result since ItemInfo
     * objects persist across rotation and can hence leak by holding stale references to the old
     * view hierarchy / activity.
     */
    public void unbind() {
    }

    @Override
    public String toString() {
        return "Item(id=" + this.id + " type=" + this.itemType + ")";
    }
}
