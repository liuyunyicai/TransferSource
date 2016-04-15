package ssl;

public interface Action_Type
{

	public final static byte USER_SET=(byte)10;  //厂家个人设置
	//(1--6)
	public final static byte CUSTOMER_SET=(byte)11; //顾客个人设置
	//(1--5)
	public final static byte USER_QUERY=(byte)20;    //厂家查询
	//(10--36)
	public final static byte CUSTOMER_QUERY=(byte)21;  //顾客查询
	//(1--6)
	public final static byte USER_INSERT=(byte)30;      //厂家添加
	//(0--7)
	public final static byte CUSTOMER_INSERT=(byte)31;  //添加顾客添加
	//(7)
	public final static byte USER_DELETE=(byte)40;   //删除
	//(0--5)
	public final static byte USER_UPDATE=(byte)50;  //修改
	//(0--5)
	public final static byte CUSTOMER_SPECIAL=(byte)60; //顾客特色操作
	//(0--3)
	public final static byte RESERVE=(byte)70;  //保留


	//个人设置等
	public final static int USER_REGISTER=(int)1; //厂家注册
	public final static int USER_LOAD=(int)2;     //厂家登录
	public final static int USER_EXIT=(int)3;     //厂家退出
	public final static int USER_CHANGEBIND=(int)4;//厂家修改联系电话
	public final static int USER_CHANGEPASSWORD=(int)5;//厂家修改密码
	public final static int USER_CHANGEINFOR=(int)6;//厂家修改名称等基本信息

	public final static int CUSTOMER_REGISTER=(int)101;    //顾客注册
	public final static int CUSTOMER_LOAD=(int)102;        //顾客登录
	public final static int CUSTOMER_EXIT=(int)103;         //顾客退出
	public final static int CUSTOMER_CHANGEBIND=(int)104;  //顾客修改绑定电话
	public final static int CUSTOMER_CHANGEPASSWORD=(int)105; //顾客修改密码

	//查询操作(管理者)
	public final static int USER_QUERY_BASICINFO=(int)10;     //查询商品基本信息（管理者+厂家）
	public final static int USER_QUERY_CURRENTSTATE=(int)11;  //查询商品当前状态（管理者+厂家）
	public final static int USER_QUERY_ALLINFO=(int)12;        //查询商品当前所有信息（管理者+厂家）
	public final static int USER_QUERY_TRANSFERSTATE=(int)13;  //查询某个产品所有物流信息（管理者+厂家）
	public final static int ONLYUSER_QUERY_ALLPRODUCT=(int)14;     //查询所有商品当前信息（管理者）
	public final static int STORE_QUERY_ALLPRODUCT=(int)15;    //查询厂家所有的商品当前信息（管理者+厂家）

	public final static int USER_QUERY_SORTONE_INFO=(int)20;  //查询一类产品的销量（管理者+商家）
	public final static int USER_QUERY_SORTTWO_INFO=(int)21;  //查询二类产品的销量（管理者+商家）
	public final static int USER_QUERY_CLASSIFICATION_INFO=(int)22;//查询某种产品的信息（如销量，评分）
	public final static int STORE_QUERY_SORTONE_INFO=(int)23;  //查询一类产品的某商家销量（管理者+商家）
	public final static int STORE_QUERY_SORTTWO_INFO=(int)24;  //查询二类产品的某商家销量（管理者+商家）
	public final static int STORE_QUERY_CLASSIFICATION_INFO=(int)25;//查询某种产品的某商家信息（如销量，评分）

	public final static int ONLYUSER_QUERY_ALLSTORELIST=(int)16;//查询所有产品的所有厂家信息（管理者）
	public final static int ONLYUSER_QUERY_UNIQUE_STORELIST=(int)17;//查询某类产品的所有厂家信息（管理者）
	public final static int ONLYUSER_QUERY_ALLONSELL_LIST=(int)18;//查询某个产品的顾客信息（管理者）

	public final static int USER_QUERY_SORTONE_LIST=(int)26;//查询所有一类表（管理者+厂家）
	public final static int USER_QUERY_SORTTWO_LIST=(int)27;//查询所有二类表（管理者+厂家）
	public final static int USER_QUERY_CLASSIFICATION_LIST=(int)28;//查询所有类产品表（管理者+厂家）

	public final static int ONLYUSER_QUERY_CUSTOMERINFO=(int)30;//查询顾客信息表（管理者）
	public final static int ONLYUSER_QUERY_USERINFO=(int)31;//查询厂家管理者信息表（管理者）
	public final static int ONLYUSER_QUERY_ALARMLIST=(int)32;//查询报警记录（管理者）
	public final static int ONLYUSER_QUERY_UNIQUE_ALARM=(int)33;//查询某个商品报警记录（管理者）
	public final static int ONLYUSER_QUERY_SORTONE_ALARM=(int)34;//查询一级分类报警记录（管理者）
	public final static int ONLYUSER_QUERY_SORTTWO_ALARM=(int)35;//查询二级分类报警记录（管理者）
	public final static int ONLYUSER_QUERY_CLASSIFICATION_ALARM=(int)36;//查询某种商品报警记录（管理者）

