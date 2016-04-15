package mainview.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;

import fourtabview.TabFourFragment;
import fourtabview.TabOneFragment;
import fourtabview.TabThreeFragemnt;
import fourtabview.TabTwoFragment;
import ssl.Action_Type;
import ssl.PackageHeader;

/*主操作界面*/
public class MainActivity extends FragmentActivity {
    private static final String LOG_TAG = "LOG_TAG";

    // 标签配置
    public static TabHost myTabHost;
    // 通信接受的字符串
    public static DemoApplication myApplication = null;
    private SSLSocket sslSocket;
    private PackageHeader packageHeader;
    public static SharedPreferences share;

    /* 自定义Tab */
    private RadioGroup bottomRg;
    private RadioButton radio1, radio2, radio3, radio4;
    private TextView barTxt;

    /* Fragment管理 */
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private enum HandleTag {
        EXITSUCCESS, EXITFAILURE, // 退出标志
        FAILURE, ONE_SUCCESS, TWO_SUCCESS, THREE_SUCCESS, FOURSUCCESS;
    }

    private MyHandler myHandler;

    public static MainActivity instance = null;

    /* 四个界面组 */
    private TabOneFragment taboneFragemnt;
    private TabTwoFragment tabTwoFragment;
    private TabThreeFragemnt tabThreeFragemnt;
    private TabFourFragment tabFourFragment;

    private ArrayList<Fragment> fragmentList;
    private ViewPager myViewPager;
    private MyViewAdapter mAdapter;

    private int curIndex = 0;

