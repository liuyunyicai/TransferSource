package product_info;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

import mainview.demo.DemoApplication;
import mainview.demo.R;
import ssl.Action_Type;
import ssl.PackageHeader;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import fourtabview.ProductInfo_Activity;

/**
 * 获取相近产品界面
 */
public class InfoThree_Activity extends Activity {
	// 获取ID
	private Button range_style, range_search;
	private PopupWindow popWindow;
	private View popView;
	// 排序方式弹出窗口
	private ListView range_list = null;
	private RangeList_Adapter adapter;
	//
	private TextView range_detail;
	private ImageButton exitrangebt;
	private String[] style_array = { "距离最近", "价钱最低", "评分最高", "信用最好", "相似度最高 " };
	private String[] detail_array = { "筛选距离您最近的商品", "按商品价格的大小进行排序",
			"按顾客总评分大小进行排序", "根据商品信誉度进行排序", "按照商品关联相似度排序" };
	private int[] picture_array = { R.drawable.range_place,
			R.drawable.range_money, R.drawable.range_score,
			R.drawable.range_trust, R.drawable.range_word };

	private String[] simple_array = { "距离", "价钱", "评分", "信用", "相似度" };
	private int[] pictureset_array = { R.drawable.range_placeset,
			R.drawable.range_moneyset, R.drawable.range_scoreset,
			R.drawable.range_trustset, R.drawable.range_wordset };
	// 用户存放五个筛选顺序
	private List<Integer> num_list = new ArrayList<Integer>();
	private boolean[] flag_all = { false, false, false, false, false };
	// 主界面按钮
	private LinearLayout[] itemlayoutLayouts = new LinearLayout[5];
	private ImageView[] range_icon = new ImageView[5];
	private Button[] range_text = new Button[5];
	private ImageButton[] range_select = new ImageButton[5];

	// 与服务器通信参数
	/* 消息分类枚举 */
	private enum HandleTag {
		GETSIMILIAR_SUCCESS, GETSIMILIAR_FAILURE,
	}

	private MyHandler myHandler;

	private DemoApplication myApplication;
	private SSLSocket sslSocket;
	private PackageHeader packageHeader;
	private SharedPreferences share;// 轻量存储

	private String rangeString = "";
	// 用于存放结果的数组
	private String[][] dataarray = null;

	// 默认经纬度参数
	private final double lat_comp = 111.7126915064;
	private final double long_comp = 102.8347425802;

