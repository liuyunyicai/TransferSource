package product_info;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

import listadapter.CommandList_Adapter;
import load_reg.LogIn_Activity;
import mainview.demo.DemoApplication;
import mainview.demo.R;
import ssl.Action_Type;
import ssl.PackageHeader;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 获取评论信息界面
 */
public class InfoTwo_Activity extends Activity {

	private ListView Command_List;// 界面中列表
	private List<CommandItem> item_list = new ArrayList<CommandItem>();// 评论数据列表
	private CommandItem commandItem;// 单个数据组
	private CommandList_Adapter adapter;
	private TextView commandall_num = null;
	private int total = 0;
	// 添加评论按钮
	private ImageButton addcomment_bt = null;
	private ImageButton addcomment_bt1 = null;
	private boolean openflag = false;
	private LinearLayout send_layout = null;
	private ImageButton sendcomment_bt = null;
	private EditText comment_edittext = null;

	// 返回数据
	String[][] rece_data = null;

	// 通信模块
	private SharedPreferences share;// 轻量存储
	private DemoApplication myApplication;
	private SSLSocket sslSocket;
	private PackageHeader packageHeader;

	// 操作标志
	/* 消息分类枚举 */
	private enum HandleTag {
		ADDCOMMENT_SUCCESS, ADDCOMMENT_FAILURE, //评论
		GETCOMMENT_SUCCESS, GETCOMMENT_FAILURE, //获取评论
		DIANZAN_SUCCESS, DIANZAN_FAILURE,       // 点赞
		CANCELZAN_SUCCESS, CANCELZAN_FAILURE,   //取消点赞
	}
	private final int DIANZAN = 2;
	private final int CANCELZAN = 3;
	private MyHandler myHandler;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.infotwo_layout);
		myApplication = (DemoApplication) super.getApplication();
		sslSocket = myApplication.getSSlSocket();
		share = getSharedPreferences("transfer", Activity.MODE_PRIVATE);
		myHandler = new MyHandler(this);
		init();
		initpackage();
	}

	// 初始化
	private void init() {

		commandall_num = (TextView) super.findViewById(R.id.commandall_num);
		addcomment_bt = (ImageButton) super.findViewById(R.id.addcomment_bt);
		addcomment_bt.setOnClickListener(new AddCommentOnClick());
		addcomment_bt1 = (ImageButton) super.findViewById(R.id.newcomment_bt);
		addcomment_bt1.setOnClickListener(new NextCommentClick());
		send_layout = (LinearLayout) super
				.findViewById(R.id.sendcomment_layout);
		sendcomment_bt = (ImageButton) super.findViewById(R.id.sendcomment_bt);
		sendcomment_bt.setOnClickListener(new SendCommentOnClick());
		comment_edittext = (EditText) super.findViewById(R.id.comment_text);
		ReGetInfo regetInfo = new ReGetInfo();
		regetInfo.start();
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

	// 刷新界面
	private void refresh_list(PackageHeader returnHeader) {
		init_list(returnHeader);
		Command_List = (ListView) findViewById(R.id.commandListView);
		adapter = new CommandList_Adapter(this, item_list, myApplication);
		Command_List.setAdapter(adapter);
	}

	// 翻下一页评论
	private class NextCommentClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			myApplication.comment_position++;
			myApplication.comment_flag = true;
		}

	}

	// 添加评论按钮监听函数
	private class AddCommentOnClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (myApplication.anonymity_flag) {
				Toast.makeText(InfoTwo_Activity.this, "对不起，您需要先登录",
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(InfoTwo_Activity.this,
						LogIn_Activity.class);
				InfoTwo_Activity.this.startActivity(intent);
			} else {
				if (!openflag) {
					openflag = true;
					send_layout.setVisibility(View.VISIBLE);
				} else {
					openflag = false;
					send_layout.setVisibility(View.GONE);
				}
			}

		}
	}

	// 添加评论按钮监听函数
	private class SendCommentOnClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (comment_edittext.getText().toString().trim().equals("")) {
				Toast.makeText(InfoTwo_Activity.this, "评论不能为空",
						Toast.LENGTH_SHORT).show();
			} else {
				addcomment();
			}

		}
	}

	// 监听是否进行重刷新操作
	private class ReGetInfo extends Thread {
		public void run() {
			while (true) {
				if (myApplication.comment_flag)// 如果需要查询进行显示
				{
					myApplication.comment_flag = false;
					getcomment("" + myApplication.comment_position);
				}

				if (myApplication.dianzan_flag) {
					myApplication.dianzan_flag = false;
					dianzan(DIANZAN);
				}

				if (myApplication.cancelzan_flag) {
					myApplication.cancelzan_flag = false;
					dianzan(CANCELZAN);
				}
			}
		}
	}

	/**
	 * 发送获取评论请求
	 */
	private void getcomment(String n) {
		/* 参数：tag_id,n(查询次数，一次默认查询十个) */
		List<String> lists = new ArrayList<String>();
		lists.add(myApplication.tag_id);
		lists.add(n);
		net_function(lists, Action_Type.CUSTOMER_PRODUCT_GETCOMMENT, 0);
	}

	// 发送添加评论请求
	private void addcomment() {
		// 参数： 参数：tag_id,command_text
		List<String> lists = new ArrayList<String>();
		lists.add(myApplication.tag_id);
		lists.add(comment_edittext.getText().toString());
		net_function(lists, Action_Type.CUSTOMER_PRODUCT_ADDCOMMENT, 1);
	}

	/**
	 * 发送添加点赞请求 // n为2时为点赞，n为3时为取消赞
	 */
	private void dianzan(int n) {
		/* 参数： command_id */
		int type;
		List<String> lists = new ArrayList<String>();
		lists.add(myApplication.commentItem.command_id);
		type = (n == DIANZAN)
				? Action_Type.CUSTOMER_DIANZAN
				: Action_Type.CUSTOMER_CANCEL_DIANZAN;
		net_function(lists, type, n);
	}

	/**
	 * 总网络函数
	 * */
	private void net_function(List<String> lists, int action_type, int type) {
		/* 组装发送参数 */
		sslSocket = myApplication.getSSlSocket();
		/* 组装字符串 */
		String dataString = packageHeader.joinString(lists);
		/* 设置package参数 */
		packageHeader.send_string = dataString;
		packageHeader.data_length = dataString.length();
		packageHeader.now_time = System.currentTimeMillis();
		try
		{
			packageHeader.place_long = Double.valueOf(myApplication.user_placelong);
			packageHeader.place_lat = Double.valueOf(myApplication.user_placelat);
		} catch(Exception e)
		{

		}

		/* 发送信息（登录操作） */
		Rece_Thread rece_Thread = new Rece_Thread(sslSocket, action_type,
				packageHeader, type);
		rece_Thread.start();
	}

	// 构造list
	private void init_list(PackageHeader returnHeader) {

		try {
			transfer_data(returnHeader);
			commandall_num.setText("总评价(" + total + ")");
			for (int i = 0; i < rece_data.length; i++) {
				commandItem = new CommandItem(rece_data[i][0], rece_data[i][1],
						rece_data[i][2], rece_data[i][3], rece_data[i][4],rece_data[i][5]);
				item_list.add(commandItem);
			}
		} catch (Exception e) {
			Log.e("comment", e.toString());
		}
	}

	// 将返回的字符串转化为二维数组
	private void transfer_data(PackageHeader returnHeader) {
		try {
			String[] receString = returnHeader.send_string.split("#");
			total = Integer.valueOf(receString[receString.length - 1]);
			rece_data = new String[receString.length / 6][6];
			for (int i = 0; i < rece_data.length; i++) {
				for (int j = 0; j < 6; j++) {
					rece_data[i][j] = receString[6 * i + j];
				}
			}
		} catch (Exception e) {
			Log.e("comment_data", e.toString());
		}
	}

	private static class MyHandler extends Handler {
		/* 建立弱引用 */
		private WeakReference<InfoTwo_Activity> mOuter;

		/* 构造函数 */
		public MyHandler(InfoTwo_Activity activity) {
			mOuter = new WeakReference<InfoTwo_Activity>(activity);
		}

		public void handleMessage(Message msg) {
			// 防止内存泄露
			InfoTwo_Activity outer = mOuter.get();
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
			case ADDCOMMENT_SUCCESS :
				item_list.clear();
				myApplication.comment_position = 0;
				getcomment("" + myApplication.comment_position);
				openflag = false;
				send_layout.setVisibility(View.GONE);
				break;
			case ADDCOMMENT_FAILURE :
				try {
					Toast.makeText(InfoTwo_Activity.this,
							returnHeader.send_string, Toast.LENGTH_SHORT)
							.show();
				} catch (Exception e) {
					Log.e("addcomment", e.toString());
				}
				break;
			case GETCOMMENT_SUCCESS :
				refresh_list(returnHeader);
				break;
			case GETCOMMENT_FAILURE :
				try {
					Toast.makeText(InfoTwo_Activity.this,
							returnHeader.send_string, Toast.LENGTH_SHORT)
							.show();
				} catch (Exception e) {
					Log.e("getcomment", e.toString());
				}
				break;
			case DIANZAN_SUCCESS :
				break;
			case DIANZAN_FAILURE :
				break;

			case CANCELZAN_SUCCESS :
				break;
			case CANCELZAN_FAILURE :
				break;
		}
	}

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
			if (successflag) {
				switch (type) {
					case 0 :
						msg.what = HandleTag.GETCOMMENT_SUCCESS.ordinal();
						break;
					case 1 :
						msg.what = HandleTag.ADDCOMMENT_SUCCESS.ordinal();
						break;
					case 2 :
						msg.what = HandleTag.DIANZAN_SUCCESS.ordinal();
						break;
					case 3 :
						msg.what = HandleTag.CANCELZAN_SUCCESS.ordinal();
						break;
				}
			} else {
				switch (type) {
					case 0 :
						msg.what = HandleTag.GETCOMMENT_FAILURE.ordinal();
						break;
					case 1 :
						msg.what = HandleTag.ADDCOMMENT_FAILURE.ordinal();
						break;
					case 2 :
						msg.what = HandleTag.DIANZAN_FAILURE.ordinal();
						break;
					case 3 :
						msg.what = HandleTag.CANCELZAN_FAILURE.ordinal();
						break;
				}
			}
			msg.obj = returnHeader;
			myHandler.sendMessage(msg);
		}
	}
}
