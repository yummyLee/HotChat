package com.yummy.hotchat.Adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;
import com.yummy.hotchat.Model.ChatModel;
import com.yummy.hotchat.Model.ItemModel;
import com.yummy.hotchat.R;
import com.yummy.hotchat.Widget.UserInfoDialog;

import java.util.ArrayList;

/**
 * Created by yummy on 2016/11/28.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.BaseAdapter> {

    private ArrayList<ItemModel> dataList = new ArrayList<>();
    private OnChatModelItemClickListener listener;
    private final static String TAG = "ChatAdapter";

    public void replaceAll(ArrayList<ItemModel> list) {
        dataList.clear();
        if (list != null && list.size() > 0) {
            dataList.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<ItemModel> list) {
        if (dataList != null && list != null) {
            dataList.addAll(list);
            notifyItemRangeChanged(dataList.size(), list.size());
        }

    }

    public void clearData(){
        dataList.clear();
        notifyDataSetChanged();
    }

    @Override
    public ChatAdapter.BaseAdapter onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ItemModel.CHAT_A:
                return new ChatAViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_a, parent, false));
            case ItemModel.CHAT_B:
                return new ChatBViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_b, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ChatAdapter.BaseAdapter holder, int position) {
        holder.setData(dataList.get(position).object);
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position).type;
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    public class BaseAdapter extends RecyclerView.ViewHolder {

        public BaseAdapter(View itemView) {
            super(itemView);
        }

        void setData(Object object) {

        }
    }

    private class ChatAViewHolder extends BaseAdapter {
        private ImageView civIcon;
        private TextView tvContent;
        private TextView tvName;
        private ImageView ivSex;
        private TextView tvTime;

        public ChatAViewHolder(View view) {
            super(view);
            civIcon = (ImageView) itemView.findViewById(R.id.civ_bubble_user_icon);
            tvContent = (TextView) itemView.findViewById(R.id.tv_bubble_content);
            tvName = (TextView) itemView.findViewById(R.id.tv_bubble_user_name);
            ivSex = (ImageView) itemView.findViewById(R.id.iv_bubble_user_sex);
            tvTime = (TextView) itemView.findViewById(R.id.tv_bubble_user_time);
        }

        @Override
        void setData(Object object) {
            super.setData(object);
            final ChatModel model = (ChatModel) object;
//            Picasso.with(itemView.getContext()).load(model.getIcon()).placeholder(R.mipmap.ic_launcher).into(ic_user);
            switch (model.getIcon()) {
                case "1":
                    civIcon.setImageResource(R.drawable.header);
                    break;
                case "2":
                    civIcon.setImageResource(R.drawable.header2);
                    break;
                case "3":
                    civIcon.setImageResource(R.drawable.header3);
                    break;
                case "4":
                    civIcon.setImageResource(R.drawable.header4);
                    break;
                default:
                    civIcon.setImageResource(R.drawable.header4);
                    break;
            }

            civIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, model.getNick() + "--" + model.getIllustration() + "--" + model.getSex() + "--" + model.getIcon());
                    listener.onIconClick(model);
                }
            });

            tvContent.setText(model.getContent());
            tvName.setText(model.getNick());
            switch (model.getSex()) {
                case "man":
                    ivSex.setImageResource(R.drawable.man);
                    break;
                case "woman":
                    ivSex.setImageResource(R.drawable.woman);
                    break;
                default:
                    break;
            }

            tvTime.setText(model.getTime());
        }
    }

    private class ChatBViewHolder extends BaseAdapter {
        private ImageView civIcon;
        private TextView tvContent;
        private TextView tvName;
        private ImageView ivSex;
        private TextView tvTime;

        public ChatBViewHolder(View view) {
            super(view);
            civIcon = (ImageView) itemView.findViewById(R.id.civ_bubble_user_icon);
            tvContent = (TextView) itemView.findViewById(R.id.tv_bubble_content);
            tvName = (TextView) itemView.findViewById(R.id.tv_bubble_user_name);
            ivSex = (ImageView) itemView.findViewById(R.id.iv_bubble_user_sex);
            tvTime = (TextView) itemView.findViewById(R.id.tv_bubble_user_time);
        }

        @Override
        void setData(Object object) {
            super.setData(object);
            ChatModel model = (ChatModel) object;
//            Picasso.with(itemView.getContext()).load(model.getIcon()).placeholder(R.mipmap.ic_launcher).into(ic_user);
            switch (model.getIcon()) {
                case "1":
                    civIcon.setImageResource(R.drawable.header);
                    break;
                case "2":
                    civIcon.setImageResource(R.drawable.header2);
                    break;
                case "3":
                    civIcon.setImageResource(R.drawable.header3);
                    break;
                case "4":
                    civIcon.setImageResource(R.drawable.header4);
                    break;
                default:
                    break;
            }

            civIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            tvContent.setText(model.getContent());
            tvName.setText(model.getNick());
            switch (model.getSex()) {
                case "man":
                    ivSex.setImageResource(R.drawable.man);
                    break;
                case "woman":
                    ivSex.setImageResource(R.drawable.woman);
                    break;
                default:
                    break;
            }
            tvTime.setText(model.getTime());
        }
    }

    public void setOnChatModelItemClickListener(OnChatModelItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnChatModelItemClickListener {
        void onIconClick(ChatModel model);
    }

}
