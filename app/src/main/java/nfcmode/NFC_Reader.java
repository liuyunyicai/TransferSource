package nfcmode;

/**
 * 修改NFC参数
 */
import mainview.demo.DemoApplication;

public class NFC_Reader extends Thread {
	private DemoApplication myApplication;
	private String uid_string;
	private String tag_string;

	public NFC_Reader(DemoApplication myApplication, String uid_string,
					  String tag_string) {
		this.myApplication = myApplication;
		this.uid_string = uid_string;
		this.tag_string = tag_string;
	}

	public void run() {
		if (uid_string.equals("") || tag_string.equals("")
				|| tag_string.length() < 11) {
			return;
		} else {
			myApplication.tag_id = uid_string;
			myApplication.sort_id_one = tag_string.substring(0, 5);
			myApplication.sort_id_two = tag_string.substring(0, 7);
			myApplication.definition_id = tag_string.substring(0, 10);
			myApplication.product_id = tag_string.substring(10,
					tag_string.length());
			// 设置为搜索进行
			/*设置一些基本的控制参数*/
			myApplication.search_flag = true;
			myApplication.state_flag = true;
			myApplication.comment_flag = true;
			myApplication.comment_position = 0;
			myApplication.scaned_falg = true;
			myApplication.saved_flag = false;
			myApplication.scored_falg = false;
			;
			myApplication.current_num = 1;
			myApplication.store_flag = true;
		}

	}
}
