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
import android.graphics.Bitmap;

import com.cooeeui.brand.zenlauncher.LauncherSettings;
import com.cooeeui.brand.zenlauncher.scenes.utils.IconNameOrId;

public class ShortcutInfo extends ItemInfo {

    public static final int NO_ID = -1;

    public int position;

    public Bitmap mIcon;

    public boolean mRecycle;

    public int mIconId;

    public ShortcutInfo() {
        super();
        itemType = LauncherSettings.ITEM_TYPE_SHORTCUT;
    }

    public ShortcutInfo(AppInfo info) {
        super(info);
        itemType = LauncherSettings.ITEM_TYPE_SHORTCUT;
        mIcon = info.iconBitmap;
        mIconId = NO_ID;
        mRecycle = false;
    }

    @Override
    public void onAddToDatabase(ContentValues values) {
        super.onAddToDatabase(values);
        values.put(LauncherSettings.Shortcuts.POSITION, position);
        String name = IconNameOrId.getIconName(mIconId);
        values.put(LauncherSettings.Shortcuts.ICON_NAME, name);
    }

    @Override
    public String toString() {
        return "ShortcutInfo(title=" + title.toString() + ", intent=" + intent + ", id=" + this.id
               + ", type=" + this.itemType + ", position=" + this.position + ")";
    }
}
