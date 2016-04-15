package product_info;

/*** 评论的相关信息 */
public class CommandItem {
	public String user_name = "";// 用户名
	public String user_level = "";// 用户等级
	public String command_time = "";// 评论时间
	public String command_text = "";// 评论详情
	public String zan_num = "";// 点赞人数
	public String command_id = "";// 点赞的ID

	public CommandItem(String command_id, String user_name, String user_level,
					   String command_time, String command_text, String zan_num) {
		this.command_id = command_id;
		this.user_name = user_name;
		this.user_level = user_level;
		this.command_time = command_time;
		this.command_text = command_text;
		this.zan_num = zan_num;
	}

}
