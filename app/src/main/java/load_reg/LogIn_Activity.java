package load_reg;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

import mainview.demo.DemoApplication;
import mainview.demo.MainActivity;
import mainview.demo.R;
import ssl.Action_Type;
import ssl.PackageHeader;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LogIn_Activity extends Activity {
	private EditText laodname_text = null, laodpassword_text = null;
	private Button login_bt = null;

	/* 登录是否成功标志 */
	private enum HandleTag {
		LOGSUCCESS, LOGFAILURE;
	}

	private MyHandler myHandler;
	/* 数据存储模块 */
	private SharedPreferences share;// 轻量存储
	private String user_name = "";
	private String user_password = "";
	/* 网络模块 */
	private DemoApplication myApplication = null;
	private SSLSocket sslSocket;
	private PackageHeader packageHeader;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login_layout_two);

		init();

	}

	/**
	 * 界面初始化
	 */
	private void init() {
		myApplication = (DemoApplication) super.getApplication();
		sslSocket = myApplication.getSSlSocket();
		/* 获取存储的数据 */
		share = getSharedPreferences("transfer", Activity.MODE_PRIVATE);
		user_name = share.getString("user_name", "xiaoaojianghu");
		user_password = share.getString("user_password", "");

		laodname_text = (EditText) super.findViewById(R.id.laodname_text);
		laodpassword_text = (EditText) super
				.findViewById(R.id.laodpassword_text);
		laodname_text.setText(user_name);
		laodpassword_text.setText(user_password);

		login_bt = (Button) super.findViewById(R.id.login_bt);
		login_bt.setOnClickListener(new Login_Click());
		myHandler = new MyHandler(this);
	}

	/**
	 * 登录功能
	 */
	private class Login_Click implements OnClickListener {
		public void onClick(View view) {
			if (laodname_text.getText().toString().trim().equals("")
					|| laodpassword_text.getText().toString().trim().equals("")) {
				Toast.makeText(LogIn_Activity.this, "用户名和密码不能为空",
						Toast.LENGTH_SHORT).show();
			} else {
				user_name = laodname_text.getText().toString();
				user_password = laodpassword_text.getText().toString();
				/* 登录函数 */
				List<String> lists = new ArrayList<String>();
				lists.add(user_name);
				lists.add(user_password);
				load_function(lists);
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
	 * 登录函数
	 * */
	private void load_function(List<String> lists) {
		/* 组装发送参数 */
		sslSocket = myApplication.getSSlSocket();
		packageHeader = new PackageHeader();
		/* 组装字符串 */
		String dataString = packageHeader.joinString(lists);
		myApplication.anonymity_flag = false;// 匿名登录参数
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
	 * handler处理
	 */
	private static class MyHandler extends Handler {
		/* 建立弱引用 */
		private WeakReference<LogIn_Activity> myActivity;

		/* 构造函数 */
		public MyHandler(LogIn_Activity activity) {
			myActivity = new WeakReference<LogIn_Activity>(activity);
		}

		public void handleMessage(Message msg) {
			// 防止内存泄露
			LogIn_Activity thisActivity = myActivity.get();
			if (thisActivity != null) {
				thisActivity.handleservice(msg);
			}
		}

	}

	private void handleservice(Message msg) {
		HandleTag handleTag = HandleTag.values()[msg.what];
		PackageHeader returnHeader =  (PackageHeader) msg.obj;//返回数据信息
		switch (handleTag) {
			case LOGFAILURE:
				try {
					Toast.makeText(LogIn_Activity.this, returnHeader.send_string,
							Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					Toast.makeText(LogIn_Activity.this, "登录失败", Toast.LENGTH_SHORT)
							.show();
				}

				break;
			case LOGSUCCESS:
			/* 登录数据存储 */
				SharedPreferences.Editor editor = share.edit();
				myApplication.anonymity_flag = false;
				editor.putString("user_name", user_name);
				editor.putString("user_password", user_password);
				editor.putInt("user_id", returnHeader.byte_2_id(returnHeader.user_name));
				editor.putInt("user_level",returnHeader.user_level);
				editor.putBoolean("login", true);
				editor.commit();
				Intent intent = new Intent(LogIn_Activity.this, MainActivity.class);
				LogIn_Activity.this.startActivity(intent);
				LogIn_Activity.this.finish();
				break;
		}
	}

}
