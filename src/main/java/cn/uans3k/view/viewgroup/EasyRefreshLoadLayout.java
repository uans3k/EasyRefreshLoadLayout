package cn.uans3k.view.viewgroup;


import android.content.Context;

import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import cn.uans3k.utils.LogUtils;
import cn.uans3k.utils.MoveTool;


/**
 * Created by Administrator on 2015/10/10.
 */
public class EasyRefreshLoadLayout extends ViewGroup {

    protected static final String TAG = "EasyRefreshLoadLayout";

    private View mMainView;
    protected View mTopView;
    protected View mEndView;
    /*
        statue
    */
    protected boolean mPressDown;
    protected boolean mVerticalMove;
    protected boolean mOnLoadding;
    protected boolean mHasEstimate;

    /*
        user  params
     */
    protected boolean mTopPinMain = false;
    protected boolean mEndPinMain = false;
    protected float mResistance = 2f;
    protected long mDuration = 1000;



    /*
       run params
     */
    protected PointF mLastTouch=new PointF();
    protected PointF mCurrTouch=new PointF();


    protected int mScrollY = 0;
    private int mTopScrollHeight;
    private int mTopOverHeight;
    private int mEndScrollHeight;
    private int mEndOverHeight;

    private UIHandler mTopHandler;
    private UIHandler mEndHandler;
    private Indicator mTopIndicator;
    private Indicator mEndIndicator;


    protected OnLoadMoreListener mLoadrMoreCallback = null;
    protected OnRefreshListener mRefreshCallback = null;
    private int mTopRefreshHeight;
    private int mEndRefreshHeight;

    /*
        listenr define and set
     */

    public static interface OnLoadMoreListener {
        public void onLoadMore();
    }

    public static interface OnRefreshListener {
        public void onRefresh();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener callback){
        this.mLoadrMoreCallback=callback;
    }
    public void setOnRefreshListener(OnRefreshListener callback){
        this.mRefreshCallback=callback;
    }

    /*

    constructor
     */
    public EasyRefreshLoadLayout(Context context) {
        this(context, null);
    }

