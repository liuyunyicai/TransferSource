package load_reg;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

import firstuse.SetNet_Activity;
import mainview.demo.DemoApplication;
import mainview.demo.MainActivity;
import mainview.demo.R;
import mapview.GetPosition_Thread;
import ssl.Action_Type;
import ssl.Connect_Thread;
import ssl.PackageHeader;

public class LoadReg_Activity extends Activity implements OnClickListener {
    /* 空间参数模块 */
    private ImageButton setnet_bt;// 设置连接网络
    private Button load_bt;// 进入登录界面按钮
    private Button reg_bt;// 进入注册界面按钮
    private Button visitorlogin_bt;// 游客访问按钮
    private String IP_Adress = "";// 获取IP地址
    private String Dns_Adress = "";
    private Button changelogin_bt;// 切换账号按钮
    private DemoApplication myApplication = null;
    private SharedPreferences share;// 轻量存储
    private boolean login_ed;// 是否已登录标志
    private String user_name = "";// 登录所需的信息
    private String user_password = "";

    /* 消息分类枚举 */
    private enum HandleTag {
        FAILURE_MSG, LOADSUCCESS_MSG, REGSUCCESS_MSG, LOGSUCCESS, LOGFAILURE, RELOGSUCCESS, RELOGFAILURE;
    }

    private MyHandler myHandler;

    /* 网络模块 */
    private SSLSocket sslSocket;
    private PackageHeader packageHeader;

