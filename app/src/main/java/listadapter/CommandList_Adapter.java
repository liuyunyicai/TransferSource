package listadapter;

import java.util.List;

import mainview.demo.DemoApplication;
import mainview.demo.R;
import product_info.CommandItem;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CommandList_Adapter extends BaseAdapter {

	private List<CommandItem> lists;
	private Context context;
	private LayoutInflater inflater;
	private Bitmap bitmap = null;
	private DemoApplication myApplication;

	public CommandList_Adapter(Context context, List<CommandItem> lists,
							   DemoApplication myApplication) {
		this.lists = lists;
		this.context = context;
		this.myApplication = myApplication;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		if (lists != null) {
			return lists.size();
		}
		return 0;
	}

	@Override
	public CommandItem getItem(int position) {
		if (lists != null && lists.size() != 0) {
			return lists.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// 获取数据
		final CommandItem commandItem = getItem(position);
		final Flag flag = new Flag();
		flag.tap_flag = false;

		convertView = inflater.inflate(R.layout.commentlist_layout, null);
		final TextView name_list = (TextView) convertView
				.findViewById(R.id.name_list);
		final ImageView vipicon = (ImageView) convertView
				.findViewById(R.id.vipicon);
		final ImageView level1icon = (ImageView) convertView
				.findViewById(R.id.level1icon);
		final ImageView level2icon = (ImageView) convertView
				.findViewById(R.id.level2icon);
		final ImageView level3icon = (ImageView) convertView
				.findViewById(R.id.level3icon);
		final TextView time_text = (TextView) convertView
				.findViewById(R.id.time_text);
		final TextView command_text = (TextView) convertView
				.findViewById(R.id.command_text);
		final TextView zannum_text = (TextView) convertView
				.findViewById(R.id.zannum_text);
		final ImageButton dianzan_bt = (ImageButton) convertView
				.findViewById(R.id.dianzan_bt);

		// 数据显示
		name_list.setText(commandItem.user_name);
		// 显示等级图标
		if (reutrnLevel(commandItem.user_level) == 0) {
			level1icon.setBackgroundDrawable(convertView.getResources()
					.getDrawable(R.drawable.common_icon));
			level2icon.setBackgroundDrawable(convertView.getResources()
					.getDrawable(R.drawable.common_icon));
			level3icon.setBackgroundDrawable(convertView.getResources()
					.getDrawable(R.drawable.common_icon));
		} else if (reutrnLevel(commandItem.user_level) == 1) {
			level2icon.setVisibility(View.VISIBLE);
			level3icon.setVisibility(View.VISIBLE);
		} else if (reutrnLevel(commandItem.user_level) == 2) {
			level3icon.setVisibility(View.VISIBLE);
		} else if (reutrnLevel(commandItem.user_level) == 3) {

		} else if (reutrnLevel(commandItem.user_level) == 4) {
			vipicon.setBackgroundDrawable(convertView.getResources()
					.getDrawable(R.drawable.vip_icon_pressed));
		}
		time_text.setText(commandItem.command_time.substring(0, 16));
		command_text.setText(commandItem.command_text);
		zannum_text.setText("赞(" + commandItem.zan_num + ")");
		// 设置点赞按钮响应
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (myApplication.anonymity_flag) {
					Toast.makeText(context, "对不起，您需要先登录", Toast.LENGTH_SHORT)
							.show();
				} else {
					if (!flag.tap_flag) {
						myApplication.commentItem = commandItem;
						myApplication.dianzan_flag = true;
						dianzan_bt.setBackgroundDrawable(v.getResources()
								.getDrawable(R.drawable.dianzan_bt_pressed));
						// 向数据库传递信息
						int num = Integer.parseInt(commandItem.zan_num);
						num++;
						commandItem.zan_num = String.valueOf(num);
						zannum_text.setText("赞(" + String.valueOf(num) + ")");
						CommandList_Adapter.this.notifyDataSetChanged();
						flag.tap_flag = true;
					} else {
						myApplication.commentItem = commandItem;
						myApplication.cancelzan_flag = true;
						dianzan_bt.setBackgroundDrawable(v.getResources()
								.getDrawable(R.drawable.dianzan_bt));
						// 向数据库传递信息
						int num = Integer.parseInt(commandItem.zan_num);
						num--;
						commandItem.zan_num = String.valueOf(num);
						zannum_text.setText("赞(" + String.valueOf(num) + ")");
						CommandList_Adapter.this.notifyDataSetChanged();
						flag.tap_flag = false;
					}
				}

			}
		};

		// 添加按钮响应
		dianzan_bt.setOnClickListener(listener);

		return convertView;
	}

	// 判断权限
	private int reutrnLevel(String level_string) {
		int level = 0;
		if (level_string.equals("vip")) {
			level = 4;
		} else if (level_string.equals("common")) {
			level = 0;
		} else if (level_string.equals("special_one")) {
			level = 1;
		} else if (level_string.equals("special_two")) {
			level = 2;
		} else if (level_string.equals("special_three")) {
			level = 3;
		}

		return level;
	}

	private class Flag {
		public boolean tap_flag;
	}

}
