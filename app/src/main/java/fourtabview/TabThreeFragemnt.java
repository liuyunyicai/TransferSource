package fourtabview;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

import mainview.demo.DemoApplication;
import mainview.demo.MainActivity;
import mainview.demo.R;
import ssl.Action_Type;
import ssl.PackageHeader;
import tabthree_extra.StoreMap_Activity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class TabThreeFragemnt extends Fragment {
	// 商店总listView
	private ListView definition_listview;
	private List<storeproduct_item> definition_list = new ArrayList<storeproduct_item>();
	private Storeproduct_Adapter definition_adapter;

	// 网络通信模块
	private DemoApplication myApplication;
	private SSLSocket sslSocket;
	private PackageHeader packageHeader;
	private SharedPreferences share;// 轻量存储
	/* 消息分类枚举 */
	private enum HandleTag {
		GETSTORE_SUCCESS, GETSTORE_FAILURE,
	}
	private MyHandler myHandler;

	// 存放返回数据
	private String[] store_array = null;
	private String[][] prodcut_array = null;
	// 经纬度参数
	private final double lat_comp = 111.7126915064;
	private final double long_comp = 102.8347425802;
	// 界面参数
	private TextView storename_text = null;
	private ImageButton sharestore_bt, savestore_bt;
	private ImageButton storepicture;
	private TextView storedetail;
	private RatingBar scorestore_bar;
	private TextView storescore, storepeople;

	private Button storephone, callstore;
	private Button storeplace, findstore;
	private View view;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.tabthree_view, null);

		myApplication = (DemoApplication) MainActivity.myApplication;
		sslSocket = myApplication.getSSlSocket();
		share = MainActivity.share;
		myHandler = new MyHandler(this);
		initpackage();
		init(view);

		return view;
	}

	/**
	 * 界面初始化
	 * */
	private void init(View view)
	{
		storename_text = (TextView) view.findViewById(R.id.storename_text);
		sharestore_bt = (ImageButton) view.findViewById(R.id.sharestore_bt);
		savestore_bt = (ImageButton) view.findViewById(R.id.savestore_bt);
		storedetail = (TextView) view.findViewById(R.id.storedetail);
		scorestore_bar = (RatingBar) view.findViewById(R.id.scorestore_bar);
		storescore = (TextView) view.findViewById(R.id.storescore);
		storepeople = (TextView) view.findViewById(R.id.storepeople);

		storephone = (Button) view.findViewById(R.id.storephone);
		callstore = (Button) view.findViewById(R.id.callstore);
		storephone.setOnClickListener(new CallStore());
		callstore.setOnClickListener(new CallStore());
		storeplace = (Button) view.findViewById(R.id.storeplace);
		findstore = (Button) view.findViewById(R.id.findstore);
		storeplace.setOnClickListener(new FindStore());
		findstore.setOnClickListener(new FindStore());

		definition_list.add(new storeproduct_item());
		definition_list.add(new storeproduct_item());
		definition_list.add(new storeproduct_item());
		definition_list.add(new storeproduct_item());
		definition_listview = (ListView) view
				.findViewById(R.id.commandListView);
		definition_adapter = new Storeproduct_Adapter(this.getActivity());
		definition_listview.setAdapter(definition_adapter);
		definition_listview.setDivider(null);

		// 进行初始界面刷新
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

	// 给商家打电话
	private class CallStore implements OnClickListener {
		public void onClick(View v) {
			if (!myApplication.scaned_falg) {
				Toast.makeText((MainActivity)getActivity(), "请先扫描标签！",
						Toast.LENGTH_SHORT).show();
			} else {
				if (!storephone.getText().equals("")) {
					String calltel = "tel:" + storephone.getText();
					Intent intent = new Intent(Intent.ACTION_DIAL,
							Uri.parse(calltel));
					startActivity(intent);
				}
			}
		}

	}

	// 在地图上查看商家位置
	private class FindStore implements OnClickListener {
		public void onClick(View v) {
			if (!myApplication.scaned_falg) {
				Toast.makeText((MainActivity)getActivity(), "请先扫描标签！",
						Toast.LENGTH_SHORT).show();
			} else {
				if (!storeplace.getText().equals("")) {
					try {
						myApplication.storelat = Double.valueOf(store_array[8]);
						myApplication.storelong = Double
								.valueOf(store_array[7]);
						Intent intent = new Intent((MainActivity)getActivity(),
								StoreMap_Activity.class);
						startActivity(intent);
					} catch (Exception e) {
						Log.e("FindStore", e.toString());
					}
				}
			}
		}
	}

	// 监听是否重刷新操作
	private class ReGetInfo extends Thread {
		public void run() {
			while (true) {
				if (myApplication.store_flag)// 如果需要查询进行显示
				{
					myApplication.store_flag = false;
					getstore();
				}
			}
		}
	}

	// 刷新界面
	private void refresh_view(PackageHeader returnHeader) {
		rechange_data(returnHeader);
		get_list();
		set_store();
		definition_adapter = new Storeproduct_Adapter((MainActivity)getActivity());
		definition_listview.setAdapter(definition_adapter);
		definition_listview.setDivider(null);
	}

	// 刷新商店界面
	private void set_store() {
		try {
			storename_text.setText(store_array[1]);
			storedetail.setText(store_array[2]);
			storescore.setText(store_array[3]);
			storepeople.setText("/" + store_array[4] + "人");
			storephone.setText(store_array[5]);
			storeplace.setText(store_array[6]);
			scorestore_bar.setRating(Float.valueOf(store_array[3]));
		} catch (Exception e) {
			Log.e("set_store", e.toString());
		}

	}

	// 将receString转化为array数据
	private void rechange_data(PackageHeader returnHeader) {
		try {
			String[] receString = returnHeader.send_string.split("#");
			store_array = new String[9];
			for (int i = 0; i < store_array.length; i++) {
				store_array[i] = receString[i + 1];
			}
			prodcut_array = new String[(receString.length - 10) / 9][9];
			for (int i = 0; i < prodcut_array.length; i++) {
				for (int j = 0; j < prodcut_array[i].length; j++) {
					prodcut_array[i][j] = receString[9 * i + j + 10];
				}
			}
		} catch (Exception e) {
			Log.e("rechange_data", e.toString());
		}
	}

	// 组装List
	private void get_list() {
		storeproduct_item storeItem = null;
		definition_list.clear();
		try {
			for (int i = 0; i < prodcut_array.length; i++) {
				product_item pitem = new product_item(prodcut_array[i]);
				storeItem = new storeproduct_item(prodcut_array[i][0], pitem,
						prodcut_array[i][8]);
				definition_list.add(storeItem);
			}
		} catch (Exception e) {
			Log.e("get_list", e.toString());
		}

	}

	// 组装发送字符串
	private void getstore() {
		// tag_id
		List<String> lists = new ArrayList<String>();
		lists.add(myApplication.tag_id);
		
		/* 组装发送参数 */
		sslSocket = myApplication.getSSlSocket();
		/* 组装字符串 */
		String dataString = packageHeader.joinString(lists);
		/* 设置package参数 */
		packageHeader.send_string = dataString;
		packageHeader.data_length = dataString.length();
		packageHeader.now_time = System.currentTimeMillis();
		try {
			packageHeader.place_long = Double.valueOf(myApplication.user_placelong);
			packageHeader.place_lat = Double.valueOf(myApplication.user_placelat);
		} catch (Exception e) {
		}

		/* 发送信息（登录操作） */
		Rece_Thread rece_Thread = new Rece_Thread(sslSocket,
				Action_Type.CUSTOMER_QUERY_UNQIUE_STORELIST, packageHeader);
		rece_Thread.start();
	}

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
				msg.what = HandleTag.GETSTORE_SUCCESS.ordinal();
			} else {
				msg.what = HandleTag.GETSTORE_FAILURE.ordinal();
			}
			myHandler.sendMessage(msg);
		}
	}

	/**
	 * 信息处理Handle
	 */
	public static class MyHandler extends Handler {
		/* 建立弱引用 */
		private WeakReference<TabThreeFragemnt> mOuter;

		/* 构造函数 */
		public MyHandler(TabThreeFragemnt activity) {
			mOuter = new WeakReference<TabThreeFragemnt>(activity);
		}

		public void handleMessage(Message msg) {
			// 防止内存泄露
			TabThreeFragemnt outer = mOuter.get();
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
		PackageHeader returnHeader =  (PackageHeader) msg.obj;//返回数据信息
		switch (handleTag) {
			case GETSTORE_SUCCESS:
				refresh_view(returnHeader);
				break;
			case GETSTORE_FAILURE:
				try {
					Toast.makeText((MainActivity)getActivity(), returnHeader.send_string,
							Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					Log.e("myHandler", e.toString());
				}
				break;

		}
	}
	// List1
	private class storeproduct_item {
		public String definitiontitle;
		public product_item pitem;
		public boolean flag = false;
		public String sort_id_one;

		// 无参数时的构造函数
		public storeproduct_item() {
			flag = false;
		}

		public storeproduct_item(String definitiontitle, product_item pitem,
								 String sort_id_one) {
			flag = true;
			this.definitiontitle = definitiontitle;
			this.pitem = pitem;
			this.sort_id_one = sort_id_one;
		}

	}

	// 一类产品的信息列表
	private class Storeproduct_Adapter extends BaseAdapter {
		private Context context;
		private LayoutInflater inflater;

		public Storeproduct_Adapter(Context context) {
			this.context = context;
			inflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return definition_list.size();
		}

		public storeproduct_item getItem(int pos) {
			return definition_list.get(pos);
		}

		public long getItemId(int pos) {
			return pos;
		}

		public View getView(int pos, View convertView, ViewGroup parent) {
			final storeproduct_item item = getItem(pos);
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.storeprolist_layout,
						null);
				final ListView product = (ListView) convertView
						.findViewById(R.id.storeitemListView);
				final List<product_item> product_list = new ArrayList<product_item>();
				final Button definitiontitle = (Button) convertView
						.findViewById(R.id.definitiontitle);
				final Button detailmore = (Button) convertView
						.findViewById(R.id.detailmore);
				OnClickListener listener = new OnClickListener() {

					@Override
					public void onClick(View v) {

					}
				};
				if (item.flag) {
					product_list.add(item.pitem);
					definitiontitle.setText(item.definitiontitle);
					definitiontitle.setOnClickListener(listener);
					detailmore.setOnClickListener(listener);
				} else {
					product_item pItem = new product_item();
					product_list.add(pItem);
				}
				final Product_Adapter product_adapter = new Product_Adapter(
						context, product_list);
				product.setAdapter(product_adapter);
				product.setDivider(null);
			}
			return convertView;
		}

	}

	// list2
	private class product_item {
		public String definition_name, sorttwo_name, price;
		public String product_detail;
		public String factory_name;;
		public String similar_place, similar_distance, placelat, placelong;
		public String similar_score, similar_trust, similar_people;
		public boolean flag = false;

		public product_item() {
			flag = false;
		}

		public product_item(String[] array) {
			flag = true;
			definition_name = array[1];
			sorttwo_name = array[2];
			product_detail = array[3];
			price = array[4];
			factory_name = store_array[1];
			similar_place = store_array[6];
			placelat = store_array[8];
			placelong = store_array[7];
			similar_distance = get_distance(placelat, placelong);

			similar_score = array[5];
			similar_people = array[6];
			similar_trust = array[7];

		}
	}

	// 计算GPS距离
	private String get_distance(String placelat, String placelong) {
		String diatance_string = "0.0";
		double distance = 0;
		try {
			double nowlat, nowlong;
			double thatlat, thatlong;

			nowlat = Double.valueOf(myApplication.user_placelat);
			nowlong = Double.valueOf(myApplication.user_placelong);
			thatlat = Double.valueOf(placelat);
			thatlong = Double.valueOf(placelong);
			distance = (thatlat - nowlat) * (thatlat - nowlat) * lat_comp
					* lat_comp + (thatlong - nowlong) * long_comp
					* (thatlong - nowlong) * long_comp;
			distance = Math.sqrt(distance);
			diatance_string = String.valueOf(distance);

			if (diatance_string.length() > 6) {
				diatance_string = diatance_string.substring(0, 5);
			}

		} catch (Exception e) {
			Log.e("get_distance", e.toString());
		}
		return diatance_string;
	}

	// 单个产品的信息列表
	private class Product_Adapter extends BaseAdapter {

		private LayoutInflater inflater;
		// 单个产品list
		private List<product_item> product_list = new ArrayList<product_item>();

		public Product_Adapter(Context context, List<product_item> product_list) {
			inflater = LayoutInflater.from(context);
			this.product_list = product_list;
		}

		public int getCount() {
			return product_list.size();
		}

		public product_item getItem(int pos) {
			return product_list.get(pos);
		}

		public long getItemId(int pos) {
			return pos;
		}

		public View getView(int pos, View convertView, ViewGroup parent) {
			final product_item item = getItem(pos);
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.similarlist_layout,
						null);
				final ImageView similarproduct_photo = (ImageView) convertView
						.findViewById(R.id.similarproduct_photo);
				final TextView similiar_productname = (TextView) convertView
						.findViewById(R.id.similiar_productname);
				final TextView similar_sortone = (TextView) convertView
						.findViewById(R.id.similar_sortone);
				final TextView similar_price = (TextView) convertView
						.findViewById(R.id.similar_price);
				final TextView similar_detail = (TextView) convertView
						.findViewById(R.id.similar_detail);
				final TextView similar_place = (TextView) convertView
						.findViewById(R.id.similar_place);
				final TextView similar_distance = (TextView) convertView
						.findViewById(R.id.similar_distance);
				final TextView similar_score = (TextView) convertView
						.findViewById(R.id.similar_score);
				final TextView similar_people = (TextView) convertView
						.findViewById(R.id.similar_people);
				final TextView similar_trust = (TextView) convertView
						.findViewById(R.id.similar_trust);
				final TextView trust_level = (TextView) convertView
						.findViewById(R.id.trust_level);

				if (item.flag) {
					similiar_productname.setText(item.definition_name);
					similar_sortone.setText(item.sorttwo_name);
					similar_price.setText(item.price);
					similar_detail.setText(item.product_detail);
					similar_place.setText(item.similar_place);
					similar_distance.setText(item.similar_distance);
					similar_score.setText(item.similar_score);
					similar_people.setText("/" + item.similar_people);
					similar_trust.setText(item.similar_trust);
					trust_level.setText(gettrust_level(item.similar_trust));
				}

			}
			return convertView;
		}

		// 判断信用等级
		private String gettrust_level(String level) {
			String level_string = "";

			try {
				double thelevel = Double.valueOf(level);
				if (thelevel < 4.0) {
					level_string = "(不可信)";
				} else {
					level_string = "(可信)";
				}

			} catch (Exception e) {
				Log.e("gettrust_level", e.toString());
			}
			return level_string;

		}

	}
}
