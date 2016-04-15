package mapview;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

import listadapter.CurrentStateList_Adapter;
import listadapter.TimeStepList_Adapter;
import mainview.demo.DemoApplication;
import mainview.demo.R;
import ssl.Action_Type;
import ssl.PackageHeader;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.Geometry;
import com.baidu.mapapi.map.Graphic;
import com.baidu.mapapi.map.GraphicsOverlay;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.map.Symbol;
import com.baidu.platform.comapi.basestruct.GeoPoint;

/**
 * 演示MapView的基本用法
 */
@SuppressLint({ "SimpleDateFormat", "HandlerLeak" })
public class BaseMapDemo extends Activity {

	// 地图控制
	private MapView mMapView = null;
	private MapController mMapController = null;
	// 图层
	private MyOverlay mOverlay = null;
	private PopupOverlay pop = null;
	// 一些View
	private TextView popupText = null;
	private View viewCache = null;
	private View popupInfo = null;
	private View popupLeft = null;
	private View popupRight = null;
	private Button button = null;
	// 设置 一些数据
	double mLon1 = 114.407760;// 武汉
	double mLat1 = 30.51415;
	double mLon2 = 116.4075260000;// 北京
	double mLat2 = 39.9040300000;
	double mLon3 = 117.2009830000;// 天津
	double mLat3 = 39.0841580000;
	double mLon4 = 116.8388350000;// 沧州
	double mLat4 = 38.3044770000;
	double mLon5 = 116.3574650000;// 德州
	double mLat5 = 37.4340930000;
	double mLon6 = 117.1204970000;// 济南
	double mLat6 = 36.6509970000;
	double mLon7 = 113.6253680000;// 郑州
	double mLat7 = 34.7466000000;
	double mLon8 = 114.4077600000;// 武汉
	double mLat8 = 30.5141500000;

	// 各状态标志
	public final int PRODUCING = 20;
	public final int CHECKING = 21;
	public final int OUTPUT = 22;
	public final int BUS_TRANSFER = 23;
	public final int TRAIN_TRANSFER = 24;
	public final int PLANE_TRANSFER = 25;
	public final int SHIP_TRANSFER = 26;
	public final int STORE = 27;

	double[][] mLNum = { { mLon2, mLat2, 0 }, { mLon3, mLat3, BUS_TRANSFER },
			{ mLon4, mLat4, TRAIN_TRANSFER }, { mLon5, mLat5, BUS_TRANSFER },
			{ mLon6, mLat6, TRAIN_TRANSFER }, { mLon7, mLat7, PLANE_TRANSFER },
			{ mLon8, mLat8, PLANE_TRANSFER } };
	// 返回数据
	String[][] rece_data = null;
	String[] receString = new String[2];

	private int current_popindex = 0;

	private DemoApplication myApplication;
	// 消息类型
	private enum HandleTag {
		GETSTATE_SUCCESS, GETSTATE_FAILURE,
	}
	private MyHandler myHandler;

	// 网络通信需要的参数
	// Share轻量存储模块
	private SharedPreferences share;// 轻量存储

	private SSLSocket sslSocket;
	private PackageHeader packageHeader;

	private PopupWindow popWindow;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myApplication = (DemoApplication) super.getApplication();
		DemoApplication app = (DemoApplication) this.getApplication();
		if (app.mBMapManager == null) {
			app.mBMapManager = new BMapManager(getApplicationContext());
			app.mBMapManager.init(DemoApplication.strKey,
					new DemoApplication.MyGeneralListener());
		}
		setContentView(R.layout.mapview_layout);

		// 轻量存储模块
		sslSocket = myApplication.getSSlSocket();
		share = getSharedPreferences("transfer", Activity.MODE_PRIVATE);
		myHandler = new MyHandler(this);

