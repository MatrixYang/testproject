package com.cooeeui.brand.zenlauncher.appIntentUtils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.brand.zenlauncher.utils.LauncherConstants;
import com.cooeeui.zenlauncher.R;

import java.util.ArrayList;
import java.util.List;

public class BrowserIntentUtil {

    private Context context = null;
    private List<ResolveInfo> listInfo = null;
    private Button positiveButton = null;
    private Button negativeButton = null;
    private int intentposition = -1;
    public Intent browserIntent = null;
    public static final String CLASS_NAME = "getClassName";
    public static final String PACKAGE_NAME = "getPackageName";

    public AlertDialog chooseDialog;

    public BrowserIntentUtil(Context context) {
        this.context = context;
    }

    @SuppressLint("NewApi")
    public void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                                                              AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(StringUtil.getString(context, R.string.chooseApp));
        builder.setNegativeButton(StringUtil.getString(context, R.string.always),
                                  new OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialog, int which) {
                                          browserIntent = getIntentByPosition();
                                          saveIntentInfo(context, browserIntent);
                                          context.startActivity(browserIntent);
                                      }

                                  });
        builder.setPositiveButton(StringUtil.getString(context, R.string.once),
                                  new OnClickListener() {

                                      @Override
                                      public void onClick(DialogInterface dialog, int which) {
                                          Intent intent = getIntentByPosition();
                                          context.startActivity(intent);
                                      }
                                  });
        ListView listView = new ListView(context);
        listInfo = findAllbrowserApp();
        final MyBrowserAdapter adapter = new MyBrowserAdapter(listInfo, context);
        listView.setAdapter(adapter);
        builder.setView(listView);
        chooseDialog = builder.create();
        chooseDialog.setCanceledOnTouchOutside(true);
        chooseDialog.show();// 必现要先show以后，获得的positiveButton和negativeButton才不会为null
        positiveButton = chooseDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        negativeButton = chooseDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        positiveButton.setEnabled(false);
        negativeButton.setEnabled(false);
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                intentposition = position;
                if (parent != null) {
                    adapter.setSelectItem(position);
                    adapter.notifyDataSetInvalidated();
                    positiveButton.setEnabled(true);
                    negativeButton.setEnabled(true);
                }
            }
        });
    }

    private Intent getIntentByPosition() {
        Intent intent = null;
        if (listInfo != null && listInfo.size() > intentposition) {
            ResolveInfo info = listInfo.get(intentposition);
            String pkgName = info.activityInfo.packageName;
            String clsName = info.activityInfo.name;
            ComponentName cp = new ComponentName(pkgName, clsName);
            intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(cp);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }
        return intent;
    }

    boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                                                                      PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * 对intent做持久化
     *
     * @param context 上下文对象
     * @param intent  持久化对象
     */
    private void saveIntentInfo(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        Log.i("browserIntent", "browserIntent:  " + "  " +
                               intent.getComponent().getClassName() + "  " +
                               intent.getComponent().getPackageName()
        );
        SharedPreferences sharedPreference = context.getSharedPreferences(
            LauncherConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Editor edit = sharedPreference.edit();
        edit.putString(CLASS_NAME, intent.getComponent().getClassName());
        edit.putString(PACKAGE_NAME, intent.getComponent().getPackageName());
        edit.commit();
    }

    /**
     * 查找手机中所有的
     */
    public List<ResolveInfo> findAllbrowserApp() {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://"));
        List<ResolveInfo> listInfo1 = new ArrayList<ResolveInfo>();
        List<ResolveInfo> list = pm.queryIntentActivities(intent,
                                                          PackageManager.GET_INTENT_FILTERS);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            String pkgName = info.activityInfo.packageName;
            String clsName = info.activityInfo.name;
            ComponentName cp = new ComponentName(pkgName, clsName);
            intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(cp);
            if (isIntentAvailable(context, intent)) {
                listInfo1.add(info);
            }
        }
        return listInfo1;
    }

    private class MyBrowserAdapter extends BaseAdapter {

        private List<ResolveInfo> listInfo = null;
        private Context context = null;
        private PackageManager pm = null;

        public MyBrowserAdapter(List<ResolveInfo> listInfo, Context context) {
            this.listInfo = listInfo;
            this.context = context;
            pm = context.getPackageManager();
        }

        @Override
        public int getCount() {
            return listInfo.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressWarnings("deprecation")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(
                    R.layout.browser_adapter_layout,
                    null);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.browser_iamge);
                viewHolder.textView = (TextView) convertView.findViewById(R.id.browser_text);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ResolveInfo info = listInfo.get(position);
            viewHolder.imageView.setBackgroundDrawable(info.loadIcon(pm));
            String browserName;
            if (info.loadLabel(pm).toString().contains("UC")) {
                browserName = (String) info
                    .loadLabel(pm)
                    .toString()
                    .subSequence(info.loadLabel(pm).toString().indexOf("UC"),
                                 info.loadLabel(pm).toString().length());
            } else {
                browserName = info.loadLabel(pm).toString();
            }
            viewHolder.textView.setText(browserName.trim());
            if (position == selectItem) {
                convertView.setBackgroundColor(0xff1aa3d2);
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }
            return convertView;
        }

        public void setSelectItem(int selectItem) {
            this.selectItem = selectItem;
        }

        private int selectItem = -1;
    }

    class ViewHolder {

        ImageView imageView;
        TextView textView;
    }
}
