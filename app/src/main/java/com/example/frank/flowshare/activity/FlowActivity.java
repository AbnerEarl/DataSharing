package com.example.frank.flowshare.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.frank.flowshare.R;
import com.example.frank.flowshare.comman.Constant;
import com.example.frank.flowshare.comman.WifiAdmin;
import com.example.frank.flowshare.comman.WifiApAdmin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

public class FlowActivity extends AppCompatActivity {

    private Button btn_create,btn_join;




    public boolean UdpReceiveOut = true;//8秒后跳出udp接收线程
    /**LingDongRootFolder此程序自己的文件目录*/
    String LingDongRootFolder = "/sdcard/LingDong/";
    /**发送离线文件的按钮*/
    private Button btnSend_offlinefiles;
    /**弹出对话框下载离线文件的按钮**/
    private Button btnDown_offlinefiles;
    /**点两次返回按键退出程序的时间*/
    private long mExitTime;
    /**显示离线文件传输的日志提醒的Textview，默认情况下文本为空*/
    public static TextView offline_trans_log;

    private static String LOG_TAG = "WifiBroadcastActivity";
    private boolean wifiFlag = true;//扫描wifi的子线程的标志位，如果已经连接上正确的wifi热点，线程将结束
    private String address;
    private WifiAdmin wifiAdmin;
    private ArrayList<String> arraylist = new ArrayList<String>();
    private ArrayAdapter adapter;
    private boolean update_wifi_flag = true;
    String ip;
    private ListView listView;
    public static final int DEFAULT_PORT = 43708;
    private static final int MAX_DATA_PACKET_LENGTH = 40;
    private byte[] buffer = new byte[MAX_DATA_PACKET_LENGTH];
    public boolean run = false;//判断是否接收到TCP返回，若接收到则不再继续接受
    public boolean show = false;//判断是否是由于超时而退出线程，若是则显示dialog
    private static boolean tcpout = false;
    private boolean a = false;

    //开启wifi ... ...
    private WifiManager wifiManager = null;
    /**********************************************************************************************/
    private ImageView iv_scanning;
    private android.support.v4.widget.DrawerLayout rl_root;
    /*********************UdpReceive线程**********************/

    private boolean udpout = false;
    /*******************************************************/
    //用以存储传送到文件发送界面的IP，即接收方的IP
    public static String IP_DuiFangde;

    public static SQLiteDatabase dbWriter;
    public static String Device_ID = "";



    /********************************************************************************/



    /**
     * handler用于子线程更新
     */
    private Handler handler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    btn_create.setEnabled(true);
                    btn_join.setEnabled(true);
                    offline_trans_log.setText("并发送请求到此设备" + "UDP接受已关闭");
                    break;
                case 2:
                    UdpReceiveOut = false;
                    if (!udpout) {
                        Log.i("tag", "00000000000000000000000000000000000000000000000000000000000");
                        if (setWifiApEnabled(true)) {
                            //热点开启成功
                            Log.i("tag", "111111111111111111111111111111111111111111111111111");
                            Toast.makeText(getApplicationContext(), "WIFI热点开启成功,热点名称为:" + Constant.HOST_SPOT_SSID + ",密码为：" + Constant.HOST_SPOT_PASS_WORD, Toast.LENGTH_LONG).show();
                            //这里可以设置为当用户连接到自己开的热点后，就跳转到文件发送界面，并将连接到自己热点设备的IP传过去
                            //getConnectDeviceIP()返回的值前面自带IP加一个回车 字样，如IP 192.168.0.111 所以需要截取一下才可以
                            Log.i("tag", "2222222222222222222222222222222222222222");
                            btn_create.setEnabled(true);
                            btn_join.setEnabled(true);
                            startNew startNew = new startNew();
                            startNew.start();

                        } else {
                            //热点开启失败
                            Toast.makeText(getApplicationContext(), "WIFI热点开启失败", Toast.LENGTH_LONG).show();
                            offline_trans_log.setText("WIFI热点开启失败");
                        }
                    }
                    break;
                case 3:
                    if (!udpout) {
                        Toast.makeText(FlowActivity.this, "局域网内未搜索到设备，将自动启用热点模式分享文件", Toast.LENGTH_SHORT).show();
                        offline_trans_log.setText("局域网内未搜索到设备，将自动启用热点模式分享文件");
                    }
                    break;
                default:
                    offline_trans_log.setText("查找到可用设备，其IP为：" + msg.obj + "\n");

