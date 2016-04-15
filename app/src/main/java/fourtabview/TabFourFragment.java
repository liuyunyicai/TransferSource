package fourtabview;

import java.lang.ref.WeakReference;

import javax.net.ssl.SSLSocket;

import load_reg.LogIn_Activity;
import mainview.demo.DemoApplication;
import mainview.demo.MainActivity;
import mainview.demo.R;
import ssl.Action_Type;
import ssl.PackageHeader;
import tabfour_extra.Password_Activity;
import tabfour_extra.PhoneBook_Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import downloadimage.RoundImageView;

public class TabFourFragment extends Fragment {
	// 控件参数
	private TextView selinfo_username;
	/*
	 * private ImageButton selfphoto; private ImageView selfinfo_vipicon,
	 * selfinfo_level1icon, selfinfo_level2icon, selfinfo_level3icon;// 等级控件
	 */
	private RoundImageView selfphoto;
	private Button myinfo;// 个人资料

	private Button correctinfo;// 完善资料
	private Button changepassword;// 修改密码
	private Button changebind;// 绑定手机
	private Button manage_phonebook;// 通讯录管理
	/*
	 * private Button mangage_infor;// 消息管理 private Button set_privacy;// 隐私设置
	 *  private Button myproduct_save;//
	 * 我的产品收藏 private Button mystore_save;// 我的商店收藏 private Button myhistory;//
	 * 我的消费记录
	 */
	// 网络连接模块
	private DemoApplication myApplication = null;
	private SSLSocket sslSocket;
	private PackageHeader packageHeader;

	private SharedPreferences share;// 轻量存储
	private PopupWindow popWindow = null;
	private View popView;
	private Button thebt;
	private View view;