    private int[] headTitles = {R.string.tab_title_txt1, R.string.tab_title_txt2, R.string.tab_title_txt3, R.string.tab_title_txt4};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_load);
        // 判断是否需要跳转到登录界面
        instance = this;
        myApplication = (DemoApplication) super.getApplication();
        sslSocket = myApplication.getSSlSocket();
        // 获取存储的数据
        share = getSharedPreferences("transfer", Activity.MODE_PRIVATE);
        myHandler = new MyHandler(this);
        initpackage();
        init();

    }

    /* 界面初始化 */
    private void init() {

        bottomRg = $(R.id.bottomRg);
        radio1 = $(R.id.radio1);
        radio2 = $(R.id.radio2);
        radio3 = $(R.id.radio3);
        radio4 = $(R.id.radio4);
        barTxt = $(R.id.barTxt);
        myViewPager = $(R.id.myViewPager);

        fragmentManager  = super.getSupportFragmentManager();
        taboneFragemnt   = new TabOneFragment();
        tabTwoFragment   = new TabTwoFragment();
        tabThreeFragemnt = new TabThreeFragemnt();
        tabFourFragment  = new TabFourFragment();


        fragmentList = new ArrayList<Fragment>();
        fragmentList.add(taboneFragemnt);
        fragmentList.add(tabTwoFragment);
        fragmentList.add(tabThreeFragemnt);
        fragmentList.add(tabFourFragment);
        mAdapter = new MyViewAdapter(fragmentManager, fragmentList);
        setCurFragment(0);
        myViewPager.setAdapter(mAdapter);
        myViewPager.addOnPageChangeListener(new MyOnPageChangeListener());

        bottomRg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int index = 0;
                switch (checkedId) {
                    case R.id.radio1:
                        index = 0;
                        break;
                    case R.id.radio2:
                        index = 1;
                        break;
                    case R.id.radio3:
                        index = 2;
                        break;
                    case R.id.radio4:
                        index = 3;
                        break;
                    default:
                        break;
                }
                setCurFragment(index);
            }
        });

    }

    private <T> T $(int resId) {
        return (T) findViewById(resId);
    }

    // 设置当前界面
    private void setCurFragment(int newIndex) {
        SetRadioTextColor(newIndex);

        myViewPager.setCurrentItem(newIndex, true);
        curIndex = newIndex;
    }


    // 构建ViewPager的Adapter
    private class MyViewAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragmentList;

        public MyViewAdapter(FragmentManager fm, ArrayList<Fragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        @Override
        public Fragment getItem(int position) {
            if (fragmentList != null)
                return fragmentList.get(position);
            return null;
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }
    }

    // 监听Viewpager滑动
    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            setCurFragment(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            Log.w(LOG_TAG, "state == " + state);
        }
    }


    /*转换店中TAb后的文字颜色变化*/
    private void SetRadioTextColor(int index) {
        RadioButton[] radioButtons = {radio1, radio2, radio3, radio4};
        barTxt.setText(headTitles[index]);
        if (index < radioButtons.length) {
            for (int i = 0; i < radioButtons.length; i++) {
                if (index == i) {
                    radioButtons[i].setChecked(true);
                }
            }
        }
    }

    /**
     * packageHeader初始化
     */
    private void initpackage() {
        try {
            packageHeader = new PackageHeader();
            packageHeader.return_flag = 0;
            packageHeader.success_flag = 0;
            packageHeader.user_level = (byte) share.getInt("user_level", 5);
            packageHeader.user_name = packageHeader.id_2_byte(share.getInt(
                    "user_id", 0));

        } catch (Exception e) {
        }
    }

    // handler处理事件
    // 网络通信线程
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
                msg.what = HandleTag.EXITSUCCESS.ordinal();
            } else {
                msg.what = HandleTag.EXITFAILURE.ordinal();
            }
            msg.obj = returnHeader;
            myHandler.sendMessage(msg);
        }
    }

    /**
     * 退出函数
     */
    private void exit_function() {
		/* 组装发送参数 */
        sslSocket = myApplication.getSSlSocket();
		/* 设置package参数 */
        packageHeader.now_time = System.currentTimeMillis();
        try {
            packageHeader.place_long = Double
                    .valueOf(myApplication.user_placelong);
            packageHeader.place_lat = Double
                    .valueOf(myApplication.user_placelat);
        } catch (Exception e) {
        }

		/* 发送信息（登录操作） */
        Rece_Thread rece_Thread = new Rece_Thread(sslSocket,
                Action_Type.CUSTOMER_EXIT, packageHeader);
        rece_Thread.start();
    }

    private static class MyHandler extends Handler {
        /* 建立弱引用 */
        private WeakReference<MainActivity> mOuter;

        /* 构造函数 */
        public MyHandler(MainActivity activity) {
            mOuter = new WeakReference<MainActivity>(activity);
        }

        public void handleMessage(Message msg) {
            // 防止内存泄露
            MainActivity outer = mOuter.get();
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
        PackageHeader returnHeader = (PackageHeader) msg.obj;// 返回数据信息
        switch (handleTag) {
            case EXITFAILURE:
                try {
                    Toast.makeText(MainActivity.this, returnHeader.send_string,
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, R.string.toast_exit_failed, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case EXITSUCCESS:
                Toast.makeText(MainActivity.this, R.string.toast_exit_success, Toast.LENGTH_SHORT)
                        .show();
                // 登录数据存储
                myApplication.saved_flag = false;
                MainActivity.this.finish();
                break;

        }
    }

    // 对返回键进行控制
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            MainActivity.this.exitDialog();
        }
        return true;
    }

    // 退出警告对话框
    private void exitDialog() {
        Dialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setIcon(R.drawable.warn_icon)
                .setTitle(R.string.warning)
                .setMessage(R.string.ensure_to_exit)
                .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (myApplication.anonymity_flag) {
                            myApplication.saved_flag = false;
                            MainActivity.this.finish();
                        } else {
                            exit_function();
                        }

                        // MainActivity.this.finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNeutralButton(R.string.exit_anyway,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                myApplication.saved_flag = false;
                                MainActivity.this.finish();
                            }
                        }).create();
        dialog.show();
    }

}
