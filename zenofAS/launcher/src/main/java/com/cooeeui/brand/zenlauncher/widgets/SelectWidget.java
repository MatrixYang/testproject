package com.cooeeui.brand.zenlauncher.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.LauncherModel;
import com.cooeeui.brand.zenlauncher.wallpaper.util.PreferencesUtils;
import com.cooeeui.zenlauncher.R;

import java.util.ArrayList;
import java.util.List;

public class SelectWidget extends Activity {

    public static final String EXTRA_APPWIDGET_INFO = "appWidgetInfo";

    private ArrayList<AppWidgetProviderInfo> mWidgets;

    private PackageManager mPackageManager;

    private ArrayList<AppWidgetProviderInfo> mNanoWidgets;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DeviceUtils.hasMeiZuSmartBar()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            DeviceUtils.hideNavigationBar(getWindow().getDecorView());
            if (Build.VERSION.SDK_INT >= 19) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        } else if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        setContentView(R.layout.widget_list);

        mPackageManager = getPackageManager();
        mNanoWidgets = new ArrayList<>();
        if (!getIntent().getBooleanExtra("home_widget", true)) {
            Intent intentLockView = new Intent(NanoWidgetUtils.ACTION_WIDGET_VIEW);
            List<ResolveInfo>
                infoList =
                mPackageManager
                    .queryBroadcastReceivers(intentLockView, PackageManager.GET_META_DATA);
            for (ResolveInfo info : infoList) {
                ComponentName componentName = new ComponentName(info.activityInfo.packageName,
                                                                info.activityInfo.name);
                AppWidgetProviderInfo
                    nanowidget =
                    NanoWidgetUtils.getNanoWidgetProviderInfo(this, componentName);
                if (nanowidget.minWidth > 0 && nanowidget.minHeight > 0) {
                    int[] spanXY = Launcher.getSpanForWidget(this, nanowidget);
                    if (spanXY[1] < 7) {
                        mNanoWidgets.add(nanowidget);
                    }
                }
            }
        }

        mWidgets = new ArrayList<AppWidgetProviderInfo>();
        ArrayList<AppWidgetProviderInfo> widgets = LauncherModel.getSortedWidgets(this);
        for (AppWidgetProviderInfo info : widgets) {
            if (info.minWidth > 0 && info.minHeight > 0) {
                int[] spanXY = Launcher.getSpanForWidget(this, info);
                if (spanXY[1] < 7) {
                    mWidgets.add(info);
                }
            }
        }

        ListView list = (ListView) findViewById(R.id.widget_list);
        list.setAdapter(new AddWidgetAdapter());
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position < mNanoWidgets.size()) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(EXTRA_APPWIDGET_INFO, mNanoWidgets.get(position));
                    intent.putExtras(bundle);
                    intent.putExtra("nano_widget", true);
                    SelectWidget.this.setResult(0, intent);
                } else {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(EXTRA_APPWIDGET_INFO,
                                         mWidgets.get(position - mNanoWidgets.size()));
                    intent.putExtras(bundle);
                    SelectWidget.this.setResult(0, intent);
                }
                SelectWidget.this.finish();
                SelectWidget.this.overridePendingTransition(R.anim.push_left_in,
                                                            R.anim.push_left_out);
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Launcher.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private class AddWidgetAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mWidgets.size() + mNanoWidgets.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(SelectWidget.this)
                    .inflate(R.layout.widget_list_item, null);
            }

            ImageView imageView = (ImageView) convertView.findViewById(R.id.item_image);
            TextView textView = (TextView) convertView.findViewById(R.id.item_text);
            ImageView imageViewTip = (ImageView) convertView.findViewById(R.id.new_tip);

            if (position < mNanoWidgets.size()) {
                String packageName = mNanoWidgets.get(position).provider.getPackageName();

                Drawable drawable = mPackageManager.getDrawable(packageName,
                                                                mNanoWidgets.get(position).icon,
                                                                null);

                if (drawable != null) {
                    imageView.setImageDrawable(drawable);
                }

                textView.setText(mNanoWidgets.get(position).label);
                textView.setTextColor(Color.WHITE);
                if (!PreferencesUtils.getBoolean(SelectWidget.this, "weather_widget", false)) {
                    imageViewTip.setVisibility(View.VISIBLE);
                }
            } else {
                String
                    packageName =
                    mWidgets.get(position - mNanoWidgets.size()).provider.getPackageName();

                Drawable drawable = mPackageManager.getDrawable(packageName,
                                                                mWidgets.get(position - mNanoWidgets
                                                                    .size()).icon, null);

                if (drawable != null) {
                    imageView.setImageDrawable(drawable);
                }

                textView.setText(mWidgets.get(position - mNanoWidgets.size()).label);
                textView.setTextColor(Color.WHITE);
                imageViewTip.setVisibility(View.GONE);
            }

            return convertView;
        }

    }

}
