package product_info;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

import listadapter.SaveGroup_Adapter;
import load_reg.LogIn_Activity;
import mainview.demo.DemoApplication;
import mainview.demo.MainActivity;
import mainview.demo.R;
import ssl.Action_Type;
import ssl.PackageHeader;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import downloadimage.SetImageView;
/**
 * 获取产品基本信息界面
 */
@SuppressLint("InflateParams")
public class InfoOne_Activity extends Activity implements OnGestureListener {
	// 手势操作
	private GestureDetector gestureDetector;
	// 需要切换的图片
	private FrameLayout imagechange_layout = null;
	private ViewFlipper viewFlipper1 = null;
	private ImageView imageView1 = null, imageView2 = null, imageView3 = null,
			imageView4 = null;
	// 图片提示信息
	private TextView photowarn_textview = null;
	private LinearLayout photo_layout = null;
	private int now = 0;
	private int pictureCounts = 4;
	// 四张图片的ID
	private int[] picture_id = { R.drawable.picture1, R.drawable.picture2,
			R.drawable.picture3, R.drawable.picture4 };
	// 网络通信模块
	private DemoApplication myApplication;
	private SSLSocket sslSocket;
	private PackageHeader packageHeader;
	// Share轻量存储模块
	private SharedPreferences share;// 轻量存储

	/****** 商品信息界面 ******/
	// 存储商品信息
	String[] basic_array = null;
	String[] receString = null;
	// 其他信息界面
	private Button basicinfo_bt = null,// 商品信息按钮
			factoryname_bt = null,// 生产厂家按钮
			outputdate_bt = null,// 出厂日期按钮
			enddate_bt = null;// 有效日期按钮
	private Button currentstate_bt = null,// 当前状态按钮
			storename_bt = null,// 销售商家按钮
			storelevel_bt = null,// 商家级别按钮
			current_place_bt = null;// 当前所在地按钮
	private Button detailinfo_bt = null,// 详细信息按钮
			productcomponent_bt = null,// 商品规格按钮
			productinstrustions_bt = null,// 商品分类按钮
			productnotice_bt = null;// 注意事项按钮

	// 分类界面
	private Button sortonename_bt = null,// 一级分类按钮
			sorttwoname_bt = null,// 二级分类按钮
			definitionname_bt = null,// 商品分类按钮
			productid_bt = null;// 商品号码按钮
	private TextView productname_text = null;// 商品名称

	// 商品图片界面
	private ImageButton photoimage_bt = null;// 商品图片按钮
	private TextView detailinfo_text = null;// 商品信息简介

	private Button price_bt = null,// 参考价格按钮
			scorenum_text = null,// 用户评分按钮
			interestindex_text = null,// 兴趣指数按钮
			peopleindex_text = null,// 兴趣人群按钮
			realindex_text = null,// 正品指数按钮
			savebt = null,// 点击收藏按钮
			introduction_text = null;// 查看更多详情按钮

	private RatingBar score_bar = null,// 评分显示图标
			interest_bar = null,// 兴趣指数显示图标
			people_bar = null,// 兴趣人群显示图标
			real_bar = null;// 正品指数显示图标

	/* 消息分类枚举 */
	private enum HandleTag {
		GETBASIC_SUCCESS, // 获取信息成功
		GETBASIC_FAILURE, // 获取信息失败
		GETGROUP_SUCCESS, // 获取分组成功
		GETGROUP_FAILURE, // 获取分组失败
		ADDGROUP_SUCCESS, // 添加分组成功
		ADDGROUP_FAILURE, // 添加分组失败
		DELEGROUP_SUCCESS, // 删除分组成功
		DELEGROUP_FAILURE, // 删除分组失败
		SAVEPRODUCT_SUCCESS, // 收藏商品成功
		SAVEPRODUCT_FAILURE, // 收藏商品失败

		SCOREPRODUCT_SUCCESS, // 商品评分成功
		SCOREPRODUCT_FAILURE, // 商品评分失败
		GETSCORE_SUCCESS, // 获取商品评分信息成功
		GETSCORE_FAILURE, // 获取商品评分信息失败
	}

	private MyHandler myHandler;
	/*设置图片*/
	private SetImageView setImageView;
	
	private final int GETBASIC = 0, GETGROUP = 1, ADDGROUP = 2, DELEGROUP = 3,
			SAVEPRODUCT = 4, SCOREPRODUCT = 5, GETSCORE = 6;
	/************ 收藏分组信息 ***************/
	// 收藏分组信息界面
	private ListView Savegroup_list;
	private List<String> group_arraylist = new ArrayList<String>();
	private SaveGroup_Adapter adapter;
	private View popView;
	// 弹出窗口
	private PopupWindow popWindow = null;
	View parent;
	// popupWindow中的控件
	private ImageButton addgroup_bt, ensureaddgroup_bt, deletegroup_bt,
			finishadd_bt, exitpop_bt;
	private EditText addgroup_name;
	// 响应标志
	private boolean addgroup_flag = false;
	// 获得分组信息
	private String groupstring = "";
	private String[] grouparray = null;
	private String addgroup = "";

	/************* 基本信息弹出窗口 ***************/
	private ImageButton exit_basicbt = null;
	private TextView item1, item2, item3 = null;
	private TextView text1, text2, text3 = null;
	private TextView header = null;
	/************** 评分信息窗口 *************/
	private ImageButton exit_scorebt;
	// 商品评分
	private TextView productscore_text;
	private RatingBar productscore_bar;
	private ImageButton product_detail;
	private LinearLayout productscore_layout;
	private RatingBar productprecent_bar5, productprecent_bar4,
			productprecent_bar3, productprecent_bar2, productprecent_bar1;
	private TextView productprecent_text5, productprecent_text4,
			productprecent_text3, productprecent_text2, productprecent_text1;
	// 同类商品评分
	private TextView definitionscore_text;
	private RatingBar definitionscore_bar;
	private ImageButton definition_detail;
	private LinearLayout definitionscore_layout;
	private RatingBar definitionprecent_bar5, definitionprecent_bar4,
			definitionprecent_bar3, definitionprecent_bar2,
			definitionprecent_bar1;
	private TextView definitionprecent_text5, definitionprecent_text4,
			definitionprecent_text3, definitionprecent_text2,
			definitionprecent_text1;

