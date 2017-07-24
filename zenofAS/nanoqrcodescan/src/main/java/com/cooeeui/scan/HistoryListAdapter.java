package com.cooeeui.scan;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooeeui.nanoqrcodescan.R;

import java.util.List;


public class HistoryListAdapter extends BaseAdapter {

    private Context context;
    private List<HistoryBean> historyList;
    private  DBHelper dbHelper;
    private  LinearLayoutListView listView;


    public HistoryListAdapter(
        Context context,
        List<HistoryBean> historyList,LinearLayoutListView listView
    ) {
        this.context = context;
        this.historyList = historyList;
        this.listView =listView;
    }

    @Override
    public int getCount() {
        return historyList.size();
    }

    @Override
    public Object getItem(
        int position) {
        return historyList.get(position);
    }

    @Override
    public long getItemId(
        int position) {
        return position;
    }

    @Override
    public View getView(
        final int position,
        View convertView,
        ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_history_list, null);
            holder.ll = (LinearLayout) convertView.findViewById(R.id.ll);
            holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
            holder.btn_share = (RelativeLayout) convertView.findViewById(R.id.rl_share);
            holder.btn_delete = (RelativeLayout) convertView.findViewById(R.id.rl_delete);
            holder.btn_copy = (RelativeLayout) convertView.findViewById(R.id.rl_copy);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //为空
        if(historyList.size()==0){
            TextView textView = new TextView(context);
            textView.setText("没有记录");
        }
        if (historyList.get(getCount() - position - 1).getType() == 1) {
            holder.iv_icon.setImageResource(R.drawable.browser_icon);
            //holder.tv_title.setText( context.getString( R.string.string_interlinkage ) );
        } else {
            holder.iv_icon.setImageResource(R.drawable.text_icon);
            //holder.tv_title.setText( context.getString( R.string.string_text ) );
        }
        holder.tv_title.setText(historyList.get(getCount() - position - 1).getCurrtime());
        holder.tv_content.setText(historyList.get(getCount() - position - 1).getText());
        holder.ll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(
                View v) {
                if (historyList.get(getCount() - position - 1).getType() == 1) {
                    Uri uri = Uri.parse(historyList.get(getCount() - position - 1).getText());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    context.startActivity(intent);
                } else {
                    AlertDialog.Builder builder = new Builder(context);
                    builder.setMessage(historyList.get(getCount() - position - 1).getText());
                    builder.setTitle(context.getResources().getString(R.string.string_text));
                    builder.setNegativeButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        holder.btn_share.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(
                View v) {
                Log.v("QrCode", "share click");

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_title));
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                                     historyList.get(getCount() - position - 1).getText());
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(shareIntent);
            }
        });

        holder.btn_copy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(historyList.get(getCount() - position - 1).getText());
                Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show();

            }
        });
        holder.btn_delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper = new DBHelper(context);
                dbHelper.delete(new String[]{historyList.get(getCount() - position - 1).getText()});
                historyList.remove(historyList.get(getCount() - position - 1));
                listView.removeAllViews();
                listView.bindLinearLayout();
                notifyDataSetChanged();
            }
        });


        return convertView;
    }

    class ViewHolder {

        LinearLayout ll;
        ImageView iv_icon;
        TextView tv_title;
        RelativeLayout btn_share;
        RelativeLayout btn_copy;
        RelativeLayout btn_delete;
        TextView tv_content;
    }


}
