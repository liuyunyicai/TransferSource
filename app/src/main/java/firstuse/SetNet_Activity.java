package firstuse;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import mainview.demo.DemoApplication;
import mainview.demo.R;
import ssl.Connect_Thread;

public class SetNet_Activity extends Activity {
    Switch switch_nfc = null,// 打开NFC
            switch_gps = null,// 打开GPS
            switch_net = null;// 打开网络连接
    EditText netchange_text = null, netchange_text1 = null,
            netchange_text2 = null, netchange_text3 = null;// 修改后的网络地址
    Button changebet_bt = null;// 修改网络按钮
    TextView netdefault_line = null, netchange_line = null;// 两个分割线
    RelativeLayout changenet_layout = null;

    private EditText dns_text = null;
    private Button changedns_bt = null;
    boolean change_flag = false;
    private SharedPreferences share;// 轻量存储

    public static final String DEFAULT_IP = "172.27.35.1";// 系统默认IP
    public static final String DEFAULT_DNS = "pngye.xicp.net";// 系统默认IP
    String getcurrent_IP = "";// 获取当前IP

    // Handler消息类
    private enum HandleTag {
        FAILURE_MSG, SUCCESS_MAG;
    }

    private MyHandler myHandler;

    private DemoApplication myApplication = null;
    private LocationManager locationManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.network_layout);
        myApplication = (DemoApplication) super.getApplication();

        init();
        myHandler = new MyHandler(this);
    }

    /*界面初始化*/
    private void init() {
        // 获取ID
        switch_nfc = (Switch) super.findViewById(R.id.switch_nfc);
        switch_gps = (Switch) super.findViewById(R.id.switch_gps);
        switch_net = (Switch) super.findViewById(R.id.switch_net);

        netchange_text = (EditText) super.findViewById(R.id.netchange_text);
        netchange_text1 = (EditText) super.findViewById(R.id.netchange_text_1);
        netchange_text2 = (EditText) super.findViewById(R.id.netchange_text_2);
        netchange_text3 = (EditText) super.findViewById(R.id.netchange_text_3);
        dns_text = (EditText) super.findViewById(R.id.dns_text);
        // 查询上次设置的历史，显示设置
        share = getSharedPreferences("transfer", Activity.MODE_PRIVATE);
        getcurrent_IP = share.getString("ip_address", DEFAULT_IP);
        String[] string_array = getcurrent_IP.split("\\.");

        locationManager = (LocationManager) super.getBaseContext().
                getSystemService(Context.LOCATION_SERVICE);

        try {
            netchange_text.setText(string_array[0]);
            netchange_text1.setText(string_array[1]);
            netchange_text2.setText(string_array[2]);
            netchange_text3.setText(string_array[3]);
            dns_text.setText(share.getString("dns_address", DEFAULT_DNS));
        } catch (Exception e) {
        }

        // 修改网络按钮
        changebet_bt = (Button) super.findViewById(R.id.changebet_bt);
        changebet_bt.setOnClickListener(new ChangeNet_Click());

        changedns_bt = (Button) super.findViewById(R.id.changedns_bt);
        changedns_bt.setOnClickListener(new ChangeDnsOnClick());

        // 设置默认不显示
        netdefault_line = (TextView) super.findViewById(R.id.netdefault_line);
        netchange_line = (TextView) super.findViewById(R.id.netchange_line);
        changenet_layout = (RelativeLayout) super
                .findViewById(R.id.changenet_layout);
        if (!change_flag) {// 设置默认不显示的情况
            netchange_line.setVisibility(View.INVISIBLE);
            changenet_layout.setVisibility(View.INVISIBLE);
        }
        // 判断是否已连接
        if (myApplication.getConnected()) {
            changebet_bt.setEnabled(false);
            changebet_bt.setText("已连接");
            changedns_bt.setEnabled(false);
            changedns_bt.setText("已连接");
        }
        switch_nfc.setOnCheckedChangeListener(new OnNFCClickChanged());
        switch_gps.setOnCheckedChangeListener(new OnGPSClickChanged());
        switch_net.setOnCheckedChangeListener(new OnNETClickChanged());
        if (GPSisOpen())
            switch_gps.setChecked(true);
    }


    /**
     * 信息处理Handle
     */
    public static class MyHandler extends Handler {
        /* 建立弱引用 */
        private WeakReference<SetNet_Activity> mOuter;

        /* 构造函数 */
        public MyHandler(SetNet_Activity activity) {
            mOuter = new WeakReference<SetNet_Activity>(activity);
        }

        public void handleMessage(Message msg) {
            // 防止内存泄露
            SetNet_Activity outer = mOuter.get();
            if (outer != null) {
                outer.handleservice(msg);
            }
        }
    }

    /**
     * 消息处理函数
     */
    private void handleservice(Message msg) {
        HandleTag handleTag = HandleTag.values()[msg.what];
        switch (handleTag) {
            case FAILURE_MSG:
                Toast.makeText(SetNet_Activity.this, R.string.toast_internet_failed,
                        Toast.LENGTH_SHORT).show();
                break;
            case SUCCESS_MAG:
                // Toast.makeText(SetNet_Activity.this, "连接服务器成功",
                // Toast.LENGTH_SHORT).show();
                changebet_bt.setEnabled(false);
                changebet_bt.setText("已连接");
                changedns_bt.setEnabled(false);
                changedns_bt.setText("已连接");

                // 设置连接状态为已连接
                myApplication.setConnected(true);
                break;
        }
    }


    // 修改网络按钮功能
    private class ChangeNet_Click implements OnClickListener {
        public void onClick(View v) {
            if (netchange_text.getText().toString().trim().equals("")
                    || netchange_text1.getText().toString().trim().equals("")
                    || netchange_text2.getText().toString().trim().equals("")
                    || netchange_text3.getText().toString().trim().equals("")) {
                Toast.makeText(SetNet_Activity.this, R.string.toast_no_empty_ip,
                        Toast.LENGTH_SHORT).show();
            } else {
                String get_net = netchange_text.getText().toString() + "."
                        + netchange_text1.getText().toString() + "."
                        + netchange_text2.getText().toString() + "."
                        + netchange_text3.getText().toString();
                // 记录到手机中，便于下次使用
                SharedPreferences.Editor editor = share.edit();
                editor.putString("ip_address", get_net);
                editor.putBoolean("ip_flag", true);
                editor.putBoolean("dns_flag", false);
                editor.commit();
                // 创建连接线程
                ChangeNet_Connect connect_Thread = new ChangeNet_Connect(
                        SetNet_Activity.this, get_net);
                connect_Thread.start();

            }
        }
    }

    // 修改域名并连接
    private class ChangeDnsOnClick implements OnClickListener {
        public void onClick(View v) {
            if (dns_text.getText().toString().trim().equals("")) {
                Toast.makeText(SetNet_Activity.this, R.string.toast_no_empty_domain,
                        Toast.LENGTH_SHORT).show();
            } else {
                String get_net = dns_text.getText().toString();
                // 记录到手机中，便于下次使用
                SharedPreferences.Editor editor = share.edit();
                editor.putString("dns_address", get_net);
                editor.putBoolean("ip_flag", false);
                editor.putBoolean("dns_flag", true);
                editor.commit();
                // 创建连接线程
                ChangeNet_Connect connect_Thread = new ChangeNet_Connect(
                        SetNet_Activity.this, get_net);
                connect_Thread.start();
            }
        }
    }

    // NFC修改功能
    private class OnNFCClickChanged implements OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                // 选中时 do some thing

            } else {
                // 非选中时 do some thing
            }

        }
    }

    // GPS修改功能
    private class OnGPSClickChanged implements OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                if (!GPSisOpen()) {
                    // 打开GPS
                    Intent gpsIntent = new Intent();
                    gpsIntent
                            .setClassName("com.android.settings",
                                    "com.android.settings.widget.SettingsAppWidgetProvider");
                    gpsIntent
                            .addCategory("android.intent.category.ALTERNATIVE");
                    gpsIntent.setData(Uri.parse("custom:3"));
                    try {
                        PendingIntent.getBroadcast(SetNet_Activity.this, 0,
                                gpsIntent, 0).send();
                        Toast.makeText(SetNet_Activity.this, R.string.toast_gps_opened,
                                Toast.LENGTH_SHORT).show();
                    } catch (CanceledException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                // 关闭GPS
                if (GPSisOpen()) {
                    Intent gpsIntent = new Intent();
                    gpsIntent
                            .setClassName("com.android.settings",
                                    "com.android.settings.widget.SettingsAppWidgetProvider");
                    gpsIntent
                            .addCategory("android.intent.category.ALTERNATIVE");
                    gpsIntent.setData(Uri.parse("custom:3"));
                    try {
                        PendingIntent.getBroadcast(SetNet_Activity.this, 0,
                                gpsIntent, 0).send();
                        Toast.makeText(SetNet_Activity.this, R.string.toast_gps_closed,
                                Toast.LENGTH_SHORT).show();
                    } catch (CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    // 判断GPS是否开启
    private boolean GPSisOpen() {

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		/*String str = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_MODE);
		Log.v("GPS", str);
		if (str != null) {
			return str.contains("gps");
		} else {
			return false;
		}*/

    }

    // NET修改功能
    private class OnNETClickChanged implements OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                // 选中时 do some thing
                netdefault_line.setVisibility(View.INVISIBLE);
                changenet_layout.setVisibility(View.VISIBLE);
                netchange_line.setVisibility(View.VISIBLE);

            } else {
                // 非选中时 do some thing
                netdefault_line.setVisibility(View.VISIBLE);
                changenet_layout.setVisibility(View.INVISIBLE);
                netchange_line.setVisibility(View.INVISIBLE);
            }

        }
    }

    // 连接SSL子线程
    private class ChangeNet_Connect extends Thread {
        private Context mContext = null;
        private Connect_Thread connect_thread;

        // 构造函数
        public ChangeNet_Connect(Context mContext, String changed_host) {
            this.mContext = mContext;
            connect_thread = new Connect_Thread(mContext, changed_host);
        }

        // 线程主函数
        public void run() {
            try {
                // 尝试连接
                connect_thread.init();
                boolean flag = connect_thread.process();
                // 发送已连接Handler消息
                Message msg = new Message();
                if (flag) {
                    msg.what = HandleTag.SUCCESS_MAG.ordinal();
                    // 保存SSLSocket
                    myApplication.setSSlSocket(connect_thread.getssl_socket());
                } else {
                    msg.what = HandleTag.FAILURE_MSG.ordinal();
                }
                SetNet_Activity.this.myHandler.sendMessage(msg);
            } catch (Exception e) {
                Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


    private int startX;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int)event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (event.getX() - startX > 20)
                    this.finish();
                break;
        }

        return super.onTouchEvent(event);
    }
}
