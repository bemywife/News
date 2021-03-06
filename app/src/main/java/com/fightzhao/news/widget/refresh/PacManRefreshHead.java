package com.fightzhao.news.widget.refresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.WindowManager;

import com.fightzhao.news.R;
import com.socks.library.KLog;


/**
 * ClassName: PacManRefreshHead<p>
 * Author:oubowu<p>
 * Fuction: 吃豆人效果刷新头部<p>
 * CreateDate:2016/2/12 22:55<p>
 * UpdateUser:<p>
 * UpdateDate:<p>
 */
public class PacManRefreshHead extends RefreshHead {

    // 画笔
    private Paint mPaint;
    // 方块的长宽
    private int mPacManRadius;
    // view的宽度
    private int mWidth;
    // view的长度
    private int mHeight;
    // 获取屏幕宽高的Point
    private Point mScreenSize = new Point();
    // 总的长度
    private int mTotalLength;
    // 用于做加载动画的值动画
    private ValueAnimator mLoadingAnimator1;
    private ValueAnimator mLoadingAnimator2;
    private ValueAnimator mLoadingAnimator3;
    private ValueAnimator mLoadingAnimator4;
    private AnimatorSet mLoadAnimatorSet;

    private int mPacManXoffset;

    private float mEatAngle;

    private boolean mStartEat;

    private RectF mRectF = new RectF();
    private int mStep;
    private int mRotateSweep = -1;
    private int mBeanRadius;

    public PacManRefreshHead(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PacManRefreshHead(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        //        mPaint.setColor(ContextCompat.getColor(context, R.color.material_black55));

        if (attrs != null) {

            final TypedArray typedArray = context
                    .obtainStyledAttributes(attrs, R.styleable.PacManRefreshHead);
            mPaint.setColor(typedArray.getColor(R.styleable.PacManRefreshHead_pacIconColor,
                    ContextCompat.getColor(context, R.color.material_black55)));
            typedArray.recycle();
        }

        // 画笔填充
        mPaint.setStyle(Paint.Style.FILL);

        // 获取屏幕宽高
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(mScreenSize);

        mEatBeanPos = new int[]{-1, -1, -1, -1, -1};

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 算出宽度
        if (mWidth == 0) {
            mWidth = measureSize(widthMeasureSpec,
                    mScreenSize.x + getPaddingLeft() + getPaddingRight());
            mPaint.setStrokeWidth(mWidth * 1.0f / 200);

            mPacManRadius = (int) (mWidth * 1.0f / 50);

            mTotalLength = mPacManRadius * 12;

            mBeanRadius = mPacManRadius / 3;

            mHeight = measureSize(heightMeasureSpec,
                    mPacManRadius * 4 + getPaddingTop() + getPaddingBottom());

            if (mPacManRadius * 4 + getPaddingTop() + getPaddingBottom() < 0) {
                mHeight = 0;
            }
        }

        setMeasuredDimension(mWidth, mHeight);

    }

    // 测量尺寸
    private int measureSize(int measureSpec, int defaultSize) {

        final int mode = MeasureSpec.getMode(measureSpec);
        final int size = MeasureSpec.getSize(measureSpec);

        if (mode == MeasureSpec.EXACTLY) {
            return size;
        } else if (mode == MeasureSpec.AT_MOST) {
            return Math.min(size, defaultSize);
        } else if (mode == MeasureSpec.UNSPECIFIED) {
            // 一般都是父控件是AdapterView，通过measure方法传入的模式
            return defaultSize;
        }

        return size;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mStep % 2 != 0 && mPacManXoffset <= mPacManRadius * 11) {
            // 开始闭嘴吃豆豆
            mEatAngle = (mPacManXoffset - mPacManRadius * mStep) * (45.0f / mPacManRadius * 1.5f);
        } else {
            mEatAngle = 0;
        }

        mRectF.set((mWidth - mTotalLength) / 2 + mPacManXoffset, mPacManRadius + getPaddingTop(),
                (mWidth - mTotalLength) / 2 + mPacManRadius * 2 + mPacManXoffset,
                mPacManRadius * 3 + getPaddingTop());
        canvas.drawArc(mRectF, 45 - mEatAngle - mRotateSweep, 360 - (45 - mEatAngle) * 2, true,
                mPaint);

        for (int i = 0; i < 5; i++) {
            if (mStep != 0 && (mStep - 1) / 2 == i && 45 - mEatAngle <= Math
                    .toDegrees(Math.atan(0.25f)) || mEatBeanPos[i] == i) {
                // 通过mStep和角度来判断是否吃了豆豆
                mEatBeanPos[i] = i;
                continue;
            }
            canvas.drawCircle(mRectF.right - mPacManXoffset + mPacManRadius * (1 + 2 * i),
                    mRectF.top + mPacManRadius, mBeanRadius, mPaint);
        }

    }

    private int[] mEatBeanPos;


    @Override
    public boolean isLoading() {
        return mLoadAnimatorSet != null && mLoadAnimatorSet.isRunning();
    }

    @Override
    public boolean isReadyLoad() {
        return getPaddingTop() >= 0;
    }