		init_package();
		init();
	}

	/*界面初始化*/
	private void init()
	{
		mMapView = (MapView) super.findViewById(R.id.bmapView);

		mMapController = mMapView.getController();
		mMapController.enableClick(true);
		mMapController.setZoom(7);
		mMapView.setBuiltInZoomControls(true);
		try {
			/*地理位置初始化*/
			mLat1 = Double.valueOf(myApplication.user_placelat);
			mLon1 = Double.valueOf(myApplication.user_placelong);
		} catch (Exception e) {
			mLat1 = mLat8;
			mLon1 = mLon8;
		}

		GeoPoint p = new GeoPoint((int) (mLat1 * 1E6), (int) (mLon1 * 1E6));
		mMapController.setCenter(p);

		try {
			/***重绘地图********/
			if (myApplication.rece_data != null) {
				if (!myApplication.rece_data[0][0].equals("")) {
					setrece_data(myApplication.rece_data);
					addMap();
				}
			}
		} catch (Exception e) {

		}

		ReGetInfo regetInfo = new ReGetInfo();
		regetInfo.start();
	}
	/**
	 * package初始化
	 **/
	private void init_package()
	{
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

	// 刷新地图界面
	private void refreshView() {
		List<String> lists = new ArrayList<String>();
		lists.add(myApplication.tag_id);
		lists.add(myApplication.sort_id_one);
		lists.add(myApplication.sort_id_two);
		lists.add(myApplication.definition_id);
		lists.add(myApplication.product_id);
		
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
				Action_Type.CUSTOMER_QUERY_TRANSFERSTATE, packageHeader);
		rece_Thread.start();
	}

	// 添加地图刷新
	private void addMap() {

		// 绘制折线图
		GraphicsOverlay graphicsOverlay = new GraphicsOverlay(mMapView);
		mMapView.getOverlays().add(graphicsOverlay);

		for (int i = 1; i < rece_data.length; i++) {
			try {
				// 绘制折线
				graphicsOverlay.setData(drawLine());
			} catch (Exception e) {
				Log.e("map", e.toString());
			}

		}
		// 添加位置点
		mOverlay = new MyOverlay(getResources().getDrawable(
				R.drawable.icon_marka), mMapView);
		addPoint(Double.valueOf(rece_data[0][3]),
				Double.valueOf(rece_data[0][2]),
				getpointlevel(rece_data[0][8], 0));
		for (int i = 1; i < rece_data.length; i++) {
			// 添加节点
			addPoint(Double.valueOf(rece_data[i][3]),
					Double.valueOf(rece_data[i][2]),
					getpointlevel(rece_data[i][8], i));

		}

		for (int i = 1; i < rece_data.length; i++) {
			// 添加中间状态点
			addPoint(
					(double) ((Double.valueOf(rece_data[i - 1][3]) + Double
							.valueOf(rece_data[i][3])) / 2.00),
					(double) ((Double.valueOf(rece_data[i - 1][2]) + Double
							.valueOf(rece_data[i][2])) / 2.00),
					getstate(rece_data[i - 1][0]));
		}
		mMapView.getOverlays().add(mOverlay);
		mMapView.refresh();

		viewCache = getLayoutInflater()
				.inflate(R.layout.custom_text_view, null);
		popupInfo = (View) viewCache.findViewById(R.id.popinfo);
		popupLeft = (View) viewCache.findViewById(R.id.popleft);
		popupRight = (View) viewCache.findViewById(R.id.popright);
		popupText = (TextView) viewCache.findViewById(R.id.textcache);

		button = new Button(this);
		button.setBackgroundResource(R.drawable.popup);

		/**
		 * 创建一个popupoverlay
		 */
		// 可用于添加细节
		PopupClickListener popListener = new PopupClickListener() {
			@Override
			public void onClickedPopup(int index) {
				if (index == 0) {
					pop.hidePop();
					show_timestep();
				} else if (index == 2) {
					pop.hidePop();
					current_state();
				}
			}
		};
		pop = new PopupOverlay(mMapView, popListener);
	}

	// 对返回的状态信息进行解读
	private int getstate(String state_string) {
		int the_state = 0;
		if (state_string.equals("生产中")) {
			return PRODUCING;
		}
		if (state_string.equals("产检中")) {
			return CHECKING;
		}
		if (state_string.equals("出厂")) {
			return OUTPUT;
		}
		if (state_string.equals("货车运输中")) {
			return BUS_TRANSFER;
		}
		if (state_string.equals("火车运输中")) {
			return TRAIN_TRANSFER;
		}
		if (state_string.equals("飞机运输中")) {
			return PLANE_TRANSFER;
		}
		if (state_string.equals("生产中")) {
			return SHIP_TRANSFER;
		}
		if (state_string.equals("在售中")) {
			return STORE;
		}

		return the_state;
	}

	// 对返回的厂家权限信息进行解读
	private int getpointlevel(String levelstring, int i) {
		int levelint = 0;

		if (levelstring.equals("工厂负责人")) {
			return PRODUCING;
		}
		if (levelstring.equals("产检负责人")) {
			return CHECKING;
		}
		if (levelstring.equals("运输负责人")) {
			return i + 1;
		}
		if (levelstring.equals("销售商")) {
			return STORE;
		}
		return levelint;
	}

	// 将获得物流信息转化存储到recedata数组中
	private void getdata(PackageHeader returnHeader) {
		final int item_length = 10;
		receString = returnHeader.send_string.split("#");
		// 开辟存储空间
		rece_data = new String[receString.length / item_length][item_length];

		for (int i = 0; i < receString.length / item_length; i++) {
			for (int j = 0; j < item_length; j++) {
				rece_data[i][j] = receString[item_length * i + j];
			}
		}
	}

	// 监听是否重刷新操作
	private class ReGetInfo extends Thread {
		public void run() {
			while (true) {
				if (myApplication.state_flag)// 如果需要查询进行显示
				{
					myApplication.state_flag = false;
					try {
						mMapView.getOverlays().clear();
						mMapView.refresh();
					} catch (Exception e) {
					}
					refreshView();
				}
			}
		}
	}

	private static class MyHandler extends Handler
	{
		/* 建立弱引用 */
		private WeakReference<BaseMapDemo> mOuter;
		/* 构造函数 */
		public MyHandler(BaseMapDemo activity) {
			mOuter = new WeakReference<BaseMapDemo>(activity);
		}

		public void handleMessage(Message msg) {
			// 防止内存泄露
			BaseMapDemo outer = mOuter.get();
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
			// 操作成功的操作
			case GETSTATE_SUCCESS:
				getdata(returnHeader);
				addMap();
				if (rece_data != null) {
					if (!rece_data[0][0].equals(""))
						myApplication.setrece_data(rece_data);
				}
				break;
			// 操作失败的应答
			case GETSTATE_FAILURE:
				Toast.makeText(BaseMapDemo.this, "获取物流信息失败", Toast.LENGTH_SHORT)
						.show();
				break;
		}
	}


	// 获得物流信息的线程函数
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
				msg.what = HandleTag.GETSTATE_SUCCESS.ordinal();
			} else {
				msg.what = HandleTag.GETSTATE_FAILURE.ordinal();
			}
			msg.obj = returnHeader;
			myHandler.sendMessage(msg);
		}
	}

	// 实现监听器
	/*
	 * public class MyLocationListenner implements BDLocationListener {
	 * 
	 * @Override public void onReceiveLocation(BDLocation location) { if
	 * (location == null) return ; locData.latitude = location.getLatitude();
	 * locData.longitude = location.getLongitude(); //如果不显示定位精度圈，将accuracy赋值为0即可
	 * locData.accuracy = location.getRadius(); locData.direction =
	 * location.getDerect(); //更新定位数据 mLocationOverlay.setData(locData);
	 * //更新图层数据执行刷新后生效 mMapView.refresh(); //是手动触发请求或首次定位时，移动到定位点
	 * 
	 * mMapController.animateTo(new GeoPoint((int)(locData.latitude* 1e6),
	 * (int)(locData.longitude * 1e6))); //
	 * mLocationOverlay.setLocationMode(LocationMode.NORMAL); //停止跟踪
	 * mLocClient.stop(); }
	 * 
	 * public void onReceivePoi(BDLocation poiLocation) { if (poiLocation ==
	 * null){ return ; } } }
	 */

	// 添加点
	private void addPoint(double mLat, double mLon, int n) {
		OverlayItem item1 = null;
		GeoPoint p1 = new GeoPoint((int) (mLat * 1E6), (int) (mLon * 1E6));
		if ((n < PRODUCING) && (n != 0)) {
			try {
				item1 = new OverlayItem(p1, rece_data[n - 1][1], "");
			} catch (Exception e) {
				Log.e("addPoint", e.toString());
			}

		}
		switch (n) {
			case 0:
				item1 = new OverlayItem(p1, "起点", "");
				item1.setMarker(getResources().getDrawable(R.drawable.icon_geo));
				break;
			case 1:
				item1.setMarker(getResources()
						.getDrawable(R.drawable.icon_gcoding1));
				break;
			case 2:
				item1.setMarker(getResources()
						.getDrawable(R.drawable.icon_gcoding2));
				break;
			case 3:
				item1.setMarker(getResources()
						.getDrawable(R.drawable.icon_gcoding3));
				break;
			case 4:
				item1.setMarker(getResources()
						.getDrawable(R.drawable.icon_gcoding4));
				break;
			case 5:
				item1.setMarker(getResources()
						.getDrawable(R.drawable.icon_gcoding5));
				break;
			case 6:
				item1.setMarker(getResources()
						.getDrawable(R.drawable.icon_gcoding6));
				break;
			case 7:
				item1.setMarker(getResources()
						.getDrawable(R.drawable.icon_gcoding7));
				break;
			case 8:
				item1.setMarker(getResources()
						.getDrawable(R.drawable.icon_gcoding8));
				break;
			case 9:
				item1.setMarker(getResources()
						.getDrawable(R.drawable.icon_gcoding9));
				break;
			case 10:
				item1.setMarker(getResources().getDrawable(
						R.drawable.icon_gcoding10));
				break;
			case 11:
				item1.setMarker(getResources().getDrawable(
						R.drawable.icon_gcoding11));
				break;
			case 12:
				item1.setMarker(getResources().getDrawable(
						R.drawable.icon_gcoding12));
				break;
			case 13:
				item1.setMarker(getResources().getDrawable(
						R.drawable.icon_gcoding13));
				break;
			case 14:
				item1.setMarker(getResources().getDrawable(
						R.drawable.icon_gcoding14));
				break;

			case PRODUCING:
				item1 = new OverlayItem(p1, "生产工厂", "");
				item1.setMarker(getResources()
						.getDrawable(R.drawable.industry_mark));
				break;
			case CHECKING:
				item1 = new OverlayItem(p1, "产检机构", "");
				item1.setMarker(getResources().getDrawable(R.drawable.line_mark));
				break;
			case OUTPUT:
				item1 = new OverlayItem(p1, "产品出厂", "");
				item1.setMarker(getResources()
						.getDrawable(R.drawable.industry_mark));
				break;
			case STORE:
				item1 = new OverlayItem(p1, "销售商店", "");
				item1.setMarker(getResources().getDrawable(R.drawable.store_mark));
				break;
			case BUS_TRANSFER:
				item1 = new OverlayItem(p1, "货车运输中..", "");
				item1.setMarker(getResources().getDrawable(R.drawable.trunk_mark));
				break;
			case TRAIN_TRANSFER:
				item1 = new OverlayItem(p1, "火车运输中..", "");
				item1.setMarker(getResources().getDrawable(R.drawable.traincon));
				break;
			case PLANE_TRANSFER:
				item1 = new OverlayItem(p1, "飞机运输中..", "");
				item1.setMarker(getResources().getDrawable(R.drawable.plane_mark));
				break;
			case SHIP_TRANSFER:
				item1 = new OverlayItem(p1, "轮船运输中..", "");
				item1.setMarker(getResources().getDrawable(R.drawable.ship_marker));
				break;
			default:
				break;
		}

		mOverlay.addItem(item1);
	}

	// 定义注释图层
	public void initOverlay() {
		mOverlay = new MyOverlay(getResources().getDrawable(
				R.drawable.icon_marka), mMapView);

		mMapView.getOverlays().add(mOverlay);
		mMapView.refresh();

		// 在地图中添加标注
		viewCache = getLayoutInflater()
				.inflate(R.layout.custom_text_view, null);
		popupInfo = (View) viewCache.findViewById(R.id.popinfo);
		popupLeft = (View) viewCache.findViewById(R.id.popleft);
		popupRight = (View) viewCache.findViewById(R.id.popright);
		popupText = (TextView) viewCache.findViewById(R.id.textcache);

		button = new Button(this);
		button.setBackgroundResource(R.drawable.popup);

		/**
		 * 创建一个popupoverlay
		 */
		// 可用于添加细节
		PopupClickListener popListener = new PopupClickListener() {
			@Override
			public void onClickedPopup(int index) {
				if (index == 0) {
					// 更新item位置
					pop.hidePop();
					mMapView.refresh();
					show_timestep();
				} else if (index == 2) {
					// 更新item位置
					pop.hidePop();
					mMapView.refresh();
					current_state();
				}
			}
		};
		pop = new PopupOverlay(mMapView, popListener);

	}

	// 显示时间进度函数
	@SuppressWarnings("deprecation")
	private void show_timestep() {
		try {
			LayoutInflater factory = LayoutInflater.from(BaseMapDemo.this);
			final View myview = factory.inflate(R.layout.timestep_layout, null);
			ListView timeStep_List = (ListView) myview
					.findViewById(R.id.timestep_listview);
			TimeStepList_Adapter adapter = new TimeStepList_Adapter(
					BaseMapDemo.this, rece_data);
			timeStep_List.setAdapter(adapter);
			timeStep_List.setDivider(null);
			// 设置弹出窗口
			popWindow = new PopupWindow(myview,
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			// 使其聚集
			popWindow.setFocusable(true);
			// 设置允许在外点击消失
			popWindow.setBackgroundDrawable(new BitmapDrawable());
			popWindow.setOutsideTouchable(true);
			popWindow.showAtLocation(mMapView, Gravity.CENTER, 0, 0);
		} catch (Exception e) {
			Log.e("dialog", e.toString());
		}

	}

	// 显示当前时刻状态
	@SuppressWarnings("deprecation")
	private void current_state() {
		try {
			LayoutInflater factory = LayoutInflater.from(BaseMapDemo.this);
			final View myview = factory.inflate(R.layout.timestep_layout, null);
			ListView timeStep_List = (ListView) myview
					.findViewById(R.id.timestep_listview);
			CurrentStateList_Adapter adapter = new CurrentStateList_Adapter(
					BaseMapDemo.this, rece_data[current_popindex]);
			timeStep_List.setAdapter(adapter);
			timeStep_List.setDivider(null);
			TextView timestep_header = (TextView) myview
					.findViewById(R.id.timestep_header);
			timestep_header.setText("状态信息表");

			// 设置弹出窗口
			popWindow = new PopupWindow(myview,
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			// 使其聚集
			popWindow.setFocusable(true);
			// 设置允许在外点击消失
			popWindow.setBackgroundDrawable(new BitmapDrawable());
			popWindow.setOutsideTouchable(true);
			popWindow.showAtLocation(mMapView, Gravity.CENTER, 0, 0);
		} catch (Exception e) {
			Log.e("dialog", e.toString());
		}
	}

	/**
	 * 绘制折线，该折线状态随地图状态变化
	 *
	 * @return 折线对象
	 */
	public Graphic drawLine() {
		int lat;
		int lon;
		GeoPoint pt1;
		List<GeoPoint> list = new ArrayList<GeoPoint>();

		for (int i = 0; i < rece_data.length; i++) {
			for (int j = 0; j < rece_data[i].length; j++) {
				lat = (int) (Double.valueOf(rece_data[i][3]) * 1E6);
				lon = (int) (Double.valueOf(rece_data[i][2]) * 1E6);
				pt1 = new GeoPoint(lat, lon);
				list.add(pt1);
			}
		}
		// 构建线
		Geometry lineGeometry = new Geometry();
		// 设定折线点坐标
		GeoPoint[] linePoints = new GeoPoint[list.size()];
		for (int i = 0; i < list.size(); i++) {
			linePoints[i] = list.get(i);
		}

		lineGeometry.setPolyLine(linePoints);
		// 设定样式
		Symbol lineSymbol = new Symbol();
		Symbol.Color lineColor = lineSymbol.new Color();
		lineColor.red = 255;
		lineColor.green = 0;
		lineColor.blue = 0;
		lineColor.alpha = 255;
		lineSymbol.setLineSymbol(lineColor, 4);
		// 生成Graphic对象
		Graphic lineGraphic = new Graphic(lineGeometry, lineSymbol);
		return lineGraphic;
	}

	private void setrece_data(String[][] rece_data) {
		this.rece_data = new String[rece_data.length][rece_data[0].length];
		for (int i = 0; i < rece_data.length; i++) {
			for (int j = 0; j < rece_data[i].length; j++) {
				this.rece_data[i][j] = rece_data[i][j];
			}
		}
	}

	@Override
	protected void onPause() {
		/**
		 * MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		 */
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		/**
		 * MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		 */
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		/**
		 * MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		 */
		mMapView.destroy();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mMapView.onRestoreInstanceState(savedInstanceState);
	}

	// 自定义图层
	@SuppressWarnings("rawtypes")
	public class MyOverlay extends ItemizedOverlay {

		public MyOverlay(Drawable defaultMarker, MapView mapView) {
			super(defaultMarker, mapView);
		}

		@Override
		public boolean onTap(int index) {
			OverlayItem item = getItem(index);
			// 记录当前点击位置

			popupText.setText(getItem(index).getTitle());
			if (index < rece_data.length) {
				TextView textview = (TextView) popupLeft
						.findViewById(R.id.popleft);
				textview.setText(rece_data[index][4]);
			}
			Bitmap[] bitMaps = { BMapUtil.getBitmapFromView(popupLeft),
					BMapUtil.getBitmapFromView(popupInfo),
					BMapUtil.getBitmapFromView(popupRight) };

			pop.showPopup(bitMaps, item.getPoint(), 32);
			current_popindex = index;
			return true;
		}

		@Override
		public boolean onTap(GeoPoint pt, MapView mMapView) {
			if (pop != null) {
				pop.hidePop();
				mMapView.removeView(button);
			}
			return false;
		}

	}

}
