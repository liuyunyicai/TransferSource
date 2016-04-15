package load_reg;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

import mainview.demo.DemoApplication;
import mainview.demo.R;
import ssl.Action_Type;
import ssl.PackageHeader;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

@SuppressLint("ResourceAsColor")
public class Register_Activity extends Activity {
	/** 第一界面 */
	private LinearLayout regselfinfo_layout = null;// 个人信息界面
	private LinearLayout regphone_layout = null;// 绑定手机界面
	private Button next_bt = null;// 下翻按钮
	private Button return_bt = null;// 上翻按钮
	/* 信息编辑框 */
	private EditText username_text = null, password_text = null,
			ensure_text = null, email_text = null, realname_text = null;
	/* 信息图像按钮 */
	private Button username_bt = null, password_bt = null, ensure_bt = null,
			email_bt = null, realname_bt = null;
	private Button regleftexit_bt;
	/* 标定编辑框现在状态 */
	private boolean username_flag = false, password_flag = false,
			ensure_flag = false, email_flag = false, realname_flag = false;
	/** 第二界面 */
	private Button agree_bt = null, disagree_bt;// 同意不同意协议按钮
	private boolean agree_flag = false;
	private LinearLayout bindview_layout = null;// 手机号码输入界面
	private ScrollView protocol_scoll = null;

	private Button regend_bt = null,// 绑定帮助按钮
			getrandom_bt = null,// 获取验证码
			successreg_bt = null;// 完成注册按钮
	private EditText phonenum_text = null,// 手机号输入框
			random_text = null;// 输入验证码

	private LinearLayout bindview1, bindview2, bindview3;

	private LinearLayout bindtabview11, bindtabview21, bindtabview31,
			bindtabview12, bindtabview22, bindtabview32;

	// 网络连接模块
	private DemoApplication myApplication = null;
	private SSLSocket sslSocket;
	private PackageHeader packageHeader;

	// 重新获取验证码标志
	private boolean reget_flag = false;
	// 验证验证码标志
	private boolean ensurerandom_flag = false;

