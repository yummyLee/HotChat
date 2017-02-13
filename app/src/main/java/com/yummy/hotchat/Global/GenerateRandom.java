package com.yummy.hotchat.Global;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/12/14 17:41
 * 修改人：qq985
 * 修改时间：2016/12/14 17:41
 * 修改备注：
 */
public class GenerateRandom {

    /**
     * 返回一万到99999之间的整数
     */
    public static int randomW() {
        return (int) (Math.random() * (99999 - 10000 + 1)) + 10000;
    }
}
