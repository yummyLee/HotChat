package com.yummy.hotchat.Widget;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yummy.hotchat.Object.FileInfo;
import com.yummy.hotchat.R;

import java.io.File;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/12/17 13:37
 * 修改人：qq985
 * 修改时间：2016/12/17 13:37
 * 修改备注：
 */
public class FileDetailDialog extends AlertDialog {

    LayoutInflater layoutInflater;
    TextView tvName;
    TextView tvSize;
    TextView tvPath;
    TextView tvTime;
    Button btnOpenFile;
    Button btnCancel;

    View view;
    int layoutId;
    FileInfo fileInfo;
    private OnOpenClickedListener listener;

    public FileDetailDialog(Context context, LayoutInflater layoutInflater, int layoutId, FileInfo fileInfo) {
        super(context);
        this.layoutInflater = layoutInflater;
        this.layoutId = layoutId;
        view = layoutInflater.inflate(layoutId, (ViewGroup) findViewById(layoutId));
        tvName = (TextView) view.findViewById(R.id.tv_file_list_name);
        tvSize = (TextView) view.findViewById(R.id.tv_file_list_size);
        tvPath = (TextView) view.findViewById(R.id.tv_file_list_path);
        tvTime = (TextView) view.findViewById(R.id.tv_file_list_time);
        btnOpenFile = (Button) view.findViewById(R.id.btn_file_list_open_file);
        btnCancel = (Button) view.findViewById(R.id.btn_file_list_cancel);
        this.fileInfo = fileInfo;
    }


    public void initParameterAndShow() {

        tvName.setText(fileInfo.getFileName());
        tvPath.setText(fileInfo.getFilePath());
        tvTime.setText(fileInfo.getFileTime());
        tvSize.setText(fileInfo.getFileSize());

        btnOpenFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.OnOpenClick();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.OnCancelClick();
                }
            }
        });

        setTitle("文件详情");
        setView(view);
        show();

    }

    public void setOnOpenClickedListener(OnOpenClickedListener listener) {
        this.listener = listener;
    }

    public interface OnOpenClickedListener {
        void OnOpenClick();
        void OnCancelClick();
    }

}
