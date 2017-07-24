package com.cooeeui.brand.zenlauncher.tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.common.ui.uieffect.UIEffectTools;
import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.out.MvWallHandler;
import com.umeng.analytics.MobclickAgent;

import java.util.Map;

/**
 * Created by Administrator on 2015/9/23.
 */
public class ToolsAdpter extends BaseAdapter {


    private int[]
        toolsRes =
        {R.drawable.nano_tools_settings, R.drawable.nano_tools_remind, R.drawable.nano_tools_top,
         R.drawable.nano_tools_wallpaper};
    private String[] toolsStr;
    private Context context;
    private MvWallHandler mvHandler;

    public ToolsAdpter(Context context) {
        this.context = context;
        toolsStr = new String[]{
            StringUtil.getString(context, R.string.nano_tools_settings),
            StringUtil.getString(context, R.string.nano_tools_remind),
            StringUtil.getString(context, R.string.nano_tools_top),
            StringUtil.getString(context, R.string.wallpaper)

        };
    }

    public void initString(){
        toolsStr[0] = StringUtil.getString(context, R.string.nano_tools_settings);
        toolsStr[1] = StringUtil.getString(context, R.string.nano_tools_remind);
        toolsStr[2] = StringUtil.getString(context, R.string.nano_tools_top);
        toolsStr[3] = StringUtil.getString(context, R.string.wallpaper);
        notifyDataSetChanged();
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

        if (position == 2) {
            Map<String, Object> properties = MvWallHandler.getWallProperties("187");
            properties.put(MobVistaConstans.PROPERTIES_WALL_TITLE_LOGO_ID,
                           R.drawable.mobvista_wall_hot_app_img_logo);

            View view = new RelativeLayout(context) {
                @Override
                public boolean onTouchEvent(MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        UIEffectTools.onClickEffect(this);
                        MobclickAgent.onEvent(Launcher.getInstance(), "Adintoolsclick");
                    }
                    return super.onTouchEvent(event);
                }
            };
            mvHandler = new MvWallHandler(properties, context, (ViewGroup) view);
            //customer entry layout begin
            View content =
                LayoutInflater.from(context).inflate(R.layout.customer_entry_favorites, null);
            ImageView icon = (ImageView) content.findViewById(R.id.iv_icon);
            icon.setTag(MobVistaConstans.WALL_ENTRY_ID_IMAGEVIEW_IMAGE);
            icon.setImageResource(toolsRes[position]);
            TextView title = (TextView) content.findViewById(R.id.tv_title);
            title.setText(toolsStr[position]);
            mvHandler.setHandlerCustomerLayout(content);
            //customer entry layout end */
            mvHandler.load();

            return view;
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.temp_icon, null);
            ImageView icon = (ImageView) view.findViewById(R.id.iv_icon);
            TextView title = (TextView) view.findViewById(R.id.tv_title);
            icon.setImageResource(toolsRes[position]);
            title.setText(toolsStr[position]);
            return view;
        }
    }

    @Override
    public int getCount() {
        return toolsRes.length;
    }
}
