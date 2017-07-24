package com.cooeeui.brand.zenlauncher.changeicon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import com.cooeeui.zenlauncher.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/4/5.
 */
public class AppIconSpinnerAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> iconAppName = new ArrayList<String>();

    public AppIconSpinnerAdapter(Context context, List<String> iconAppName) {
        this.mContext = context;
        this.iconAppName.clear();
        this.iconAppName = iconAppName;
    }

//    public void updateIconAppName(){
//        this.iconAppName.clear();
//        this.iconAppName =
//    }

    @Override
    public int getCount() {
        return this.iconAppName.size();
    }

    @Override
    public Object getItem(int position) {
        return this.iconAppName.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                R.layout.change_app_icon_spainner_item, null);
        }
        CheckedTextView checkedTextView =
            (CheckedTextView) convertView.findViewById(R.id.checkedTextView);
        if (ChangeAppIcon.ICON_APP_NAME.equals(iconAppName.get(position))) {
            checkedTextView.setBackgroundResource(R.drawable.change_app_icon_spinner_selector);
            checkedTextView.setText(iconAppName.get(position));
            checkedTextView.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                                                    mContext.getResources()
                                                                        .getDrawable(
                                                                            R.drawable.change_icon_new),
                                                                    null);
        } else {
            checkedTextView.setBackgroundResource(R.drawable.change_nano_icon_spinner_selector);
            checkedTextView.setText(iconAppName.get(position));
            checkedTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
        return convertView;
    }
}
