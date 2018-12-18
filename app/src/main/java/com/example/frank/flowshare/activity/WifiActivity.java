package com.example.frank.flowshare.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.frank.flowshare.R;
import com.example.frank.flowshare.config.WifiConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class WifiActivity extends AppCompatActivity {

    private Button btn_get,btn_share,btn_closewifi,btn_closeap,btn_repeat_net,btn_use_state;
    private WifiManager wifiManager;
    private boolean flag=false;

    private List<ScanResult> wifiList;
    //private WifiManager wifiManagerc;
    private List<String> passableHotsPot;
    private WifiReceiver wifiReceiver;
    private boolean isConnected=false,isOpenWifi=false;
    //private static ArrayList<HashMap<String, Object>> wificonnect = new ArrayList<HashMap<String,     Object>>();

    private HashMap<String, Object> wificonnect=new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        init();


    }

    private void init(){
        btn_get=(Button)this.findViewById(R.id.btn_get_flow);
        btn_share=(Button)this.findViewById(R.id.btn_share_flow);
        btn_closewifi=(Button)this.findViewById(R.id.btn_close_wifi);
        btn_closeap=(Button)this.findViewById(R.id.btn_close_ap);
        btn_repeat_net=(Button)this.findViewById(R.id.btn_repeat_net);
        btn_use_state=(Button)this.findViewById(R.id.btn_use_state);

        boolean gps=isLocationEnabled();
        Log.i("定位打开情况：",gps+"");

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //wifiReceiver = new WifiReceiver();
        //registerBroadcast();
        wifiRegister();




        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果是打开状态就关闭，如果是关闭就打开
                closeWifi();
                closeWifiAp();
                flag=true;
                boolean success=setWifiApEnabled(true);
                if (success){
                    Intent intent=new Intent(WifiActivity.this,AuctionActivity.class);
                    startActivity(intent);

                }else {
                    Toast.makeText(WifiActivity.this,"共享网络创建失败，请关闭wifi后，重启手机，再次使用！",Toast.LENGTH_LONG).show();
                }


            }
        });


        btn_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*switch (wifiState) {
                    case 0:
                        state = "WIFI_STATE_DISABLING";
                        break;
                    case 1:
                        state = "WIFI_STATE_DISABLED";
                        break;
                    case 2:
                        state = "WIFI_STATE_ENABLING";
                        break;
                    case 3:
                        state = "WIFI_STATE_ENABLED";
                        break;
                    case 4:
                        state = "WIFI_STATE_UNKNOWN";
                        break;
                    default:
                        break;
                        •enabled（已连接）
•disabled（已关闭）
•enabling（连接中）
•disabling（关闭中）
•unknown（未知）

                }*/


                isConnected=false;
                if (isOpenWifi&&WifiManager.WIFI_STATE_ENABLED==3&&wifiList!=null){
                    onReceiveNewNetworks(wifiList);
                }else {
                    closeWifi();
                    closeWifiAp();
                    isOpenWifi=true;
                    wifiManager.setWifiEnabled(true);
                    wifiManager.startScan();
                }

                Log.i("扫描结果：","========"+wifiManager.getConnectionInfo());

            }
        });

        btn_closewifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOpenWifi=false;
                isConnected=false;
                wifiList.clear();
                //wifiList=null;
                closeWifi();
                Toast.makeText(WifiActivity.this,"手机WIFI已关闭！",Toast.LENGTH_LONG).show();
            }
        });


        btn_closeap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeWifi();
                closeWifiAp();
                Toast.makeText(WifiActivity.this,"共享网络已关闭！",Toast.LENGTH_LONG).show();
            }
        });


        btn_repeat_net.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(WifiActivity.this,"正在重新扫描网络！",Toast.LENGTH_SHORT).show();
                wificonnect.clear();
                isConnected=false;
                if (WifiManager.WIFI_STATE_ENABLED==3&&wifiList!=null){
                    onReceiveNewNetworks(wifiList);
                }else {
                    closeWifi();
                    closeWifiAp();
                    isOpenWifi=true;
                    wifiManager.setWifiEnabled(true);
                    wifiManager.startScan();
                }

                Log.i("扫描结果：","========"+wifiManager.getConnectionInfo());
            }
        });

        btn_use_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }



    // wifi热点开关
    public boolean setWifiApEnabled(boolean enabled) {
        if (enabled) { // disable WiFi in any case
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            wifiManager.setWifiEnabled(false);
        }
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称(可以在名字后面加点随机数什么的)
            Random random=new Random();
            int r=random.nextInt(1000);
            apConfig.SSID = WifiConfig.WifiSSID+r;
            //配置热点的密码
            apConfig.preSharedKey=WifiConfig.WifiKey;
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            Toast.makeText(WifiActivity.this,"共享网络创建成功！",Toast.LENGTH_LONG).show();
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);
        } catch (Exception e) {
            return false;
        }
    }




    /* 监听热点变化 */
    private final class WifiReceivers extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isOpenWifi){
                Log.i("扫描成功","ss");
                if (wifiList!=null){
                    wifiList.clear();
                }

                wifiList = wifiManager.getScanResults();
                if (wifiList == null || wifiList.size() == 0 || isConnected)
                    return;
                onReceiveNewNetworks(wifiList);
            }

        }
    }

    /*当搜索到新的wifi热点时判断该热点是否符合规格*/
    public void onReceiveNewNetworks(List<ScanResult> wifiList){
        passableHotsPot=new ArrayList<String>();
        for(ScanResult result:wifiList){
            System.out.println(result.SSID);
            if (isConnected){
                break;
            }
            if((result.SSID).contains(WifiConfig.WifiSSID)){
                if (wificonnect.get(result.SSID)==null||wificonnect.get(result.SSID).equals("false")){
                    passableHotsPot.add(result.SSID);

                }

            }

        }

        synchronized (this) {
            if (!isConnected){
                connectToHotpot();
            }

        }
    }

    /*连接到热点*/
    public void connectToHotpot(){
        if(passableHotsPot==null || passableHotsPot.size()==0)
            return;
        WifiConfiguration wifiConfig=this.setWifiParams(passableHotsPot.get(0));
        int wcgID = wifiManager.addNetwork(wifiConfig);
        boolean flag=wifiManager.enableNetwork(wcgID, true);
        isConnected=flag;
        if (isConnected){
            wificonnect.put(passableHotsPot.get(0),"true");
            Toast.makeText(WifiActivity.this,"网络连接成功，请注意参加流量竞拍！",Toast.LENGTH_LONG).show();
            Intent intent=new Intent(WifiActivity.this,OfferActivity.class);
            startActivity(intent);
        }else {
            Toast.makeText(WifiActivity.this,"正在扫描和连接可用网络，请稍等！",Toast.LENGTH_LONG).show();

        }

        System.out.println("connect success? "+flag);
    }

    /*设置要连接的热点的参数*/
    public WifiConfiguration setWifiParams(String ssid){
        WifiConfiguration apConfig=new WifiConfiguration();
        Log.i("wifi密码配置：",ssid);
        apConfig.SSID="\""+ssid+"\"";
        apConfig.preSharedKey="\""+WifiConfig.WifiKey+"\"";
        apConfig.hiddenSSID = true;
        apConfig.status = WifiConfiguration.Status.ENABLED;
        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        return apConfig;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
             /*销毁时注销广播*/
        wifiList.clear();
        wificonnect.clear();
        unregisterReceiver(wifiReceiver);
    }




    public void closeWifi() {
        if (wifiManager.isWifiEnabled()) {

            wifiManager.setWifiEnabled(false);

        }


    }


