package com.yummy.hotchat.Widget;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.yummy.hotchat.Fragment.Chat;
import com.yummy.hotchat.Model.ChatModel;
import com.yummy.hotchat.Object.FileInfo;
import com.yummy.hotchat.R;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/12/22 0:28
 * 修改人：qq985
 * 修改时间：2016/12/22 0:28
 * 修改备注：
 */
public class UserInfoDialog extends AlertDialog {
    LayoutInflater layoutInflater;
    TextView tvName;
    TextView tvSex;
    TextView tvIllustration;
    CircleImageView civIcon;

    View view;
    int layoutId;
    ChatModel chatModel;

    public UserInfoDialog(Context context, LayoutInflater layoutInflater, int layoutId, ChatModel chatModel) {
        super(context);
        this.layoutInflater = layoutInflater;
        this.layoutId = layoutId;
        view = layoutInflater.inflate(layoutId, (ViewGroup) findViewById(layoutId));
        tvName = (TextView) view.findViewById(R.id.tv_dialog_user_info_name);
        tvSex = (TextView) view.findViewById(R.id.tv_dialog_user_info_sex);
        tvIllustration = (TextView) view.findViewById(R.id.tv_dialog_user_info_illustration);
        civIcon= (CircleImageView) view.findViewById(R.id.civ_dialog_user_info_header);
        this.chatModel=chatModel;
    }


    public void initParameterAndShow() {

        tvName.setText(chatModel.getNick());
        switch (chatModel.getSex()){
            case "man":
                tvSex.setText("男");
                break;
            case "woman":
                tvSex.setText("女");
                break;
            default:
                tvSex.setText("未知");
                break;
        }
        tvIllustration.setText(chatModel.getIllustration());
        switch (chatModel.getIcon()){
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
                civIcon.setImageResource(R.drawable.header);
                break;
        }

        setView(view);
        show();
    }

}