	// 功能标志
	/* 消息分类枚举 */
	private enum HandleTag {
		EXITSUCCESS, EXITFAILURE, EXIT,
	}
	private final int CORRECTINFO = 0, CHANGEPASSWORD = 1, CHANGEBIND = 2;
	private MyHandler myHandler;


	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.tabfour_view, null);

		myApplication = (DemoApplication) MainActivity.myApplication;
		sslSocket = myApplication.getSSlSocket();
		share = MainActivity.share;
		myHandler = new MyHandler(this);
		initpackage();
		init(view);

		return view;
	}
	/* 界面初始化 */
	private void init(View view) {
		selinfo_username = (TextView) view.findViewById(R.id.selinfo_username);
		/*
		 * selfinfo_vipicon = (ImageView) super
		 * .findViewById(R.id.selfinfo_vipicon); selfinfo_level1icon =
		 * (ImageView) super .findViewById(R.id.selfinfo_level1icon);
		 * selfinfo_level2icon = (ImageView) super
		 * .findViewById(R.id.selfinfo_level2icon); selfinfo_level3icon =
		 * (ImageView) super .findViewById(R.id.selfinfo_level3icon);
		 */
		selfphoto = (RoundImageView) view.findViewById(R.id.selfphoto);

		selinfo_username.setText(share.getString("user_name", "xiaoaojianghu"));

		myinfo = (Button) view.findViewById(R.id.myinfo);
		myinfo.setOnClickListener(new MyInfoOnClick());
		correctinfo = (Button) view.findViewById(R.id.correctinfo);
		changepassword = (Button) view.findViewById(R.id.changepassword);
		changebind = (Button) view.findViewById(R.id.changebind);
		correctinfo.setOnClickListener(new InfoChange(CORRECTINFO));
		changepassword.setOnClickListener(new InfoChange(CHANGEPASSWORD));
		changebind.setOnClickListener(new InfoChange(CHANGEBIND));

		/*
		 * mangage_infor = (Button) super.findViewById(R.id.mangage_infor);
		 * set_privacy = (Button) super.findViewById(R.id.set_privacy);
		 * myproduct_save = (Button) super.findViewById(R.id.myproduct_save);
		 * mystore_save = (Button) super.findViewById(R.id.mystore_save);
		 * myhistory = (Button) super.findViewById(R.id.myhistory);
		 */

		manage_phonebook = (Button) view.findViewById(R.id.manage_phonebook);
		manage_phonebook.setOnClickListener(new ManagePhoneBook());
	}

	/**
	 * packageHeader初始化
	 * */
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

	/****************** 个人信息修改界面 *********************/
	private class InfoChange implements OnClickListener {
		private String type;

		public InfoChange(int type) {
			this.type = "" + type;
		}

		public void onClick(View v) {
			Intent intent = new Intent((MainActivity)getActivity(),
					Password_Activity.class);
			intent.putExtra("state", type);
			TabFourFragment.this.startActivity(intent);
		}
	}

	// manage_phonebook通讯录管理
	private class ManagePhoneBook implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent((MainActivity)getActivity(),
					PhoneBook_Activity.class);
			startActivity(intent);
		}

	}

	// myInfo个人信息按钮点击
	private class MyInfoOnClick implements OnClickListener {
		@SuppressWarnings("deprecation")
		public void onClick(View v) {
			LayoutInflater inflater = LayoutInflater
					.from((MainActivity)getActivity());
			popView = inflater.inflate(R.layout.empty_layout, null);
			thebt = (Button) popView.findViewById(R.id.thebt);
			thebt.setText("退出当前账号");
			thebt.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					TabFourFragment.this.exitDialog();
				}
			});
			// 设置弹出窗口
			popWindow = new PopupWindow(popView,
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			// 使其聚集
			popWindow.setFocusable(true);
			// 设置允许在外点击消失
			popWindow.setBackgroundDrawable(new BitmapDrawable());
			popWindow.setOutsideTouchable(true);
			popWindow.showAsDropDown(v);
		}
	}

	/************ 网络功能控制模块 ****************/
	// 网络通信线程
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
			HandleTag handleTag = HandleTag.values()[type];
			if (successflag) {
				switch (handleTag) {
					case EXIT :
						msg.what = HandleTag.EXITSUCCESS.ordinal();
						break;
					default :
						break;
				}
			} else {
				switch (handleTag) {
					case EXIT :
						msg.what = HandleTag.EXITFAILURE.ordinal();
						break;
					default :
						break;
				}

			}
			msg.obj = returnHeader;
			myHandler.sendMessage(msg);
		}
	}

	private static class MyHandler extends Handler {
		/* 建立弱引用 */
		private WeakReference<TabFourFragment> mOuter;

		/* 构造函数 */
		public MyHandler(TabFourFragment activity) {
			mOuter = new WeakReference<TabFourFragment>(activity);
		}

		public void handleMessage(Message msg) {
			// 防止内存泄露
			TabFourFragment outer = mOuter.get();
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
			case EXITFAILURE :
				try {
					Toast.makeText((MainActivity)getActivity(),
							returnHeader.send_string, Toast.LENGTH_SHORT)
							.show();
				} catch (Exception e) {
					Toast.makeText((MainActivity)getActivity(), R.string.toast_exit_failed,
							Toast.LENGTH_SHORT).show();
				}
				break;
			case EXITSUCCESS :
				Toast.makeText((MainActivity)getActivity(), R.string.toast_exit_success,
						Toast.LENGTH_SHORT).show();
				// 登录数据存储
				myApplication.saved_flag = false;
				Intent intent = new Intent((MainActivity)getActivity(),
						LogIn_Activity.class);
				startActivity(intent);
				MainActivity.instance.finish();
				break;
			default :
				break;
		}
	}

	/**
	 * 登录函数
	 * */
	private void exit_function() {
		/* 组装发送参数 */
		sslSocket = myApplication.getSSlSocket();
		/* 组装字符串 */
		/* 设置package参数 */
		packageHeader.now_time = System.currentTimeMillis();
		try {
			packageHeader.place_long = Double.valueOf(myApplication.user_placelong);
			packageHeader.place_lat = Double.valueOf(myApplication.user_placelat);
		} catch (Exception e) {
		}
		/* 发送信息（登录操作） */
		Rece_Thread rece_Thread = new Rece_Thread(sslSocket,
				Action_Type.CUSTOMER_EXIT, packageHeader,
				HandleTag.EXIT.ordinal());
		rece_Thread.start();
	}

	// 退出警告对话框
	private void exitDialog() {
		Dialog dialog = new AlertDialog.Builder((MainActivity)getActivity())
				.setIcon(R.drawable.warn_icon)
				.setTitle("警告！")
				.setMessage("确定退出当前账号？")
				.setPositiveButton("退出", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						if (myApplication.anonymity_flag) {
							myApplication.saved_flag = false;
							Intent intent = new Intent((MainActivity)getActivity(),
									LogIn_Activity.class);
							startActivity(intent);
							MainActivity.instance.finish();
						} else {
							exit_function();
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.setNeutralButton("强制退出",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								myApplication.saved_flag = false;
								Intent intent = new Intent(
										(MainActivity)getActivity(),
										LogIn_Activity.class);
								startActivity(intent);
								MainActivity.instance.finish();
							}
						}).create();
		dialog.show();
	}

}