//作热点之前先关闭wifi热点服务


    public void closeWifiAp( ) {
        //if (isWifiApEnabled()) {
        //if (flag) {
            try {

                flag=false;
                Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
                method.setAccessible(true);
                WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);
                Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                method2.invoke(wifiManager, config, false);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }



    }


    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // wifi已成功扫描到可用wifi。
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                //Log.d(TAG, "接收到" + DateUtils.getCurrentDateString());
                wifiList = wifiManager.getScanResults();
                // Log.d(TAG, "mScanResults.size()===" + mScanResults.size());

            }
            //系统wifi的状态
            else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifiState = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        //Log.d(TAG, "WiFi已启用" + DateUtils.getCurrentTime());
                        wifiManager.startScan();
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        //Log.d(TAG, "Wifi已关闭" + DateUtils.getCurrentTime());
                        break;
                }
            }
        }
    };



    //注册：
    private void wifiRegister(){
        IntentFilter filter = new IntentFilter();
        // filter.addAction(WifiManager.ERROR_AUTHENTICATING);
        filter.addAction(WifiManager.ACTION_PICK_WIFI_NETWORK);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        // 测试wifi验证密码错误问题
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(new WifiReceivers(), filter);
    }

    //接收：
    class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            wifiList = wifiManager.getScanResults();
            String action = intent.getAction();
            onReceiveNewNetworks(wifiList);
            Log.i("WifiReceiver", action);
            // / Wifi 状态变化
            if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                WifiInfo info = wifiManager.getConnectionInfo();
                SupplicantState state = info.getSupplicantState();
                if (state == SupplicantState.COMPLETED) {

                    Log.i("WifiReceiver", "(验证成功)");
                }
                int errorCode = intent.getIntExtra(
                        WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                if (errorCode == WifiManager.ERROR_AUTHENTICATING) {

                    Log.i("WifiReceiver", "(验证失败)");
                }
            }

        }
    }



    public boolean isLocationEnabled() {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

}
