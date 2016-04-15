package ssl;

import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import mainview.demo.R;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

//SSL连接
public class Connect_Thread {
	// 默认的一些网络设置
	private String Default_Host = "172.27.35.1";
	private static final int DEFAULT_PORT = 8088;
	private static final String CLIENT_KEY_STORE_PASSWORD = "kaiyuan";
	private static final String CLIENT_TRUST_KEY_STORE_PASSWORD = "kaiyuan";
	// SSL通信socket
	private SSLSocket sslSocket;
	private Context mContext = null;
	// 构造函数
	public Connect_Thread(Context mContext) {
		this.mContext = mContext;
	}

	// 用于IP修改后的构造函数
	public Connect_Thread(Context mContext, String changed_host) {
		this.mContext = mContext;
		this.Default_Host = changed_host;
	}

	// 判断是否连接成功
	public boolean process() {
		if (sslSocket == null) {
			System.out.println("ERROR");
			return false;
		}
		return true;

	}

	// 进行；连接
	public void init() {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");

			KeyStore ks = KeyStore.getInstance("BKS");
			KeyStore tks = KeyStore.getInstance("BKS");
			ks.load(mContext.getResources().openRawResource(R.raw.sslkandroid),
					CLIENT_KEY_STORE_PASSWORD.toCharArray());
			tks.load(
					mContext.getResources().openRawResource(R.raw.ssltandroid),
					CLIENT_TRUST_KEY_STORE_PASSWORD.toCharArray());
			kmf.init(ks, CLIENT_KEY_STORE_PASSWORD.toCharArray());
			tmf.init(tks);
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			sslSocket = (SSLSocket) ctx.getSocketFactory().createSocket(
					Default_Host, DEFAULT_PORT);
			Toast.makeText(mContext, "连接服务器成功", Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			System.out.println(e);
			Log.e("error", e.toString());
		}
	}

	public SSLSocket getssl_socket() {
		return sslSocket;
	}
}
