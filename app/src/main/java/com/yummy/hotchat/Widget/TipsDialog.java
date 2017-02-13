package com.yummy.hotchat.Widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yummy.hotchat.R;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/11/27 21:05
 * 修改人：qq985
 * 修改时间：2016/11/27 21:05
 * 修改备注：
 */
public class TipsDialog extends Dialog {


    LayoutInflater layoutInflater;
    View view;
    int layoutId;
    String title;
    String content;
    String pString;
    String nString;
    OnTipsDialogClickedListener dialogClickedListener;

    public TipsDialog(Context context, LayoutInflater layoutInflater, int layoutId, String title, String content, String pString, String nString) {

        super(context);
        this.layoutInflater = layoutInflater;
        this.layoutId = layoutId;
        this.content = content;
        this.pString = pString;
        this.nString = nString;
        this.title = title;
        view = layoutInflater.inflate(layoutId, (ViewGroup) findViewById(layoutId));
        TextView textView = (TextView) view.findViewById(R.id.tv_dialog_tips);
        textView.setText(content);
    }


    public void initParameterAndShow() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setPositiveButton(pString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogClickedListener.onPositiveClicked();
            }
        });
        builder.setNegativeButton(nString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogClickedListener.onNegativeClicked();
            }
        });
        builder.setView(view);
        builder.setCancelable(false);
        builder.show();
    }

    public void setOnTipsDialogListener(OnTipsDialogClickedListener dialogListener) {

        this.dialogClickedListener = dialogListener;
    }

    public interface OnTipsDialogClickedListener {
        void onPositiveClicked();

        void onNegativeClicked();
    }

}
