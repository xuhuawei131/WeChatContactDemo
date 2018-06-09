package com.knowbox.wechatcontactdemo;

public class MyConst {
    //微信的根路径
    public static final String WX_ROOT_PATH = "/data/data/com.tencent.mm/";
    // uin 文件路径  微信的数据库 需要这个uin
    public static final String WX_SP_UIN_PATH = WX_ROOT_PATH + "shared_prefs/auth_info_key_prefs.xml";
    //聊天记录以及通讯录的数据都在这个数据库中
    public static final String WX_DB_FILE_NAME = "EnMicroMsg.db";
    //微信聊天消息的路径
    public static final String WX_DB_DIR_PATH = WX_ROOT_PATH + "MicroMsg";
    //复制出来的数据库 我们重新命名
    public static final String COPY_WX_DATA_DB = "wx_data.db";

}