    /* 双击退出模块 */
    private int mBackKeyPressedTimes = 0;
    /* 位置管理模块 */
    private LocationManager locationManager = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loadreg_layout);
        /* 界面参数初始化 */
        init();
        myHandler = new MyHandler(this);
    }

    /**
     * 界面参数初始化
     */
    private void init() {
        myApplication = (DemoApplication) super.getApplication();
		/* 获取默认存储的IP地址 */
        share = getSharedPreferences("transfer", Activity.MODE_PRIVATE);
        IP_Adress = share.getString("ip_address", SetNet_Activity.DEFAULT_IP);
        Dns_Adress = share
                .getString("dns_address", SetNet_Activity.DEFAULT_DNS);

        login_ed = share.getBoolean("login", false);
        user_name = share.getString("user_name", "xiaoaojianghu");
        user_password = share.getString("user_password", "12345678");
		/* 获得Id */
        setnet_bt = (ImageButton) super.findViewById(R.id.setnet_bt);
        load_bt = (Button) super.findViewById(R.id.load_bt);
        reg_bt = (Button) super.findViewById(R.id.reg_bt);
        changelogin_bt = (Button) super.findViewById(R.id.changelogin_bt);
        visitorlogin_bt = (Button) super.findViewById(R.id.visitorlogin_bt);
        setnet_bt.setOnClickListener(this);
        load_bt.setOnClickListener(this);
        reg_bt.setOnClickListener(this);
        changelogin_bt.setOnClickListener(this);
        visitorlogin_bt.setOnClickListener(this);

		/* 开启获取用户位置模式 */
        locationManager = (LocationManager) super.getSystemService(Context.LOCATION_SERVICE);
        GetPosition_Thread myPosition = new GetPosition_Thread(myApplication, locationManager, LoadReg_Activity.this);
        myPosition.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reg_bt:
                registerOnClick();
                break;
            case R.id.setnet_bt:
                setNetOnClick();
                break;
            case R.id.load_bt:
                test_loadOnClick();
                break;
            case R.id.changelogin_bt:
                changeLoginOnClick();
                break;
            case R.id.visitorlogin_bt:
                visitorOnClick();
                break;

        }
    }

    /**
     * changelogin_bt切换账号
     */
    private void changeLoginOnClick() {
        /* 已连接情况 */
        if (myApplication.getConnected()) {
            Intent intent = new Intent(LoadReg_Activity.this,
                    LogIn_Activity.class);
            LoadReg_Activity.this.startActivity(intent);
        } else {
			/* 若未连接 */
            connect_function(3);
        }
    }

    /**
     * 信息处理Handle
     */
    public static class MyHandler extends Handler {
        /* 建立弱引用 */
        private WeakReference<LoadReg_Activity> mOuter;

        /* 构造函数 */
        public MyHandler(LoadReg_Activity activity) {
            mOuter = new WeakReference<LoadReg_Activity>(activity);
        }

        public void handleMessage(Message msg) {
            // 防止内存泄露
            LoadReg_Activity outer = mOuter.get();
            if (outer != null) {
                outer.handleservice(msg);
            }
        }
    }

    /**
     * 消息处理函数
     */
    @SuppressLint("CommitPrefEdits")
    private void handleservice(Message msg) {
        HandleTag handleTag = HandleTag.values()[msg.what];
        PackageHeader returnHeader = (PackageHeader) msg.obj;//返回数据信息
        switch (handleTag) {
            case FAILURE_MSG:
                Toast.makeText(LoadReg_Activity.this, R.string.toast_internet_failed, Toast.LENGTH_SHORT).show();
                break;
		/* 登录按钮成功 */
            case LOADSUCCESS_MSG:
                onLoginSuccess();
                break;
		/* 注册按钮 */
            case REGSUCCESS_MSG:
                Intent intent1 = new Intent(LoadReg_Activity.this, Register_Activity.class);
                startActivity(intent1);
                myApplication.setConnected(true);
                break;
            case LOGFAILURE:
                if ((returnHeader.send_string != null) || (!returnHeader.send_string.equals("")))
                    Toast.makeText(LoadReg_Activity.this, returnHeader.send_string, Toast.LENGTH_SHORT).show();
                break;
		/* 登录功能 */
            case LOGSUCCESS:
                onLogSuccess(returnHeader);

                break;
            case RELOGFAILURE:
                if ((returnHeader.send_string != null) || (!returnHeader.send_string.equals("")))
                    Toast.makeText(LoadReg_Activity.this, returnHeader.send_string, Toast.LENGTH_SHORT).show();
                break;
		/* 重新登录功能 */
            case RELOGSUCCESS:
                Intent intent2 = new Intent(LoadReg_Activity.this, LogIn_Activity.class);
                LoadReg_Activity.this.startActivity(intent2);
                break;
        }
    }

    // 第二次登录使用函数
    private void onLogSuccess(PackageHeader returnHeader) {
    /*保存返回值*/
        SharedPreferences.Editor editor = share.edit();
        editor.putInt("user_id", returnHeader.byte_2_id(returnHeader.user_name));
        editor.putInt("user_level", returnHeader.user_level);
        editor.putBoolean("login", true);
        editor.commit();
			/*跳转至主界面*/
        Intent intent = new Intent(LoadReg_Activity.this, MainActivity.class);
        LoadReg_Activity.this.startActivity(intent);
    }

    // 登录成功函数
    private void onLoginSuccess() {
        myApplication.setConnected(true);
        myApplication.anonymity_flag = false;
        login_ed = share.getBoolean("login", false);
        user_name = share.getString("user_name", "xiaoaojianghu");
        user_password = share.getString("user_password", "12345678");
			/* 当第二次使用同一账号登录时 */
        if (login_ed) {
            /* 登录函数 */
            List<String> lists = new ArrayList<String>();
            lists.add(user_name);
            lists.add(user_password);
            load_function(lists);
        } else {
            Intent intent = new Intent(LoadReg_Activity.this, LogIn_Activity.class);
            startActivity(intent);
        }
    }

    // ================  点击相应函数 =============

    /**
     * 设置网络函数
     */
    private void setNetOnClick() {
        Intent intent = new Intent(LoadReg_Activity.this,
                SetNet_Activity.class);
        LoadReg_Activity.this.startActivity(intent);
    }

    /**
     * 游客登录函数
     */
    private void visitorOnClick() {
        /* 已连接情况 */
        if (myApplication.getConnected()) {
            login_ed = share.getBoolean("login", false);
            if (!login_ed) {
                Intent intent = new Intent(LoadReg_Activity.this,
                        LogIn_Activity.class);
                LoadReg_Activity.this.startActivity(intent);
            } else {
					/* 登录函数 */
                List<String> lists = new ArrayList<String>();
                lists.add("hust");
                lists.add("hust");
                load_function(lists);
                myApplication.anonymity_flag = true;// 匿名登录参数
            }

        }
			/* 若未连接 */
        else {
            connect_function(0);
        }
    }

    // 测试Load函数
    private void test_loadOnClick() {
        startActivity(new Intent(LoadReg_Activity.this, MainActivity.class));
    }

    /**
     * 登录函数
     */
    private void loadOnClick() {
        /* 已连接情况 */
        if (myApplication.getConnected()) {
            login_ed = share.getBoolean("login", false);
            user_name = share.getString("user_name", "hust");
            user_password = share.getString("user_password", "hust");
            myApplication.anonymity_flag = false;
            if (!login_ed) {
                Intent intent = new Intent(LoadReg_Activity.this,
                        LogIn_Activity.class);
                LoadReg_Activity.this.startActivity(intent);
            } else {
					/* 登录函数 */
                List<String> lists = new ArrayList<String>();
                lists.add(user_name);
                lists.add(user_password);
                load_function(lists);
                myApplication.anonymity_flag = true;// 匿名登录参数
            }

        }
			/* 若未连接 */
        else {
				/* 连接服务器 */
            connect_function(0);
        }
    }

    /**
     * 连接服务器快捷函数入口
     */
    private void connect_function(int type) {
        ChangeNet_Connect connect_thread = null;
		/* IP方式连接 */
        if (share.getBoolean("ip_flag", false)) {
            connect_thread = new ChangeNet_Connect(LoadReg_Activity.this,
                    IP_Adress, type);
        }
		/* DNS方式连接 */
        else if (share.getBoolean("dns_flag", true)) {
            connect_thread = new ChangeNet_Connect(LoadReg_Activity.this,
                    Dns_Adress, type);
        }
        connect_thread.start();
    }

    /**
     * 登录函数
     */
    private void load_function(List<String> lists) {
		/* 组装发送参数 */
        sslSocket = myApplication.getSSlSocket();
        packageHeader = new PackageHeader();
		/* 组装字符串 */
        String dataString = packageHeader.joinString(lists);
		/* 设置package参数 */
        packageHeader.send_string = dataString;
        packageHeader.data_length = dataString.length();
        packageHeader.now_time = System.currentTimeMillis();

		/* 发送信息（登录操作） */
        Rece_Thread rece_Thread = new Rece_Thread(sslSocket,
                Action_Type.CUSTOMER_LOAD, packageHeader);
        rece_Thread.start();
    }

    /**
     * 注册函数
     */
    private void registerOnClick() {
        /* 已连接情况 */
        if (myApplication.getConnected()) {
            Intent intent = new Intent(LoadReg_Activity.this, Register_Activity.class);
            LoadReg_Activity.this.startActivity(intent);
        } else {
			/* 若未连接 */
            connect_function(1);
        }
    }

    /**
     * 连接SSL子线程
     */
    private class ChangeNet_Connect extends Thread {
        private Context mContext = null;
        private Connect_Thread connect_thread;
        private int type = 0;

        /* 构造函数 */
        public ChangeNet_Connect(Context mContext, String changed_host, int type) {
            this.mContext = mContext;
            this.type = type;
            connect_thread = new Connect_Thread(mContext, changed_host);
        }

        /* 线程主函数 */
        public void run() {
            try {
                Looper.prepare();
				/* 尝试连接 */
                connect_thread.init();
                boolean flag = connect_thread.process();
				/* 发送已连接Handler消息 */
                Message msg = new Message();
                if (!flag) {
                    msg.what = HandleTag.FAILURE_MSG.ordinal();
                } else {
                    if (type == 0) {
                        msg.what = HandleTag.LOADSUCCESS_MSG.ordinal();
                        myApplication.setSSlSocket(connect_thread
                                .getssl_socket());
                    } else if (type == 1) {
                        msg.what = HandleTag.REGSUCCESS_MSG.ordinal();
                        myApplication.setSSlSocket(connect_thread
                                .getssl_socket());
                    } else {
                        msg.what = HandleTag.RELOGSUCCESS.ordinal();
                        myApplication.setSSlSocket(connect_thread
                                .getssl_socket());
                    }
                }
                LoadReg_Activity.this.myHandler.sendMessage(msg);
                Looper.loop();
            } catch (Exception e) {
                Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * 网络通信线程
     */
    private class Rece_Thread extends Thread {

        private SSLSocket sslSocket;
        private int post_type;
        private PackageHeader packageHeader;
        private PackageHeader returnHeader = new PackageHeader();

        public Rece_Thread(SSLSocket sslSocket, int post_type,
                           PackageHeader packageHeader) {
            this.sslSocket = sslSocket;
            this.post_type = post_type;
            this.packageHeader = packageHeader;
        }

        public void run() {
            Message msg = new Message();
			/* 发送请求信息，并监听返回 */
            returnHeader = packageHeader.send_postdata(sslSocket, post_type);
            boolean successflag = packageHeader
                    .Judge_Success(returnHeader.success_flag);
            if (successflag) {
                msg.what = HandleTag.LOGSUCCESS.ordinal();
            } else {
                msg.what = HandleTag.LOGFAILURE.ordinal();
            }
            msg.obj = returnHeader;
            myHandler.sendMessage(msg);
        }
    }

    /**
     * 连续点击退出
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        }
        return false;
    }

    /**
     * 按钮监听
     */
    public void onBackPressed() {
        if (mBackKeyPressedTimes == 0) {
            Toast.makeText(this, "连续点击退出程序 ", Toast.LENGTH_SHORT).show();
            mBackKeyPressedTimes = 1;
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        mBackKeyPressedTimes = 0;
                    }
                }
            }.start();
            return;
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}
