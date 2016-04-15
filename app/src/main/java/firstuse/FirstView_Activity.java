package firstuse;

import load_reg.LoadReg_Activity;
import mainview.demo.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

@SuppressLint("ResourceAsColor")
public class FirstView_Activity extends Activity implements
		OnViewChangeListener {
	private WarnScrollLayout mScrollLayout;
	private ImageView[] imgs;
	private int count;
	private int currentItem;
	private Button startBtn;
	private RelativeLayout mainRLayout;
	private LinearLayout pointLLayout;
	private LinearLayout leftLayout;
	private LinearLayout rightLayout;
	private LinearLayout animLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.warnscreen_layout);
		initView();
	}

	private void initView() {
		mScrollLayout = (WarnScrollLayout) findViewById(R.id.ScrollLayout);
		pointLLayout = (LinearLayout) findViewById(R.id.llayout);
		mainRLayout = (RelativeLayout) findViewById(R.id.mainRLayout);
		startBtn = (Button) findViewById(R.id.startBtn);
		startBtn.setOnClickListener(onClick);
		animLayout = (LinearLayout) findViewById(R.id.animLayout);
		leftLayout = (LinearLayout) findViewById(R.id.leftLayout);
		rightLayout = (LinearLayout) findViewById(R.id.rightLayout);
		count = mScrollLayout.getChildCount();
		imgs = new ImageView[count];
		for (int i = 0; i < count; i++) {
			imgs[i] = (ImageView) pointLLayout.getChildAt(i);
			imgs[i].setEnabled(true);
			imgs[i].setTag(i);
		}
		currentItem = 0;
		imgs[currentItem].setEnabled(false);
		mScrollLayout.SetOnViewChangeListener(this);
	}

	private View.OnClickListener onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.startBtn:
					mScrollLayout.setVisibility(View.GONE);
					pointLLayout.setVisibility(View.GONE);
					animLayout.setVisibility(View.VISIBLE);
					Animation leftOutAnimation = AnimationUtils.loadAnimation(
							getApplicationContext(), R.anim.translate_left);
					Animation rightOutAnimation = AnimationUtils.loadAnimation(
							getApplicationContext(), R.anim.translate_right);

					leftLayout.setAnimation(leftOutAnimation);
					rightLayout.setAnimation(rightOutAnimation);
					leftOutAnimation.setAnimationListener(new AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
							mainRLayout.setBackgroundResource(R.color.bgColor);
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							leftLayout.setVisibility(View.GONE);
							rightLayout.setVisibility(View.GONE);
							Intent intent = new Intent(FirstView_Activity.this,
									LoadReg_Activity.class);
							FirstView_Activity.this.startActivity(intent);
							FirstView_Activity.this.finish();
							overridePendingTransition(R.anim.zoom_out_enter,
									R.anim.zoom_out_exit);
						}
					});
					break;
			}
		}
	};

	public void OnViewChange(int position) {
		setcurrentPoint(position);
	}

	private void setcurrentPoint(int position) {
		if (position < 0 || position > count - 1 || currentItem == position) {
			return;
		}
		imgs[currentItem].setEnabled(true);
		imgs[position].setEnabled(false);
		currentItem = position;
	}
}
