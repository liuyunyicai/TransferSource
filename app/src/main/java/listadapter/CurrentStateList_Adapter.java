package listadapter;

import mainview.demo.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class CurrentStateList_Adapter extends BaseAdapter {

	private LayoutInflater inflater;
	private String[] rece_data = null;
	private String[] headerstring = { "当前状态:", "当前地址:", "经度位置:", "纬度位置",
			"当前时刻:", "当前用户:", "用户实名:","联系电话:", "用户权限:", "用户操作:" };

	// 构造函数
	public CurrentStateList_Adapter(Context context, String[] rece_data) {
		inflater = LayoutInflater.from(context);
		this.rece_data = rece_data;
	}

	@Override
	public int getCount() {
		if (rece_data != null) {
			return rece_data.length;
		}
		return 0;
	}

	@Override
	public TimeStepItem getItem(int position) {
		if (rece_data != null) {
			TimeStepItem timeStepItem = new TimeStepItem(
					headerstring[position], rece_data[position]);
			return timeStepItem;
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final TimeStepItem timeStepItem = getItem(position);
		convertView = inflater.inflate(R.layout.timesteplist_layout, null);
		final TextView timestep_time = (TextView) convertView
				.findViewById(R.id.timestep_time);
		final TextView timestep_place = (TextView) convertView
				.findViewById(R.id.timestep_place);
		final ImageButton timestep_button = (ImageButton) convertView
				.findViewById(R.id.timestep_button);

		timestep_time.setText(timeStepItem.timestring);
		timestep_place.setText(timeStepItem.placestring);

		return convertView;
	}

	// 时间点函数
	private class TimeStepItem {
		public String timestring;
		public String placestring;

		public TimeStepItem(String timestring, String placestring) {
			this.timestring = timestring;
			this.placestring = placestring;
		}
	}

}
