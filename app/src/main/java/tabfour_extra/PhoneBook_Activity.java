package tabfour_extra;

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
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

/************************** 通讯录管理 ***************************/
@SuppressLint({"SimpleDateFormat", "HandlerLeak"})
public class PhoneBook_Activity extends Activity {
	// listview相关参数
	private ListView boolListView;
	private List<Bookitem> booklist = new ArrayList<Bookitem>();

	private List<Bookitem> alre_booklist = new ArrayList<Bookitem>();// 已经注册列表
	private List<Bookitem> nore_booklist = new ArrayList<Bookitem>();// 未注册列表

	private BookAdapter adapter;
	// 查询手机通讯相关参数
	private static final String[] PHONES_PROJECTION = new String[]{
			Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID, Phone.CONTACT_ID};

	private static final int PHONES_DISPLAY_NAME_INDEX = 0; // 联系人显示名称
	private static final int PHONES_NUMBER_INDEX = 1; // 电话号码
	// 网络连接模块
	private DemoApplication myApplication = null;
	private SSLSocket sslSocket;
	private PackageHeader packageHeader;
	private SharedPreferences share;// 轻量存储

	/* 消息分类枚举 */
	private enum HandleTag {
		GETFRIEND_SUCCESS, GETFRIEND_FAILURE,
	}
	private MyHandler myHandler;

	// 弹出窗口
	private View popView;
	private PopupWindow popWindow = null;
	private boolean flag;// 是否显示弹框标志

