package com.cooeeui.nanobooster.model;

import android.graphics.Color;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooeeui.nanobooster.MainActivity;
import com.cooeeui.nanobooster.R;
import com.cooeeui.nanobooster.model.domain.AppInfo;
import com.cooeeui.nanobooster.views.RippleView;

public class NanoboosterAdpter extends BaseAdapter {

    private TextView mTv_running_manager;
    private TextView mTv_running_total;
    private TextView mTv_ignore_size;
    public CheckBox ck_running;
    private MainActivity mMa;
    private View mConvertView = null;
    private ViewHolder mViewHolder = null;

    public NanoboosterAdpter(MainActivity mainActivity) {
        mMa = mainActivity;
    }


    @Override
    public int getCount() {

        if ((mMa.ignore == false) && (mMa.running == false)) {
            mMa.items_current = mMa.items_total; // 显示所有的item

        }

        return mMa.items_current.size() + 2;
    }

    @Override
    public Object getItem(int position) {

        if ((mMa.ignore == false) && (mMa.running == false)) {// 显示所有的item

            if (position == 0) {//白名单标题

                return null;

            } else if (position == mMa.items_ignore.size() + 1) {//运行名单标题
                return null;

            } else if (position < getCount()) {

                if (position < mMa.items_ignore.size() + 1 && position > 0) {//白名单条目

                    AppInfo appInfo = mMa.items_current.get(position - 1);
                    return appInfo;
                } else if (position > mMa.items_ignore.size() + 1
                           && position < getCount()) {//运行名单条目
                    AppInfo appInfo = mMa.items_current.get(position - 2);
                    return appInfo;
                }
            }

        }

        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if ((mMa.ignore == false) && (mMa.running == false)) {//使用 总的逻辑
            if (position == 0) {
                return getIngoreTtile();
            } else if (position == mMa.items_ignore.size() + 1) {
                View view_runing = getRunningTitle();
                ck_running.setChecked(mMa.isTitleChecked);
                /*#############第二次修改提交 start#############*/
                mMa.ck_running_first.setChecked(mMa.isTitleChecked);
                mMa.ck_running_second.setChecked(mMa.isTitleChecked);
                /*#############第二次修改提交 end #############*/
                return view_runing;
            } else if (position < getCount()) {
                //抽取的代码 复用
                reUse(view);
                if (position < mMa.items_ignore.size() + 1 && position > 0) {
                    final AppInfo app_b = mMa.items_current.get(position - 1);
                    mViewHolder.tv_tubiao_name
                        .setText(mMa.items_current.get(position - 1).getAppName());
                    mViewHolder.cb_item.setVisibility(View.GONE);
                    mViewHolder.iv_del_or_add.setImageResource(R.drawable.delete);
                    mViewHolder.iv_tubiao.setImageDrawable(app_b.getIcon());
                    mViewHolder.tv_tubiao_name.setText(app_b.getAppName());
                    mViewHolder.tv_size
                        .setText(Formatter.formatFileSize(mMa, app_b.getMemorySize()));
                    if (app_b.isIgonreApp()) {
                        mViewHolder.ll_parent.setBackgroundColor(Color.parseColor("#ffffff"));
                            /*#############改变##############*/
                        mViewHolder.rv_delete_common.setOnRippleCompleteListener(
                            new RippleView.OnRippleCompleteListener() {
                                @Override
                                public void onComplete(RippleView rippleView) {
                                    notifyDataSetChanged();
                                }
                            });

                    }

                    mViewHolder.iv_del_or_add.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (app_b.isIgonreApp()) {
                                    Toast.makeText(mMa, app_b.getAppName() + "已从白名单中移除",
                                                   Toast.LENGTH_SHORT).show();
                                    mMa.items_ignore_size--;
                                    mMa.items_running_size++;
                                    /*##########第二次改变提交 start#########*/
                                    mMa.tv_ignore_size_label.setText(mMa.items_ignore_size + "");
                                    /*##########第二次改变提交 start#########*/
                                    mMa.changeIngoreAndRuningSet(app_b);
                                    mMa.deleteFromSP(app_b);
                                }
                            }
                        });

                } else if (position > mMa.items_ignore.size() + 1 && position < getCount()) {
                    mViewHolder.cb_item.setVisibility(View.VISIBLE);
                    // 这里面的所有控件的值都是动态设置的,一次来实现checkbox的引用混乱问题
                    final AppInfo app_r = mMa.items_current.get(position - 2);
                    mViewHolder.tv_tubiao_name
                        .setText(mMa.items_current.get(position - 2).getAppName());
                    mViewHolder.iv_del_or_add.setImageResource(R.drawable.add);
                    mViewHolder.iv_tubiao.setImageDrawable(app_r.getIcon());
                    mViewHolder.tv_tubiao_name.setText(app_r.getAppName());
                    mViewHolder.tv_size
                        .setText(Formatter.formatFileSize(mMa, app_r.getMemorySize()));
                    if (!app_r.isIgonreApp() && !app_r.isCouldUse()) {
                        mViewHolder.ll_parent.setBackgroundColor(Color.RED);
                        mViewHolder.cb_item.setChecked(false);
                        mViewHolder.cb_item.setClickable(false);

                    } else if (!app_r.isIgonreApp() && app_r.isCouldUse() && app_r.isChecked()) {
                        mViewHolder.ll_parent.setBackgroundColor(Color.parseColor("#ffffff"));
                        mViewHolder.cb_item.setChecked(true);

                    } else if (!app_r.isIgonreApp() && app_r.isCouldUse() && !app_r.isChecked()) {
                        mViewHolder.cb_item.setChecked(false);
                        mViewHolder.ll_parent.setBackgroundColor(Color.parseColor("#ffffff"));


                    }
                    mViewHolder.iv_del_or_add.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!app_r.isIgonreApp()) {
                                    Toast.makeText(mMa, app_r.getAppName() + "已加入白名单中",
                                                   Toast.LENGTH_SHORT).show();
                                    LinearLayout
                                        ll_parent =
                                        (LinearLayout) view.getParent().getParent();
                                    RelativeLayout rl = (RelativeLayout) ll_parent.getChildAt(0);
                                    LinearLayout ll = (LinearLayout) rl.getChildAt(0);
                                    CheckBox cb = (CheckBox) ll.getChildAt(0);
                                    if (!app_r.isIgonreApp() && !app_r.isCouldUse()) {
                                        mTv_running_manager.setText(++mMa.items_running_size + "");
                                        mTv_ignore_size.setText(--mMa.items_ignore_size + "");

                                        /*################第二次提交修改 start#################*/
                                        mMa.tv_ignore_size_label
                                            .setText(mMa.items_ignore_size + "");
                                        /*################第二次提交修改 end #################*/

                                        ll_parent.setBackgroundColor(Color.parseColor("#ffffff"));
                                        app_r.setIsChecked(true);
                                        cb.setChecked(true);
                                        mMa.items_delFrom_running.remove(app_r);
                                        mMa.deleteFromSP(app_r);

                                    } else if (!app_r.isIgonreApp() && app_r.isCouldUse()) {
                                        if (app_r.isChecked()) {
                                            mTv_running_manager
                                                .setText(--mMa.items_running_size + "");
                                            mTv_ignore_size.setText(++mMa.items_ignore_size + "");
                                         /*################第二次提交修改 start#################*/
                                            mMa.tv_ignore_size_label
                                                .setText(mMa.items_ignore_size + "");
                                        /*################第二次提交修改 end #################*/
                                        } else {
                                            mTv_ignore_size.setText(++mMa.items_ignore_size + "");
                                          /*################第二次提交修改 start#################*/
                                            mMa.tv_ignore_size_label
                                                .setText(mMa.items_ignore_size + "");
                                          /*################第二次提交修改 end #################*/
                                        }
                                        ll_parent.setBackgroundColor(Color.RED);
                                        app_r.setIsChecked(false);
                                        cb.setChecked(false);
                                        mMa.items_delFrom_running.add(app_r);
                                        mMa.putIgnorePackname2SP(
                                            app_r.getPackName());//将选中item对应app加入到sp

                                    }
                                    app_r.setIsCouldUse(!app_r.isCouldUse());
                                    ll_parent.setEnabled(false);
                                    cb.setClickable(false);
                                    mMa.isTitleChecked = mMa.runningItermIsChecked();
                                    ck_running.setChecked(mMa.isTitleChecked);
                                    /*#############第二次修改提交 start#############*/
                                    mMa.ck_running_first.setChecked(mMa.isTitleChecked);
                                    mMa.ck_running_second.setChecked(mMa.isTitleChecked);
                                   /*#############第二次修改提交 end #############*/
                                }

                            }
                        });


                }
                return mConvertView;
            }

        }

        return null;

    }

    //用来复用的viewHolder
    class ViewHolder {

        RelativeLayout rl_parent;
        CheckBox cb_item;
        ImageView iv_tubiao;
        TextView tv_tubiao_name;
        TextView tv_size;
        ImageView iv_del_or_add;
        LinearLayout ll_parent;
        /*################改变###############*/
        RippleView rv_delete_common;

    }

    private View getIngoreTtile() {
        View view = (View) View.inflate(mMa, R.layout.item_title_ignore, null);
        mTv_ignore_size = (TextView) view.findViewById(R.id.tv_ignore_size);

        mTv_ignore_size.setText(mMa.items_ignore_size + "");

           /*################改变###############*/
        RippleView rv_title_ignore = (RippleView) view.findViewById(R.id.rv_title_ignore);
        rv_title_ignore.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                notifyDataSetChanged();
            }
        });

        return view;
    }

    private View getRunningTitle() {

        View view = (View) View.inflate(mMa, R.layout.item_title_running, null);
        ck_running = (CheckBox) view.findViewById(R.id.ck_running);

        mTv_running_manager = (TextView) view.findViewById(R.id.tv_running_manager);
        mTv_running_manager.setText(mMa.items_running_size + "");

        mTv_running_total = (TextView) view.findViewById(R.id.tv_running_total);
        mTv_running_total.setText("(" + mMa.items_total_size + ")");

           /*################改变###############*/
        RippleView rv_title_running = (RippleView) view.findViewById(R.id.rv_title_running);
        rv_title_running.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                notifyDataSetChanged();
            }
        });

        return view;
    }


    private void reUse(View view) {
           /*################改变###############*/
        View viewChild = null;
        if (view != null && (view instanceof RelativeLayout)) {
            RelativeLayout rl = (RelativeLayout) view;
            viewChild = rl.getChildAt(0);
        }

        if (view != null && (view instanceof RelativeLayout) && viewChild != null
            && (viewChild instanceof RelativeLayout)) {
            mViewHolder = (ViewHolder) view.getTag();

        } else {

            view = (View) View.inflate(mMa, R.layout.item_content, null);
            mViewHolder = new ViewHolder();

               /*################改变###############*/
            RippleView rv_delete_common = (RippleView) view.findViewById(R.id.rv_delete_common);
            mViewHolder.rv_delete_common = rv_delete_common;

            mViewHolder.ll_parent = (LinearLayout) view.findViewById(R.id.ll_parent);
            mViewHolder.rl_parent = (RelativeLayout) view.findViewById(R.id.rl_parent);
            mViewHolder.cb_item = (CheckBox) view.findViewById(R.id.cb_item);// running中的勾选按钮
            mViewHolder.iv_tubiao = (ImageView) view.findViewById(R.id.iv_tubiao);//应用图标
            mViewHolder.tv_tubiao_name = (TextView) view.findViewById(R.id.tv_tubiao_name);//应用名称
            mViewHolder.tv_size = (TextView) view.findViewById(R.id.tv_size);//应用大小
            mViewHolder.iv_del_or_add = (ImageView) view.findViewById(R.id.iv_del_or_add);//删除或添加图标
            view.setTag(mViewHolder);
        }

        mConvertView = view;
    }


    public void updateItemView(View itemView, int position) {

        CheckBox cb = (CheckBox) itemView.findViewById(R.id.cb_item);
        AppInfo appInfo = (AppInfo) getItem(position);
        if (appInfo != null) {

            if (!appInfo.isIgonreApp() && appInfo.isCouldUse()) {
                //
                if (!appInfo.isIgonreApp() && appInfo.isCouldUse() && cb.isChecked()) {
                    if (mTv_running_manager != null) {
                        mTv_running_manager.setText(--mMa.items_running_size + "");
                    }

                } else if (!appInfo.isIgonreApp() && appInfo.isCouldUse() && !cb.isChecked()) {
                    if (mTv_running_manager != null) {
                        mTv_running_manager.setText(++mMa.items_running_size + "");
                    }

                }
                appInfo.setIsChecked(!appInfo.isChecked());
                cb.setChecked(appInfo.isChecked());

                //running标题的checkbox状态发生改变，若前天运行条目的checkbox为不选中那么 running标题的checkbox 取消选择状态
                mMa.isTitleChecked = mMa.runningItermIsChecked();
                ck_running.setChecked(mMa.isTitleChecked);
                /*#############第二次修改提交 start#############*/
                mMa.ck_running_first.setChecked(mMa.isTitleChecked);
                mMa.ck_running_second.setChecked(mMa.isTitleChecked);
                /*#############第二次修改提交 end #############*/

            }       //如果复用混乱的话 还是需要加else的
        }

    }

}
