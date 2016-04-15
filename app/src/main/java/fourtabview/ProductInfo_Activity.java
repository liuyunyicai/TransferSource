package fourtabview;

import mainview.demo.R;
import mapview.BaseMapDemo;
import product_info.InfoOne_Activity;
import product_info.InfoThree_Activity;
import product_info.InfoTwo_Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class ProductInfo_Activity extends TabActivity
{
    //标签配置
    public static TabHost myTabHost;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.productinfo_layout);
        //判断是否需要跳转到登录界面       
        //初始化标签
        Resources res = getResources();//得到Drawables的资源对象
        myTabHost= getTabHost();       //TabHost 的Activity
        myTabHost.setup();
        //添加TAB

        //第一个tab
        TabSpec myTab1=myTabHost.newTabSpec("tabone");
        myTab1.setIndicator("商品信息",res.getDrawable(R.drawable.infotagone_layout));
        Intent intent1=new Intent(ProductInfo_Activity.this,InfoOne_Activity.class);
        myTab1.setContent(intent1);
        myTabHost.addTab(myTab1);

        //第二个tab
        TabSpec myTab_map=myTabHost.newTabSpec("tabmap");
        myTab_map.setIndicator("溯源流程",res.getDrawable(R.drawable.mapinfo_layout));
        Intent intent_map=new Intent(ProductInfo_Activity.this,BaseMapDemo.class);
        myTab_map.setContent(intent_map);
        myTabHost.addTab(myTab_map);

        //第二个tab
        TabSpec myTab2=myTabHost.newTabSpec("tabtwo");
        myTab2.setIndicator("详细评价",res.getDrawable(R.drawable.infotagtwo_layout));
        Intent intent2=new Intent(ProductInfo_Activity.this,InfoTwo_Activity.class);
        myTab2.setContent(intent2);
        myTabHost.addTab(myTab2);

        //第三个tab
        TabSpec myTab3=myTabHost.newTabSpec("tabthree");
        myTab3.setIndicator("相关产品",res.getDrawable(R.drawable.infotagthree_layout));
        Intent intent3=new Intent(ProductInfo_Activity.this,InfoThree_Activity.class);
        myTab3.setContent(intent3);
        myTabHost.addTab(myTab3);

        myTabHost.setCurrentTab(0);
        updateTab(myTabHost);//初始化Tab的颜色，和字体的颜色
        myTabHost.setOnTabChangedListener(new OnTabChangedListener()); // 选择监听器


    }

    //对tab添加监听器
    class OnTabChangedListener implements OnTabChangeListener {
        @Override
        public void onTabChanged(String tabId) {
            myTabHost.setCurrentTabByTag(tabId);
            System.out.println("tabid " + tabId);
            System.out.println("curreny after: " + myTabHost.getCurrentTabTag());
            updateTab(myTabHost);
        }
    }
    //对tab状态进行更新
    private void updateTab(final TabHost tabHost) {
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            View view = tabHost.getTabWidget().getChildAt(i);
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextSize(16);
            tv.setTypeface(Typeface.SERIF, 2); // 设置字体和风格  
            if (tabHost.getCurrentTab() == i) {//选中  
                view.setBackgroundDrawable(getResources().getDrawable(R.drawable.tabbg_pressed));//选中后的背景  
                tv.setTextColor(this.getResources().getColorStateList(
                        android.R.color.black));
            } else {//不选中  
                view.setBackgroundDrawable(getResources().getDrawable(R.drawable.tabbg));//非选择的背景  
                tv.setTextColor(this.getResources().getColorStateList(
                        android.R.color.white));
            }
        }
    }
}
