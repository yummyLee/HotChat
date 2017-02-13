package com.yummy.hotchat.Adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yummy.hotchat.Object.FileInfo;
import com.yummy.hotchat.R;
import com.yummy.hotchat.WifiTool.WifiTools;

import java.util.List;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/12/17 0:32
 * 修改人：qq985
 * 修改时间：2016/12/17 0:32
 * 修改备注：
 */
public class HotFilesAdapter extends RecyclerView.Adapter<HotFilesAdapter.MyViewHolder> {

    private List<FileInfo> fileInfos;
    private Context mContext;
    private LayoutInflater inflater;

    private OnFileItemClickListener listener = null;

    public HotFilesAdapter(Context context, List<FileInfo> fileInfos) {
        this.mContext = context;
        this.fileInfos = fileInfos;
        inflater = LayoutInflater.from(mContext);
    }

    /**
     * 设置列表的数据
     *
     * @param fileInfos
     */
    public void setData(List<FileInfo> fileInfos) {
        this.fileInfos = fileInfos;

    }

    @Override
    public int getItemCount() {

        return fileInfos.size();
    }

    //填充onCreateViewHolder方法返回的holder中的控件
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        holder.tvName.setText(fileInfos.get(position).getFileName());

        if (listener != null) {

            holder.ivOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onOpenClick(position);
                }
            });

            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDeleteClick(position);
                }
            });

            holder.cvItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onFileItemLongClicked(position);
                    return true;
                }
            });

        }
    }

    //重写onCreateViewHolder方法，返回一个自定义的ViewHolder
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.files_list_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        ImageView ivDelete;
        ImageView ivOpen;
        CardView cvItem;

        public MyViewHolder(View view) {
            super(view);
            tvName = (TextView) view.findViewById(R.id.tv_file_name);
            ivDelete = (ImageView) view.findViewById(R.id.iv_file_delete);
            ivOpen = (ImageView) view.findViewById(R.id.iv_file_open);
            cvItem = (CardView) view.findViewById(R.id.cv_file_list);
        }

    }

    /**
     * 文件项目被点击的接口
     */
    public interface OnFileItemClickListener {

        void onDeleteClick(int position);

        void onOpenClick(int position);

        void onFileItemLongClicked(int position);

    }

    /**
     * 设置文件被点击项目接口
     *
     * @param listener
     */
    public void setOnFileItemClickListener(OnFileItemClickListener listener) {

        this.listener = listener;
    }
}

