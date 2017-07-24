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

package com.cooeeui.brand.zenlauncher;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Settings related utilities.
 */
public class LauncherSettings {

    public static final int ITEM_TYPE_NONE = -1;
    /**
     * The gesture is an application
     */
    public static final int ITEM_TYPE_APPLICATION = 0;

    /**
     * The gesture is a shortcut of application
     */
    public static final int ITEM_TYPE_SHORTCUT = 1;

    /**
     * The type is common time
     */
    public static final int ITEM_TYPE_COMMON_TIME = 2;
    /**
     * The type is APP used time
     */
    public static final int ITEM_TYPE_APPS_TIME = 3;

    public static final int ITEM_TYPE_APPWIDGET = 4;

    /**
     * Columns required on table state will be subject to backup and restore.
     */
    public static interface ChangeLogColumns extends BaseColumns {

        /**
         * The time of the last update to this row. <P> Type: INTEGER </P>
         */
        public static final String MODIFIED = "modified";
    }

    public static interface BaseLauncherColumns extends ChangeLogColumns {

        /**
         * Descriptive name of the gesture that can be displayed to the user. <P> Type: TEXT </P>
         */
        public static final String TITLE = "title";

        /**
         * The Intent URL of the gesture, describing what it points to. This value is given to
         * {@link android.content.Intent#parseUri(String, int)} to create an Intent that can be
         * launched. <P> Type: TEXT </P>
         */
        public static final String INTENT = "intent";

        /**
         * The type of the gesture <P> Type: INTEGER </P>
         */
        public static final String ITEM_TYPE = "itemType";
    }

    /**
     * Shortcuts in workspaces .
     */
    public static final class Shortcuts implements BaseLauncherColumns {

        /**
         * The position of the cell holding the shortcuts <P> Type: INTEGER </P>
         */
        public static final String POSITION = "position";

        /**
         * The icon name. <P> Type: TEXT </P>
         */
        public static final String ICON_NAME = "iconName";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" +
                                                        LauncherProvider.AUTHORITY + "/"
                                                        + LauncherProvider.TABLE_SHORTCUTS +
                                                        "?" + LauncherProvider.PARAMETER_NOTIFY
                                                        + "=true");

        /**
         * The content:// style URL for this table. When this Uri is used, no notification is sent
         * if the content changes.
         */
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri.parse("content://" +
                                                                        LauncherProvider.AUTHORITY
                                                                        + "/"
                                                                        + LauncherProvider.TABLE_SHORTCUTS
                                                                        +
                                                                        "?"
                                                                        + LauncherProvider.PARAMETER_NOTIFY
                                                                        + "=false");

