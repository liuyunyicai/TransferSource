package ssl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import javax.net.ssl.SSLSocket;

/*
 * 数据包的头部
 * */

public class PackageHeader implements Action_Type {
	public byte package_type; // 数据包类型
	public byte action_type; // 操作类型
	public byte return_flag; // 状态号
	public byte success_flag; // 完成标志
	public byte[] package_tag = new byte[2];// 标识号
	public byte user_level; // 用户权限
	public byte[] user_name = new byte[8]; // 用户id
	public long now_time; // 时间
	public double place_long; // 经度
	public double place_lat; // 纬度
	public int data_length; // 数据长度

	public String send_string; // 发送字符串
	/*** 暂存发送数据的数组 **/
	private byte[] send_byte = new byte[15];
	/*** 暂存读取发送来信息的字符串 ********/
	private byte[] read_byte = new byte[14];

	/*
	 * 简易构造函数
	 */
	public PackageHeader() {
		for (int i = 0; i < user_name.length; i++) {
			user_name[i] = 0;
		}
	}

	/*
	 * 复杂构造函数
	 */
	public PackageHeader(byte package_type, byte action_type, byte return_flag,
						 byte success_flag, byte[] package_tag, byte user_level,
						 byte[] user_name, long now_time, double place_long,
						 double place_lat, int data_length, String send_string) {
		this.package_type = package_type;
		this.action_type = action_type;
		this.return_flag = return_flag;
		this.success_flag = success_flag;
		for (int i = 0; i < package_tag.length; i++) {
			this.package_tag[i] = package_tag[i];
		}
		this.user_level = user_level;
		this.now_time = now_time;
		this.place_long = place_long;
		this.place_lat = place_lat;
		this.data_length = data_length;
		for (int i = 0; i < user_name.length; i++) {
			this.user_name[i] = user_name[i];
		}
		this.send_string = send_string;

	}

	/*
	 * 发送返回信息函数
	 *//************ 发送返回数据包时，使用此函数 
	 * @throws IOException ********************/
	public void send_returndata(int action_type, DataOutputStream outputStream) throws IOException {
		try {
			JudgeType(action_type);
			outputStream.write(send_byte);
			outputStream.writeLong(now_time);
			outputStream.writeDouble(place_long);
			outputStream.writeDouble(place_lat);
			outputStream.writeInt(data_length);
			outputStream.writeChars(send_string);
		} catch (Exception e) {
			e.printStackTrace();
		} finally
		{
			outputStream.flush();
		}
	}

	/*
	 * 客户端发送请求数据包
	 */
	public PackageHeader send_postdata(final SSLSocket sslSocket,
									   final int action_type) {
		PackageHeader clinet_Package = new PackageHeader();
		DataInputStream inputStream = null;
		DataOutputStream outputStream = null;
		boolean flag = true;
		try {
			inputStream = new DataInputStream(new BufferedInputStream(
					sslSocket.getInputStream()));
			outputStream = new DataOutputStream(new BufferedOutputStream(
					sslSocket.getOutputStream()));
			/*
			 * 发送字符串
			 */
			send_returndata(action_type, outputStream);
			/*
			 * 读取响应回复
			 */
			byte[] tempbyte = new byte[1];
			while (flag) {
				inputStream.read(tempbyte, 0, 1);
				if (tempbyte[0] > 0) {
					phrase_client(clinet_Package, inputStream, tempbyte[0]);
					flag = false;
				}

			}
		} catch (IOException e) {
			System.out.print(e.toString());
		} finally {
			try {
				// inputStream.close();
			} catch (Exception e) {
				System.out.print("错误1");
			}
			try {
				// outputStream.close();
			} catch (Exception e) {
				System.out.print("错误2");
			}
		}
		return clinet_Package;
	}
/****************************************字符串操作函数**************************************************/
	/**
	 * 客户端组装发送字符串
	 */
	public String joinString(List<String> lists)// 组装字符串
	{
		String string = "";
		for (String string_item : lists) {
			string += string_item + "#";
		}
		return string;
	}

	public String[] SpiltString(String receString)//解组装字符串 stringArray[0]为数据长度
	{
		String[] stringArray = null;
		stringArray=receString.split("#");
		return stringArray;
	}

	public boolean Judge_Success(byte success_type)
	{
		if(success_type==1)
		{
			return true;
		}
		else if(success_type==2)
		{
			return false;
		}
		return false;
	}