    public EasyRefreshLoadLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasyRefreshLoadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 1) {
            throw new IllegalArgumentException("must have one child only in XML");
        }
        mMainView = getChildAt(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mMainView != null) {
            measureChildWithMargins(mMainView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
        if (mTopView != null) {
            measureChildWithMargins(mTopView, widthMeasureSpec, 0, heightMeasureSpec, 0);

        }
        if (mEndView != null) {
            measureChildWithMargins(mEndView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutMain();
        layoutTop();
        layoutEnd();
    }

    private void layoutMain() {
        MarginLayoutParams layoutParams = (MarginLayoutParams) mMainView.getLayoutParams();
        int l = layoutParams.leftMargin;
        int t = layoutParams.topMargin;
        int r = l + mMainView.getMeasuredWidth();
        int b = t + mMainView.getMeasuredHeight();
        mMainView.layout(l, t, r, b);
    }


    private void layoutTop() {
        if (mTopView != null) {
            MarginLayoutParams layoutParams = (MarginLayoutParams) mTopView.getLayoutParams();
            int l = layoutParams.leftMargin;
            int t = layoutParams.topMargin - mTopView.getMeasuredHeight();
            int r = l + mTopView.getMeasuredWidth();
            int b = t + mTopView.getMeasuredHeight();
            mTopScrollHeight = b - t;
            if (mTopIndicator != null) {
                mTopOverHeight = mTopIndicator.getOverBoundHeight(mTopScrollHeight);
                mTopRefreshHeight = mTopIndicator.getRefreshHeight(mTopScrollHeight);
            }
            mTopView.layout(l, t, r, b);
        }
    }




    private void layoutEnd() {
        if (mEndView != null) {
            MarginLayoutParams layoutParams = (MarginLayoutParams) mEndView.getLayoutParams();
            int l = layoutParams.leftMargin;
            int t = getMeasuredHeight() + layoutParams.topMargin;
            int r = l + mEndView.getMeasuredWidth();
            int b = t + mEndView.getMeasuredHeight();
            mEndView.layout(l, t, r, b);

            mEndScrollHeight = b - t;
            if (mEndIndicator != null) {
                mEndOverHeight = mEndIndicator.getOverBoundHeight(mEndScrollHeight);
                mEndRefreshHeight = mEndIndicator.getRefreshHeight(mEndScrollHeight);
            }
        }
    }

    public boolean dispatchTouchEventSuper(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    protected void sendCancelEvent(MotionEvent ev) {
        MotionEvent e = MotionEvent.obtain(ev.getDownTime(), ev.getEventTime(),
                MotionEvent.ACTION_CANCEL, ev.getX(), ev.getY(),
                ev.getMetaState());
        dispatchTouchEventSuper(e);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        int action = ev.getAction();

        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onRelease(ev);
                break;
            case MotionEvent.ACTION_DOWN:
                onDown(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                onMove(ev);
                break;
        }

        return true;
    }


    private void onDown(MotionEvent ev) {
        mPressDown = true;
        mVerticalMove = false;
        mHasEstimate = false;
        mLastTouch.x = ev.getX();
        mLastTouch.y = ev.getY();
        dispatchTouchEventSuper(ev);
    }


    private void onMove(MotionEvent ev) {
        if (mPressDown) {
            mCurrTouch.x = ev.getX();
            mCurrTouch.y = ev.getY();
            float dx = mCurrTouch.x - mLastTouch.x;
            float dy = mCurrTouch.y - mLastTouch.y;
            mLastTouch.x = mCurrTouch.x;
            mLastTouch.y = mCurrTouch.y;
            if (!mHasEstimate) {
                mHasEstimate = true;
                if (Math.abs(dy) > Math.abs(dx)) {
                    mVerticalMove = true;
                } else {
                    LogUtils.v(TAG,"not a vitical action,pass event");
                    mPressDown = false;
                    dispatchTouchEventSuper(ev);
                    return;
                }
            }

            if (mVerticalMove && !mOnLoadding) {
                if (mTopIndicator != null) {
                    if (mTopIndicator.canRefreshOrLoad(mMainView)//
                            && (mScrollY > 0 || (mScrollY == 0 && dy > 0))) {
                        //top scroll,and send cancel event to children to let children don't accept touch action
                        scrollTop(dy);
                        sendCancelEvent(ev);
                        return;
                    }
                }
                if (mEndIndicator != null) {
                    if (mEndIndicator.canRefreshOrLoad(mMainView)//
                            && (mScrollY < 0 || (mScrollY == 0 && dy < 0))) {
                        //end scroll and send cancel event to children to let children don't accept touch action
                        scrollEnd(dy);
                        sendCancelEvent(ev);
                        return;
                    }
                }
            }
            //can't scroll
            LogUtils.v(TAG,"vitical action,but not refresh or loadmore,pass event");
            dispatchTouchEventSuper(ev);
        } else {
            LogUtils.v(TAG,"not a vitical,pass event");
            dispatchTouchEventSuper(ev);
        }
    }

    /*
     *do release
     *
     */
    private void onRelease(MotionEvent ev) {
        dispatchTouchEventSuper(ev);
        if (mScrollY > 0) {
            if (mScrollY >= mTopOverHeight) {
                topOverRelease();
            } else {
                resetTop(false);
            }
        } else if (mScrollY < 0 && mEndView != null) {
            if (mScrollY <= -mEndOverHeight) {
                endOverRelease();
            } else {
                resetEnd(false);
            }
        }
    }


    protected void topOverRelease() {
        if (mScrollY > 0 && mTopView != null) {

            ValueAnimator animator = ValueAnimator.ofInt(mScrollY, mTopRefreshHeight);
            long duration = (long) (mDuration * ((float) Math.abs(mScrollY - mTopRefreshHeight) / (float) mTopScrollHeight));
            animator.setDuration(duration);


            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mScrollY = (int) animation.getAnimatedValue();
                    LogUtils.v(TAG, "Top overReleasing! --- currHeigth : %s", mScrollY);
                    ViewHelper.setTranslationY(mTopView, mScrollY);
                    scrollMain(mScrollY,mTopPinMain);
                }
            });

            animator.addListener(new BaseAnimator() {
                @Override
                public void onAnimationStart(Animator animator) {
                    if (mTopHandler != null) {
                        mTopHandler.OnOverRelease(mTopView);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    LogUtils.v(TAG, "Top overReleasing Compelte!now onRefresh ing.. --- currHeigth : %s ,targetHeight : %s", mScrollY,mTopRefreshHeight);
                    if (mRefreshCallback != null) {
                        mRefreshCallback.onRefresh();
                    }
                }
            });


            mOnLoadding = true;
            animator.start();
        }
    }

    protected void resetTop(final boolean isRefresh) {
        if (mScrollY > 0 && mTopView != null) {
            ValueAnimator animator = ValueAnimator.ofInt(mScrollY, 0);
            long duration = (long) (mDuration * ((float) (mScrollY) / (float) (mEndScrollHeight)));
            animator.setDuration(duration);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mScrollY = (int) valueAnimator.getAnimatedValue();

                    ViewHelper.setTranslationY(mTopView, mScrollY);
                    scrollMain(mScrollY,mTopPinMain);

                }
            });
            animator.addListener(new BaseAnimator() {
                @Override
                public void onAnimationStart(Animator animator) {
                    if (mTopHandler != null) {
                        if (isRefresh) {
                            mTopHandler.OnReset(mTopView);
                        } else {
                            mTopHandler.OnRelease(mTopView);
                        }
                    }
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (isRefresh) {
                        mOnLoadding = false;
                    }
                }
            });

            animator.start();
        }
    }

    private void endOverRelease() {
        if (mScrollY < 0 && mEndView != null) {
            ValueAnimator animator = ValueAnimator.ofInt(mScrollY, -mEndRefreshHeight);
            long duration = (long) (mDuration * ((float) Math.abs(-mScrollY - mEndRefreshHeight) / (float) mTopScrollHeight));
            animator.setDuration(duration);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mScrollY = (int) animation.getAnimatedValue();

                    LogUtils.v(TAG, "End overReleasing! --- currHeight : %s  , targetHeigt : %s",-mScrollY,mEndRefreshHeight);

                    ViewHelper.setTranslationY(mEndView, mScrollY);
                    scrollMain(mScrollY,mEndPinMain);
                }
            });

            animator.addListener(new BaseAnimator() {
                @Override
                public void onAnimationStart(Animator animator) {
                    if (mEndHandler != null) {
                        mEndHandler.OnOverRelease(mEndView);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    LogUtils.v(TAG, "End overReleasing compelte!now onLoadMore ing... --- currHeigth : %s",mScrollY);
                    if (mLoadrMoreCallback != null) {
                        mLoadrMoreCallback.onLoadMore();
                    }
                }
            });

            mOnLoadding = true;
            animator.start();
        }
    }

    private void resetEnd(final boolean isLoadMore) {
        if (mScrollY < 0 && mTopView != null) {
            ValueAnimator animator = ValueAnimator.ofInt(mScrollY, 0);
            long duration = (long) (mDuration * ((float) (-mScrollY) / (float) (mEndScrollHeight)));
            animator.setDuration(duration);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mScrollY = (int) valueAnimator.getAnimatedValue();
                    ViewHelper.setTranslationY(mEndView, mScrollY);
                    scrollMain(mScrollY,mEndPinMain);
                }
            });
            animator.addListener(new BaseAnimator() {
                @Override
                public void onAnimationStart(Animator animator) {
                    if (mTopHandler != null) {
                        if (isLoadMore) {
                            mEndHandler.OnReset(mEndView);
                        } else {
                            mEndHandler.OnRelease(mEndView);
                        }
                    }
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (isLoadMore) {
                        mOnLoadding = false;
                    }
                }
            });


            animator.start();
        }
    }


    public void refreshCompelte() {
        resetTop(true);
    }

    public void loadMoreComelte() {
        resetEnd(true);
    }