	/* 消息分类枚举 */
	private enum HandleTag {
		RECERANDOM_FAILURE, RECERANDOM_SUCCESS, REGET_FAILURE, REGET_SUCCESS, ENSURE_FAILURE, ENSURE_SUCCESS;
	}
	private MyHandler myHandler;
	// 清凉存储
	private SharedPreferences share;// 轻量存储
	private String user_name = "";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register_layout);
		myApplication = (DemoApplication) super.getApplication();
		sslSocket = myApplication.getSSlSocket();
		share = getSharedPreferences("transfer", Activity.MODE_PRIVATE);

		init();

		myHandler = new MyHandler(this);
	}

	/* 参数初始化 */
	private void init() {
		// 第一界面
		regselfinfo_layout = (LinearLayout) super
				.findViewById(R.id.regselfinfo_layout);
		regphone_layout = (LinearLayout) super
				.findViewById(R.id.regphone_layout);
		regphone_layout.setVisibility(View.INVISIBLE);
		regselfinfo_layout.setVisibility(View.VISIBLE);
		bindview_layout = (LinearLayout) super
				.findViewById(R.id.bindview_layout);
		protocol_scoll = (ScrollView) super.findViewById(R.id.protocol_scoll);

		next_bt = (Button) super.findViewById(R.id.next_bt);
		next_bt.setOnClickListener(new NextView_Click());
		return_bt = (Button) super.findViewById(R.id.return_bt);
		return_bt.setOnClickListener(new ReturnView_Click());

		username_text = (EditText) super.findViewById(R.id.username_text);
		password_text = (EditText) super.findViewById(R.id.password_text);
		ensure_text = (EditText) super.findViewById(R.id.ensure_text);
		email_text = (EditText) super.findViewById(R.id.email_text);
		realname_text = (EditText) super.findViewById(R.id.realname_text);

		username_bt = (Button) super.findViewById(R.id.username_bt);
		username_bt.setOnClickListener(new UsernameClick());

		password_bt = (Button) super.findViewById(R.id.password_bt);
		password_bt.setOnClickListener(new Password_Click());

		ensure_bt = (Button) super.findViewById(R.id.ensure_bt);
		ensure_bt.setOnClickListener(new Ensure_Click());

		email_bt = (Button) super.findViewById(R.id.email_bt);
		email_bt.setOnClickListener(new Email_Click());

		realname_bt = (Button) super.findViewById(R.id.realname_bt);
		realname_bt.setOnClickListener(new RelaName_Click());
		// 第二界面
		agree_bt = (Button) super.findViewById(R.id.agree_bt);
		agree_bt.setOnClickListener(new AgreeBt_Click());
		disagree_bt = (Button) super.findViewById(R.id.disagree_bt);
		disagree_bt.setOnClickListener(new DisagreeBt_Click());

		getrandom_bt = (Button) super.findViewById(R.id.getrandom_bt);
		getrandom_bt.setOnClickListener(new GetRandom_OnClick());
		successreg_bt = (Button) super.findViewById(R.id.successreg_bt);
		successreg_bt.setOnClickListener(new SuccessBt_Click());
		regend_bt = (Button) super.findViewById(R.id.regend_bt);
		regend_bt.setOnClickListener(new RegEndClick());

		bindview1 = (LinearLayout) super.findViewById(R.id.bindview1);
		bindview2 = (LinearLayout) super.findViewById(R.id.bindview2);
		bindview3 = (LinearLayout) super.findViewById(R.id.bindview3);

		phonenum_text = (EditText) super.findViewById(R.id.phonenum_text);
		phonenum_text.setText("13349888348");
		random_text = (EditText) super.findViewById(R.id.random_text);

		bindtabview11 = (LinearLayout) super.findViewById(R.id.bindtabview11);
		bindtabview12 = (LinearLayout) super.findViewById(R.id.bindtabview12);
		bindtabview21 = (LinearLayout) super.findViewById(R.id.bindtabview21);
		bindtabview22 = (LinearLayout) super.findViewById(R.id.bindtabview22);
		bindtabview31 = (LinearLayout) super.findViewById(R.id.bindtabview31);
		bindtabview32 = (LinearLayout) super.findViewById(R.id.bindtabview32);

		regleftexit_bt = (Button) super.findViewById(R.id.regleftexit_bt);
		regleftexit_bt.setOnClickListener(new RegLeftExitClick());
	}

	/* 退出注册regleftexit_bt */
	private class RegLeftExitClick implements OnClickListener {
		public void onClick(View v) {
			Register_Activity.this.exitDialog();
		}
	}

	/**
	 * 下翻显示
	 */
	private class NextView_Click implements OnClickListener {
		public void onClick(View view) {
			boolean flag = false;
			if (username_text.getText().toString().trim().equals("")
					|| password_text.getText().toString().trim().equals("")
					|| ensure_text.getText().toString().trim().equals("")) {
				Toast.makeText(Register_Activity.this, "必填信息不能为空",
						Toast.LENGTH_SHORT).show();
			} else if (!password_text.getText().toString()
					.equals(ensure_text.getText().toString())) {
				Toast.makeText(Register_Activity.this, "密码输入前后不一致 ",
						Toast.LENGTH_SHORT).show();
			} else if (!ErrorInput()) {

			} else {
				if (email_text.getText().toString().trim().equals("")) {
					flag = true;
				} else {
					if (email_text.getText().toString()
							.matches("\\w+@\\w+\\.\\w+")) {
						flag = true;
					} else {
						Toast.makeText(Register_Activity.this, "邮箱格式输入不正确 ",
								Toast.LENGTH_SHORT).show();
					}
				}
				if (flag) {
					regselfinfo_layout.setVisibility(View.INVISIBLE);
					regphone_layout.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	/**
	 * 对错误输入的提醒
	 */
	private boolean ErrorInput() {
		if ((username_text.getText().toString().trim().length() > 16)
				&& (username_text.getText().toString().trim().length() < 6)) {
			Toast.makeText(Register_Activity.this, "用户名长度应为6-16位 ",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		if ((password_text.getText().toString().trim().length() > 16)
				&& (password_text.getText().toString().trim().length() < 6)) {
			Toast.makeText(Register_Activity.this, "设置长度应为6-16位 ",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;

	}

	/**
	 * 上翻显示
	 */
	private class ReturnView_Click implements OnClickListener {
		public void onClick(View view) {
			regselfinfo_layout.setVisibility(View.VISIBLE);
			regphone_layout.setVisibility(View.INVISIBLE);
		}
	}

	/* 第二界面显示 */
	/**
	 * 同意协议
	 */
	private class AgreeBt_Click implements OnClickListener {
		public void onClick(View view) {
			if (!agree_flag) {
				agree_flag = true;
				agree_bt.setText("返回查看产品协议");
				bindview_layout.setVisibility(View.VISIBLE);
				protocol_scoll.setVisibility(View.GONE);
			} else {
				agree_flag = false;
				agree_bt.setText("阅读并同意产品协议");
				bindview_layout.setVisibility(View.GONE);
				protocol_scoll.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * 不同意协议
	 */
	private class DisagreeBt_Click implements OnClickListener {
		public void onClick(View view) {
			Register_Activity.this.exitDialog();
		}
	}

	/**
	 * 对返回键进行控制
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Register_Activity.this.exitDialog();
		}
		return false;
	}

	/**
	 * 退出警告对话框
	 */
	private void exitDialog() {
		Dialog dialog = new AlertDialog.Builder(Register_Activity.this)
				.setIcon(R.drawable.warn_icon).setTitle("警告！")
				.setMessage("确定退出注册？")
				.setPositiveButton("退出", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						Register_Activity.this.finish();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).create();
		dialog.show();
	}

	private void ChangeViewTwo() {
		bindview1.setVisibility(View.GONE);
		bindview2.setVisibility(View.VISIBLE);
		bindview3.setVisibility(View.GONE);
		bindtabview11.setVisibility(View.GONE);
		bindtabview12.setVisibility(View.VISIBLE);
		bindtabview21.setVisibility(View.GONE);
		bindtabview22.setVisibility(View.VISIBLE);
		bindtabview32.setVisibility(View.GONE);
		bindtabview31.setVisibility(View.VISIBLE);
	}

	private void ChangeViewThree() {
		bindview1.setVisibility(View.GONE);
		bindview3.setVisibility(View.VISIBLE);
		bindview2.setVisibility(View.GONE);
		bindtabview11.setVisibility(View.GONE);
		bindtabview12.setVisibility(View.VISIBLE);
		bindtabview22.setVisibility(View.GONE);
		bindtabview21.setVisibility(View.VISIBLE);
		bindtabview31.setVisibility(View.GONE);
		bindtabview32.setVisibility(View.VISIBLE);
	}

	/**
	 * 获取验证码帮助
	 */
	private class GetRandom_OnClick implements OnClickListener {
		public void onClick(View view) {
			/* 重新获取验证码 */
			if (reget_flag) {
				if (username_text.getText().toString().trim().equals("")
						|| phonenum_text.getText().toString().trim().equals("")) {
					Toast.makeText(Register_Activity.this, "用户名和手机号输入不应为空",
							Toast.LENGTH_SHORT).show();
				} else {
					List<String> lists = new ArrayList<String>();
					user_name = username_text.getText().toString();
					lists.add(user_name);
					lists.add(phonenum_text.getText().toString());

					net_function(lists, Action_Type.CUSTOMER_GETANOTHER_RANDOM,
							1);
				}
			}
			/* 第一次获取验证码 */
			else {
				boolean flag = false;
				if (username_text.getText().toString().trim().equals("")
						|| password_text.getText().toString().trim().equals("")
						|| ensure_text.getText().toString().trim().equals("")
						|| phonenum_text.getText().toString().trim().equals("")) {
					Toast.makeText(Register_Activity.this, "输入不应为空",
							Toast.LENGTH_SHORT).show();
				} else if (!password_text.getText().toString()
						.equals(ensure_text.getText().toString())) {
					Toast.makeText(Register_Activity.this, "密码输入前后不一致 ",
							Toast.LENGTH_SHORT).show();
				} else {
					if (email_text.getText().toString().trim().equals("")) {
						flag = true;
					} else {
						if (email_text.getText().toString()
								.matches("\\w+@\\w+\\.\\w+")) {
							flag = true;
						} else {
							Toast.makeText(Register_Activity.this,
									"邮箱格式输入不正确 ", Toast.LENGTH_SHORT).show();
						}
					}
					// 满足一切条件后，注册获取验证码
					if (flag) {
						/* 组装字符串 */
						List<String> lists = new ArrayList<String>();
						user_name = username_text.getText().toString();
						lists.add(user_name);
						lists.add(password_text.getText().toString());
						if (realname_text.getText().toString().trim()
								.equals(""))
							lists.add("无");
						else
							lists.add(realname_text.getText().toString());
						lists.add(phonenum_text.getText().toString());
						if (email_text.getText().toString().trim().equals(""))
							lists.add("无");
						else
							lists.add(email_text.getText().toString());

						net_function(lists, Action_Type.CUSTOMER_REGISTER, 0);
					}
				}
			}
		}
	}

	/**
	 * 登录函数
	 * */
	private void net_function(List<String> lists, int post_type, int type) {
		/* 组装发送参数 */
		sslSocket = myApplication.getSSlSocket();
		packageHeader = new PackageHeader();
		/* 组装字符串 */
		String dataString = packageHeader.joinString(lists);
		myApplication.anonymity_flag = true;// 匿名登录参数
		/* 设置package参数 */
		packageHeader.user_level = 5;
		packageHeader.send_string = dataString;
		packageHeader.data_length = dataString.length();
		packageHeader.now_time = System.currentTimeMillis();

		/* 发送信息（登录操作） */
		Rece_Thread rece_Thread = new Rece_Thread(sslSocket, post_type,
				packageHeader, type);
		rece_Thread.start();
	}

	/**
	 * 网络通信线程
	 */
	private class Rece_Thread extends Thread {

		private SSLSocket sslSocket;
		private int post_type;
		private PackageHeader packageHeader;
		private PackageHeader returnHeader = new PackageHeader();
		private int type = 0;

		public Rece_Thread(SSLSocket sslSocket, int post_type,
						   PackageHeader packageHeader, int type) {
			this.sslSocket = sslSocket;
			this.post_type = post_type;
			this.packageHeader = packageHeader;
			this.type = type;
		}

		public void run() {
			Message msg = new Message();
			/* 发送请求信息，并监听返回 */
			returnHeader = packageHeader.send_postdata(sslSocket, post_type);
			boolean successflag = packageHeader
					.Judge_Success(returnHeader.success_flag);
			if (successflag) {
				switch (type) {
					case 0 :
						msg.what = HandleTag.REGET_SUCCESS.ordinal();
						break;
					case 1 :
						msg.what = HandleTag.RECERANDOM_SUCCESS.ordinal();
						break;
					case 2 :
						msg.what = HandleTag.ENSURE_SUCCESS.ordinal();
						break;
				}
			} else {
				switch (type) {
					case 0 :
						msg.what = HandleTag.REGET_FAILURE.ordinal();
						break;
					case 1 :
						msg.what = HandleTag.RECERANDOM_FAILURE.ordinal();
						break;
					case 2 :
						msg.what = HandleTag.ENSURE_FAILURE.ordinal();
						break;
				}
			}
			msg.obj = returnHeader;
			myHandler.sendMessage(msg);
		}
	}

	/**
	 * handler处理
	 */

	private static class MyHandler extends Handler {
		/* 建立弱引用 */
		private WeakReference<Register_Activity> mOuter;

		/* 构造函数 */
		public MyHandler(Register_Activity activity) {
			mOuter = new WeakReference<Register_Activity>(activity);
		}

		public void handleMessage(Message msg) {
			// 防止内存泄露
			Register_Activity outer = mOuter.get();
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
			case RECERANDOM_FAILURE :
				try {
					Toast.makeText(Register_Activity.this,
							returnHeader.send_string, Toast.LENGTH_SHORT)
							.show();
				} catch (Exception e) {

				}
				break;
			case RECERANDOM_SUCCESS :
				Toast.makeText(Register_Activity.this, "信息发送成功，请输入验证码",
						Toast.LENGTH_SHORT).show();
				ChangeViewTwo();
				reget_flag = true;
				break;
			case REGET_FAILURE :
				try {
					Toast.makeText(Register_Activity.this,
							returnHeader.send_string, Toast.LENGTH_SHORT)
							.show();
				} catch (Exception e) {
					Toast.makeText(Register_Activity.this, "重新获取失败",
							Toast.LENGTH_SHORT).show();
				}

				break;
			case REGET_SUCCESS :
				Toast.makeText(Register_Activity.this, "验证码重新获取成功，请输入验证码",
						Toast.LENGTH_SHORT).show();
				ChangeViewTwo();
				break;
			case ENSURE_FAILURE :
				try {
					Toast.makeText(Register_Activity.this,
							returnHeader.send_string, Toast.LENGTH_SHORT)
							.show();
				} catch (Exception e) {
					Toast.makeText(Register_Activity.this, "验证发送码失败",
							Toast.LENGTH_SHORT).show();
				}
				break;

			case ENSURE_SUCCESS :
				Toast.makeText(Register_Activity.this, "注册成功",
						Toast.LENGTH_SHORT).show();
				ensurerandom_flag = true;
				ChangeViewThree();
				break;
		}
	}

	/**
	 * 注册成功按钮
	 */
	private class SuccessBt_Click implements OnClickListener {
		public void onClick(View view) {
			/* 注册成功 */
			if (ensurerandom_flag) {

			} else {
				if (username_text.getText().toString().trim().equals("")
						|| phonenum_text.getText().toString().trim().equals("")
						|| random_text.getText().toString().trim().equals("")) {
					Toast.makeText(Register_Activity.this, "输入不能为空",
							Toast.LENGTH_SHORT).show();
				} else {
					List<String> lists = new ArrayList<String>();
					lists.add(username_text.getText().toString());
					lists.add(phonenum_text.getText().toString());
					lists.add(random_text.getText().toString());

					net_function(lists, Action_Type.CUSTOMER_REGISTER_RANDOM, 2);
				}
			}
		}
	}

	/**
	 * 注册成功转向登录界面
	 */
	private class RegEndClick implements OnClickListener {
		public void onClick(View v) {
			Editor editor = share.edit();
			editor.putString("user_name", user_name);
			editor.commit();
			Intent intent = new Intent(Register_Activity.this,
					LogIn_Activity.class);
			Register_Activity.this.startActivity(intent);
			Register_Activity.this.finish();
		}
	}

	/**
	 * 显示按钮
	 */
	private class UsernameClick implements OnClickListener {
		public void onClick(View view) {
			if (!username_flag) {
				username_bt.setVisibility(View.GONE);
				username_text.setVisibility(View.VISIBLE);
				username_text.setFocusable(true);
				username_flag = true;
			} else {
				username_bt.setVisibility(View.VISIBLE);
				username_text.setVisibility(View.GONE);
				username_flag = false;
			}

		}
	}

	/**
	 * 显示按钮
	 */
	private class Password_Click implements OnClickListener {
		public void onClick(View view) {
			if (!password_flag) {
				password_bt.setVisibility(View.GONE);
				password_text.setVisibility(View.VISIBLE);
				password_flag = true;
			} else {
				password_bt.setVisibility(View.VISIBLE);
				password_text.setVisibility(View.GONE);
				password_flag = false;
			}

		}
	}

	/**
	 * 确认密码按钮
	 */
	private class Ensure_Click implements OnClickListener {
		public void onClick(View view) {
			if (!ensure_flag) {
				ensure_bt.setVisibility(View.GONE);
				ensure_text.setVisibility(View.VISIBLE);
				ensure_flag = true;
			} else {
				ensure_bt.setVisibility(View.VISIBLE);
				ensure_text.setVisibility(View.GONE);
				ensure_flag = false;
			}

		}
	}

	/**
	 * 电子邮箱
	 */
	private class Email_Click implements OnClickListener {
		public void onClick(View view) {
			if (!email_flag) {
				email_bt.setVisibility(View.GONE);
				email_text.setVisibility(View.VISIBLE);
				email_flag = true;
			} else {
				email_bt.setVisibility(View.VISIBLE);
				email_text.setVisibility(View.GONE);
				email_flag = false;
			}

		}
	}

	/**
	 * 真实姓名
	 */
	private class RelaName_Click implements OnClickListener {
		public void onClick(View view) {
			if (!realname_flag) {
				realname_bt.setVisibility(View.GONE);
				realname_text.setVisibility(View.VISIBLE);
				realname_flag = true;
			} else {
				realname_bt.setVisibility(View.VISIBLE);
				realname_text.setVisibility(View.GONE);
				realname_flag = false;
			}
		}
	}

}
