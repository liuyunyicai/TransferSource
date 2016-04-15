package listadapter;

import java.util.ArrayList;
import java.util.List;

import mainview.demo.DemoApplication;
import mainview.demo.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

//收藏分组List适配器
public class SaveGroup_Adapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<String> group_list = new ArrayList<String>();
	private DemoApplication myApplication;

	// 构造函数
	public SaveGroup_Adapter(Context context, List<String> group_list,
							 DemoApplication myApplication) {
		this.group_list = group_list;
		inflater = LayoutInflater.from(context);
		this.myApplication = myApplication;
	}

	public int getCount() {

		if (group_list != null) {
			return group_list.size();
		}
		return 0;
	}

	public String getItem(int pos) {
		if (group_list != null) {
			return group_list.get(pos);
		}
		return "";
	}

	public long getItemId(int pos) {
		return pos;
	}

	public View getView(int pos, View convertView, ViewGroup parent) {

		final String item = getItem(pos);
		convertView = inflater.inflate(R.layout.savegrouplist_layout, null);
		final Button tapgroup_bt = (Button) convertView
				.findViewById(R.id.tapgroup_bt);
		tapgroup_bt.setText(item);
		// 设置点赞按钮响应
		OnClickListener listener = new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				if (!myApplication.grouptap_flag) {
					myApplication.grouptap_flag = true;
					myApplication.grouptap_name = item;
					tapgroup_bt.setBackgroundDrawable(v.getResources()
							.getDrawable(R.drawable.group_tap_pressed));
				} else {
					if (myApplication.grouptap_name.equals(item)) {
						myApplication.grouptap_flag = false;
						myApplication.grouptap_name = "";
						tapgroup_bt.setBackgroundDrawable(v.getResources()
								.getDrawable(R.drawable.grouptap));
					}
				}
			}
		};
		tapgroup_bt.setOnClickListener(listener);

		return convertView;
	}



}