	public final static int ONLYUSER_QUERY_PRODUCTBASIC=(int)200;//查询商品基本信息（管理者）
	public final static int ONLYUSER_QUERY_ALLINFO=(int)201;     //查询商品所有信息（管理者）
	public final static int ONLYUSER_QUERY_CURRENTLIST=(int)202;     //查询商品物流信息（管理者）
	public final static int ONLYUSER_QUERY_SORTONE=(int)203;     //查询一级分类（管理者）
	public final static int ONLYUSER_QUERY_SORTTWO=(int)204;     //查询二级分类（管理者）
	public final static int ONLYUSER_QUERY_CLASSIFICATION=(int)205;  //查询商品分类（管理者）

	public final static int ONLYUSER_QUERY_TAGID=(int)199;  //查询商品分类（管理者）

	//查询操作（顾客）	
	public final static int CUSTOMER_QUERY_BASICINFO=(int)110;     //查询商品基本信息（顾客）
	public final static int CUSTOMER_QUERY_CURRENTSTATE=(int)111;  //查询商品当前物流状态（顾客）
	public final static int CUSTOMER_QUERY_ALLINFO=(int)112;        //查询商品当前所有信息（顾客）
	public final static int CUSTOMER_QUERY_TRANSFERSTATE=(int)113;  //查询所有物流状态（顾客）(地图显示)
	public final static int CUSTOMER_QUERY_CLASSIFICATION_INFO=(int)114;  //查询某类商品基本信息（顾客）
	public final static int CUSTOMER_QUERY_ALLSTORE_LIST=(int)115;  //查询某类商品所有厂家信息（顾客）（导购）
	public final static int CUSTOMER_QUERY_UNQIUE_STORELIST=(int)116;//查询某类商品某个厂家信息（顾客）


	//顾客特色操作
	public final static int CUSTOMER_ADD_INTEREST=(int)130;     //添加某一产品的感兴趣记录（顾客）
	public final static int CUSTOMER_SCORE_DEFINITION=(int)131; //添加某一类产品的评价（顾客）
	public final static int CUSTOMER_SCORE_CLASSIFICATION=(int)131; //添加某一类产品的评价（顾客）
	public final static int CUSTOMER_SCORE_PRODUCT=(int)132; //添加某一个产品的评价（顾客）
	public final static int CUSTOMER_UPGRADE=(int)133;  	     //顾客升级（顾客）
	public final static int CUSTOMER_TIMEREPORT=(int)134;  	     //顾客向服务器定时发送信号，超过一定时间，则视为强制不在线，时间设置在十分钟左右（顾客）

	public final static int CUSTOMER_REGISTER_RANDOM=(int)190;  //顾客注册时验证随机号（顾客）
	public final static int CUSTOMER_REFIND_PASSWORD=(int)191;  //顾客找回密码（顾客）

	public final static int CUSTOMER_ADD_FRIEND=(int)140;     //添加好友（顾客）
	public final static int CUSTOMER_ADD_PRODUCT=(int)141;    //收藏商品（顾客）
	public final static int CUSTOMER_ADD_STORE=(int)142;      //收藏商家（顾客）
	public final static int CUSTOMER_ADD_FRIENDGROUP=(int)143;     //添加好友分组（顾客）
	public final static int CUSTOMER_ADD_PRODUCTGROUP=(int)144;    //添加收藏商品分组（顾客）
	public final static int CUSTOMER_ADD_STOREGROUP=(int)145;      //添加收藏商家分组（顾客）

	public final static int CUSTOMER_DELETE_FRIEND=(int)150;     //删除好友列表中某个好友（顾客）
	public final static int CUSTOMER_DELETE_PRODUCT=(int)151;    //删除收藏商品中某个商品（顾客）
	public final static int CUSTOMER_DELETE_STORE=(int)152;      //删除收藏商家中某个商家（顾客）
	public final static int CUSTOMER_DELETE_FRIENDGROUP=(int)153;     //删除好友列表中某个好友分组（顾客）
	public final static int CUSTOMER_DELETE_PRODUCTGROUP=(int)154;    //删除收藏商品中某个商品分组（顾客）
	public final static int CUSTOMER_DELETE_STOREGROUP=(int)155;      //删除收藏商家中某个商家分组（顾客）

	public final static int CUSTOMER_UPDATE_FRIEND=(int)160;     //修改好友列表中某个好友信息（顾客）
	public final static int CUSTOMER_UPDATE_PRODUCT=(int)171;    //修改收藏商品中某个商品信息（顾客）
	public final static int CUSTOMER_UPDATE_STORE=(int)172;      //修改收藏商家中某个商家信息（顾客）
	public final static int CUSTOMER_UPDATE_FRIENDGROUP=(int)173;     //修改好友列表中某个好友分组信息（顾客）
	public final static int CUSTOMER_UPDATE_PRODUCTGROUP=(int)174;    //修改收藏商品中某个商品分组信息（顾客）
	public final static int CUSTOMER_UPDATE_STOREGROUP=(int)175;      //修改收藏商家中某个商家分组信息（顾客）