        /**
         * The content:// style URL for a given row, identified by its id.
         *
         * @param id     The row id.
         * @param notify True to send a notification is the content changes.
         * @return The unique content URL for the specified row.
         */
        public static Uri getContentUri(long id, boolean notify) {
            return Uri.parse("content://" + LauncherProvider.AUTHORITY +
                             "/" + LauncherProvider.TABLE_SHORTCUTS + "/" + id + "?" +
                             LauncherProvider.PARAMETER_NOTIFY + "=" + notify);
        }
    }

    public static final class Widgets implements BaseLauncherColumns {

        public static final String APPWIDGET_ID = "appWidgetId";

        public static final String APPWIDGET_PROVIDER = "appWidgetProvider";

        public static final String WIDTH = "width";

        public static final String HEIGHT = "height";

        public static final String SPANX = "spanX";

        public static final String SPANY = "spanY";

        public static final String POSITION = "position";

        public static final String TYPE = "type";

        public static final Uri CONTENT_URI = Uri.parse("content://" +
                                                        LauncherProvider.AUTHORITY + "/"
                                                        + LauncherProvider.TABLE_WIDGETS +
                                                        "?" + LauncherProvider.PARAMETER_NOTIFY
                                                        + "=true");

        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri.parse("content://" +
                                                                        LauncherProvider.AUTHORITY
                                                                        + "/"
                                                                        + LauncherProvider.TABLE_WIDGETS
                                                                        +
                                                                        "?"
                                                                        + LauncherProvider.PARAMETER_NOTIFY
                                                                        + "=false");

        public static Uri getContentUri(long id, boolean notify) {
            return Uri.parse("content://" + LauncherProvider.AUTHORITY +
                             "/" + LauncherProvider.TABLE_WIDGETS + "/" + id + "?" +
                             LauncherProvider.PARAMETER_NOTIFY + "=" + notify);
        }
    }

    /**
     * Applications in drawer.
     */
    public static class Applications implements BaseLauncherColumns {

        /**
         * The category id of icon. <p> Type: INTEGER </p>
         */
        public static final String ORIGINAL_CATEGORY = "originalCategory";

        /**
         * The category id of icon. <p> Type: INTEGER </p>
         */
        public static final String CATEGORY = "category";

        /**
         * The launch times of app, a factor to decide favorite. <p> Type: INTEGER </p>
         */
        public static final String LAUNCH_TIMES = "launchTimes";

        /**
         * The visibility of icon, true when visible.otherwise false. <p> Type: INTEGER </p>
         */
        public static final String HIDE = "hide";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" +
                                                        LauncherProvider.AUTHORITY + "/"
                                                        + LauncherProvider.TABLE_APPS +
                                                        "?" + LauncherProvider.PARAMETER_NOTIFY
                                                        + "=true");

        /**
         * The content:// style URL for this table. When this Uri is used, no notification is sent
         * if the content changes.
         */
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri.parse("content://" +
                                                                        LauncherProvider.AUTHORITY
                                                                        + "/"
                                                                        + LauncherProvider.TABLE_APPS
                                                                        +
                                                                        "?"
                                                                        + LauncherProvider.PARAMETER_NOTIFY
                                                                        + "=false");

        /**
         * The content:// style URL for a given row, identified by its id.
         *
         * @param id     The row id.
         * @param notify True to send a notification is the content changes.
         * @return The unique content URL for the specified row.
         */
        public static Uri getContentUri(long id, boolean notify) {
            return Uri.parse("content://" + LauncherProvider.AUTHORITY +
                             "/" + LauncherProvider.TABLE_APPS + "/" + id + "?" +
                             LauncherProvider.PARAMETER_NOTIFY + "=" + notify);
        }
    }

    /**
     * the table of app used time
     */
    public static final class AppsUsedTime implements ChangeLogColumns {

        /**
         * The start time of the special app <p> Type: TEXT </p>
         */
        public static final String START_TIME = "start_time";

        /**
         * The time when the user quit the app <P> Type: TEXT </P>
         */
        public static final String QUIT_TIME = "quit_time";

        /**
         * The long of the used app <p> Type: INTEGER </p>
         */
        public static final String TIME = "time";

        /**
         * The package name of the app <P> Type: TEXT </P>
         */
        public static final String PCK_NAME = "pck_name";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" +
                                                        LauncherProvider.AUTHORITY + "/"
                                                        + LauncherProvider.TABLE_APPS_USEDTIME +
                                                        "?" + LauncherProvider.PARAMETER_NOTIFY
                                                        + "=true");

        /**
         * The content:// style URL for this table. When this Uri is used, no notification is sent
         * if the content changes.
         */
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri.parse("content://" +
                                                                        LauncherProvider.AUTHORITY
                                                                        + "/"
                                                                        + LauncherProvider.TABLE_APPS_USEDTIME
                                                                        +
                                                                        "?"
                                                                        + LauncherProvider.PARAMETER_NOTIFY
                                                                        + "=false");

        /**
         * The content:// style URL for a given row, identified by its id.
         *
         * @param id     The row id.
         * @param notify True to send a notification is the content changes.
         * @return The unique content URL for the specified row.
         */
        public static Uri getContentUri(long id, boolean notify) {
            return Uri.parse("content://" + LauncherProvider.AUTHORITY +
                             "/" + LauncherProvider.TABLE_APPS_USEDTIME + "/" + id + "?" +
                             LauncherProvider.PARAMETER_NOTIFY + "=" + notify);
        }

    }

    /**
     * time of unlock times and phone time
     */
    public static final class Common_Time implements ChangeLogColumns {

        /**
         * the time of the operation <p> Type: TEXT </p>
         */
        public static final String INPUT_TIME = "input_time";

        /**
         * how long of phone used. the unit is minute <P> Type: INTEGER </P>
         */
        public static final String PHONE_TIME = "phone_time";

        /**
         * the last operation of current day. <P> Type: INTEGER </P>
         */
        public static final String LOCK_TYPE = "lock_type";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" +
                                                        LauncherProvider.AUTHORITY + "/"
                                                        + LauncherProvider.TABLE_COMMON_TIME +
                                                        "?" + LauncherProvider.PARAMETER_NOTIFY
                                                        + "=true");

        /**
         * The content:// style URL for this table. When this Uri is used, no notification is sent
         * if the content changes.
         */
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri.parse("content://" +
                                                                        LauncherProvider.AUTHORITY
                                                                        + "/"
                                                                        + LauncherProvider.TABLE_COMMON_TIME
                                                                        +
                                                                        "?"
                                                                        + LauncherProvider.PARAMETER_NOTIFY
                                                                        + "=false");

        /**
         * The content:// style URL for a given row, identified by its id.
         *
         * @param id     The row id.
         * @param notify True to send a notification is the content changes.
         * @return The unique content URL for the specified row.
         */
        public static Uri getContentUri(long id, boolean notify) {
            return Uri.parse("content://" + LauncherProvider.AUTHORITY +
                             "/" + LauncherProvider.TABLE_COMMON_TIME + "/" + id + "?" +
                             LauncherProvider.PARAMETER_NOTIFY + "=" + notify);
        }

    }
}
