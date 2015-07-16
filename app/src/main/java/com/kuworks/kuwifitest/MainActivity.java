package com.kuworks.kuwifitest;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.View;
import android.net.wifi.ScanResult;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {

    int                                     size = 0;
    boolean                                 isActive = true;
    boolean                                 isDisplayed = false;
    boolean                                 FirstTimeFlag = true;

    WifiManager                             wifi;
    WifiInfo	                            wifiInfo;
    Button                                  buttonScan;
    List<ScanResult>                        results;

    ListView                                LView;
    SimpleAdapter                           SAdaptor;

    ArrayList<HashMap<String, String>>      WifiArrayList = new ArrayList<HashMap<String,String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

 //     buttonScan = (Button)findViewById(R.id.buttonScan);        // Valiable initialization only can be in method

        LView = (ListView)findViewById(R.id.listView);

        wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled())
        {
            //////////// 以AlertDialog向使用者要求開啟wifi ////////////
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Remind");
            dialog.setMessage("Your Wi-Fi is not enabled");
            dialog.setIcon(android.R.drawable.ic_dialog_info);
            dialog.setCancelable(false);
            dialog.setPositiveButton("Enable it!",
                                     new DialogInterface.OnClickListener()
                                     {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {

                                            wifi.setWifiEnabled(true);
                                            Toast.makeText(getApplicationContext(), "wifi is enabled", Toast.LENGTH_LONG).show();
                                        }
                                     });
            dialog.show();
            ///////////////////////////////////////////////
        }

        /*
        */


        /*
        */


        /*
        buttonScan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        }
        */

        SAdaptor = new SimpleAdapter(this, WifiArrayList,
                                     android.R.layout.simple_list_item_2,
                                     new String[] {"SSID","BSSID"},
                                     new int[] {android.R.id.text1, android.R.id.text2});
        LView.setAdapter(SAdaptor);


        // 要註冊給系統的 BroadcastReceiver
        // 當有符合的事件發生時，便會被呼叫
        BroadcastReceiver   WifiScanReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                // 收到訊息時會做以下的事 //
                if (isActive && !isDisplayed)
                {
                    results = wifi.getScanResults();
                    size = results.size();

                    for (int i=0;i<size;i++)
                    {
                        HashMap<String, String> item = new HashMap<String, String>();

                        item.put("SSID",results.get(i).SSID);
                        item.put("BSSID",results.get(i).BSSID);
                        WifiArrayList.add(item);
                    }

                    Toast.makeText(getApplicationContext(), "Find "+size+" wifi APs", Toast.LENGTH_SHORT).show(); // 如果只是要顯示int的話一定要打上""+

                    SAdaptor.notifyDataSetChanged();

                    isDisplayed = true;
                }
            }
        };

        // 向系統註冊receiver, 並透過 IntentFilter 告訴系統你要接收的事件是什麼
        // 也就是WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
        registerReceiver(
                WifiScanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        );
    }


    public void buttonScanClick(View view)
    {
        // 敲下button開始掃瞄 //
        if (isDisplayed || FirstTimeFlag) {
            WifiArrayList.clear();
            wifi.startScan();

            Toast.makeText(getApplicationContext(), "Scanning nearby wifi AP...", Toast.LENGTH_SHORT).show(); // 一定要""+

            if (FirstTimeFlag)
            {
                FirstTimeFlag = false;
            }

            isDisplayed = false;
        }
    }

    public void onPause()
    {
        super.onPause();

        if (isActive)
        {
            isActive = false;
        }
    }

    public void onResume()
    {
        super.onResume();

        if (!isActive)
        {
            isActive = true;
        }

        if (!FirstTimeFlag)
        {
            FirstTimeFlag = true;
        }
    }

}