                    if (FlowActivity.isIp((String) (msg.obj))) {
                        Toast.makeText(FlowActivity.this, "这是一个标准IP，地址为：" + msg.obj, Toast.LENGTH_LONG).show();
                        IP_DuiFangde = (String) msg.obj;
                        //跳转到文件发送界面
                        Log.i("TAG", "111111111111111111111111111111111111111");
                        Intent intent_filetrans = new Intent(FlowActivity.this, MainActivity.class);
                        startActivity(intent_filetrans);
                    } else {
                        Toast.makeText(FlowActivity.this, "这不是一个标准IP，内容为：" + msg.obj, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    };


    /**
     * Android6.0 获取更改系统设置的权限，app用了其他的方式，这段代码没有用到，删除也可以的
     */
    private void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 判断是否有WRITE_SETTINGS权限
            if (!Settings.System.canWrite(this)) {
                // 申请WRITE_SETTINGS权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 0);
            }
        }
    }


    /**获取连接到手机热点设备的IP*/
    StringBuilder resultList;
    ArrayList<String> connectedIP;

    public String getConnectDeviceIP() {

        try {
            connectedIP = getConnectIp();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        resultList = new StringBuilder();
        for (String ip : connectedIP) {
            resultList.append(ip);
            resultList.append("\n");
            try {
                connectedIP = getConnectIp();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String textString = resultList.toString();
        return textString;

    }

    //从系统/proc/net/arp文件中读取出已连接的设备的信息
    //获取连接设备的IP
    private ArrayList<String> getConnectIp() throws Exception {
        ArrayList<String> connectIpList = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] splitted = line.split(" +");
            if (splitted != null && splitted.length >= 4) {
                String ip = splitted[0];
                connectIpList.add(ip);
            }
        }
        return connectIpList;
    }


    /** wifi热点开关的方法*/
    public boolean setWifiApEnabled(boolean enabled) {
        if (enabled) { // disable WiFi in any case
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            wifiManager.setWifiEnabled(false);
        }
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称(可以在名字后面加点随机数什么的)
            apConfig.SSID = Constant.HOST_SPOT_SSID;
            //配置热点的密码
            apConfig.preSharedKey = Constant.HOST_SPOT_PASS_WORD;

            /***配置热点的其他信息  加密方式**/
            apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            //用WPA密码方式保护
            apConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);
        } catch (Exception e) {
            return false;
        }
    }


    private Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 7:
                    if (!a) {
                        Toast.makeText(FlowActivity.this, "局域网内没有搜索到可用设备，正在热点模式下搜索设备", Toast.LENGTH_LONG).show();
                        tcpout = true;
                        //更新并显示WIFI列表，此时还需要判断WIFI是否打开，可以直接写在UpdateWifiList()里面
                        // UpdateWifiList(0);
                    }
                    a = false;
                    break;
                case 8:
                    btn_create.setEnabled(true);
                    btn_join.setEnabled(true);
                    break;
                case 9:
                    UpdateWifiList(1);
                    break;
                case 10:
                    update_wifi_flag=false;
                    break;
                default:
                    tcpout = true;
                    offline_trans_log.append("查找到可用设备，其IP为：" + msg.obj + "\n");

                    System.out.println("00000000000000000000000000000000000000000000000000000000000000000000000" + msg.obj);

                    if (isIp((String) (msg.obj))) {
                        Toast.makeText(FlowActivity.this, "这是一个IP，地址为：" + (msg.obj), Toast.LENGTH_LONG).show();

                        IP_DuiFangde = (String) (msg.obj);
                        //跳转到文件发送界面
                        Log.i("TAG", "2222222222222222222222222222222");
                        Intent intent_filetrans = new Intent(FlowActivity.this, MainActivity.class);
                        startActivity(intent_filetrans);

                    } else {
                        Toast.makeText(FlowActivity.this, "这不是一个IP，内容为：" + msg.obj, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }

    };


    /***************************************************************************************************************/
    private Handler handler4 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 7:
                    if (!a) {
                        Toast.makeText(FlowActivity.this, "局域网内没有搜索到可用设备，正在热点模式下搜索设备", Toast.LENGTH_LONG).show();
                        tcpout = true;
                        //更新并显示WIFI列表，此时还需要判断WIFI是否打开，可以直接写在UpdateWifiList()里面
                        // UpdateWifiList(0);
                    }
                    a = false;
                    break;
                case 8:
                    btn_join.setEnabled(true);
                    btn_create.setEnabled(true);
                    break;
                case 9:
                    UpdateWifiList(1);
                    break;
                case 10:
                    update_wifi_flag=false;
                    break;
                default:
                    tcpout = true;
                    offline_trans_log.append("查找到可用设备，其IP为：" + msg.obj + "\n");

                    System.out.println("00000000000000000000000000000000000000000000000000000000000000000000000" + msg.obj);

                    if (isIp((String) (msg.obj))) {
                        Toast.makeText(FlowActivity.this, "这是一个IP，地址为：" + (msg.obj), Toast.LENGTH_LONG).show();

                        IP_DuiFangde = (String) (msg.obj);
                        //跳转到文件发送界面
                        Log.i("TAG", "2222222222222222222222222222222");
                        Intent intent_filetrans = new Intent(FlowActivity.this, MainActivity.class);
                        startActivity(intent_filetrans);

                    } else {
                        Toast.makeText(FlowActivity.this, "这不是一个IP，内容为：" + msg.obj, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }

    };


    /**
     * 获取系统当前的时间
     */
    public static String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date();
        String str = format.format(curDate);
        return str;
    }

    /**
     * 获取设备唯一的标志码
     */
    public String getDeviceID() {
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String imei = telephonyManager.getDeviceId();
        return imei;
    }

    public String getAndroidVersion() {
        String AndroidVersion = android.os.Build.VERSION.RELEASE;
        return AndroidVersion;
    }

    public String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取安卓手机RAM
     */
    public String getTotalMemory() {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                Log.i(str2, num + "\t");
            }

            initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();

        } catch (IOException e) {
        }
        return Formatter.formatFileSize(getBaseContext(), initial_memory);// Byte转换为KB或者MB，内存大小规格化
    }

    /**
     * 获取屏幕分辨率
     **/
    public String getScreenResolution() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        String strOpt = dm.widthPixels + " * " + dm.heightPixels;
        return strOpt;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow);

        init();

        Device_ID = getDeviceID();


        /********************************************************数据库相关操作*********************************/

        //android 6.0更改系统设置的权限
        //getPermission();

        /*****************************************************/

        wifiManager = (WifiManager) super.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        /*****************************************************/

        //初始化wifiAdmin
        wifiAdmin = new WifiAdmin(FlowActivity.this);
        /**获取设备IP*/
        address = getLocalIPAddress();
        ip = address;

        /*******************************************/
        adapter = new ArrayAdapter<String>(FlowActivity.this, android.R.layout.simple_expandable_list_item_1, arraylist);//初始化adapter


    }

    private void init(){
        btn_create=(Button)this.findViewById(R.id.btn_create_net);
        btn_join=(Button)this.findViewById(R.id.btn_join_net);

        //rl_root = (DrawerLayout) findViewById(R.id.drawer_layout);


        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CreatConnection("ShareFlow", "123456789", 3);
                showPopupWindow();
                WifiApAdmin.closeWifiAp(wifiManager);
                wifiManager.setWifiEnabled(true);
                btn_join.setEnabled(false);
                btn_create.setEnabled(false);

            }
        });

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    WifiApAdmin.closeWifiAp(wifiManager);
                    wifiManager.setWifiEnabled(true);


                showPopupWindow();
                btn_join.setEnabled(false);
                btn_create.setEnabled(false);
            }
        });


    }





    //type 0新建wifi列表
    //type 1动态更新wifi列表
    void UpdateWifiList(int type) {
        wifiAdmin.startScan();
        wifiAdmin.lookUpScan();
        arraylist.clear();

        for (ScanResult e : wifiAdmin.getWifiList()) {


            if (e.SSID.equals("LingDong"))//如果热点名有LingDong且不为空且不重复
            {
                //关闭wifi列表更新
                update_wifi_flag = false;
//                Log.i("TAG","SSID:"+e.SSID );

                //这一段输入密码，现阶段设置为默认123456789
                CreatConnection("LingDong", "123456789", 3);//这里输入密码
                //更新这个IP地址
                IP_DuiFangde = "192.168.43.1";
                //设置点击后跳转到文件发送与接收界面，还要有一个判断，判断点击的是不是LingDong热点，这里暂时就不判断了，后期会更改为只显示LingDong这个热点
                Log.i("TAG", "333333333333333444444444444444");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while(GetIpAddress().equals("192.168.43.1")) {
                                Thread.sleep(500);
                            }
                            Intent intent_filetrans = new Intent(FlowActivity.this, MainActivity.class);
                            startActivity(intent_filetrans);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();// 空线程延时

                break;
            }
        }

    }


    void CreatConnection(final String name, final String key, final int type) {
        new Thread(new Runnable()//匿名内部类的调用方式
        {
            @Override
            public void run() {
                wifiAdmin.openWifi();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(name, key, type));
                wifiFlag = false;//关闭扫描wifi热点的子线程
            }
        }).start();// 建立链接线程

    }

    public String GetIpAddress() {
        @SuppressLint("WifiManagerLeak") WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int i = wifiInfo.getIpAddress();
        String a = (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
        String b = "0.0.0.0";
        if (a.equals(b)) {
            a = "192.168.43.1";// 当手机当作WIFI热点的时候，自身IP地址为192.168.43.1
        }
        return a;
    }



    /**
     * 获取本机ip方法
     */
    private String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(LOG_TAG, ex.toString());
        }
        return null;
    }

    /**
     * 监听有没有wifi接入到wifi热点的线程
     */
    public class startNew extends Thread {
        public void run() {
            while (!(getConnectDeviceIP().length() > 6)) {
                //上面getConnectDeviceIP().length() > 6 是用来判断getConnectDeviceIP()这个字符串是否获取了IP地址，不一定非要是6，其余合适的值都行
                IP_DuiFangde = getConnectDeviceIP();


            }

            IP_DuiFangde = getConnectDeviceIP().substring(3, getConnectDeviceIP().length() - 1);

            //跳转到文件发送界面
            if (isIp(IP_DuiFangde)) {

                //如果是一个IP就跳转到文件发送界面
                Log.i("TAG", "444444444444444444444444444444");
                Intent intent_filetrans2 = new Intent(FlowActivity.this, MainActivity.class);
                startActivity(intent_filetrans2);
            }

        }

    }




    /**
     * 判断一个字符串是否是标准的IPv4地址
     */
    public static boolean isIp(String IP) {
        boolean b = false;
        //去掉IP字符串前后所有的空格
        while (IP.startsWith(" ")) {
            IP = IP.substring(1, IP.length()).trim();
        }
        while (IP.endsWith(" ")) {
            IP = IP.substring(0, IP.length() - 1).trim();
        }

        //IP = this.deleteSpace(IP);
        if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String s[] = IP.split("\\.");
            if (Integer.parseInt(s[0]) < 255)
                if (Integer.parseInt(s[1]) < 255)
                    if (Integer.parseInt(s[2]) < 255)
                        if (Integer.parseInt(s[3]) < 255)
                            b = true;
        }
        return b;
    }

    /**
     * 去除字符串前后的空格
     */
    public String deleteSpace(String IP) {//去掉IP字符串前后所有的空格
        while (IP.startsWith(" ")) {
            IP = IP.substring(1, IP.length()).trim();
        }
        while (IP.endsWith(" ")) {
            IP = IP.substring(0, IP.length() - 1).trim();
        }
        return IP;
    }



    /**
     * 点击两次退出程序
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();


            } else {

                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }





    @Override
    public void finish() {
        if (WifiApAdmin.isWifiApEnabled(wifiManager)) {
            WifiApAdmin.closeWifiAp(wifiManager);

        }


        super.finish();
        android.os.Process.killProcess(android.os.Process.myPid()); /**杀死这个应用的全部进程*/

    }

    /**雷达扫面界面的显示方法*/
    private void showPopupWindow() {
        View popView = View.inflate(getApplicationContext(), R.layout.layout_pop, null);
        iv_scanning = (ImageView) popView.findViewById(R.id.iv_scanning);
        initAnimation();
        PopupWindow popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xcc000000));

        popupWindow.showAtLocation(btn_join, Gravity.CENTER, 0, 0);
    }

    /**雷达扫面界面的实现方法*/
    private void initAnimation() {
        RotateAnimation rotateAnimation = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(1500);
        rotateAnimation.setRepeatCount(RotateAnimation.INFINITE);
        iv_scanning.startAnimation(rotateAnimation);

    }



}
