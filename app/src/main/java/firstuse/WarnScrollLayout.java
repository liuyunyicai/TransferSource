package firstuse;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class WarnScrollLayout extends ViewGroup {

	private Scroller mScroller;
	private int mCurScreen;
	private VelocityTracker mVelocityTracker; // 用于判断甩动手势
	private float mLastMotionX;
	private static final int SNAP_VELOCITY = 600;
	private static final String TAG = "WarnScrollLayout";// 用于调试标记

	private OnViewChangeListener mOnViewChangeListener;

	public WarnScrollLayout(Context context) {
		super(context);
		init(context);
	}

	public WarnScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public WarnScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(context);
	}

	// 创建初始化
	private void init(Context context) {
		mScroller = new Scroller(context);
	}

	// 画面绘制
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed) {
			int ChildLeft = 0;// 左边界
			final int ChildCount = getChildCount();// 滑动界面总数

			for (int i = 0; i < ChildCount; i++) {
				final View ChildView = getChildAt(i);// 获得第i个界面
				if (ChildView.getVisibility() != View.GONE) {
					final int childWidth = ChildView.getMeasuredWidth();
					ChildView.layout(ChildLeft, 0, ChildLeft + childWidth,
							ChildView.getMeasuredHeight()); // 设置分布
					ChildLeft += childWidth; // 边界逐渐右加
				}
			}
		}
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);

		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
		scrollTo(mCurScreen * width, 0);
	}

	public void snapToScreen(int whichScreen) { // 转换屏幕

		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1)); // 控制范围
		if (getScrollX() != (whichScreen * getWidth())) {

			final int delta = whichScreen * getWidth() - getScrollX();

			mScroller.startScroll(getScrollX(), 0, delta, 0,
					Math.abs(delta) * 2);

			mCurScreen = whichScreen;
			invalidate(); // Redraw the layout

			if (mOnViewChangeListener != null) {
				mOnViewChangeListener.OnViewChange(mCurScreen);
			}
		}
	}

	public void snapToDestination() { // 转换到指定屏幕
		final int screenWidth = getWidth();

		final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
		snapToScreen(destScreen);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		final int action = event.getAction();
		final float x = event.getX();

		switch (action) {
			case MotionEvent.ACTION_DOWN:

				Log.i("", "onTouchEvent  ACTION_DOWN");

				if (mVelocityTracker == null) {
					mVelocityTracker = VelocityTracker.obtain();
					mVelocityTracker.addMovement(event);
				}

				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
				}

				mLastMotionX = x;
				break;

			case MotionEvent.ACTION_MOVE:
				int deltaX = (int) (mLastMotionX - x);

				if (IsCanMove(deltaX)) {
					if (mVelocityTracker != null) {
						mVelocityTracker.addMovement(event);
					}
					mLastMotionX = x;

					scrollBy(deltaX, 0);
				}
				break;

			case MotionEvent.ACTION_UP:
				int velocityX = 0;
				if (mVelocityTracker != null) {
					mVelocityTracker.addMovement(event);
					mVelocityTracker.computeCurrentVelocity(1000);
					velocityX = (int) mVelocityTracker.getXVelocity();
				}
				if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
					Log.e(TAG, "snap left");
					snapToScreen(mCurScreen - 1);
				} else if (velocityX < -SNAP_VELOCITY
						&& mCurScreen < getChildCount() - 1) {
					Log.e(TAG, "snap right");
					snapToScreen(mCurScreen + 1);
				} else {
					snapToDestination();
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}

				break;
		}

		return true;
	}

	private boolean IsCanMove(int deltaX)// 判断是否可移动
	{
		if (getScrollX() <= 0 && deltaX < 0) {
			return false;
		}
		if (getScrollX() >= (getChildCount() - 1) * getWidth() && deltaX > 0) {
			return false;
		}
		return true;
	}

	public void SetOnViewChangeListener(OnViewChangeListener listener) {
		mOnViewChangeListener = listener;
	}

}
