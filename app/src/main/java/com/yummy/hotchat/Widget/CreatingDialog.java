package com.yummy.hotchat.Widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yummy.hotchat.R;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/12/16 17:35
 * 修改人：qq985
 * 修改时间：2016/12/16 17:35
 * 修改备注：
 */
public class CreatingDialog extends AlertDialog {

    LayoutInflater layoutInflater;
    ProgressBar progressBar;
    TextView textView;
    View view;
    int layoutId;
    String title;

    public CreatingDialog(Context context, LayoutInflater layoutInflater, int layoutId, String title) {
        super(context);
        this.layoutInflater = layoutInflater;
        this.layoutId = layoutId;
        view = layoutInflater.inflate(layoutId, (ViewGroup) findViewById(layoutId));
        progressBar = (ProgressBar) view.findViewById(R.id.dc_pb);
        textView = (TextView) view.findViewById(R.id.tv_dc_title);
        textView.setText(title);
    }


    public void initParameterAndShow() {

        setCancelable(false);
        setView(view);
        show();
    }

}
