package tabthree_extra;

import mainview.demo.DemoApplication;
import mainview.demo.R;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class StoreMap_Activity extends Activity {
	// 地图控制
	private MapView mMapView = null;
	private MapController mMapController = null;
	// 图层
	private MyOverlay mOverlay = null;
	private PopupOverlay pop = null;
	private OverlayItem mCurItem = null;
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
	private DemoApplication myApplication;


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

		mMapView = (MapView) super.findViewById(R.id.bmapView);

		mMapController = mMapView.getController();
		mMapController.enableClick(true);
		mMapController.setZoom(20);
		mMapView.setBuiltInZoomControls(true);

		try {
			mLon2 = myApplication.storelong;
			mLat2 = myApplication.storelat;
			mLon1 = Double.valueOf(myApplication.user_placelong);
			mLat1 = Double.valueOf(myApplication.user_placelat);

		} catch (Exception e) {
			Log.e("storemap", e.toString());
		}
		initoverlay();
	}

	// 显示坐标点
	public void initoverlay() {
		mOverlay = new MyOverlay(getResources().getDrawable(
				R.drawable.icon_marka), mMapView);
		GeoPoint p1 = new GeoPoint((int) (mLat1 * 1E6), (int) (mLon1 * 1E6));
		OverlayItem item1 = new OverlayItem(p1, "您的当前位置", "");
		item1.setMarker(getResources().getDrawable(R.drawable.nav_turn_via_1));
		mOverlay.addItem(item1);

		GeoPoint p2 = new GeoPoint((int) (mLat2 * 1E6), (int) (mLon2 * 1E6));
		OverlayItem item2 = new OverlayItem(p2, "商店位置", "");
		item2.setMarker(getResources().getDrawable(R.drawable.store_mark));
		mOverlay.addItem(item2);

		mMapController.setCenter(p2);

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
				} else if (index == 2) {
					pop.hidePop();
				}
			}
		};
		pop = new PopupOverlay(mMapView, popListener);

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
	public class MyOverlay extends ItemizedOverlay {

		public MyOverlay(Drawable defaultMarker, MapView mapView) {
			super(defaultMarker, mapView);
		}

		@Override
		public boolean onTap(int index) {
			OverlayItem item = getItem(index);
			mCurItem = item;
			// 记录当前点击位置

			popupText.setText(getItem(index).getTitle());
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