	/*************************** 相近产品查询参数 ***************************/
	private ListView similar_list;
	private List<SimilarItem> simliar_Items = new ArrayList<SimilarItem>();
	private Similar_Adapter similar_Adapter;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.infothree_layout);

		myApplication = (DemoApplication) super.getApplication();
		sslSocket = myApplication.getSSlSocket();
		share = getSharedPreferences("transfer", Activity.MODE_PRIVATE);
		myHandler = new MyHandler(this);

		init();
		initpackage();
	}

	/**
	 * 界面初始化
	 * */
	private void init() {
		range_style = (Button) super.findViewById(R.id.range_style);
		range_style.setOnClickListener(new RangeSyle_OnClick());
		range_search = (Button) super.findViewById(R.id.range_search);
		range_search.setOnClickListener(new RangeSearch_OnClick());
		itemlayoutLayouts[0] = (LinearLayout) super
				.findViewById(R.id.itemlayout1);
		itemlayoutLayouts[1] = (LinearLayout) super
				.findViewById(R.id.itemlayout2);
		itemlayoutLayouts[2] = (LinearLayout) super
				.findViewById(R.id.itemlayout3);
		itemlayoutLayouts[3] = (LinearLayout) super
				.findViewById(R.id.itemlayout4);
		itemlayoutLayouts[4] = (LinearLayout) super
				.findViewById(R.id.itemlayout5);

		range_icon[0] = (ImageView) super.findViewById(R.id.range_icon1);
		range_icon[1] = (ImageView) super.findViewById(R.id.range_icon2);
		range_icon[2] = (ImageView) super.findViewById(R.id.range_icon3);
		range_icon[3] = (ImageView) super.findViewById(R.id.range_icon4);
		range_icon[4] = (ImageView) super.findViewById(R.id.range_icon5);
		range_icon[0].setBackgroundResource(R.drawable.searchbt);

		range_text[0] = (Button) super.findViewById(R.id.range_text1);
		range_text[0].setText("默认");
		range_text[1] = (Button) super.findViewById(R.id.range_text2);
		range_text[2] = (Button) super.findViewById(R.id.range_text3);
		range_text[3] = (Button) super.findViewById(R.id.range_text4);
		range_text[4] = (Button) super.findViewById(R.id.range_text5);

		range_select[0] = (ImageButton) super.findViewById(R.id.range_select1);
		range_select[1] = (ImageButton) super.findViewById(R.id.range_select2);
		range_select[2] = (ImageButton) super.findViewById(R.id.range_select3);
		range_select[3] = (ImageButton) super.findViewById(R.id.range_select4);
		range_select[4] = (ImageButton) super.findViewById(R.id.range_select5);

		similar_list = (ListView) super.findViewById(R.id.similar_listview);
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

	// range_search删选图标响应
	private class RangeSearch_OnClick implements OnClickListener {
		public void onClick(View v) {

			if (myApplication.scaned_falg) {
				getsimiliar();
			} else {
				Toast.makeText(InfoThree_Activity.this, "请先扫描标签",
						Toast.LENGTH_SHORT).show();
			}

		}

	}

	// ListView点击事件
	private class ItemClick implements OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
								long arg3) {
			myApplication.tag_id = simliar_Items.get(pos).tag_id;
			myApplication.sort_id_one = simliar_Items.get(pos).sortone;
			myApplication.sort_id_two = simliar_Items.get(pos).sorttwo;
			myApplication.definition_id = simliar_Items.get(pos).definition_id;
			myApplication.product_id = simliar_Items.get(pos).product_id;

			myApplication.search_flag = true;
			myApplication.state_flag = true;
			myApplication.comment_flag = true;
			myApplication.comment_position = 0;
			myApplication.scaned_falg = true;
			myApplication.saved_flag = false;
			myApplication.scored_falg = false;
			;
			myApplication.current_num = 1;
			ProductInfo_Activity.myTabHost.setCurrentTab(0);

		}

	}

	// 刷新界面
	private void refresh_list(PackageHeader returnHeader) {
		get_data(returnHeader);
		for (int i = 0; i < dataarray.length; i++) {
			SimilarItem similarItem = new SimilarItem(dataarray[i]);
			simliar_Items.add(similarItem);
		}
		similar_Adapter = new Similar_Adapter(InfoThree_Activity.this);
		similar_list.setDivider(null);
		similar_list.setAdapter(similar_Adapter);
		similar_list.setOnItemClickListener(new ItemClick());
	}

	// 将获得结果receString转化为data_array中
	private void get_data(PackageHeader returnHeader) {
		String[] receString = returnHeader.send_string.split("#");

		dataarray = new String[receString.length / 16][16];
		for (int i = 0; i < dataarray.length; i++) {
			for (int j = 0; j < 16; j++) {
				dataarray[i][j] = receString[16 * i + j];
			}
		}
	}

	private static class MyHandler extends Handler {
		/* 建立弱引用 */
		private WeakReference<InfoThree_Activity> mOuter;

		/* 构造函数 */
		public MyHandler(InfoThree_Activity activity) {
			mOuter = new WeakReference<InfoThree_Activity>(activity);
		}

		public void handleMessage(Message msg) {
			// 防止内存泄露
			InfoThree_Activity outer = mOuter.get();
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
			case GETSIMILIAR_SUCCESS:
				refresh_list(returnHeader);
				break;
			case GETSIMILIAR_FAILURE:
				try {
					Toast.makeText(InfoThree_Activity.this,
							returnHeader.send_string, Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					Log.e("myHandler", e.toString());
				}
				break;
		}
	}

	// 组装相似度字符串
	private void getsimiliarString() {
		if (num_list != null) {
			if (num_list.size() == 0) {
				rangeString = "0";
			} else {
				for (int i = 0; i < num_list.size() - 1; i++) {
					rangeString = rangeString + (num_list.get(i) + 1) + "#";
				}

				rangeString = rangeString
						+ (num_list.get(num_list.size() - 1) + 1);
			}
		} else {
			rangeString = "0#";
		}
	}

	// 组装发送字符串功能
	private void getsimiliar() {
		// tag_id,definition_id,num(请求的次数),n(请求的次序),
		// 排列顺序：0（默认），1（距离），2（价钱），3（评分），4（信用），5（相似度）
		List<String> lists = new ArrayList<String>();
		lists.add(myApplication.tag_id);
		lists.add(myApplication.definition_id);
		lists.add("" + myApplication.current_num);
		myApplication.current_num++;
		getsimiliarString();
		lists.add(rangeString);

		net_function(lists, Action_Type.CUSTOMER_GETSIMILAR_PRODUCT);
	}

	/**
	 * 总网络函数
	 * */
	private void net_function(List<String> lists, int action_type) {
		/* 组装发送参数 */
		sslSocket = myApplication.getSSlSocket();
		/* 组装字符串 */
		String dataString = packageHeader.joinString(lists);
		/* 设置package参数 */
		packageHeader.send_string = dataString;
		packageHeader.data_length = dataString.length();
		packageHeader.now_time = System.currentTimeMillis();
		try {
			packageHeader.place_long = Double
					.valueOf(myApplication.user_placelong);
			packageHeader.place_lat = Double
					.valueOf(myApplication.user_placelat);
		} catch (Exception e) {
		}

		/* 发送信息（登录操作） */
		Rece_Thread rece_Thread = new Rece_Thread(sslSocket, action_type,
				packageHeader);
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
				msg.what = HandleTag.GETSIMILIAR_SUCCESS.ordinal();
			} else {
				msg.what = HandleTag.GETSIMILIAR_FAILURE.ordinal();
			}
			msg.obj = returnHeader;
			myHandler.sendMessage(msg);
		}
	}

	// 添加删除排序选择时刷新界面
	private void refresh_header() {

		try {
			if (num_list != null) {
				if (num_list.size() == 0) {
					for (int i = 1; i < itemlayoutLayouts.length; i++) {
						itemlayoutLayouts[i].setVisibility(View.INVISIBLE);
					}
					range_text[0].setText("默认");
					range_icon[0].setBackgroundResource(R.drawable.searchbt);
				} else {
					for (int i = 0; i < num_list.size(); i++) {
						int pos = num_list.get(i);
						itemlayoutLayouts[i].setVisibility(View.VISIBLE);
						range_icon[i]
								.setBackgroundResource(pictureset_array[pos]);
						range_text[i].setText(simple_array[pos]);
					}
					for (int i = num_list.size(); i < 5; i++) {
						itemlayoutLayouts[i].setVisibility(View.INVISIBLE);
					}
				}

			} else {
				for (int i = 1; i < itemlayoutLayouts.length; i++) {
					itemlayoutLayouts[i].setVisibility(View.INVISIBLE);
				}
				range_text[0].setText("默认");
				range_icon[0].setBackgroundResource(R.drawable.searchbt);
			}
		} catch (Exception e) {
			Log.e("refresh_header", e.toString());
		}

	}

	// 排序时刷新界面

	// 弹出排序选择窗口响应range_style
	private class RangeSyle_OnClick implements OnClickListener {
		@SuppressWarnings("deprecation")
		public void onClick(View v) {
			// 显示弹出窗口
			LayoutInflater inflater = LayoutInflater
					.from(InfoThree_Activity.this);
			popView = inflater.inflate(R.layout.rangepop_layout, null);
			range_detail = (TextView) popView.findViewById(R.id.range_detail);
			exitrangebt = (ImageButton) popView.findViewById(R.id.exitrangebt);
			exitrangebt.setOnClickListener(new ExitRange_OnClick());
			range_list = (ListView) popView.findViewById(R.id.range_listview);
			range_list.setDivider(null);// 取消分割线
			adapter = new RangeList_Adapter(InfoThree_Activity.this);
			range_list.setAdapter(adapter);

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
			myApplication.current_num = 1;
			simliar_Items.clear();
		}

	}

	// 退出rangepop
	private class ExitRange_OnClick implements OnClickListener {
		public void onClick(View v) {
			popWindow.dismiss();

		}
	}

	/*********************** 相近产品信息 ***************************/
	// 查询相近产品的信息
	private class SimilarItem {
		// tag_id<!-- 名称 ,sortone,money--><!-- 简介 --><!-- 地址，距离 --><!-- 评分，信誉度
		// -->
		public String tag_id;
		public String sortone, sorttwo, definition_id, product_id;
		public String similar_name, similar_sortone, similar_price;
		public String similar_detail;
		public String factory_name;;
		@SuppressWarnings("unused")
		public String similar_place, similar_distance, placelat, placelong;
		public String similar_score, similar_trust, similar_people;

		public SimilarItem(String[] datastring) {
			tag_id = datastring[0];
			sortone = datastring[1];
			sorttwo = datastring[2];
			definition_id = datastring[3];
			product_id = datastring[4];
			similar_name = datastring[5];
			similar_sortone = datastring[6];
			similar_price = datastring[7];
			similar_detail = datastring[8];
			factory_name = datastring[9];
			similar_place = datastring[10];
			similar_distance = get_distance(datastring[12], datastring[11]);
			placelat = datastring[12];
			placelong = datastring[11];
			similar_score = datastring[13];
			similar_people = datastring[14];
			similar_trust = datastring[15];
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

	// 适配器
	private class Similar_Adapter extends BaseAdapter {
		private LayoutInflater inflater;

		public Similar_Adapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		public int getCount() {

			return simliar_Items.size();
		}

		public SimilarItem getItem(int pos) {
			return simliar_Items.get(pos);
		}

		public long getItemId(int pos) {
			return pos;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			final SimilarItem similarItem = getItem(pos);
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.similarlist_layout,
						null);
				@SuppressWarnings("unused")
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

				similiar_productname.setText(similarItem.similar_name);
				similar_sortone.setText(similarItem.similar_sortone);
				similar_price.setText(similarItem.similar_price);
				similar_detail.setText(similarItem.similar_detail);
				similar_place.setText(similarItem.factory_name);
				similar_distance.setText(similarItem.similar_distance);
				similar_score.setText(similarItem.similar_score);
				similar_people.setText("/" + similarItem.similar_people);
				similar_trust.setText(similarItem.similar_trust);
				trust_level.setText(gettrust_level(similarItem.similar_trust));

			}
			return convertView;
		}

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

	// 排序方式适配器
	private class RangeList_Adapter extends BaseAdapter {
		private LayoutInflater inflater;

		public RangeList_Adapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		public int getCount() {

			return style_array.length;
		}

		public String getItem(int pos) {

			return style_array[pos];
		}

		public long getItemId(int pos) {
			return pos;
		}

		public View getView(final int pos, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.rangelist_layout, null);
				final ImageView range_icon = (ImageView) convertView
						.findViewById(R.id.range_icon);
				final Button range_text = (Button) convertView
						.findViewById(R.id.range_text);
				final ImageButton range_select = (ImageButton) convertView
						.findViewById(R.id.range_select);

				if (!flag_all[pos]) {
					range_select
							.setBackgroundResource(R.drawable.sendgroup_pressed);
				} else {
					range_select.setBackgroundResource(R.drawable.sendgroup);
				}
				range_icon.setBackgroundResource(picture_array[pos]);
				range_text.setText(style_array[pos]);

				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!flag_all[pos]) {
							flag_all[pos] = true;
							num_list.add(pos);
							range_select
									.setBackgroundResource(R.drawable.sendgroup);
							range_detail.setText(detail_array[pos]);
							refresh_header();
						} else {
							flag_all[pos] = false;

							for (int i = 0; i < num_list.size(); i++) {
								if (pos == num_list.get(i)) {
									num_list.remove(i);
								}
							}
							range_select
									.setBackgroundResource(R.drawable.sendgroup_pressed);
							range_detail.setText("");
							refresh_header();
						}

					}
				};
				range_text.setOnClickListener(listener);
			}
			return convertView;
		}
	}
}
