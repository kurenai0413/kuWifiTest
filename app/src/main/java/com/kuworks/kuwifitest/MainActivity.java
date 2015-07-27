package com.kuworks.kuwifitest;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Window;
import android.view.View;
import android.net.wifi.ScanResult;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;



public class MainActivity extends Activity {

    int                                     size = 0;
    int                                     scrollstate;
    boolean                                 isActive = true;
    boolean                                 isScanResultDisplayed = false;
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
        LView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                scrollstate = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

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
        buttonScan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        }
        */

        SAdaptor = new SimpleAdapter(this, WifiArrayList,
                                     R.layout.mylistview,
                                     new String[] {"SSID","BSSID","Power"},
                                     new int[] {R.id.SSID, R.id.BSSID, R.id.Power});
        LView.setAdapter(SAdaptor);


        // 要註冊給系統的 BroadcastReceiver
        // 當有符合的事件發生時，便會被呼叫
        BroadcastReceiver   WifiScanReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                // 收到訊息時會做以下的事 //
                if (isActive && !isScanResultDisplayed)
                {
                    results = wifi.getScanResults();
                    size = results.size();

                    for (int i=0;i<size;i++)
                    {
                        HashMap<String, String> item = new HashMap<String, String>();

                        item.put("SSID",results.get(i).SSID);
                        item.put("BSSID",results.get(i).BSSID);
                        item.put("Power",new String(results.get(i).level+" dBm"));
                        WifiArrayList.add(item);
                    }

                    Toast.makeText(getApplicationContext(), "Find "+size+" wifi APs", Toast.LENGTH_SHORT).show(); // 如果只是要顯示int的話一定要打上""+

                    SAdaptor.notifyDataSetChanged();

                    isScanResultDisplayed = true;
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
        if ((scrollstate!=2) && (isScanResultDisplayed || FirstTimeFlag)) {
            WifiArrayList.clear();
            wifi.startScan();

            Toast.makeText(getApplicationContext(), "Scanning nearby wifi AP...", Toast.LENGTH_SHORT).show(); // 一定要""+

            if (FirstTimeFlag)
            {
                FirstTimeFlag = false;
            }

            isScanResultDisplayed = false;
        }
    }

    public void buttonSaveClick(View view)
    {
        // 敲下save鈕 //
        if (isScanResultDisplayed) {
            final File Dir = getApplicationContext().getExternalFilesDir(null);
            if (!Dir.exists()){
                Dir.mkdirs();
            }

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.stationdialog, null);
            dialog.setTitle("Please input station name:");
            dialog.setView(layout);
            final EditText etDialog = (EditText)layout.findViewById(R.id.stationtext);
            dialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String StationName = etDialog.getText().toString();         // 取得輸入字串

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    Date now = new Date();
                    String fileName = StationName + "_" + formatter.format(now) + ".txt";   // 組合檔名

                    File myfile = new File(Dir.getPath()+"/"+fileName);                     // 存檔路徑

                    Toast.makeText(getApplicationContext(), "Save to " + myfile.getPath(), Toast.LENGTH_SHORT).show();

                    try {

                        FileWriter fw = new FileWriter(myfile.getPath(), false);
                        BufferedWriter bw = new BufferedWriter(fw);
                        for (int i=0;i<size;i++) {
                            bw.write("SSID: "+WifiArrayList.get(i).get("SSID")+"\r\n");
                            bw.write("BSSID: "+WifiArrayList.get(i).get("BSSID")+"\r\n");
                            bw.write("Power: "+WifiArrayList.get(i).get("Power")+"\r\n\r\n");
                        }

                        bw.close();

                        Toast.makeText(getApplicationContext(), "Writing file successed.", Toast.LENGTH_SHORT).show();
                    }
                    catch(IOException e) {
                        Toast.makeText(getApplicationContext(), "Writing file failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            dialog.show();
        }
    }

    private boolean externalStorageAvailable() {
        return
                Environment.MEDIA_MOUNTED
                        .equals(Environment.getExternalStorageState());
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