	// 顾客评分
	private TextView myscore_text;
	private RatingBar myscore_bar;
	private ImageButton my_detail;
	// 两个详细信息控制标志
	private boolean product_flag = true;
	private boolean definition_flag = true;

	/*
	 * // 三种人数 private int product_people = 0, definition_people = 0; private
	 * boolean myscored_flag = false;
	 */

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.infoone_layout);
		myApplication = (DemoApplication) super.getApplication();
		sslSocket = myApplication.getSSlSocket();
		// 轻量存储模块
		share = getSharedPreferences("transfer", Activity.MODE_PRIVATE);
		myHandler = new MyHandler(this);

		initpackage();
		init();

	}

	/**
	 * 界面初始化
	 * */
	private void init() {
		// 获取ID
		imagechange_layout = (FrameLayout) super
				.findViewById(R.id.imagechange_layout);

		gestureDetector = new GestureDetector(this, this);
		// 设置ViewFlipper
		viewFlipper1 = (ViewFlipper) findViewById(R.id.viewFlipper1);
		imageView1 = (ImageView) super.findViewById(R.id.imageView1);
		imageView2 = (ImageView) super.findViewById(R.id.imageView2);
		imageView3 = (ImageView) super.findViewById(R.id.imageView3);
		imageView4 = (ImageView) super.findViewById(R.id.imageView4);
		initPicture();
		photowarn_textview = (TextView) super
				.findViewById(R.id.photowarn_textview);
		photo_layout = (LinearLayout) findViewById(R.id.photo_layout);
		generatePageControl(now);

		basicinfo_bt = (Button) super.findViewById(R.id.basicinfo_bt);
		basicinfo_bt.setOnClickListener(new BasicInfo_OnClick());
		factoryname_bt = (Button) super.findViewById(R.id.factoryname_bt);
		outputdate_bt = (Button) super.findViewById(R.id.outputdate_bt);
		enddate_bt = (Button) super.findViewById(R.id.enddate_bt);
		factoryname_bt.setOnClickListener(new BasicInfo_OnClick());
		outputdate_bt.setOnClickListener(new BasicInfo_OnClick());
		enddate_bt.setOnClickListener(new BasicInfo_OnClick());

		currentstate_bt = (Button) super.findViewById(R.id.currentstate_bt);
		storename_bt = (Button) super.findViewById(R.id.storename_bt);
		storelevel_bt = (Button) super.findViewById(R.id.storelevel_bt);
		current_place_bt = (Button) super.findViewById(R.id.current_place_bt);
		currentstate_bt.setOnClickListener(new CurrentState_OnClick());
		storename_bt.setOnClickListener(new CurrentState_OnClick());
		storelevel_bt.setOnClickListener(new CurrentState_OnClick());
		current_place_bt.setOnClickListener(new CurrentState_OnClick());

		detailinfo_bt = (Button) super.findViewById(R.id.detailinfo_bt);
		productcomponent_bt = (Button) super
				.findViewById(R.id.productcomponent_bt);
		productinstrustions_bt = (Button) super
				.findViewById(R.id.productinstrustions_bt);
		productnotice_bt = (Button) super.findViewById(R.id.productnotice_bt);
		detailinfo_bt.setOnClickListener(new DetailInfo_OnClick());
		productcomponent_bt.setOnClickListener(new DetailInfo_OnClick());
		productinstrustions_bt.setOnClickListener(new DetailInfo_OnClick());
		productnotice_bt.setOnClickListener(new DetailInfo_OnClick());

		// 分类界面
		sortonename_bt = (Button) super.findViewById(R.id.sortonename_bt);
		sorttwoname_bt = (Button) super.findViewById(R.id.sorttwoname_bt);
		definitionname_bt = (Button) super.findViewById(R.id.definitionname_bt);
		productid_bt = (Button) super.findViewById(R.id.productid_bt);
		productname_text = (TextView) super.findViewById(R.id.productname_text);

		// 商品图片界面
		photoimage_bt = (ImageButton) super.findViewById(R.id.photoimage_bt);
		detailinfo_text = (TextView) super.findViewById(R.id.detailinfo_text);

		price_bt = (Button) super.findViewById(R.id.price_bt);
		scorenum_text = (Button) super.findViewById(R.id.scorenum_text);
		scorenum_text.setOnClickListener(new ScoreNum_OnClick());
		interestindex_text = (Button) super
				.findViewById(R.id.interestindex_text);
		peopleindex_text = (Button) super.findViewById(R.id.peopleindex_text);
		realindex_text = (Button) super.findViewById(R.id.realindex_text);

		savebt = (Button) super.findViewById(R.id.savebt);
		savebt.setOnClickListener(new SaveBtOnClick());
		if (myApplication.saved_flag) {
			saved_refresh();
		} else {
			unsaved_refresh();
		}
		introduction_text = (Button) super.findViewById(R.id.introduction_text);
		introduction_text.setOnClickListener(new LookStroe());
		score_bar = (RatingBar) super.findViewById(R.id.score_bar);
		interest_bar = (RatingBar) super.findViewById(R.id.interest_bar);
		people_bar = (RatingBar) super.findViewById(R.id.people_bar);
		real_bar = (RatingBar) super.findViewById(R.id.real_bar);

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

	// introduction_text
	private class LookStroe implements OnClickListener {
		public void onClick(View v) {
			myApplication.store_flag = true;
			MainActivity.myTabHost.setCurrentTab(2);
		}
	}

	/********************* 商品评分信息响应 ************************/
	// 评分信息scorenum_text
	private class ScoreNum_OnClick implements OnClickListener {
		public void onClick(View v) {
			if (myApplication.anonymity_flag) {
				Toast.makeText(InfoOne_Activity.this, "对不起，您需要先登录",
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(InfoOne_Activity.this,
						LogIn_Activity.class);
				InfoOne_Activity.this.startActivityForResult(intent, 0);
			} else if (!myApplication.scaned_falg) {
				Toast.makeText(InfoOne_Activity.this, "请先扫描标签",
						Toast.LENGTH_SHORT).show();
			} else {
				getscore();
				setpopWindow(v);
			}

		}
	}

	// /////////////获取评分信息
	/**
	 * 发送评分信息Thread
	 */
	private void getscore() {
		// tag_id,definition_id
		List<String> lists = new ArrayList<String>();
		lists.add(myApplication.tag_id);
		lists.add(myApplication.definition_id);
		net_function(lists, Action_Type.CUSTOMER_SCORE_DEFINITION, GETSCORE);
	}

	// 刷新评分popwindow界面
	private void refresh_scorepop() {
		try {
			productscore_text.setText(receString[1] + "/" + receString[2]);
			productscore_bar.setRating(Float.valueOf(receString[1]));
			productprecent_bar5.setRating((float) (Float.valueOf(receString[3])
					/ Float.valueOf(receString[2]) * 5.0));
			productprecent_bar4.setRating((float) (Float.valueOf(receString[4])
					/ Float.valueOf(receString[2]) * 5.0));
			productprecent_bar3.setRating((float) (Float.valueOf(receString[5])
					/ Float.valueOf(receString[2]) * 5.0));
			productprecent_bar2.setRating((float) (Float.valueOf(receString[6])
					/ Float.valueOf(receString[2]) * 5.0));
			productprecent_bar1.setRating((float) (Float.valueOf(receString[7])
					/ Float.valueOf(receString[2]) * 5.0));
			productprecent_text5.setText(receString[3]);
			productprecent_text4.setText(receString[4]);
			productprecent_text3.setText(receString[5]);
			productprecent_text2.setText(receString[6]);
			productprecent_text1.setText(receString[7]);

			definitionscore_text.setText(receString[8] + "/" + receString[9]);
			definitionscore_bar.setRating(Float.valueOf(receString[8]));
			definitionprecent_bar5
					.setRating((float) (Float.valueOf(receString[10])
							/ Float.valueOf(receString[9]) * 5.0));
			definitionprecent_bar4
					.setRating((float) (Float.valueOf(receString[11])
							/ Float.valueOf(receString[9]) * 5.0));
			definitionprecent_bar3
					.setRating((float) (Float.valueOf(receString[12])
							/ Float.valueOf(receString[9]) * 5.0));
			definitionprecent_bar2
					.setRating((float) (Float.valueOf(receString[13])
							/ Float.valueOf(receString[9]) * 5.0));
			definitionprecent_bar1
					.setRating((float) (Float.valueOf(receString[14])
							/ Float.valueOf(receString[9]) * 5.0));
			definitionprecent_text5.setText(receString[10]);
			definitionprecent_text4.setText(receString[11]);
			definitionprecent_text3.setText(receString[12]);
			definitionprecent_text2.setText(receString[13]);
			definitionprecent_text1.setText(receString[14]);
			if (receString[15].equals("true")) {
				myscore_text.setText(receString[16] + "/已评");
				myscore_text.setTextColor(Color.RED);
				myscore_bar.setIsIndicator(true);
				my_detail.setEnabled(false);
				my_detail.setBackgroundResource(R.drawable.sendscorebt_pressed);
			} else {
				my_detail.setEnabled(true);
				my_detail.setBackgroundResource(R.drawable.sendscorebt);
			}

		} catch (Exception e) {
			Log.e("refresh_scorepop", e.toString());
		}
	}

	// 显示弹出窗口
	@SuppressWarnings("deprecation")
	private void setpopWindow(View v) {
		LayoutInflater inflater = LayoutInflater.from(InfoOne_Activity.this);
		popView = inflater.inflate(R.layout.score_layout, null);

		exit_scorebt = (ImageButton) popView.findViewById(R.id.exit_scorebt);
		productscore_text = (TextView) popView
				.findViewById(R.id.productscore_text);
		productscore_bar = (RatingBar) popView
				.findViewById(R.id.productscore_bar);
		product_detail = (ImageButton) popView
				.findViewById(R.id.product_detail);
		product_detail.setOnClickListener(new ProductOnClick());
		productscore_layout = (LinearLayout) popView
				.findViewById(R.id.productscore_layout);
		productprecent_bar5 = (RatingBar) popView
				.findViewById(R.id.productprecent_bar5);
		productprecent_bar4 = (RatingBar) popView
				.findViewById(R.id.productprecent_bar4);
		productprecent_bar3 = (RatingBar) popView
				.findViewById(R.id.productprecent_bar3);
		productprecent_bar2 = (RatingBar) popView
				.findViewById(R.id.productprecent_bar2);
		productprecent_bar1 = (RatingBar) popView
				.findViewById(R.id.productprecent_bar1);
		productprecent_text5 = (TextView) popView
				.findViewById(R.id.productprecent_text5);
		productprecent_text4 = (TextView) popView
				.findViewById(R.id.productprecent_text4);
		productprecent_text3 = (TextView) popView
				.findViewById(R.id.productprecent_text3);
		productprecent_text2 = (TextView) popView
				.findViewById(R.id.productprecent_text2);
		productprecent_text1 = (TextView) popView
				.findViewById(R.id.productprecent_text1);

		definitionscore_text = (TextView) popView
				.findViewById(R.id.definitionscore_text);
		definitionscore_bar = (RatingBar) popView
				.findViewById(R.id.definitionscore_bar);
		definition_detail = (ImageButton) popView
				.findViewById(R.id.definition_detail);
		definition_detail.setOnClickListener(new DefinitionOnClick());
		definitionscore_layout = (LinearLayout) popView
				.findViewById(R.id.definitionscore_layout);
		definitionprecent_bar5 = (RatingBar) popView
				.findViewById(R.id.definitionprecent_bar5);
		definitionprecent_bar4 = (RatingBar) popView
				.findViewById(R.id.definitionprecent_bar4);
		definitionprecent_bar3 = (RatingBar) popView
				.findViewById(R.id.definitionprecent_bar3);
		definitionprecent_bar2 = (RatingBar) popView
				.findViewById(R.id.definitionprecent_bar2);
		definitionprecent_bar1 = (RatingBar) popView
				.findViewById(R.id.definitionprecent_bar1);
		definitionprecent_text5 = (TextView) popView
				.findViewById(R.id.definitionprecent_text5);
		definitionprecent_text4 = (TextView) popView
				.findViewById(R.id.definitionprecent_text4);
		definitionprecent_text3 = (TextView) popView
				.findViewById(R.id.definitionprecent_text3);
		definitionprecent_text2 = (TextView) popView
				.findViewById(R.id.definitionprecent_text2);
		definitionprecent_text1 = (TextView) popView
				.findViewById(R.id.definitionprecent_text1);

		myscore_text = (TextView) popView.findViewById(R.id.myscore_text);
		myscore_bar = (RatingBar) popView.findViewById(R.id.myscore_bar);
		myscore_bar.setOnRatingBarChangeListener(new MyScoreOnChange());
		my_detail = (ImageButton) popView.findViewById(R.id.my_detail);
		my_detail.setOnClickListener(new MyScoreOnClick());
		my_detail.setEnabled(false);
		my_detail.setBackgroundResource(R.drawable.sendscorebt_pressed);

		exit_scorebt.setOnClickListener(new ExitPopOnClick());
		// 设置弹出窗口
		popWindow = new PopupWindow(popView,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// 使其聚集
		popWindow.setFocusable(true);
		// 设置允许在外点击消失
		popWindow.setBackgroundDrawable(new BitmapDrawable());
		popWindow.setOutsideTouchable(true);

		popWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
	}

	// 响应函数product_detail
	private class ProductOnClick implements OnClickListener {
		public void onClick(View v) {
			if (product_flag) {
				product_flag = false;
				product_detail.setBackgroundResource(R.drawable.score_detailbt);
				productscore_layout.setVisibility(View.GONE);
			} else {
				product_flag = true;
				product_detail
						.setBackgroundResource(R.drawable.score_detailbt_pressed);
				productscore_layout.setVisibility(View.VISIBLE);
			}
		}
	}

	private class DefinitionOnClick implements OnClickListener {
		public void onClick(View v) {
			if (definition_flag) {
				definition_flag = false;
				definition_detail
						.setBackgroundResource(R.drawable.score_detailbt);
				definitionscore_layout.setVisibility(View.GONE);
			} else {
				definition_flag = true;
				definition_detail
						.setBackgroundResource(R.drawable.score_detailbt_pressed);
				definitionscore_layout.setVisibility(View.VISIBLE);
			}
		}
	}

	// 顾客评分条响应函数myscore_bar
	private class MyScoreOnChange implements OnRatingBarChangeListener {

		@Override
		public void onRatingChanged(RatingBar ratingBar, float rating,
				boolean fromUser) {
			myscore_bar.setRating(rating);
			myscore_text.setText(rating + "/未评");

		}

	}

	// 顾客评分操作my_detail
	private class MyScoreOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			sendscore();
		}

	}

	/**
	 * 发送评分信息Thread
	 */
	private void sendscore() {
		// 参数：tag_id,score,location_name

		List<String> lists = new ArrayList<String>();
		lists.add(myApplication.tag_id);
		lists.add("" + myscore_bar.getRating());
		lists.add(myApplication.user_place);

		net_function(lists, Action_Type.CUSTOMER_SCORE_PRODUCT, SCOREPRODUCT);
	}

	// 评分成功后刷新界面
	private void refresh_myscore() {
		myscore_text.setText(myscore_bar.getRating() + "/已评");
		myscore_text.setTextColor(Color.RED);
		myscore_bar.isIndicator();
		my_detail.setEnabled(false);
		my_detail.setBackgroundResource(R.drawable.sendscorebt_pressed);

	}

	/**************** 商品基本信息中的响应 *******************/
	// 商品信息按钮basicinfo_bt
	private class BasicInfo_OnClick implements OnClickListener {
		public void onClick(View v) {

			getpop_window(v);
			refresh_basicinfo();
		}

	}

	// 刷新basicinfo界面
	private void refresh_basicinfo() {
		try {
			header.setText("商品信息");
			item1.setText("生产厂家：");
			item2.setText("出厂日期：");
			item3.setText("有效日期：");
			text1.setText(basic_array[4]);
			text2.setText(basic_array[1]);
			text3.setText(basic_array[2]);
		} catch (Exception e) {
			Log.e("refresh_basicinfo", e.toString());
		}
	}

	// 当前状态按钮currentstate_bt
	private class CurrentState_OnClick implements OnClickListener {
		public void onClick(View v) {

			getpop_window(v);
			refresh_currentstate();
		}
	}

	private void refresh_currentstate() {
		try {
			header.setText("当前状态");
			item1.setText("销售商家：");
			item2.setText("商家级别：");
			item3.setText("当前所在地：");
			text1.setText(basic_array[15]);
			text2.setText(rechange_level(basic_array[16]));
			text3.setText(basic_array[17]);
		} catch (Exception e) {
			Log.e("refresh_currentstate", e.toString());
		}
	}

	// 详细信息按钮detailinfo_bt
	private class DetailInfo_OnClick implements OnClickListener {
		public void onClick(View v) {

			getpop_window(v);
			refresh_detailinfo();
		}
	}

	private void refresh_detailinfo() {
		try {
			header.setText("详细信息");
			item1.setText("商品规格：");
			item2.setText("使用说明：");
			item3.setText("注意事项：");
			text1.setText(basic_array[6]);
			text2.setText(basic_array[7]);
			text3.setText(basic_array[8]);
		} catch (Exception e) {
			Log.e("refresh_detailinfo", e.toString());
		}
	}

	// 设置popwindow
	@SuppressWarnings("deprecation")
	private void getpop_window(View v) {
		LayoutInflater inflater = LayoutInflater.from(InfoOne_Activity.this);
		popView = inflater.inflate(R.layout.basicinfo_layout, null);

		exit_basicbt = (ImageButton) popView.findViewById(R.id.exit_basicbt);
		item1 = (TextView) popView.findViewById(R.id.item1);
		item2 = (TextView) popView.findViewById(R.id.item2);
		item3 = (TextView) popView.findViewById(R.id.item3);
		text1 = (TextView) popView.findViewById(R.id.text1);
		text2 = (TextView) popView.findViewById(R.id.text2);
		text3 = (TextView) popView.findViewById(R.id.text3);
		header = (TextView) popView.findViewById(R.id.header);

		exit_basicbt.setOnClickListener(new ExitPopOnClick());
		// 设置弹出窗口
		popWindow = new PopupWindow(popView,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// 使其聚集
		popWindow.setFocusable(true);
		// 设置允许在外点击消失
		popWindow.setBackgroundDrawable(new BitmapDrawable());
		popWindow.setOutsideTouchable(true);
		popWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
	}

	/***************** 添加收藏功能 ********************/
	// 收藏按钮响应
	private class SaveBtOnClick implements OnClickListener {
		// ImageButton
		// addgroup_bt,ensureaddgroup_bt,deletegroup_bt,finishadd_bt,exitpop_bt;
		// private EditText addgroup_name;
		public void onClick(View v) {
			try {
				/*当为游客访问时*/
				if (myApplication.anonymity_flag) {
					Toast.makeText(InfoOne_Activity.this, "对不起，您需要先登录",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(InfoOne_Activity.this,
							LogIn_Activity.class);
					InfoOne_Activity.this.startActivity(intent);
				}
				// 未扫描时
				else if (!myApplication.scaned_falg) {
					Toast.makeText(InfoOne_Activity.this, "请先扫描标签",
							Toast.LENGTH_SHORT).show();
				} else {
					boolean searchflag = true;
					groupstring = share.getString("groupstring", "null");
					// 如果无信息
					if (groupstring.equals(null)) {
						searchflag = true;
					} else {
						// 获取字符串进行解析
						grouparray = groupstring.split("#");
						if (grouparray[0].equals(share.getString("user_name",
								"xiaoaojianghu"))) {
							searchflag = false;
						} else {
							searchflag = true;
						}
					}

					if (!searchflag) {
						getgrouplist();
						setpopwindow(v);
					} else {
						getproduct_group();
					}
				}
			} catch (Exception e) {
				Log.e("popwindow", e.toString());
			}
		}

	}

	// 弹出popupWindow
	@SuppressWarnings("deprecation")
	private void setpopwindow(View v) {
		// 显示弹出窗口
		LayoutInflater inflater = LayoutInflater.from(InfoOne_Activity.this);
		popView = inflater.inflate(R.layout.save_poplayout, null);
		Savegroup_list = (ListView) popView
				.findViewById(R.id.savegroupListView);
		Savegroup_list.setDivider(null);// 取消分割线
		adapter = new SaveGroup_Adapter(InfoOne_Activity.this, group_arraylist,
				myApplication);
		Savegroup_list.setAdapter(adapter);
		// 添加分组按钮
		addgroup_bt = (ImageButton) popView.findViewById(R.id.addgroup_bt);
		ensureaddgroup_bt = (ImageButton) popView
				.findViewById(R.id.ensureaddgroup_bt);
		deletegroup_bt = (ImageButton) popView
				.findViewById(R.id.deletegroup_bt);
		finishadd_bt = (ImageButton) popView.findViewById(R.id.finishadd_bt);
		exitpop_bt = (ImageButton) popView.findViewById(R.id.exitpop_bt);
		addgroup_name = (EditText) popView.findViewById(R.id.addgroup_name);
		// 添加事件响应
		addgroup_bt.setOnClickListener(new AddGroupOnClick());
		ensureaddgroup_bt.setOnClickListener(new EnsureAddOnClick());
		deletegroup_bt.setOnClickListener(new DeleGroup_OnClick());
		finishadd_bt.setOnClickListener(new FinishAdd_OnClick());
		exitpop_bt.setOnClickListener(new ExitPopOnClick());

		// 设置弹出窗口
		popWindow = new PopupWindow(popView,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// 使其聚集
		popWindow.setFocusable(true);
		// 设置允许在外点击消失
		popWindow.setBackgroundDrawable(new BitmapDrawable());
		popWindow.setOutsideTouchable(true);
		popWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
	}

	// 分析获得分组信息
	private void getgrouparray(PackageHeader returnHeader) {

		try {
			String tempString = returnHeader.send_string;
			String[] tempArray = tempString.split("#"); 
			grouparray = new String[tempArray.length+1];
			for (int i = 0; i < tempArray.length; i++) {
				grouparray[i+1] = tempArray[i];
			}
			grouparray[0] = share.getString("user_name", "xiaoaojianghu");
			// 信息存储到文件中
			SharedPreferences.Editor edit = share.edit();
			String group_string = "";
			for (int i = 0; i < grouparray.length; i++) {
				group_string = group_string + grouparray[i] + "#";
			}
			edit.putString("groupstring", group_string);
			edit.commit();
		} catch (Exception e) {
			Log.e("getgrouparray", e.toString());
		}
	}

	// 转化为list 更新列表
	private void getgrouplist() {
		try {
			group_arraylist.clear();
			for (int i = 1; i < grouparray.length; i++) {
				group_arraylist.add(grouparray[i]);
			}
		} catch (Exception e) {
			Log.e("getgrouplist", e.toString());
		}

	}

	/**
	 * 发送获得分组信息数据
	 */
	private void getproduct_group() {
		// n(1代表查询收藏产品，2代表分组)
		List<String> lists = new ArrayList<String>();
		lists.add("2");
		net_function(lists, Action_Type.CUSTOMER_SEARCH_PRODUCT, GETGROUP);
	}

	// 添加分组事件响应addgroup_bt
	private class AddGroupOnClick implements OnClickListener {
		public void onClick(View v) {
			if (!addgroup_flag) {
				addgroup_flag = true;
				// 修改按钮
				addgroup_bt
						.setBackgroundResource(R.drawable.cancel_addgroupbt_layout);
				addgroup_name.setVisibility(View.VISIBLE);
				ensureaddgroup_bt.setVisibility(View.VISIBLE);
				deletegroup_bt.setVisibility(View.GONE);
			} else {
				addgroup_flag = false;
				// 修改按钮
				addgroup_bt.setBackgroundResource(R.drawable.addgroupbt_layout);
				addgroup_name.setVisibility(View.GONE);
				ensureaddgroup_bt.setVisibility(View.GONE);
				deletegroup_bt.setVisibility(View.VISIBLE);
			}
		}
	}

	// 完成添加分组操作响应ensureaddgroup_bt
	@SuppressLint("SimpleDateFormat")
	private class EnsureAddOnClick implements OnClickListener {
		public void onClick(View v) {
			// 参数：productGroupname
			if (addgroup_name.getText().toString().trim().equals("")) {
				Toast.makeText(InfoOne_Activity.this, "分组名不能为空",
						Toast.LENGTH_SHORT).show();
			} else {
				List<String> lists = new ArrayList<String>();
				addgroup = addgroup_name.getText().toString();
				lists.add(addgroup);
				net_function(lists, Action_Type.CUSTOMER_ADD_PRODUCTGROUP,
						ADDGROUP);
			}
		}
	}

	// 添加成功后刷新popWindow
	private void addrefreshpop() {
		// 信息存储到文件中
		SharedPreferences.Editor edit = share.edit();
		String group_string = "";
		for (int i = 0; i < grouparray.length; i++) {
			group_string = group_string + grouparray[i] + "#";
		}
		group_string += addgroup;
		edit.putString("groupstring", group_string);
		edit.commit();
		// 刷新操作列表
		group_arraylist.add(addgroup);
		adapter = new SaveGroup_Adapter(InfoOne_Activity.this, group_arraylist,
				myApplication);
		Savegroup_list.setAdapter(adapter);
		addgroup_flag = false;
		// 按钮界面刷新响应
		addgroup_bt.setBackgroundResource(R.drawable.addgroupbt_layout);
		addgroup_name.setVisibility(View.GONE);
		ensureaddgroup_bt.setVisibility(View.GONE);
		deletegroup_bt.setVisibility(View.VISIBLE);
	}

	/**
	 * 删除分组操作响应deletegroup_bt
	 */
	private class DeleGroup_OnClick implements OnClickListener {
		public void onClick(View v) {
			if (!myApplication.grouptap_flag) {
				Toast.makeText(InfoOne_Activity.this, "请先选定分组",
						Toast.LENGTH_SHORT).show();
			} else {
				List<String> lists = new ArrayList<String>();
				lists.add(myApplication.grouptap_name);
				net_function(lists, Action_Type.CUSTOMER_DELETE_PRODUCTGROUP,
						DELEGROUP);
			}
		}
	}

	// 删除成功后的刷新操作
	private void delete_refresh() {
		// 信息存储到文件中
		SharedPreferences.Editor edit = share.edit();
		String group_string = "";
		for (int i = 0; i < grouparray.length; i++) {
			if (!grouparray[i].equals(myApplication.grouptap_name)) {
				group_string = group_string + grouparray[i] + "#";
			}
		}
		edit.putString("groupstring", group_string);
		edit.commit();
		// 刷新操作列表
		for (int i = 0; i < group_arraylist.size(); i++) {
			if (group_arraylist.get(i).equals(myApplication.grouptap_name)) {
				group_arraylist.remove(i);
			}
		}
		adapter = new SaveGroup_Adapter(InfoOne_Activity.this, group_arraylist,
				myApplication);
		Savegroup_list.setAdapter(adapter);
		addgroup_flag = false;
		// 按钮界面刷新响应
	}

	// 添加收藏操作finishadd_bt
	private class FinishAdd_OnClick implements OnClickListener {
		public void onClick(View v) {
			if (!myApplication.grouptap_flag) {
				Toast.makeText(InfoOne_Activity.this, "请先选定分组",
						Toast.LENGTH_SHORT).show();
			} else {
				// tag_id,productGroupname
				List<String> lists = new ArrayList<String>();
				lists.add(myApplication.tag_id);
				lists.add(myApplication.grouptap_name);
				net_function(lists, Action_Type.CUSTOMER_ADD_PRODUCT,
						SAVEPRODUCT);
			}
		}
	}

	// 设置收藏按钮状态
	private void saved_refresh() {
		savebt.setBackgroundResource(R.drawable.savebt_pressed);
		savebt.setText("已收藏");
		savebt.setEnabled(false);
		myApplication.saved_flag = true;
	}

	// 回复收藏按钮状态
	private void unsaved_refresh() {
		savebt.setBackgroundResource(R.drawable.savebt);
		savebt.setText("点击收藏");
		savebt.setEnabled(true);
		myApplication.saved_flag = false;
	}

	// 退出收藏popwindow按钮exitpop_bt
	private class ExitPopOnClick implements OnClickListener {
		public void onClick(View v) {
			// 关闭弹出框
			popWindow.dismiss();
		}
	}

	/**
	 * 进行重刷新通信
	 */
	private void refresh_view() {
		// 参数：tag_id,sort_id_one,sort_id_two,definition_id,product_id,
		List<String> lists = new ArrayList<String>();
		lists.add(myApplication.tag_id);
		lists.add(myApplication.sort_id_one);
		lists.add(myApplication.sort_id_two);
		lists.add(myApplication.definition_id);
		lists.add(myApplication.product_id);

		net_function(lists, Action_Type.CUSTOMER_QUERY_BASICINFO, GETBASIC);
	}

	// 进行界面显示更新
	private void refreshView(PackageHeader returHeader) {
		try {
			receString = returHeader.send_string.split("#");

			String[] temp_array = new String[receString.length];
			basic_array = new String[receString.length];
			int max = receString.length;
			for (int i = 0; i < max; i++) {
				basic_array[i] = receString[i];
				if (receString[i].length() > 8) {
					temp_array[i] = receString[i].substring(0, 7) + "..";
				} else {
					temp_array[i] = receString[i];
				}
			}
			sortonename_bt.setText(temp_array[19]);
			sorttwoname_bt.setText(temp_array[20]);
			productid_bt.setText("");
			productname_text.setText(temp_array[0]);
			outputdate_bt.setText(temp_array[1]);
			enddate_bt.setText(temp_array[2]);
			definitionname_bt.setText(temp_array[3]);
			factoryname_bt.setText(temp_array[4]);
			productcomponent_bt.setText(temp_array[6]);
			productinstrustions_bt.setText(temp_array[7]);
			productnotice_bt.setText(temp_array[8]);
			price_bt.setText(temp_array[9]);
			scorenum_text.setText(temp_array[10]);
			peopleindex_text.setText(temp_array[11]);
			interestindex_text.setText(temp_array[12]);
			storename_bt.setText(temp_array[15]);
			storelevel_bt.setText(rechange_level(temp_array[16]));
			current_place_bt.setText(temp_array[17]);

			score_bar.setRating(Float.valueOf(temp_array[10]));
			interest_bar.setRating(Float.valueOf(temp_array[12]));
			people_bar.setRating(Float.valueOf(temp_array[11]) / 100);
			real_bar.setRating(Float.valueOf(temp_array[10]));
			myApplication.saved_flag = Boolean.valueOf(receString[18]);
			
			setImageView = new SetImageView(InfoOne_Activity.this);
			setImageView.setview(photoimage_bt, receString[21]);

			if (myApplication.saved_flag) {
				saved_refresh();
			} else {
				unsaved_refresh();
			}
		} catch (Exception e) {
			Log.e("refreshView", e.toString());
		}

	}

	// 对于获取的英文权限改写为中文值
	private String rechange_level(String level_string) {
		String changed_level = "零售商家";

		if (level_string.equals("factory_host"))
			return "生产厂家";
		if (level_string.equals("factory_checker"))
			return "产检机构";
		if (level_string.equals("transfer_checker"))
			return "运输站点";
		if (level_string.equals("bussiness_man"))
			return "零售商家";
		return changed_level;
	}

	private static class MyHandler extends Handler {
		/* 建立弱引用 */
		private WeakReference<InfoOne_Activity> mOuter;

		/* 构造函数 */
		public MyHandler(InfoOne_Activity activity) {
			mOuter = new WeakReference<InfoOne_Activity>(activity);
		}

		public void handleMessage(Message msg) {
			// 防止内存泄露
			InfoOne_Activity outer = mOuter.get();
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
		case GETBASIC_FAILURE:
			try {
				Toast.makeText(InfoOne_Activity.this, returnHeader.send_string,
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Log.e("GETBASIC_FAILURE", e.toString());
			}
			break;
		case GETBASIC_SUCCESS:
			refreshView(returnHeader);
			break;

		// ////////////////
		case GETGROUP_FAILURE:
			try {
				Toast.makeText(InfoOne_Activity.this, returnHeader.send_string,
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Log.e("GETGROUP_FAILURE", e.toString());
			}
			break;
		case GETGROUP_SUCCESS:
			getgrouparray(returnHeader);
			getgrouplist();
			setpopwindow(savebt);
			break;
		// ////////////////
		case ADDGROUP_SUCCESS:
			addrefreshpop();
			break;
		case ADDGROUP_FAILURE:
			try {
				Toast.makeText(InfoOne_Activity.this, returnHeader.send_string,
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Log.e("ADDGROUP_FAILURE", e.toString());
			}
			break;
		// /////////////////////
		case DELEGROUP_SUCCESS:
			myApplication.grouptap_flag = false;
			delete_refresh();
			break;
		case DELEGROUP_FAILURE:
			try {
				Toast.makeText(InfoOne_Activity.this, returnHeader.send_string,
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Log.e("DELEGROUP_FAILURE", e.toString());
			}
			break;
		// /////////////////////
		case SAVEPRODUCT_SUCCESS:
			Toast.makeText(InfoOne_Activity.this, "添加分组成功", Toast.LENGTH_SHORT)
					.show();
			// 关闭弹出框
			popWindow.dismiss();
			saved_refresh();
			break;
		case SAVEPRODUCT_FAILURE:
			try {
				Toast.makeText(InfoOne_Activity.this, returnHeader.send_string,
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Log.e("SAVEPRODUCT_FAILURE", e.toString());
			}
			break;
		// /////////////////////////////
		case SCOREPRODUCT_SUCCESS:
			Toast.makeText(InfoOne_Activity.this, "评分成功", Toast.LENGTH_SHORT)
					.show();
			refresh_myscore();
			break;
		case SCOREPRODUCT_FAILURE:
			try {
				Toast.makeText(InfoOne_Activity.this, returnHeader.send_string,
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Log.e("SAVEPRODUCT_FAILURE", e.toString());
			}
			break;
		// ///////////////////////
		case GETSCORE_SUCCESS:
			refresh_scorepop();
			break;
		case GETSCORE_FAILURE:
			try {
				Toast.makeText(InfoOne_Activity.this, returnHeader.send_string,
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Log.e("GETSCORE_FAILURE", e.toString());
			}
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
				case GETBASIC:
					msg.what = HandleTag.GETBASIC_SUCCESS.ordinal();
					break;
				case GETGROUP:
					msg.what = HandleTag.GETGROUP_SUCCESS.ordinal();
					break;
				case ADDGROUP:
					msg.what = HandleTag.ADDGROUP_SUCCESS.ordinal();
					break;
				case DELEGROUP:
					msg.what = HandleTag.DELEGROUP_SUCCESS.ordinal();
					break;
				case SAVEPRODUCT:
					msg.what = HandleTag.SAVEPRODUCT_SUCCESS.ordinal();
					break;
				case SCOREPRODUCT:
					msg.what = HandleTag.SCOREPRODUCT_SUCCESS.ordinal();
					break;
				case GETSCORE:
					msg.what = HandleTag.GETSCORE_SUCCESS.ordinal();
					break;
				}

			} else {
				switch (type) {
				case GETBASIC:
					msg.what = HandleTag.GETBASIC_FAILURE.ordinal();
					break;
				case GETGROUP:
					msg.what = HandleTag.GETGROUP_FAILURE.ordinal();
					break;
				case ADDGROUP:
					msg.what = HandleTag.ADDGROUP_FAILURE.ordinal();
					break;
				case DELEGROUP:
					msg.what = HandleTag.DELEGROUP_FAILURE.ordinal();
					break;
				case SAVEPRODUCT:
					msg.what = HandleTag.SAVEPRODUCT_FAILURE.ordinal();
					break;
				case SCOREPRODUCT:
					msg.what = HandleTag.SCOREPRODUCT_FAILURE.ordinal();
					break;
				case GETSCORE:
					msg.what = HandleTag.GETSCORE_FAILURE.ordinal();
					break;
				}
			}
			msg.obj = returnHeader;
			myHandler.sendMessage(msg);
		}
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
		try {
			packageHeader.place_long = Double
					.valueOf(myApplication.user_placelong);
			packageHeader.place_lat = Double
					.valueOf(myApplication.user_placelat);
		} catch (Exception e) {
		}

		/* 发送信息（登录操作） */
		Rece_Thread rece_Thread = new Rece_Thread(sslSocket, action_type,
				packageHeader, type);
		rece_Thread.start();
	}

	// 监听是否重刷新操作
	private class ReGetInfo extends Thread {
		public void run() {
			while (true) {
				if (myApplication.search_flag)// 如果需要查询进行显示
				{
					myApplication.search_flag = false;
					refresh_view();
				}
			}
		}
	}

	// 初始化图片
	public void initPicture() {
		imageView1.setBackgroundResource(picture_id[0]);
		imageView2.setBackgroundResource(picture_id[1]);
		imageView3.setBackgroundResource(picture_id[2]);
		imageView4.setBackgroundResource(picture_id[3]);
		viewFlipper1.setDisplayedChild(now);
	}

	// 图片切换按钮控制
	private void generatePageControl(int pos) {
		photo_layout.removeAllViews();
		for (int i = 0; i < pictureCounts; i++) {
			ImageView imageView = new ImageView(this);
			imageView.setPadding(0, 0, 4, 0);
			if (now == i) {
				imageView.setImageResource(R.drawable.progress_go_small);
			} else {
				imageView.setImageResource(R.drawable.progress_bg_small);
			}
			this.photo_layout.addView(imageView);
		}
	}

	// 对图片进行设置
	public boolean setPictureId(int[] newId) {
		boolean flag = false;
		try {
			for (int i = 0; i < newId.length; i++) {
				picture_id[i] = newId[i];
			}

		} catch (Exception e) {

		}
		return flag;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Animation lInAnim = AnimationUtils.loadAnimation(InfoOne_Activity.this,
				R.anim.push_left_in); // 鍚戝乏婊戝姩宸︿晶杩涘叆鐨勬笎鍙樻晥鏋滐紙alpha 0.1 -> 1.0锛�
		Animation lOutAnim = AnimationUtils.loadAnimation(
				InfoOne_Activity.this, R.anim.push_left_out); // 鍚戝乏婊戝姩鍙充晶婊戝嚭鐨勬笎鍙樻晥鏋滐紙alpha
																// 1.0 -> 0.1锛�
		viewFlipper1.setInAnimation(lInAnim);
		viewFlipper1.setOutAnimation(lOutAnim);
		viewFlipper1.showNext();
		now++;
		if (now > pictureCounts - 1) {
			now = 0;
		}
		generatePageControl(now);
		return gestureDetector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e2.getX() - e1.getX() > 120) {
			Animation rInAnim = AnimationUtils.loadAnimation(
					InfoOne_Activity.this, R.anim.push_right_in); // 鍚戝彸婊戝姩宸︿晶杩涘叆鐨勬笎鍙樻晥鏋滐紙alpha
																	// 0.1 ->
																	// 1.0锛�
			Animation rOutAnim = AnimationUtils.loadAnimation(
					InfoOne_Activity.this, R.anim.push_right_out); // 鍚戝彸婊戝姩鍙充晶婊戝嚭鐨勬笎鍙樻晥鏋滐紙alpha
																	// 1.0 ->
																	// 0.1锛�
			viewFlipper1.setInAnimation(rInAnim);
			viewFlipper1.setOutAnimation(rOutAnim);
			viewFlipper1.showPrevious();
			now--;
			if (now < 0) {
				now = pictureCounts - 1;
			}
			generatePageControl(now);
			return true;
		} else if (e2.getX() - e1.getX() < -120) {
			Animation lInAnim = AnimationUtils.loadAnimation(
					InfoOne_Activity.this, R.anim.push_left_in); // 鍚戝乏婊戝姩宸︿晶杩涘叆鐨勬笎鍙樻晥鏋滐紙alpha
																	// 0.1 ->
																	// 1.0锛�
			Animation lOutAnim = AnimationUtils.loadAnimation(
					InfoOne_Activity.this, R.anim.push_left_out); // 鍚戝乏婊戝姩鍙充晶婊戝嚭鐨勬笎鍙樻晥鏋滐紙alpha
																	// 1.0 ->
																	// 0.1锛�
			viewFlipper1.setInAnimation(lInAnim);
			viewFlipper1.setOutAnimation(lOutAnim);
			viewFlipper1.showNext();
			now++;
			if (now > pictureCounts - 1) {
				now = 0;
			}
			generatePageControl(now);
			return true;
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {

	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {

	}

	// 按下事件控制
	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		return false;
	}
}
