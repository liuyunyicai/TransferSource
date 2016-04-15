package loading_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

import mainview.demo.R;

/**
 * Created by admin on 2016/2/24.
 * 自定义加载条
 */
public class LoadingProgressBar extends View {
    private static final String TAG = "TAG";

    private int width, height; // 控件的宽与高
    private double bar_angle;     // 弧形的角度
    private float bar_height;  // 线的高度
    private int bar_color;     // 线的颜色
    private int bar_during;    // 加载的时长
    private int circle_color;  // 圆球的颜色
    private float circle_radius; // 圆球的半径

    private int radius;  // 半径
    private double swap_angle; // 当前扫过的角度
    private int temp_dis;

    private double angle_step;

    // 默认值
    private static final int DEFAULT_BAR_ANGLE = 150;
    private static final float DEFAULT_BAR_HEIGHT = 4.0f;
    private static final int DEFAULT_BAR_COLOR = Color.BLUE;
    private static final int DEFAULT_BAR_DURING = 3 * 1000;

    private Paint mPaint;

    private long start_time;

    private ArrayList<LoadingListener> listeners = new ArrayList<LoadingListener>();

    public LoadingProgressBar(Context context) {
        super(context);
    }

    public LoadingProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 获取自定义的属性值
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingProgressBarAttr);

        bar_angle = typedArray.getInteger(R.styleable.LoadingProgressBarAttr_bar_angle, DEFAULT_BAR_ANGLE);
        bar_height = typedArray.getDimension(R.styleable.LoadingProgressBarAttr_bar_height, DEFAULT_BAR_HEIGHT);
        bar_color = typedArray.getColor(R.styleable.LoadingProgressBarAttr_bar_color, DEFAULT_BAR_COLOR);
        bar_during = typedArray.getInteger(R.styleable.LoadingProgressBarAttr_bar_during, DEFAULT_BAR_DURING);

        circle_radius = typedArray.getDimension(R.styleable.LoadingProgressBarAttr_circle_radius, 10);
        circle_color = typedArray.getColor(R.styleable.LoadingProgressBarAttr_circle_color, Color.RED);
        typedArray.recycle();

        angle_step = bar_angle / (bar_during);

        // 定义Paint
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(bar_color);
        mPaint.setStrokeWidth(bar_height); // 设置线宽

        start_time = System.currentTimeMillis();
        // 通知Listener动画开始
        if (listeners != null) {
            for (LoadingListener listener : listeners) {
                listener.onAnimationStart();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 计算height大小
        width = MeasureSpec.getSize(widthMeasureSpec);
        radius = (int)((width / 2) / (Math.sin(angleToPi(bar_angle / 2)) ));
        temp_dis = (int)((width / 2) / (Math.tan(angleToPi(bar_angle / 2))));
        height = radius - temp_dis;
        setMeasuredDimension((int) (width + circle_radius * 2), (int) (height + circle_radius * 2));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (swap_angle <= bar_angle) {
            // 绘制动画
            int midX;
            int midY;
            double half_angle = bar_angle / 2;

            // 根据几何公式计算圆所在位置
            if (swap_angle <= half_angle) {
                midX = (int)((width / 2) - radius * Math.sin(angleToPi(half_angle - swap_angle)));
                midY = (int)(radius * Math.cos(angleToPi(half_angle - swap_angle)) - temp_dis);
            } else {
                midX = (int)((width / 2) + radius * Math.sin(angleToPi(swap_angle - half_angle)));
                midY = (int)(radius * Math.cos(angleToPi(swap_angle - half_angle)) - temp_dis);
            }

            midY += circle_radius;
            // 绘制曲线
            mPaint.setColor(bar_color);
            canvas.drawLine(midX, midY, width, circle_radius, mPaint);

            // 绘制中间圆心
            mPaint.setColor(circle_color);
            canvas.drawLine(0, circle_radius, midX, midY, mPaint);
            canvas.drawCircle(midX, midY, circle_radius, mPaint);

            // 定时刷新
            swap_angle = angle_step * (System.currentTimeMillis() - start_time);
            invalidate();
        } else {
            mPaint.setColor(circle_color);
            canvas.drawLine(0, circle_radius, width, circle_radius, mPaint);

            // 通知Listener动画结束
            if (listeners != null) {
                for (LoadingListener listener : listeners) {
                    listener.onAnimationEnd();
                }
            }
        }
    }

    // 计算角度
    private double angleToPi(double angle) {
        return angle * Math.PI / 180;
    }

    // 添加Listener判断动画是否结束
    public interface LoadingListener {
        void onAnimationStart();
        void onAnimationEnd();
    }

    // 添加删除Listener
    public void setLoadingListener(LoadingListener listener) {
        if (listeners != null)
            listeners.add(listener);
    }

    public void removeLoadingListener(LoadingListener listener) {
        if (listener != null)
            listeners.remove(listener);
    }
}
