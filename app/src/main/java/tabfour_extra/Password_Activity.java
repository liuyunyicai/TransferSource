package tabfour_extra;

import mainview.demo.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Password_Activity extends Activity {

	// 界面参数
	private ImageButton setselfphoto_bt;// 修改个人头像
	private ImageButton setzonebg_bt;// 修改空间背景
	private boolean flagone, flagtwo, flagthree = false;// 三个界面显示状态

	/************** 完善个人资料 ****************/
	private Button btone;// 主按钮
	private LinearLayout btone_view;// 界面
	private Button selfinfo_ok, selfinfo_cancel;// 保存修改，取消按钮
	private TextView self_nickname;// 个人昵称
	private TextView self_level;// 账号等级
	private TextView self_bind;// 绑定手机
	private Button self_realname_bt;
	private EditText self_realname_et;// 真实姓名
	private Button self_email_bt;
	private EditText self_email_et;// 联系邮箱
	private Button self_place_bt;
	private EditText self_place_et;// 通讯地址
	/************** 修改密码 ****************/
	private Button bttwo,// 主按钮
			password_ok,//保存修改
			password_cancel;//取消
	private EditText orinal_password,//原始密码
			input_password,//设置密码
			input_again;//重新输入密码
	private LinearLayout bttwo_view;// 界面
	/************** 修改绑定 ****************/
	private Button btthree;// 主按钮
	private LinearLayout btthree_view;// 界面

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.changeinfo_layout);
		Intent intent = super.getIntent();
		String state = intent.getStringExtra("state");// 获得按钮事件

		// 获取ID
		setselfphoto_bt = (ImageButton) super
				.findViewById(R.id.setselfphoto_bt);
		setzonebg_bt = (ImageButton) super.findViewById(R.id.setzonebg_bt);

		btone = (Button) super.findViewById(R.id.btone);
		btone.setOnClickListener(new BtClick(0));
		btone_view = (LinearLayout) super.findViewById(R.id.btone_view);

		bttwo = (Button) super.findViewById(R.id.bttwo);
		bttwo.setOnClickListener(new BtClick(1));
		password_ok = (Button) super.findViewById(R.id.password_ok);
		bttwo_view = (LinearLayout) super.findViewById(R.id.bttwo_view);

		btthree = (Button) super.findViewById(R.id.btthree);
		btthree.setOnClickListener(new BtClick(2));
		btthree_view = (LinearLayout) super.findViewById(R.id.btthree_view);

		// 三界面ID
		selfinfo_ok = (Button) super.findViewById(R.id.selfinfo_ok);
		selfinfo_cancel = (Button) super.findViewById(R.id.selfinfo_cancel);
		self_nickname = (TextView) super.findViewById(R.id.self_nickname);
		self_level = (TextView) super.findViewById(R.id.self_level);
		self_bind = (TextView) super.findViewById(R.id.self_bind);
		self_realname_bt = (Button) super.findViewById(R.id.self_realname_bt);
		self_realname_et = (EditText) super.findViewById(R.id.self_realname_et);
		self_email_bt = (Button) super.findViewById(R.id.self_email_bt);
		self_email_et = (EditText) super.findViewById(R.id.self_email_et);
		self_place_bt = (Button) super.findViewById(R.id.self_place_bt);
		self_place_et = (EditText) super.findViewById(R.id.self_place_et);

		initview(state);
	}

	// 初始化界面
	private void initview(String state) {
		if (state.equals("0")) {
			btone_view.setVisibility(View.VISIBLE);
			bttwo_view.setVisibility(View.GONE);
			btthree_view.setVisibility(View.GONE);
			flagone = true;
		} else if (state.equals("1")) {
			btone_view.setVisibility(View.GONE);
			bttwo_view.setVisibility(View.VISIBLE);
			btthree_view.setVisibility(View.GONE);
			flagtwo = true;
		} else if (state.equals("2")) {
			btone_view.setVisibility(View.GONE);
			bttwo_view.setVisibility(View.GONE);
			btthree_view.setVisibility(View.VISIBLE);
			flagthree = true;
		}
	}

	// 三个界面按钮
	private class BtClick implements OnClickListener {
		private int type;

		public BtClick(int type) {
			this.type = type;
		}

		public void onClick(View v) {
			switch (type) {
				case 0:
					if (flagone) {
						flagone = false;
						btone_view.setVisibility(View.GONE);
					} else {
						flagone = true;
						btone_view.setVisibility(View.VISIBLE);
					}
					break;
				case 1:
					if (flagtwo) {
						flagtwo = false;
						bttwo_view.setVisibility(View.GONE);
					} else {
						flagtwo = true;
						bttwo_view.setVisibility(View.VISIBLE);
					}
					break;
				case 2:
					if (flagthree) {
						flagthree = false;
						btthree_view.setVisibility(View.GONE);
					} else {
						flagthree = true;
						btthree_view.setVisibility(View.VISIBLE);
					}
					break;

			}
		}
	}
}
