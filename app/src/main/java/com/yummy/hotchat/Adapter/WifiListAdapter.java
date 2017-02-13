package com.yummy.hotchat.Adapter;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/12/2 21:41
 * 修改人：qq985
 * 修改时间：2016/12/2 21:41
 * 修改备注：
 */

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yummy.hotchat.Object.ScanResultWithLock;
import com.yummy.hotchat.R;
import com.yummy.hotchat.WifiTool.WifiTools;

import java.util.List;

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.MyViewHolder> {

    private List<ScanResultWithLock> scanResultWithLocks;
    private Context mContext;
    private LayoutInflater inflater;

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public WifiListAdapter(Context context, List<ScanResultWithLock> scanResultWithLocks) {
        this.mContext = context;
        this.scanResultWithLocks = scanResultWithLocks;
        inflater = LayoutInflater.from(mContext);
    }

    public void setData(List<ScanResultWithLock> scanResultWithLocks) {
        this.scanResultWithLocks = scanResultWithLocks;

    }

    @Override
    public int getItemCount() {

        return scanResultWithLocks.size();
    }

    //填充onCreateViewHolder方法返回的holder中的控件
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        holder.tvName.setText(scanResultWithLocks.get(position).getScanResult().SSID);
        int level = WifiTools.calculateSignalLevel(scanResultWithLocks.get(position).getScanResult().level, 4);
//        System.out.println(level);
        switch (level) {
            case 1:
                holder.ivIcon.setImageResource(R.drawable.wifi_yellow1);
                break;
            case 2:
                holder.ivIcon.setImageResource(R.drawable.wifi_yellow2);
                break;
            case 3:
                holder.ivIcon.setImageResource(R.drawable.wifi_yellow3);
                break;
            case 4:
                holder.ivIcon.setImageResource(R.drawable.wifi_yellow4);
                break;
            default:
                break;
        }

        if (scanResultWithLocks.get(position).isLocked()) {
            holder.ivLock.setImageResource(R.drawable.lock);
        } else {
            holder.ivLock.setImageResource(R.drawable.lock_empty);
        }

        if (mOnItemClickListener != null) {
            holder.tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onNameClick(position);
                }
            });
            holder.ivIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onImageClick(position);
                }
            });
            holder.tvConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onConnectClick(position);
                }
            });
        }
    }

    //重写onCreateViewHolder方法，返回一个自定义的ViewHolder
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.wifi_list_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    class MyViewHolder extends ViewHolder {

        TextView tvName;
        ImageView ivIcon;
        TextView tvConnect;
        ImageView ivLock;

        public MyViewHolder(View view) {
            super(view);
            tvName = (TextView) view.findViewById(R.id.tv_name);
            tvConnect = (TextView) view.findViewById(R.id.tv_connect);
            ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
            ivLock = (ImageView) view.findViewById(R.id.iv_wifi_item_lock);
        }

    }

    /**
     * wifi列表项目被点击的事件
     */
    public interface OnRecyclerViewItemClickListener {
        void onNameClick(int position);

        void onImageClick(int position);

        void onConnectClick(int position);
    }

    public void setOnRItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}
