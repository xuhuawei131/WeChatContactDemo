package com.knowbox.wechatcontactdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private String dbPassword;
    private File targetFile;

    private ListView listview;

    private List<String> arryaList;
    private BaseAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listview = findViewById(R.id.listview);
        arryaList=new ArrayList<>(500);
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,arryaList);
        listview.setAdapter(adapter);

        //动态申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 100);
        }else{
            startRun();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1000){
            int grantResult = grantResults[0];
            if (grantResult == PackageManager.PERMISSION_GRANTED){
                startRun();
            }
        }
    }


    private void startRun(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                executeRootCmd();
                getPasswordAndDBFile();
                SQLiteDatabase db=openDBFile();
                getDBList(db);
            }
        }).start();
    }
    /**
     * 执行root操作
     */
    private void executeRootCmd() {
        //数据库执行root操作
        boolean isRoot = WeChatUtils.execRootCmd("chmod -R 777 " + MyConst.WX_ROOT_PATH);
        if (!isRoot) {
            Toast.makeText(this, "执行root失败！", Toast.LENGTH_SHORT).show();
        } else {
            WeChatUtils.execRootCmd("chmod  777 " + MyConst.WX_SP_UIN_PATH);
        }
    }

    /**
     * 获取打开数据库 需要的密码以及数据库
     */
    private void getPasswordAndDBFile() {
        String uin = WeChatUtils.getCurrWxUin();
        String IMEI = WeChatUtils.getIMEI(this);

        dbPassword = WeChatUtils.getDbPassword(IMEI, uin);
        targetFile=WeChatUtils.getMyCopyWXDB(this);
    }

    /**
     * 打开数据库
     * @return
     */
    private  SQLiteDatabase openDBFile(){
        SQLiteDatabase openWxDb=WeChatUtils.openWxDb(this,targetFile,dbPassword);
        return openWxDb;
    }

    /**
     *
     * 获取数据库中的数据
     */
    private void getDBList(SQLiteDatabase openWxDb){
        if (openWxDb!=null){
            Cursor cursor = openWxDb.query("rcontact", null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("username"));
                arryaList.add(name);
            }
            handler.sendEmptyMessage(0);
        }
    }



    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            adapter.notifyDataSetChanged();
        }
    };


}
