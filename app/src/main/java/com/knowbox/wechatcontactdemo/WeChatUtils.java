package com.knowbox.wechatcontactdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class WeChatUtils {

    /**
     * 执行root命令
     *
     * @param command
     */
    public static boolean execRootCmd(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

    /**
     * 获取当前用户的uin值
     *
     * @return
     */
    public static String getCurrWxUin() {
        String uin = null;
        File file = new File(MyConst.WX_SP_UIN_PATH);
        try {
            FileInputStream in = new FileInputStream(file);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(in);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            for (Element element : elements) {
                if ("_auth_uin".equals(element.attributeValue("name"))) {
                    uin = element.attributeValue("value");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return uin;
    }

    /**
     * 获取IMEI设备编码
     *
     * @param context
     * @return
     */
    public static String getIMEI(Context context) {
        TelephonyManager phone = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            String IMEI = phone.getDeviceId();
            return IMEI;
        } else {
            return null;
        }
    }

    /**
     * 合成数据库的密码
     *
     * @param imei
     * @param uin
     * @return
     */
    public static String getDbPassword(String imei, String uin) {
        if (TextUtils.isEmpty(imei) || TextUtils.isEmpty(uin)) {
            return null;
        }
        String md5 = getMD5(imei + uin);
        String password = md5.substring(0, 7).toLowerCase();
        return password;
    }

    /**
     * 对字符串进行md5
     *
     * @param info
     * @return
     */
    private static String getMD5(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] encryption = md5.digest();

            StringBuffer strBuf = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                    strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
                } else {
                    strBuf.append(Integer.toHexString(0xff & encryption[i]));
                }
            }

            return strBuf.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * 递归查询微信本地数据库文件
     *
     * @param file     目录
     * @param fileName 需要查找的文件名称
     */
    public static void searchFile(File file, String fileName, List<File> mWxDbPathList) {
        // 判断是否是文件夹
        if (file.isDirectory()) {
            // 遍历文件夹里所有数据
            File[] files = file.listFiles();
            if (files != null) {
                for (File childFile : files) {
                    searchFile(childFile, fileName, mWxDbPathList);
                }
            }
        } else {
            if (fileName.equals(file.getName())) {
                mWxDbPathList.add(file);
            }
        }
    }

    /**
     * 拷贝文件
     *
     * @param oldPath
     * @param newPath
     */
    public static boolean copyFile(String oldPath, String newPath) {
        try {
            int byteRead = 0;
            File oldFile = new File(oldPath);
            if (oldFile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteRead = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteRead);
                }
                inStream.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static File getMyCopyWXDB(Context context){
            List<File> mWxDbPathList = new ArrayList<>();
            File wxDataDir = new File(MyConst.WX_DB_DIR_PATH);
            //递归遍历
            WeChatUtils.searchFile(wxDataDir, MyConst.WX_DB_FILE_NAME, mWxDbPathList);
            //获取最后一个数据 也可以都遍历出来
            if (mWxDbPathList.size() > 0) {
                File file = mWxDbPathList.get(mWxDbPathList.size() - 1);
                //获取自己包名下的 数据库
                File targetFileDir = context.getDatabasePath("wechat");
                if (!targetFileDir.exists()){
                    targetFileDir.mkdirs();
                }
                File targetFile=new File(targetFileDir,MyConst.COPY_WX_DATA_DB);
                if (!targetFile.exists()){
                    try {
                        targetFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    targetFile.delete();
                }
                boolean isSuccess = WeChatUtils.copyFile(file.getAbsolutePath(), targetFile.getAbsolutePath());
                if (isSuccess) {
                    return targetFile;
                } else {
                    return null;
                }
            }
            return null;
    }

    /**
     * 连接数据库
     *
     * @param dbFile
     */
    public static SQLiteDatabase openWxDb(Context context, File dbFile, String mDbPassword) {
        SQLiteDatabase.loadLibs(context);
        SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
            public void preKey(SQLiteDatabase database) {
            }

            public void postKey(SQLiteDatabase database) {
                database.rawExecSQL("PRAGMA cipher_migrate;"); //兼容2.0的数据库
            }
        };

        try {
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile, mDbPassword, null, hook);
            return db;
        } catch (Exception e) {
            return null;
        }
    }

}