	public final static int CUSTOMER_SEARCH_FRIEND=(int)180;     //查看好友列表（顾客）
	public final static int CUSTOMER_SEARCH_PRODUCT=(int)181;    //查看收藏商品（顾客）
	public final static int CUSTOMER_SEARCH_STORE=(int)182;      //查看收藏商家（顾客）

	public final static int CUSTOMER_LOOKOTHER_PRODUCT=(int)183;    //查看其他人的收藏商品（顾客）
	public final static int CUSTOMER_LOOKOTHER_STORE=(int)184;      //查看其他人的收藏商家（顾客）
	//评价操作
	public final static int CUSTOMER_CLASSIFICATION_ADDCOMMENT =(int)135;//添加某一类产品的评价（顾客）
	public final static int CUSTOMER_CLASSIFICATION_GETCOMMENT=(int)136; //查看某一类产品的评价（顾客）
	public final static int CUSTOMER_PRODUCT_ADDCOMMENT=(int)137; //添加某一个产品的评价（顾客）
	public final static int CUSTOMER_PRODUCT_GETCOMMENT=(int)138; //查看某一个产品的评价（顾客）
	public final static int CUSTOMER_DIANZAN=(int)195;  	        //评价点赞操作
	public final static int CUSTOMER_CANCEL_DIANZAN=(int)196;  	//取消评价点赞操作

	public final static int CUSTOMER_GETSIMILAR_PRODUCT=(int)197;  //获取相近产品
	public final static int CUSTOMER_HANGOUT=(int)198;  	        //随便看看操作




	//添加操作
	public final static int ONLYUSER_INSERT_SORTONE=(int)40;     //添加一级分类（管理者）
	public final static int ONLYUSER_INSERT_SORTTWO=(int)41;     //添加二级分类（管理者）
	public final static int ONLYUSER_INSERT_CLASSIFICATION=(int)42;  //添加商品分类（管理者）
	public final static int ONLYUSER_INSERT_PRODUCTBASIC=(int)43;//添加商品基本信息（管理者）（出厂）
	public final static int USER_INSERT_CURRENTLIST=(int)44;     //添加商品物流信息（管理者+厂家）
	public final static int ONLYUSER_INSERT_ALLINFO=(int)45;     //添加一次性所有信息（管理者）（出厂操作）
	public final static int ONLYUSER_INSERT_ALARMLIST=(int)46;     //添加商品报警记录（管理者）

	public final static int STORE_INSERT_STORELIST=(int)47;     //添加商品商家列表（商家）
	public final static int CUSTOMER_INSERT_ONSELLLIST=(int)48;     //添加商品销售表（顾客）


	//更新操作
	public final static int ONLYUSER_UPDATE_SORTONE=(int)60;     //修改一级分类（管理者）
	public final static int ONLYUSER_UPDATE_SORTTWO=(int)61;     //修改二级分类（管理者）
	public final static int ONLYUSER_UPDATE_CLASSIFICATION=(int)62;  //修改商品分类（管理者）
	public final static int ONLYUSER_UPDATE_PRODUCTBASIC=(int)63;//修改商品基本信息（管理者）
	public final static int USER_UPDATE_CURRENTLIST=(int)64;     //修改商品物流信息（管理者+厂家）
	public final static int ONLYUSER_UPDATE_ALLINFO=(int)65;     //修改商品所有信息（管理者）
	public final static int ONLYUSER_UPDATE_CUSTOMERINFO=(int)66;//修改顾客注册表信息（管理者）
	public final static int ONLYUSER_UPDATE_USERINFO=(int)67;   //修改厂家管理者注册表信息（管理者）

	//删除操作
	public final static int ONLYUSER_DELETE_SORTONE=(int)80;     //删除一级分类（管理者）
	public final static int ONLYUSER_DELETE_SORTTWO=(int)81;     //删除二级分类（管理者）
	public final static int ONLYUSER_DELETE_CLASSIFICATION=(int)82;  //删除商品分类（管理者）
	public final static int ONLYUSER_DELETE_PRODUCTBASIC=(int)83;//删除商品基本信息（管理者）
	public final static int ONLYUSER_DELETE_CURRENTLIST=(int)84; //删除商品所有物流信息（管理者）
	public final static int ONLYUSER_DELETE_CURRENTONCE=(int)85;//删除商品某次物流信息（管理者）

	//注册管理操作
	public final static int MANAGE_LOGIN=(int)192;     //管理总机登入
	public final static int MANAGE_SENDRANDOM=(int)193;     //管理者发送随机码给总机
	public final static int CUSTOMER_GETANOTHER_RANDOM=(int)194; //顾客请求另一个验证码

}
