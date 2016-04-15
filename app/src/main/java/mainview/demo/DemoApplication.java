package mainview.demo;

import javax.net.ssl.SSLSocket;

import product_info.CommandItem;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;

public class DemoApplication extends Application {

	private static DemoApplication mInstance = null;
	public boolean m_bKeyRight = true;
	public BMapManager mBMapManager = null;

	public static final String strKey = "NHASC0UiqnTOZT3XaK2k07yQ";
	private boolean already_connect = false;// 判断是否已经连接
	private SSLSocket sslSocket;
	// 查询所需要的信息
	public String tag_id, sort_id_one, sort_id_two, definition_id, product_id;
	public String user_place, user_placelong, user_placelat;
	// 已进行标签扫描操作标志
	public boolean scaned_falg = false;
	// 该产品是否已被该添加收藏
	public boolean saved_flag = false;
	// 查询标志，表示是否进行查询状态
	public boolean search_flag;
	// 是否进行状态查询
	public boolean state_flag = false;
	// 查询商店标志位
	public boolean store_flag = false;
	// 查看评论操作
	public boolean comment_flag = false;
	public int comment_position = 0;

	// 获取相近产品参数
	public int current_num = 0;// 当前请求次数
	// 点赞操作相关信息
	public CommandItem commentItem = null;
	public boolean dianzan_flag = false;// 点赞标志
	public boolean cancelzan_flag = false;
	// 暂存地图信息
	public String[][] rece_data = null;

	/************ 添加收藏分组有关参数 ***********/
	public boolean grouptap_flag = false;
	public String grouptap_name = "";

	/*********** 收藏评分操作 *************/
	public boolean scored_falg = false;

	/************ 商店相关信息 *************/
	public double storelat, storelong;

	/************** 匿名登录参数 ************/
	public boolean anonymity_flag = false;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;

		/*
		 * //调试用的参数 tag_id="A456890A"; sort_id_one="01"; sort_id_two="01001";
		 * definition_id="0100101"; product_id="0001"; //设置为搜索进行
		 * search_flag=true;
		 */
		user_place = "武汉";
		initEngineManager(this);
	}

	public void initEngineManager(Context context) {

		if (mBMapManager == null) {
			mBMapManager = new BMapManager(context);
		}

		if (!mBMapManager.init(strKey, new MyGeneralListener())) {
			Toast.makeText(DemoApplication.getInstance().getApplicationContext(),
					R.string.toast_bpmanager_init_failure, Toast.LENGTH_LONG).show();
		}
	}

	public static DemoApplication getInstance() {
		return mInstance;
	}

	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
	public static class MyGeneralListener implements MKGeneralListener {

		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				Toast.makeText(
						DemoApplication.getInstance().getApplicationContext(),
						R.string.toast_network_failure, Toast.LENGTH_LONG).show();
			} else if (iError == MKEvent.ERROR_NETWORK_DATA) {
				Toast.makeText(
						DemoApplication.getInstance().getApplicationContext(),
						R.string.toast_network_wrong_input, Toast.LENGTH_LONG).show();
			}
			// ...
		}

		@Override
		public void onGetPermissionState(int iError) {
			// 非零值表示key验证未通过
			if (iError != 0) {
				// 授权Key错误：
//				Toast.makeText(
//						DemoApplication.getInstance().getApplicationContext(),
//						R.string.toast_network_wrong_key, Toast.LENGTH_LONG).show();
				DemoApplication.getInstance().m_bKeyRight = false;
			} else {
				DemoApplication.getInstance().m_bKeyRight = true;
				/*
				 * Toast.makeText(DemoApplication.getInstance().
				 * getApplicationContext(), "key认证成功",
				 * Toast.LENGTH_LONG).show();
				 */
			}
		}
	}

	// 设置获取相关函数
	public void setConnected(boolean already_connect) {
		this.already_connect = already_connect;
	}

	public boolean getConnected() {
		return already_connect;
	}

	// 设置SSLSocket
	public void setSSlSocket(SSLSocket sslSocket) {
		this.sslSocket = sslSocket;
	}

	public SSLSocket getSSlSocket() {
		return sslSocket;
	}

	public void setrece_data(String[][] rece_data) {
		this.rece_data = new String[rece_data.length][rece_data[0].length];
		for (int i = 0; i < rece_data.length; i++) {
			for (int j = 0; j < rece_data[i].length; j++) {
				this.rece_data[i][j] = rece_data[i][j];
			}
		}
	}

}