package loading_view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.Toast;

import firstuse.FirstView_Activity;
import load_reg.LoadReg_Activity;
import mainview.demo.R;

@SuppressLint({"CommitPrefEdits", "NewApi", "ShowToast", "HandlerLeak"})
public class LoadActivity extends Activity {
    private boolean flag = false;
    boolean isStop = false;

    private static final int TYPE_STOP = 3;
    NfcAdapter myAdapter;

    private LoadingProgressBar loadingBar;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.hostview_layout);
        myAdapter = NfcAdapter.getDefaultAdapter(this);
        if (myAdapter == null) {
//            Toast.makeText(this, R.string.toast_has_no_nfc, Toast.LENGTH_SHORT).show();
        } else {
            if (!myAdapter.isEnabled()) {
                Toast.makeText(this, R.string.toast_info_open_nfc, Toast.LENGTH_SHORT).show();
            }
        }

        loadingBar = (LoadingProgressBar) findViewById(R.id.loadingBar);
        // 添加监听器
        loadingBar.setLoadingListener(new LoadingProgressBar.LoadingListener() {
            @Override
            public void onAnimationStart() {
            }

            @Override
            public void onAnimationEnd() {
                myHandler.sendEmptyMessage(TYPE_STOP);
            }
        });
    }

    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TYPE_STOP:
                    SharedPreferences share = getSharedPreferences("treansfer", Activity.MODE_PRIVATE);
                    flag = share.getBoolean("fisrt_flag", true);// 记录登录状态
                    // 初次登陆
                    if (flag) {
                        // 转换到初次登陆提醒界面
                        SharedPreferences.Editor edit = share.edit();
                        edit.putBoolean("fisrt_flag", false);
                        edit.commit();
                        Intent intent = new Intent(LoadActivity.this, FirstView_Activity.class);
                        LoadActivity.this.startActivity(intent);

                    } else {
                        // 转换到主界面
                        Intent intent_two = new Intent(LoadActivity.this, LoadReg_Activity.class);
                        LoadActivity.this.startActivity(intent_two);
                    }
                    isStop = true;
                    finish();
                    break;
            }
        }
    };

}
