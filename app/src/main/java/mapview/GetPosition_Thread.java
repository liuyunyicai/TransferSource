package mapview;

import java.text.DecimalFormat;

import mainview.demo.DemoApplication;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

//获得用户当前地址线程
public class GetPosition_Thread extends Thread {
	private DemoApplication myApplication;
	private LocationManager locationManager = null;// 位置管理
	private double place_long, place_lat;// 经度纬度
	private String place_long_string, place_lat_string;

	public GetPosition_Thread(DemoApplication myApplication,
							  LocationManager locationManager, Context context) {
		this.myApplication = myApplication;
		this.locationManager = locationManager;
	}

	public void run() {
		try {
			Looper.prepare();
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 1,
					new LocationListenerImpl());
			Location location = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				place_long = location.getLongitude();
				place_lat = location.getLatitude();
				DecimalFormat df = new DecimalFormat("0.000000");
				place_long_string = df.format(place_long);
				place_lat_string = df.format(place_lat);
				myApplication.user_placelat = place_lat_string;
				myApplication.user_placelong = place_long_string;
			}
		} catch (Exception e) {
			Log.e("posotion", e.toString());
		}

	}

	private class LocationListenerImpl implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			if (location != null) {
				place_long = location.getLongitude();
				place_lat = location.getLatitude();
				DecimalFormat df = new DecimalFormat("0.000000 ");
				place_long_string = df.format(place_long);
				place_lat_string = df.format(place_lat);
				myApplication.user_placelat = place_lat_string;
				myApplication.user_placelong = place_long_string;
			}

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	}
}