    @Override
    public void performLoaded() {
        if (mLoadAnimatorSet != null && mLoadAnimatorSet.isRunning()) {

            mLoadingAnimator1.removeAllListeners();
            mLoadingAnimator1.removeAllUpdateListeners();
            mLoadingAnimator1.cancel();

            mLoadingAnimator2.removeAllListeners();
            mLoadingAnimator2.removeAllUpdateListeners();
            mLoadingAnimator2.cancel();

            mLoadingAnimator3.removeAllListeners();
            mLoadingAnimator3.removeAllUpdateListeners();
            mLoadingAnimator3.cancel();

            mLoadingAnimator4.removeAllListeners();
            mLoadingAnimator4.removeAllUpdateListeners();
            mLoadingAnimator4.cancel();

            mLoadAnimatorSet.removeAllListeners();
            mLoadAnimatorSet.cancel();

            mStep = 0;
            mEatAngle = 0;
            mRotateSweep = 0;
            mPacManXoffset = 0;
            for (int i = 0; i < mEatBeanPos.length; i++) {
                mEatBeanPos[i] = -1;
            }
            postInvalidate();
        }
    }

    @Override
    public void performLoading() {

        post(new Runnable() {
            @Override
            public void run() {
                if (mLoadAnimatorSet != null && mLoadAnimatorSet.isRunning()) {
                    KLog.e("动画还在运行，不操作");
                    return;
                }
                KLog.e("开始吃豆豆咯");
                mLoadingAnimator1 = new ValueAnimator();
                mLoadingAnimator1.setIntValues(0, mPacManRadius * 12);
                mLoadingAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if ((int) animation.getAnimatedValue() != 0) {
                            mStep = (int) animation.getAnimatedValue() / mPacManRadius;
                        }
                        mPacManXoffset = (int) animation.getAnimatedValue();
                        postInvalidate();
                    }
                });
                mLoadingAnimator1.setDuration(1500);

                mLoadingAnimator2 = new ValueAnimator();
                mLoadingAnimator2.setIntValues(0, 180);
                mLoadingAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mRotateSweep = (int) animation.getAnimatedValue();
                        for (int i = 0; i < mEatBeanPos.length; i++) {
                            mEatBeanPos[i] = -1;
                        }
                        mBeanRadius = (int) (mPacManRadius / 3 * animation.getAnimatedFraction());
                        postInvalidate();
                    }
                });
                mLoadingAnimator2.setDuration(200);

                mLoadingAnimator3 = new ValueAnimator();
                mLoadingAnimator3.setIntValues(mPacManRadius * 12, 0);
                mLoadingAnimator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if ((int) animation.getAnimatedValue() != 0) {
                            mStep = (int) animation.getAnimatedValue() / mPacManRadius;
                        }
                        mPacManXoffset = (int) animation.getAnimatedValue();
                        postInvalidate();
                    }
                });
                mLoadingAnimator3.setDuration(1500);

                mLoadingAnimator4 = new ValueAnimator();
                mLoadingAnimator4.setIntValues(180, 0);
                mLoadingAnimator4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mRotateSweep = (int) animation.getAnimatedValue();
                        for (int i = 0; i < mEatBeanPos.length; i++) {
                            mEatBeanPos[i] = -1;
                        }
                        mBeanRadius = (int) (mPacManRadius / 3 * animation.getAnimatedFraction());
                        postInvalidate();
                    }
                });
                mLoadingAnimator4.setDuration(200);

                mLoadAnimatorSet = new AnimatorSet();
                mLoadAnimatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mLoadAnimatorSet.start();
                    }
                });
                mLoadAnimatorSet
                        .playSequentially(mLoadingAnimator1, mLoadingAnimator2, mLoadingAnimator3,
                                mLoadingAnimator4);
                mLoadAnimatorSet.start();
            }
        });

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mLoadAnimatorSet != null && mLoadAnimatorSet.isRunning()) {

            mLoadingAnimator1.removeAllListeners();
            mLoadingAnimator1.removeAllUpdateListeners();
            mLoadingAnimator1.cancel();

            mLoadingAnimator2.removeAllListeners();
            mLoadingAnimator2.removeAllUpdateListeners();
            mLoadingAnimator2.cancel();

            mLoadingAnimator3.removeAllListeners();
            mLoadingAnimator3.removeAllUpdateListeners();
            mLoadingAnimator3.cancel();

            mLoadingAnimator4.removeAllListeners();
            mLoadingAnimator4.removeAllUpdateListeners();
            mLoadingAnimator4.cancel();

            mLoadAnimatorSet.removeAllListeners();
            mLoadAnimatorSet.cancel();

            mStep = 0;
            mEatAngle = 0;
            mRotateSweep = 0;
            mPacManXoffset = 0;
            for (int i = 0; i < mEatBeanPos.length; i++) {
                mEatBeanPos[i] = -1;
            }
        }
    }

    /**
     * 下拉的时候，方块矩阵的下降上升效果
     *
     * @param ratio 比值
     */
    @Override
    public void performPull(float ratio) {

    }

}