/*
    scroll exe;
 */

    private void scrollTop(float dy) {

        if (mScrollY >= mTopScrollHeight) {
            LogUtils.v(TAG, "Top scrolling,and has over bound ! --- overHieght : %s .currHeigth : %s", mTopOverHeight, mScrollY);
            dy /= mResistance;
        } else if (mScrollY >= 0) {
            LogUtils.v(TAG, "Top scrolling,and has not over bound ! --- overHieght : %s .currHeigth : %s", mTopOverHeight, mScrollY);
        }

        int tempY = MoveTool.getBound(mScrollY, (int) dy, 0, mTopScrollHeight);
        if (tempY != mScrollY) {
            int deltaY = mScrollY - tempY;
            mScrollY = tempY;
            ViewHelper.setTranslationY(mTopView, tempY);
            scrollMain(mScrollY,mTopPinMain);
            if (mTopHandler != null) {
                if (mScrollY >= mTopOverHeight) {
                    mTopHandler.OnOverScroll(mTopView, mScrollY, mScrollY - mTopOverHeight, deltaY);
                } else if (mScrollY >= 0) {
                    mTopHandler.OnScroll(mTopView, mScrollY, deltaY);
                }
            }
        }
    }

    private void scrollEnd(float dy) {
        if (mScrollY <= -mEndOverHeight) {
            LogUtils.v(TAG, "End scrolling,and has over bound ! --- overHieght : %s .currHeigth : %s", mEndOverHeight, -mScrollY);
            dy /= mResistance;
        } else if (mScrollY <= 0) {
            LogUtils.v(TAG, "End scrolling,and has not over bound ! --- overHieght : %s .currHeigth : %s", mEndOverHeight, -mScrollY);
        }
        int tempY = MoveTool.getBound(mScrollY, (int) dy, -mEndScrollHeight, 0);
        if (tempY != mScrollY) {
            int deltaY = mScrollY - tempY;
            mScrollY = tempY;
            ViewHelper.setTranslationY(mEndView, tempY);
            scrollMain(mScrollY,mEndPinMain);
            if (mEndHandler != null) {
                if (mScrollY <= -mEndOverHeight) {
                    mEndHandler.OnOverScroll(mEndView, -mScrollY, -mScrollY - mEndOverHeight, deltaY);
                } else if (mScrollY <= 0) {
                    mEndHandler.OnScroll(mEndView, -mScrollY, deltaY);
                }
            }

        }
    }

    protected void scrollMain(int scrollY,boolean isPinMain) {
        if (!isPinMain) {
            ViewHelper.setTranslationY(mMainView, scrollY);
        }
    }


    /*
        params set
     */
    public void setTopPinMain(boolean topPinMain) {
        this.mTopPinMain = topPinMain;
    }
    public void setEndPinMain(boolean endPinMain){
        this.mEndPinMain=endPinMain;
    }


    /*
     *
     *  Indicator to indicator which time to refreash
     */
    public static interface Indicator {
        public boolean canRefreshOrLoad(View mainView);

        public int getOverBoundHeight(int scrollHeight);

        public int getRefreshHeight(int scrollHeight);
    }

    public static interface UIHandler {
        public void OnScroll(View scrollView, int mScrollY, int deltaY);

        public void OnOverScroll(View scrollView, int mScrollY, int overScrollY, int deltaY);

        public void OnRelease(View scrollView);

        public void OnOverRelease(View scrollView);

        public void OnReset(View scrollView);
    }

    public void addTop(View topView, UIHandler handler, Indicator indicator) {
        if (topView == null || indicator == null) {
            throw new IllegalArgumentException("topView or indicator can't be set null");
        }
        this.mTopView = topView;
        addView(mTopView);
        mTopHandler = handler;
        mTopIndicator = indicator;
        requestLayout();
    }

    public void addTop(int resourceId, UIHandler handler, Indicator indicator) {
        View topView = LayoutInflater.from(getContext()).inflate(resourceId, this, false);
        addTop(topView,handler,indicator);
    }

    public void addEnd(View endView, UIHandler handler, Indicator indicator) {
        if (endView == null || indicator == null) {
            throw new IllegalArgumentException("endView or indicator can't be set null");
        }
        this.mEndView = endView;
        addView(mEndView);
        mEndHandler = handler;
        mEndIndicator = indicator;
        requestLayout();
    }

    public void addEnd(int resourceId, UIHandler handler, Indicator indicator) {
        View endView = LayoutInflater.from(getContext()).inflate(resourceId, this, false);
        addEnd(endView, handler, indicator);
    }

    /*
                ensure child get MarginLayoutParams
             */
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p != null && p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(
            ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        @SuppressWarnings({"unused"})
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    protected abstract class BaseAnimator implements Animator.AnimatorListener {


        @Override
        public void onAnimationCancel(Animator animator) {
        }

        @Override
        public void onAnimationRepeat(Animator animator) {
        }
    }
}