	/*id转化为byte[],0为个位数端
	 * */
	public byte[] id_2_byte(int id)
	{
		byte[] idbyte=new byte[8];
		try
		{
			for (int i = 0,num=10; i < idbyte.length; i++,id/=10) {
				idbyte[idbyte.length-1-i]=(byte) (id%num);
			}
		} catch (Exception e) {

		}


		return idbyte;
	}
	/*byte[]转化为id(int)
	 * */
	public int byte_2_id(byte[] idbyte)
	{
		int id = 0;
		try
		{
			for (int i = 0; i < idbyte.length; i++) {
				id=id*10+idbyte[i];
			}
		} catch (Exception e) {

		}
		return id;
	}

	/*
	 * 客户端解析数据包
	 */

	private void phrase_client(PackageHeader clinet_Package,
							   DataInputStream inputStream, byte first_byte) {
		try {
			inputStream.read(clinet_Package.read_byte, 0,
					clinet_Package.read_byte.length);
			clinet_Package.package_type = first_byte;
			clinet_Package.action_type = clinet_Package.read_byte[0];
			clinet_Package.return_flag = clinet_Package.read_byte[1];
			clinet_Package.success_flag = clinet_Package.read_byte[2];
			clinet_Package.package_tag[0] = clinet_Package.read_byte[3];
			clinet_Package.package_tag[1] = clinet_Package.read_byte[4];
			clinet_Package.user_level = clinet_Package.read_byte[5];
			for (int i = 0; i < clinet_Package.user_name.length; i++) {
				clinet_Package.user_name[i] = clinet_Package.read_byte[6 + i];
			}
			clinet_Package.action_type = clinet_Package.read_byte[0];
			clinet_Package.now_time = inputStream.readLong();
			clinet_Package.place_long = inputStream.readDouble();
			clinet_Package.place_lat = inputStream.readDouble();
			clinet_Package.data_length = inputStream.readInt();

			if (data_length > 0) {
				char[] tempchar = new char[clinet_Package.data_length];
				for (int i = 0; i < tempchar.length; i++) {
					tempchar[i] = inputStream.readChar();
				}
				clinet_Package.send_string = new String(tempchar);
			}

		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}

	/**
	 * 根据Action_Type或获取对应类型byte信息
	 * */
	public void JudgeType(int send_type) {
		switch (send_type) {
			// 个人设置等
			case USER_REGISTER:
				join_byte(USER_SET, (byte) 1);
				break;
			case USER_LOAD:
				join_byte(USER_SET, (byte) 2);
				break;
			case USER_EXIT:
				join_byte(USER_SET, (byte) 3);
				break;
			case USER_CHANGEBIND:
				join_byte(USER_SET, (byte) 4);
				break;
			case USER_CHANGEPASSWORD:
				join_byte(USER_SET, (byte) 5);
				break;
			case USER_CHANGEINFOR:
				join_byte(USER_SET, (byte) 6);
				break;
			// 个人设置等
			// ////////////////////////////////////////
			case CUSTOMER_REGISTER:
				join_byte(CUSTOMER_SET, (byte) 1);
				break;
			case CUSTOMER_LOAD:
				join_byte(CUSTOMER_SET, (byte) 2);
				break;
			case CUSTOMER_EXIT:
				join_byte(CUSTOMER_SET, (byte) 3);
				break;
			case CUSTOMER_CHANGEBIND:
				join_byte(CUSTOMER_SET, (byte) 4);
				break;
			case CUSTOMER_CHANGEPASSWORD:
				join_byte(CUSTOMER_SET, (byte) 5);
				break;

			// 查询操作(管理者)
			// /////////////////////////////////////////////
			case USER_QUERY_BASICINFO:
				join_byte(USER_QUERY, (byte) 10);
				break;
			case USER_QUERY_CURRENTSTATE:
				join_byte(USER_QUERY, (byte) 11);
				break;
			case USER_QUERY_ALLINFO:
				join_byte(USER_QUERY, (byte) 12);
				break;
			case USER_QUERY_TRANSFERSTATE:
				join_byte(USER_QUERY, (byte) 13);
				break;
			case ONLYUSER_QUERY_ALLPRODUCT:
				join_byte(USER_QUERY, (byte) 14);
				break;
			case STORE_QUERY_ALLPRODUCT:
				join_byte(USER_QUERY, (byte) 15);
				break;

			case USER_QUERY_SORTONE_INFO:
				join_byte(USER_QUERY, (byte) 20);
				break;
			case USER_QUERY_SORTTWO_INFO:
				join_byte(USER_QUERY, (byte) 21);
				break;
			case USER_QUERY_CLASSIFICATION_INFO:
				join_byte(USER_QUERY, (byte) 22);
				break;
			case STORE_QUERY_SORTONE_INFO:
				join_byte(USER_QUERY, (byte) 23);
				break;
			case STORE_QUERY_SORTTWO_INFO:
				join_byte(USER_QUERY, (byte) 24);
				break;
			case STORE_QUERY_CLASSIFICATION_INFO:
				join_byte(USER_QUERY, (byte) 25);
				break;

			case ONLYUSER_QUERY_ALLSTORELIST:
				join_byte(USER_QUERY, (byte) 16);
				break;
			case ONLYUSER_QUERY_UNIQUE_STORELIST:
				join_byte(USER_QUERY, (byte) 17);
				break;
			case ONLYUSER_QUERY_ALLONSELL_LIST:
				join_byte(USER_QUERY, (byte) 18);
				break;

			case USER_QUERY_SORTONE_LIST:
				join_byte(USER_QUERY, (byte) 26);
				break;
			case USER_QUERY_SORTTWO_LIST:
				join_byte(USER_QUERY, (byte) 27);
				break;
			case USER_QUERY_CLASSIFICATION_LIST:
				join_byte(USER_QUERY, (byte) 28);
				break;

			case ONLYUSER_QUERY_CUSTOMERINFO:
				join_byte(USER_QUERY, (byte) 30);
				break;
			case ONLYUSER_QUERY_USERINFO:
				join_byte(USER_QUERY, (byte) 31);
				break;
			case ONLYUSER_QUERY_ALARMLIST:
				join_byte(USER_QUERY, (byte) 32);
				break;
			case ONLYUSER_QUERY_UNIQUE_ALARM:
				join_byte(USER_QUERY, (byte) 33);
				break;
			case ONLYUSER_QUERY_SORTONE_ALARM:
				join_byte(USER_QUERY, (byte) 34);
				break;
			case ONLYUSER_QUERY_SORTTWO_ALARM:
				join_byte(USER_QUERY, (byte) 35);
				break;
			case ONLYUSER_QUERY_CLASSIFICATION_ALARM:
				join_byte(USER_QUERY, (byte) 36);
				break;

			case ONLYUSER_QUERY_PRODUCTBASIC:
				join_byte(USER_QUERY, (byte) 200);
				break;
			case ONLYUSER_QUERY_ALLINFO:
				join_byte(USER_QUERY, (byte) 201);
				break;
			case ONLYUSER_QUERY_CURRENTLIST:
				join_byte(USER_QUERY, (byte) 202);
				break;
			case ONLYUSER_QUERY_SORTONE:
				join_byte(USER_QUERY, (byte) 203);
				break;
			case ONLYUSER_QUERY_SORTTWO:
				join_byte(USER_QUERY, (byte) 204);
				break;
			case ONLYUSER_QUERY_CLASSIFICATION:
				join_byte(USER_QUERY, (byte) 205);
				break;

			case ONLYUSER_QUERY_TAGID:
				join_byte(USER_QUERY, (byte) 199);
				break;

			// 查询操作（顾客）
			// ///////////////////////////////////////////////
			case CUSTOMER_QUERY_BASICINFO:
				join_byte(CUSTOMER_QUERY, (byte) 0);
				break;
			case CUSTOMER_QUERY_CURRENTSTATE:
				join_byte(CUSTOMER_QUERY, (byte) 1);
				break;
			case CUSTOMER_QUERY_ALLINFO:
				join_byte(CUSTOMER_QUERY, (byte) 2);
				break;
			case CUSTOMER_QUERY_TRANSFERSTATE:
				join_byte(CUSTOMER_QUERY, (byte) 3);
				break;
			case CUSTOMER_QUERY_CLASSIFICATION_INFO:
				join_byte(CUSTOMER_QUERY, (byte) 4);
				break;
			case CUSTOMER_QUERY_ALLSTORE_LIST:
				join_byte(CUSTOMER_QUERY, (byte) 5);
				break;
			case CUSTOMER_QUERY_UNQIUE_STORELIST:
				join_byte(CUSTOMER_QUERY, (byte) 6);
				break;

			// 顾客特色操作
			// /////////////////////////////////////////////////
			case CUSTOMER_ADD_INTEREST:
				join_byte(CUSTOMER_SPECIAL, (byte) 0);
				break;
			case CUSTOMER_SCORE_CLASSIFICATION:
				join_byte(CUSTOMER_SPECIAL, (byte) 1);
				break;
			case CUSTOMER_SCORE_PRODUCT:
				join_byte(CUSTOMER_SPECIAL, (byte) 2);
				break;
			case CUSTOMER_UPGRADE:
				join_byte(CUSTOMER_SPECIAL, (byte) 3);
				break;
			case CUSTOMER_TIMEREPORT:
				join_byte(CUSTOMER_SPECIAL, (byte) 4);
				break;
			// ///////////////////////////////////////
			case CUSTOMER_REGISTER_RANDOM:
				join_byte(CUSTOMER_SPECIAL, (byte) 8);
				break;
			case CUSTOMER_REFIND_PASSWORD:
				join_byte(CUSTOMER_SPECIAL, (byte) 9);
				break;
			// //////////////////////////////////////
			case CUSTOMER_ADD_FRIEND:
				join_byte(CUSTOMER_SPECIAL, (byte) 10);
				break;
			case CUSTOMER_ADD_PRODUCT:
				join_byte(CUSTOMER_SPECIAL, (byte) 11);
				break;
			case CUSTOMER_ADD_STORE:
				join_byte(CUSTOMER_SPECIAL, (byte) 12);
				break;
			case CUSTOMER_ADD_FRIENDGROUP:
				join_byte(CUSTOMER_SPECIAL, (byte) 13);
				break;
			case CUSTOMER_ADD_PRODUCTGROUP:
				join_byte(CUSTOMER_SPECIAL, (byte) 14);
				break;
			case CUSTOMER_ADD_STOREGROUP:
				join_byte(CUSTOMER_SPECIAL, (byte) 15);
				break;
			// //////////////////////////////////////
			case CUSTOMER_DELETE_FRIEND:
				join_byte(CUSTOMER_SPECIAL, (byte) 16);
				break;
			case CUSTOMER_DELETE_PRODUCT:
				join_byte(CUSTOMER_SPECIAL, (byte) 17);
				break;
			case CUSTOMER_DELETE_STORE:
				join_byte(CUSTOMER_SPECIAL, (byte) 18);
				break;
			case CUSTOMER_DELETE_FRIENDGROUP:
				join_byte(CUSTOMER_SPECIAL, (byte) 19);
				break;
			case CUSTOMER_DELETE_PRODUCTGROUP:
				join_byte(CUSTOMER_SPECIAL, (byte) 20);
				break;
			case CUSTOMER_DELETE_STOREGROUP:
				join_byte(CUSTOMER_SPECIAL, (byte) 21);
				break;
			// //////////////////////////////////
			case CUSTOMER_UPDATE_FRIEND:
				join_byte(CUSTOMER_SPECIAL, (byte) 22);
				break;
			case CUSTOMER_UPDATE_PRODUCT:
				join_byte(CUSTOMER_SPECIAL, (byte) 23);
				break;
			case CUSTOMER_UPDATE_STORE:
				join_byte(CUSTOMER_SPECIAL, (byte) 24);
				break;
			case CUSTOMER_UPDATE_FRIENDGROUP:
				join_byte(CUSTOMER_SPECIAL, (byte) 25);
				break;
			case CUSTOMER_UPDATE_PRODUCTGROUP:
				join_byte(CUSTOMER_SPECIAL, (byte) 26);
				break;
			case CUSTOMER_UPDATE_STOREGROUP:
				join_byte(CUSTOMER_SPECIAL, (byte) 27);
				break;
			// ///////////////////////////////////////////
			case CUSTOMER_SEARCH_FRIEND:
				join_byte(CUSTOMER_SPECIAL, (byte) 28);
				break;
			case CUSTOMER_SEARCH_PRODUCT:
				join_byte(CUSTOMER_SPECIAL, (byte) 29);
				break;
			case CUSTOMER_SEARCH_STORE:
				join_byte(CUSTOMER_SPECIAL, (byte) 30);
				break;
			// /////////////////////////////////////
			case CUSTOMER_LOOKOTHER_PRODUCT:
				join_byte(CUSTOMER_SPECIAL, (byte) 31);
				break;
			case CUSTOMER_LOOKOTHER_STORE:
				join_byte(CUSTOMER_SPECIAL, (byte) 32);
				break;

			// 添加操作
			// ///////////////////////////////////////////////////
			case ONLYUSER_INSERT_SORTONE:
				join_byte(USER_INSERT, (byte) 0);
				break;
			case ONLYUSER_INSERT_SORTTWO:
				join_byte(USER_INSERT, (byte) 1);
				break;
			case ONLYUSER_INSERT_CLASSIFICATION:
				join_byte(USER_INSERT, (byte) 2);
				break;
			case ONLYUSER_INSERT_PRODUCTBASIC:
				join_byte(USER_INSERT, (byte) 3);
				break;
			case USER_INSERT_CURRENTLIST:
				join_byte(USER_INSERT, (byte) 4);
				break;
			case ONLYUSER_INSERT_ALLINFO:
				join_byte(USER_INSERT, (byte) 5);
				break;
			case ONLYUSER_INSERT_ALARMLIST:
				join_byte(USER_INSERT, (byte) 6);
				break;

			case STORE_INSERT_STORELIST:
				join_byte(USER_INSERT, (byte) 7);
				break;
			case CUSTOMER_INSERT_ONSELLLIST:
				join_byte(USER_INSERT, (byte) 8);
				break;

			// 更新操作
			// ////////////////////////////////////////////
			case ONLYUSER_UPDATE_SORTONE:
				join_byte(USER_UPDATE, (byte) 0);
				break;
			case ONLYUSER_UPDATE_SORTTWO:
				join_byte(USER_UPDATE, (byte) 1);
				break;
			case ONLYUSER_UPDATE_CLASSIFICATION:
				join_byte(USER_UPDATE, (byte) 2);
				break;
			case ONLYUSER_UPDATE_PRODUCTBASIC:
				join_byte(USER_UPDATE, (byte) 3);
				break;
			case USER_UPDATE_CURRENTLIST:
				join_byte(USER_UPDATE, (byte) 4);
				break;
			case ONLYUSER_UPDATE_ALLINFO:
				join_byte(USER_UPDATE, (byte) 5);
				break;

			// 删除操作
			// ////////////////////////////////////////////////
			case ONLYUSER_DELETE_SORTONE:
				join_byte(USER_DELETE, (byte) 0);
				break;
			case ONLYUSER_DELETE_SORTTWO:
				join_byte(USER_DELETE, (byte) 1);
				break;
			case ONLYUSER_DELETE_CLASSIFICATION:
				join_byte(USER_DELETE, (byte) 2);
				break;
			case ONLYUSER_DELETE_PRODUCTBASIC:
				join_byte(USER_DELETE, (byte) 3);
				break;
			case ONLYUSER_DELETE_CURRENTLIST:
				join_byte(USER_DELETE, (byte) 4);
				break;
			case ONLYUSER_DELETE_CURRENTONCE:
				join_byte(USER_DELETE, (byte) 5);
				break;

			// 用户注册随机码
			case MANAGE_LOGIN:
				join_byte(CUSTOMER_SPECIAL, (byte) 33);
				break;
			case MANAGE_SENDRANDOM:
				join_byte(CUSTOMER_SPECIAL, (byte) 34);
				break;
			case CUSTOMER_GETANOTHER_RANDOM:
				join_byte(CUSTOMER_SPECIAL, (byte) 35);
				break;

			// 评价操作
			case CUSTOMER_CLASSIFICATION_ADDCOMMENT:
				join_byte(CUSTOMER_SPECIAL, (byte) 36);
				break;
			case CUSTOMER_CLASSIFICATION_GETCOMMENT:
				join_byte(CUSTOMER_SPECIAL, (byte) 37);
				break;
			case CUSTOMER_PRODUCT_ADDCOMMENT:
				join_byte(CUSTOMER_SPECIAL, (byte) 38);
				break;
			case CUSTOMER_PRODUCT_GETCOMMENT:
				join_byte(CUSTOMER_SPECIAL, (byte) 39);
				break;
			case CUSTOMER_DIANZAN:
				join_byte(CUSTOMER_SPECIAL, (byte) 40);
				break;
			case CUSTOMER_CANCEL_DIANZAN:
				join_byte(CUSTOMER_SPECIAL, (byte) 41);
				break;

			case CUSTOMER_GETSIMILAR_PRODUCT:
				join_byte(CUSTOMER_SPECIAL, (byte) 42);
				break;
			case CUSTOMER_HANGOUT:
				join_byte(CUSTOMER_SPECIAL, (byte) 43);
				break;

			default:

				break;
		}

	}

	/*
	 * 将byte类型的存储到一个byte数组中
	 */
	private void join_byte(byte package_type, byte action_type) {
		this.package_type = package_type;
		this.action_type = action_type;

		send_byte[0] = package_type;
		send_byte[1] = action_type;
		send_byte[2] = return_flag;
		send_byte[3] = success_flag;
		send_byte[4] = package_tag[0];
		send_byte[5] = package_tag[1];
		send_byte[6] = user_level;
		for (int i = 0; i < user_name.length; i++) {
			send_byte[7 + i] = user_name[i];
		}
	}

	/**
	 * 解析接收到的数据包信息(后续的仅需从PackageHeader中读取信息)
	 * */
	public int ParsePackage(DataInputStream inputStream, byte first_byte) {
		try {
			inputStream.read(read_byte, 0, read_byte.length);
			package_type = first_byte;
			action_type = read_byte[0];
			return_flag = read_byte[1];
			success_flag = read_byte[2];
			package_tag[0] = read_byte[3];
			package_tag[1] = read_byte[4];
			user_level = read_byte[5];
			for (int i = 0; i < user_name.length; i++) {
				user_name[i] = read_byte[6 + i];
			}
			action_type = read_byte[0];
			now_time = inputStream.readLong();
			place_long = inputStream.readDouble();
			place_lat = inputStream.readDouble();
			data_length = inputStream.readInt();

			char[] tempchar = new char[data_length];
			for (int i = 0; i < tempchar.length; i++) {
				tempchar[i] = inputStream.readChar();
			}
			send_string = new String(tempchar);

		} catch (IOException e) {
			System.out.println(e.toString());
		}

		return split_byte_type(package_type, action_type);// 返回值
	}

	private int split_byte_type(byte byte_one, byte byte_two)
	// TODO Auto-generated method stub
	{
		int return_type = 0;
		// 个人设置等
		// /////////////////////////////////////////////////////
		if ((byte_one == USER_SET) && (byte_two == (byte) 1)) {
			return USER_REGISTER;
		}
		if ((byte_one == USER_SET) && (byte_two == (byte) 2)) {
			return USER_LOAD;
		}
		if ((byte_one == USER_SET) && (byte_two == (byte) 3)) {
			return USER_EXIT;
		}
		if ((byte_one == USER_SET) && (byte_two == (byte) 4)) {
			return USER_CHANGEBIND;
		}
		if ((byte_one == USER_SET) && (byte_two == (byte) 5)) {
			return USER_CHANGEPASSWORD;
		}
		if ((byte_one == USER_SET) && (byte_two == (byte) 6)) {
			return USER_CHANGEINFOR;
		}

		// 个人设置等
		// ////////////////////////////////////////////////////////////////
		else if ((byte_one == CUSTOMER_SET) && (byte_two == (byte) 1)) {
			return CUSTOMER_REGISTER;
		} else if ((byte_one == CUSTOMER_SET) && (byte_two == (byte) 2)) {
			return CUSTOMER_LOAD;
		} else if ((byte_one == CUSTOMER_SET) && (byte_two == (byte) 3)) {
			return CUSTOMER_EXIT;
		} else if ((byte_one == CUSTOMER_SET) && (byte_two == (byte) 4)) {
			return CUSTOMER_CHANGEBIND;
		} else if ((byte_one == CUSTOMER_SET) && (byte_two == (byte) 5)) {
			return CUSTOMER_CHANGEPASSWORD;
		}

		// 查询操作(管理者)
		// ///////////////////////////////////////////////////////////////
		else if ((byte_one == USER_QUERY) && (byte_two == (byte) 10)) {
			return USER_QUERY_BASICINFO;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 11)) {
			return USER_QUERY_CURRENTSTATE;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 12)) {
			return USER_QUERY_ALLINFO;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 13)) {
			return USER_QUERY_TRANSFERSTATE;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 14)) {
			return ONLYUSER_QUERY_ALLPRODUCT;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 15)) {
			return STORE_QUERY_ALLPRODUCT;
		}

		else if ((byte_one == USER_QUERY) && (byte_two == (byte) 20)) {
			return USER_QUERY_SORTONE_INFO;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 21)) {
			return USER_QUERY_SORTTWO_INFO;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 22)) {
			return USER_QUERY_CLASSIFICATION_INFO;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 23)) {
			return STORE_QUERY_SORTONE_INFO;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 24)) {
			return STORE_QUERY_SORTTWO_INFO;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 25)) {
			return STORE_QUERY_CLASSIFICATION_INFO;
		}

		else if ((byte_one == USER_QUERY) && (byte_two == (byte) 16)) {
			return ONLYUSER_QUERY_ALLSTORELIST;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 17)) {
			return ONLYUSER_QUERY_UNIQUE_STORELIST;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 18)) {
			return ONLYUSER_QUERY_ALLONSELL_LIST;
		}

		else if ((byte_one == USER_QUERY) && (byte_two == (byte) 26)) {
			return USER_QUERY_SORTONE_LIST;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 27)) {
			return USER_QUERY_SORTTWO_LIST;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 28)) {
			return USER_QUERY_CLASSIFICATION_LIST;
		}

		else if ((byte_one == USER_QUERY) && (byte_two == (byte) 30)) {
			return ONLYUSER_QUERY_CUSTOMERINFO;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 31)) {
			return ONLYUSER_QUERY_USERINFO;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 32)) {
			return ONLYUSER_QUERY_ALARMLIST;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 33)) {
			return ONLYUSER_QUERY_UNIQUE_ALARM;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 34)) {
			return ONLYUSER_QUERY_SORTONE_ALARM;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 35)) {
			return ONLYUSER_QUERY_SORTTWO_ALARM;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 36)) {
			return ONLYUSER_QUERY_CLASSIFICATION_ALARM;
		}

		else if ((byte_one == USER_QUERY) && (byte_two == (byte) 200)) {
			return ONLYUSER_QUERY_PRODUCTBASIC;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 201)) {
			return ONLYUSER_QUERY_ALLINFO;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 202)) {
			return ONLYUSER_QUERY_CURRENTLIST;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 203)) {
			return ONLYUSER_QUERY_SORTONE;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 204)) {
			return ONLYUSER_QUERY_SORTTWO;
		} else if ((byte_one == USER_QUERY) && (byte_two == (byte) 205)) {
			return ONLYUSER_QUERY_CLASSIFICATION;
		}

		else if ((byte_one == USER_QUERY) && (byte_two == (byte) 199)) {
			return ONLYUSER_QUERY_TAGID;
		}

		// 查询操作（顾客）
		// ///////////////////////////////////////////////////////
		else if ((byte_one == CUSTOMER_QUERY) && (byte_two == (byte) 0)) {
			return CUSTOMER_QUERY_BASICINFO;
		} else if ((byte_one == CUSTOMER_QUERY) && (byte_two == (byte) 1)) {
			return CUSTOMER_QUERY_CURRENTSTATE;
		} else if ((byte_one == CUSTOMER_QUERY) && (byte_two == (byte) 2)) {
			return CUSTOMER_QUERY_ALLINFO;
		} else if ((byte_one == CUSTOMER_QUERY) && (byte_two == (byte) 3)) {
			return CUSTOMER_QUERY_TRANSFERSTATE;
		} else if ((byte_one == CUSTOMER_QUERY) && (byte_two == (byte) 4)) {
			return CUSTOMER_QUERY_CLASSIFICATION_INFO;
		} else if ((byte_one == CUSTOMER_QUERY) && (byte_two == (byte) 5)) {
			return CUSTOMER_QUERY_ALLSTORE_LIST;
		} else if ((byte_one == CUSTOMER_QUERY) && (byte_two == (byte) 6)) {
			return CUSTOMER_QUERY_UNQIUE_STORELIST;
		}

		// 顾客特色操作
		// ///////////////////////////////////////////////////////////////////
		else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 0)) {
			return CUSTOMER_ADD_INTEREST;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 1)) {
			return CUSTOMER_SCORE_CLASSIFICATION;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 2)) {
			return CUSTOMER_SCORE_PRODUCT;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 3)) {
			return CUSTOMER_UPGRADE;
		}

		// 添加操作
		// /////////////////////////////////////////////////////////////////////
		else if ((byte_one == USER_INSERT) && (byte_two == (byte) 0)) {
			return ONLYUSER_INSERT_SORTONE;
		} else if ((byte_one == USER_INSERT) && (byte_two == (byte) 1)) {
			return ONLYUSER_INSERT_SORTTWO;
		} else if ((byte_one == USER_INSERT) && (byte_two == (byte) 2)) {
			return ONLYUSER_INSERT_CLASSIFICATION;
		} else if ((byte_one == USER_INSERT) && (byte_two == (byte) 3)) {
			return ONLYUSER_INSERT_PRODUCTBASIC;
		} else if ((byte_one == USER_INSERT) && (byte_two == (byte) 4)) {
			return USER_INSERT_CURRENTLIST;
		} else if ((byte_one == USER_INSERT) && (byte_two == (byte) 5)) {
			return ONLYUSER_INSERT_ALLINFO;
		} else if ((byte_one == USER_INSERT) && (byte_two == (byte) 6)) {
			return ONLYUSER_INSERT_ALARMLIST;
		} else if ((byte_one == USER_INSERT) && (byte_two == (byte) 7)) {
			return STORE_INSERT_STORELIST;
		}

		else if ((byte_one == CUSTOMER_INSERT) && (byte_two == (byte) 8)) {
			return CUSTOMER_INSERT_ONSELLLIST;
		}

		// 更新操作
		// ////////////////////////////////////////////////////////////////////
		else if ((byte_one == USER_UPDATE) && (byte_two == (byte) 0)) {
			return ONLYUSER_UPDATE_SORTONE;
		} else if ((byte_one == USER_UPDATE) && (byte_two == (byte) 1)) {
			return ONLYUSER_UPDATE_SORTTWO;
		} else if ((byte_one == USER_UPDATE) && (byte_two == (byte) 2)) {
			return ONLYUSER_UPDATE_CLASSIFICATION;
		} else if ((byte_one == USER_UPDATE) && (byte_two == (byte) 3)) {
			return ONLYUSER_UPDATE_PRODUCTBASIC;
		} else if ((byte_one == USER_UPDATE) && (byte_two == (byte) 4)) {
			return USER_UPDATE_CURRENTLIST;
		} else if ((byte_one == USER_UPDATE) && (byte_two == (byte) 5)) {
			return ONLYUSER_UPDATE_ALLINFO;
		}

		// 删除操作
		// ////////////////////////////////////////////////////////////////
		else if ((byte_one == USER_DELETE) && (byte_two == (byte) 0)) {
			return ONLYUSER_DELETE_SORTONE;
		} else if ((byte_one == USER_DELETE) && (byte_two == (byte) 1)) {
			return ONLYUSER_DELETE_SORTTWO;
		} else if ((byte_one == USER_DELETE) && (byte_two == (byte) 2)) {
			return ONLYUSER_DELETE_CLASSIFICATION;
		} else if ((byte_one == USER_DELETE) && (byte_two == (byte) 3)) {
			return ONLYUSER_DELETE_PRODUCTBASIC;
		} else if ((byte_one == USER_DELETE) && (byte_two == (byte) 4)) {
			return ONLYUSER_DELETE_CURRENTLIST;
		} else if ((byte_one == USER_DELETE) && (byte_two == (byte) 5)) {
			return ONLYUSER_DELETE_CURRENTONCE;
		}

		// 顾客特色操作
		else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 7)) {
			return CUSTOMER_TIMEREPORT;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 8)) {
			return CUSTOMER_REGISTER_RANDOM;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 9)) {
			return CUSTOMER_REFIND_PASSWORD;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 10)) {
			return CUSTOMER_ADD_FRIEND;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 11)) {
			return CUSTOMER_ADD_PRODUCT;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 12)) {
			return CUSTOMER_ADD_STORE;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 13)) {
			return CUSTOMER_ADD_FRIENDGROUP;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 14)) {
			return CUSTOMER_ADD_PRODUCTGROUP;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 15)) {
			return CUSTOMER_ADD_STOREGROUP;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 16)) {
			return CUSTOMER_DELETE_FRIEND;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 17)) {
			return CUSTOMER_DELETE_PRODUCT;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 18)) {
			return CUSTOMER_DELETE_STORE;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 19)) {
			return CUSTOMER_DELETE_FRIENDGROUP;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 20)) {
			return CUSTOMER_DELETE_PRODUCTGROUP;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 21)) {
			return CUSTOMER_DELETE_STOREGROUP;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 22)) {
			return CUSTOMER_UPDATE_FRIEND;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 23)) {
			return CUSTOMER_UPDATE_PRODUCT;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 24)) {
			return CUSTOMER_UPDATE_STORE;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 25)) {
			return CUSTOMER_UPDATE_FRIENDGROUP;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 26)) {
			return CUSTOMER_UPDATE_PRODUCTGROUP;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 27)) {
			return CUSTOMER_UPDATE_STOREGROUP;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 28)) {
			return CUSTOMER_SEARCH_FRIEND;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 29)) {
			return CUSTOMER_SEARCH_PRODUCT;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 30)) {
			return CUSTOMER_SEARCH_STORE;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 31)) {
			return CUSTOMER_LOOKOTHER_PRODUCT;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 32)) {
			return CUSTOMER_LOOKOTHER_STORE;
		}

		else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 33)) {
			return MANAGE_LOGIN;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 34)) {
			return MANAGE_SENDRANDOM;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 35)) {
			return CUSTOMER_GETANOTHER_RANDOM;
		}

		else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 36)) {
			return CUSTOMER_CLASSIFICATION_ADDCOMMENT;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 37)) {
			return CUSTOMER_CLASSIFICATION_GETCOMMENT;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 38)) {
			return CUSTOMER_PRODUCT_ADDCOMMENT;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 39)) {
			return CUSTOMER_PRODUCT_GETCOMMENT;
		}

		else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 40)) {
			return CUSTOMER_DIANZAN;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 41)) {
			return CUSTOMER_CANCEL_DIANZAN;
		}

		else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 42)) {
			return CUSTOMER_GETSIMILAR_PRODUCT;
		} else if ((byte_one == CUSTOMER_SPECIAL) && (byte_two == (byte) 43)) {
			return CUSTOMER_HANGOUT;
		}
		return return_type;
	}

}