	// 发送邀请信息
	private Button setnotdisplay;
	private Button inviteok_bt;
	private Button inviteno_bt;
	private Bookitem invite_item;
	private SmsManager sms_manager;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phonebook_layout);
		myApplication = (DemoApplication) super.getApplication();
		sslSocket = myApplication.getSSlSocket();
		share = getSharedPreferences("transfer", Activity.MODE_PRIVATE);
		myHandler = new MyHandler(this);

		initpackage();

		boolListView = (ListView) super.findViewById(R.id.booklist);
		sms_manager = SmsManager.getDefault();
		getfriend();
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

	private static class MyHandler extends Handler {
		/* 建立弱引用 */
		private WeakReference<PhoneBook_Activity> mOuter;

		/* 构造函数 */
		public MyHandler(PhoneBook_Activity activity) {
			mOuter = new WeakReference<PhoneBook_Activity>(activity);
		}

		public void handleMessage(Message msg) {
			// 防止内存泄露
			PhoneBook_Activity outer = mOuter.get();
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
			case GETFRIEND_FAILURE :
				try {
					Toast.makeText(PhoneBook_Activity.this,
							returnHeader.send_string, Toast.LENGTH_SHORT)
							.show();
				} catch (Exception e) {
					Toast.makeText(PhoneBook_Activity.this, "获取通讯录失败",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case GETFRIEND_SUCCESS :
				setdata(returnHeader);
				sortdata();
				setView();
				break;

		}
	}

	/**
	 * 组装发送字符串功能
	 */
	private void getfriend() {
		String datastring = getphone_string();
		/* 组装发送参数 */
		sslSocket = myApplication.getSSlSocket();
		/* 组装字符串 */
		myApplication.anonymity_flag = true;// 匿名登录参数
		/* 设置package参数 */
		packageHeader.send_string = datastring;
		packageHeader.data_length = datastring.length();
		packageHeader.now_time = System.currentTimeMillis();

		/* 发送信息（登录操作） */
		Rece_Thread rece_Thread = new Rece_Thread(sslSocket,
				Action_Type.CUSTOMER_SEARCH_FRIEND, packageHeader);
		rece_Thread.start();
	}

	// 组装发送号码字符串
	private String getphone_string() {
		String phoneString = "";

		try {
			getPhoneContacts(PhoneBook_Activity.this);

			if ((booklist != null) && (booklist.size() != 0)) {
				for (int i = 0; i < booklist.size(); i++) {
					phoneString = phoneString + booklist.get(i).book_number
							+ "#";
				}
			} else {
				Toast.makeText(PhoneBook_Activity.this, "获取通讯录失败",
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.e("getphone_string", e.toString());
		}
		return phoneString;
	}

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
				msg.what = HandleTag.GETFRIEND_SUCCESS.ordinal();

			} else {
				msg.what = HandleTag.GETFRIEND_FAILURE.ordinal();
			}
			msg.obj = returnHeader;
			myHandler.sendMessage(msg);
		}
	}

	/** 得到手机通讯录联系人信息 **/
	private void getPhoneContacts(Context mContext) {
		try {
			ContentResolver resolver = PhoneBook_Activity.this
					.getContentResolver();
			// 获取手机联系人
			Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
					PHONES_PROJECTION, null, null, null);
			if (phoneCursor != null) {
				while (phoneCursor.moveToNext()) {
					String phoneNumber = phoneCursor
							.getString(PHONES_NUMBER_INDEX); // 得到手机号码
					phoneNumber = phoneNumber.replaceAll(" ", "");
					if (TextUtils.isEmpty(phoneNumber)) // 当手机号码为空的或者为空字段 跳过当前循环
						continue;
					String contactName = phoneCursor
							.getString(PHONES_DISPLAY_NAME_INDEX); // 得到联系人名称
					Bookitem item = new Bookitem(contactName,
							phoneNumber.trim(), false);
					booklist.add(item);
				}
				phoneCursor.close();
			}
		} catch (Exception e) {
			Log.e("getPhoneContacts", e.toString());
		}

	}

	// 将获取到的服务器返回信息转化
	private void setdata(PackageHeader returnHeader) {
		String[] receString=returnHeader.send_string.split("#");
		if ((receString != null) && (receString.length >= 2)) {
			for (int i = 1; i < receString.length; i++) {
				if (receString[i].equals("0")) {
					booklist.get(i - 1).flag = false;
				} else if (receString[i].equals("1")) {
					booklist.get(i - 1).flag = true;
				}
			}
		} else {
			Toast.makeText(PhoneBook_Activity.this, "通讯录为空", Toast.LENGTH_SHORT)
					.show();
		}
	}

	// 将获得的信息分类
	private void sortdata() {
		for (int i = 0; i < booklist.size(); i++) {
			if (booklist.get(i).flag) {
				alre_booklist.add(booklist.get(i));
			} else {
				nore_booklist.add(booklist.get(i));
			}
		}

		booklist.clear();
		for (int i = 0; i < alre_booklist.size(); i++) {
			booklist.add(alre_booklist.get(i));
		}

		for (int i = 0; i < nore_booklist.size(); i++) {
			booklist.add(nore_booklist.get(i));
		}
	}

	// 刷新界面
	private void setView() {
		adapter = new BookAdapter(PhoneBook_Activity.this);
		boolListView.setDivider(null);
		boolListView.setAdapter(adapter);
	}

	// 每个联系人的信息
	private class Bookitem {
		public String book_name;
		public String book_number;
		public boolean flag;

		public Bookitem(String book_name, String book_number, boolean flag) {
			this.book_name = book_name;
			this.book_number = book_number;
			this.flag = flag;
		}
	}

	// listView适配器
	private class BookAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public BookAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		public int getCount() {

			return booklist.size();
		}

		public Bookitem getItem(int pos) {
			return booklist.get(pos);
		}

		public long getItemId(int pos) {

			return pos;
		}

		public View getView(int pos, View convertView, ViewGroup parent) {
			try {
				final Bookitem item = getItem(pos);
				convertView = inflater.inflate(R.layout.booklist_layout, null);
				final TextView bookname = (TextView) convertView
						.findViewById(R.id.bookname);
				final TextView booknum = (TextView) convertView
						.findViewById(R.id.booknum);
				final TextView action_bt = (TextView) convertView
						.findViewById(R.id.action_bt);

				bookname.setText(item.book_name);
				booknum.setText(item.book_number);

				OnClickListener listener = new OnClickListener() {
					@SuppressWarnings("deprecation")
					public void onClick(View v) {

						invite_item = item;
						flag = share.getBoolean("invite_popflag", false);
						if (!flag) {
							LayoutInflater inflater = LayoutInflater
									.from(PhoneBook_Activity.this);
							popView = inflater.inflate(
									R.layout.invitefriend_layout, null);

							setnotdisplay = (Button) popView
									.findViewById(R.id.setnotdisplay);
							inviteok_bt = (Button) popView
									.findViewById(R.id.inviteok_bt);
							inviteno_bt = (Button) popView
									.findViewById(R.id.inviteno_bt);

							setnotdisplay
									.setOnClickListener(new SetDisplayClick());
							inviteno_bt.setOnClickListener(new InviteClick());
							inviteok_bt.setOnClickListener(new InviteOnClick());

							// 设置弹出窗口
							popWindow = new PopupWindow(popView,
									ViewGroup.LayoutParams.WRAP_CONTENT,
									ViewGroup.LayoutParams.WRAP_CONTENT);
							// 使其聚集
							popWindow.setFocusable(true);
							// 设置允许在外点击消失
							popWindow
									.setBackgroundDrawable(new BitmapDrawable());
							popWindow.setOutsideTouchable(true);
							popWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
						} else {
							SendInvite();
						}
					}

				};

				if (item.flag) {
					action_bt.setText("已注册");
					action_bt.setBackgroundColor(Color.GRAY);
					action_bt.setEnabled(false);
				} else {
					action_bt.setText("邀请他");
					action_bt.setOnClickListener(listener);
				}
			} catch (Exception e) {
				Log.e("getView", e.toString());
			}

			return convertView;
		}

	}

	// setnotdisplay设置是否显示提示弹框
	private class SetDisplayClick implements OnClickListener {
		public void onClick(View v) {
			flag = share.getBoolean("invite_popflag", false);
			if (!flag) {
				SharedPreferences.Editor edit = share.edit();
				edit.putBoolean("invite_popflag", true);
				edit.commit();
				setnotdisplay.setBackgroundResource(R.drawable.invitetheok_yes);
			} else {
				SharedPreferences.Editor edit = share.edit();
				edit.putBoolean("invite_popflag", false);
				edit.commit();
				setnotdisplay.setBackgroundResource(R.drawable.invitetheok_no);
			}
		}
	}

	// inviteno_bt取消邀请
	private class InviteClick implements OnClickListener {
		public void onClick(View v) {
			popWindow.dismiss();
		}
	}

	// inviteok_bt发送邀请短信按钮
	private class InviteOnClick implements OnClickListener {
		public void onClick(View v) {
			SendInvite();
		}
	}

	// 发送邀请短信
	private void SendInvite() {
		try {
			// String thisstring="推荐你使用一款好用的购物软件，名字叫作溯源助手................";
			String thisstring = "................";
			sms_manager.sendTextMessage(invite_item.book_number, null,
					thisstring, null, null);
			Toast.makeText(PhoneBook_Activity.this, R.string.toast_send_invite_msg, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Log.e("SendInvite", e.toString());
		}
	}
}
