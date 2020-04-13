package com.example.xqq.myapplication.refreshlib.RefreshViews;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;

import com.example.xqq.myapplication.R;
import com.example.xqq.myapplication.refreshlib.RefreshViews.RefreshWrap.Base.RefreshOuterHanderImpl;

import static java.lang.Math.signum;


public class RefreshLayout extends FrameLayout implements NestedScrollingParent, ValueAnimator.AnimatorUpdateListener, Runnable {
    public static final String TAG = "RefreshLayout";
    private NestedScrollingParentHelper helper;

    /**
     * 头部，尾部，中间的滑动控件
     */
    private View mHeader, mFooter, mScroll;

    /**
     * 滑动距离
     */
    private int scrolls = 0;

    /**
     * 刷新状态
     */
    State state = State.IDEL;

    private ValueAnimator valueAnimator;

    /**
     * 属性解析 保存类
     */
    private AttrsUtils attrsUtils;

    /**
     * 方向
     */
    public enum Orentation {
        HORIZONTAL, VERTICAL
    }

    /**
     * 刷新状态
     */
    public enum State {
        //正在刷新
        REFRESHING,
        //正在加载
        LOADING,
        //刷新完成位置
        REFRESHCOMPLETE,
        //加载完成位置
        LOADINGCOMPLETE,
        //拉头部
        PULL_HEADER,
        //拉尾部
        PULL_FOOTER,
        //闲置
        IDEL
    }

    public RefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public RefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        attrsUtils = new AttrsUtils();
        attrsUtils.ParseAttrs(context, attrs);
        try {
            if (mHandler == null) {
                mHandler = (BaseRefreshHeaderAndFooterHandler) AttrsUtils.builder.defaultRefreshWrap.newInstance();
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView(getContext());
    }

    /**
     * 初始化当前的布局
     * @param context
     */
    private void initView(Context context) {
        helper = new NestedScrollingParentHelper(this);
        ViewCompat.setNestedScrollingEnabled(this, true);
        LayoutInflater inflater = LayoutInflater.from(context);
        mScroll=getChildAt(0);
        if(!(mScroll instanceof NestedScrollingChild2)){
            throw new RuntimeException("contentChild must be NestedScrollingChild2!");
        }
            if (attrsUtils.CANHEADER) {
                mHeader = inflater.inflate(attrsUtils.HEADER_LAYOUTID, this, false);
                addView(mHeader);
            }
            if (attrsUtils.CANFOOTR) {
                mFooter = inflater.inflate(attrsUtils.FOOTER_LAYOUTID, this, false);
                addView(mFooter);
            }

        initAnimator();

    }

    /**
     * 初始化动画
     */
    private void initAnimator() {
        valueAnimator = ValueAnimator.ofInt();
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d("xqq","onLayout");
        if (top == bottom || left == right) {
            return;
        }
        LayoutParams layoutParams = (LayoutParams) mScroll.getLayoutParams();
        right = right - left - getPaddingRight();
        bottom = bottom - top - getPaddingBottom();
        top = getPaddingTop();
        left = getPaddingLeft();
        mScroll.layout(left + layoutParams.leftMargin, top + layoutParams.topMargin, right - layoutParams.rightMargin, bottom - layoutParams.bottomMargin);

        if (mHeader != null) {
            LayoutParams headerParams = (LayoutParams) mScroll.getLayoutParams();

            if (attrsUtils.mFlingmax == -1) {
                attrsUtils.mFlingmax = mHeader.getMeasuredHeight() / 6;
            }
            if (attrsUtils.mMaxHeaderScroll == -1) {
                attrsUtils.mMaxHeaderScroll = 4 * mHeader.getMeasuredHeight();
            }
            if (attrsUtils.mHeaderRefreshPosition == -1) {
                attrsUtils.mHeaderRefreshPosition = mHeader.getMeasuredHeight() + headerParams.bottomMargin;
            }

            mHeader.layout(left + headerParams.leftMargin, top - mHeader.getMeasuredHeight() - headerParams.bottomMargin, right - headerParams.rightMargin, top - headerParams.bottomMargin);
        }
        if (mFooter != null) {
            LayoutParams footerParams = (LayoutParams) mScroll.getLayoutParams();

            if (attrsUtils.mMaxFooterScroll == -1) {
                attrsUtils.mMaxFooterScroll = (int) (mFooter.getMeasuredHeight() * 1.5f);
            }
            if (attrsUtils.mFooterRefreshPosition == -1) {
                attrsUtils.mFooterRefreshPosition = mFooter.getMeasuredHeight() + footerParams.bottomMargin;
            }
            mFooter.layout(left + footerParams.leftMargin, bottom + footerParams.topMargin, right - footerParams.rightMargin, bottom + mFooter.getMeasuredHeight() + footerParams.topMargin);
        }

        if (changed) {
            mHandler.initView(this);
            mHandler.isinit = true;
        }

    }

    private void aninatorTo(int from, int to) {
        if (from == to) {
            return;
        }
        valueAnimator.cancel();
        valueAnimator.setIntValues(from, to);
        valueAnimator.setDuration(250 + 150 * Math.abs(from - to) / attrsUtils.mMaxHeaderScroll);
        valueAnimator.start();
    }

    public void NotifyCompleteRefresh0() {
        if (scrolls == 0) {
            return;
        }
        state = scrolls < 0 ? State.REFRESHCOMPLETE : State.LOADINGCOMPLETE;
        callbackState(state);

        postDelayed(this, attrsUtils.delayCompleteTime);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    @Override
    public void run() {
        aninatorTo(scrolls, 0);
    }

    /**
     * 刷新停留到配置位置，再归位
     */
    public void NotifyCompleteRefresh1(Object obj) {
        if (scrolls == 0) {
            return;
        }
        mHandler.setData(obj);
        if (state == State.REFRESHING || state == State.LOADING) {
            state = state == State.REFRESHING ? State.REFRESHCOMPLETE : State.LOADINGCOMPLETE;
            int position = state == State.REFRESHING ? -attrsUtils.mHeaderRefreshCompletePosition : attrsUtils.mFooterRefreshPosition;
            if (position == 0) {
                callbackState(state);
            }
            aninatorTo(scrolls, position);
        } else {
            NotifyCompleteRefresh0();
        }
    }

    /**
     * 刷新停留到某个位置，再归位
     *
     * @param obj
     * @param position
     */
    public void NotifyCompleteRefresh1(int position, Object obj) {
        if (scrolls == 0) {
            return;
        }
        mHandler.setData(obj);
        if (state == State.REFRESHING || state == State.LOADING) {
            state = state == State.REFRESHING ? State.REFRESHCOMPLETE : State.LOADINGCOMPLETE;
            if (position == 0) {
                callbackState(state);
            }
            aninatorTo(scrolls, state == State.REFRESHCOMPLETE ? -position : position);
        } else {
            NotifyCompleteRefresh0();
        }

    }

    /**
     * 设置动画到正在刷新
     */
    public void setRefreshing() {
        if (state != State.REFRESHING || state != State.LOADING) {
            state = State.REFRESHING;
            aninatorTo(scrolls, -attrsUtils.mHeaderRefreshPosition);
        }

    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float animatedFraction = animation.getAnimatedFraction();
        int animatedValue = (int) animation.getAnimatedValue();
        scrolls = animatedValue;
        scrollTo(0, scrolls);
        /**
         * 刷新完成时
         * 状态已变成 State.REFRESHCOMPLETE State.LOADINGCOMPLETE
         */
        boolean isComplete = (state == State.REFRESHCOMPLETE) || (state == State.LOADINGCOMPLETE);
        if (isComplete) {
            callbackScroll(state == State.REFRESHCOMPLETE ? State.PULL_HEADER : State.PULL_FOOTER, animatedValue);
        } else {
            callbackScroll(state, animatedValue);
        }
        if (animatedFraction == 1) {
            if (animatedValue != 0) {
                if (!isComplete) {
                    if (animatedValue > 0) {
                        state = State.LOADING;
                    } else {
                        state = State.REFRESHING;
                    }
                } else {
                    NotifyCompleteRefresh0();
                }
            } else {
                state = State.IDEL;
            }
            callbackState(state);
        }
    }

    private void callbackScroll(State state, int value) {
        if (callback != null) {
            callback.call(state, value);
        }

        if (state == State.PULL_HEADER) {
            mHandler.onPullHeader(mHeader, -value);
        } else {
            mHandler.onPullFooter(mFooter, value);
        }
    }

    private void callbackState(State state) {
        if (callback != null) {
            callback.call(state);
        }
        mHandler.OnStateChange(state);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target) {
        helper.onStopNestedScroll(target);

        if (scrolls != 0 && (state.ordinal() > 3)) {
            changeState(scrolls, 0);
            int mRefreshPosition = scrolls > 0 ? attrsUtils.mFooterRefreshPosition : attrsUtils.mHeaderRefreshPosition;
            if (Math.abs(scrolls) >= mRefreshPosition ) {
                aninatorTo(scrolls, (int) signum(scrolls) * mRefreshPosition);
            } else {
                aninatorTo(scrolls, 0);
            }
        }
    }

    private void changeState(int scrolls, int dy) {
        State statex;
        if (scrolls == 0) {
            statex = dy > 0 ? State.PULL_FOOTER : State.PULL_HEADER;
        } else {
            statex = scrolls > 0 ? State.PULL_FOOTER : State.PULL_HEADER;
        }
        if (statex != state) {
            callbackState(statex);
        }
        this.state = statex;
    }

    private void checkBounds(int scrolltemp) {

        int maxheader = attrsUtils.CANHEADER && scrolltemp <= 0 ? attrsUtils.mMaxHeaderScroll : 0;
        int maxfooter = attrsUtils.CANFOOTR && scrolltemp >= 0 ? attrsUtils.mMaxFooterScroll : 0;


        if (scrolls < -maxheader) {
            scrolls = -maxheader;
        }
        if (scrolls > maxfooter) {
            scrolls = maxfooter;
        }
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (state.ordinal() < 4) {
            return;
        } else if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }

        boolean isvertical = attrsUtils.orentation == Orentation.VERTICAL;
        int dscroll = isvertical ? dyUnconsumed : dxUnconsumed;

        if ((dscroll < 0 && !canScroll(isvertical, -1)) || (dscroll > 0 && !canScroll(isvertical, 1))) {
            int tempscrolls = scrolls;
            scrolls += dscroll / attrsUtils.PULLRATE;
            checkBounds(tempscrolls);
            scrollTo(0, scrolls);
            changeState(tempscrolls, dscroll);
            callbackScroll(state, scrolls);
        }
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @Nullable int[] consumed) {
        if (state.ordinal() < 4) {
            return;
        } else if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }

        int dscroll =  dy - consumed[1];
        if ((dscroll > 0 && scrolls < 0) || (dscroll < 0 && scrolls > 0)) {
            int scrolltemp = scrolls;
            scrolls += dscroll / attrsUtils.PULLRATE;
            checkBounds(scrolltemp);
            consumed[1] = dscroll;
            scrollTo(0, scrolls);
            changeState(scrolltemp, dscroll);
            callbackScroll(state, scrolls);
        }
    }

    private boolean canScroll(boolean isvertical, int direction) {
        if (isvertical) {
            return mScroll.canScrollVertically(direction);
        } else {
            return mScroll.canScrollHorizontally(direction);
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes) {
        int ore =  ViewCompat.SCROLL_AXIS_VERTICAL;
        return (axes & ore) != 0;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        helper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return helper.getNestedScrollAxes();
    }


    Callback callback;

    public void setListener(Callback callback) {
        this.callback = callback;
    }


    public abstract static class Callback {
        public abstract void call(State t);

        public void call(State t, int scroll) {
        }
    }

    /**
     * 初始化全局配置
     *
     * @param defaultBuilder
     */
    public static void init(DefaultBuilder defaultBuilder) {
        AttrsUtils.setBuilder(defaultBuilder);
    }

    /**
     * 解析xml属性
     */
    public static class AttrsUtils {
        /**
         * 头部最大滑动距离
         */
        private int mMaxHeaderScroll = -1;

        /**
         * 尾部最大刷新距离
         */
        private int mMaxFooterScroll = -1;

        /**
         * 头部刷新停留的位置
         */
        private int mHeaderRefreshPosition = -1;

        /**
         * 尾部刷新停留的位置
         */
        private int mFooterRefreshPosition = -1;

        /**
         * 快速滑动Overscroll的距离
         */
        private int mFlingmax = 100;

        /**
         * 刷新完成停留的位置
         */
        private int mHeaderRefreshCompletePosition = -1;
        private int mFooterLoadingCompletePosition = -1;

        /**
         * 布局文件
         */
        private int HEADER_LAYOUTID, SCROLL_LAYOUT_ID, FOOTER_LAYOUTID;

        /**
         * 头部 尾部是否可滑
         */
        private Boolean CANHEADER = true, CANFOOTR = true;

        /**
         * 滑动方向
         */
        private Orentation orentation = Orentation.VERTICAL;

        /**
         * 默认全局配置
         */
        private static DefaultBuilder builder = new DefaultBuilder();

        /**
         * 刷新完成延迟时间
         */
        private int delayCompleteTime = 300;

        /**
         * 拉伸张力
         */
        private float PULLRATE = -1;

        private static void setBuilder(DefaultBuilder builderx) {
            builder = builderx;
        }

        public void ParseAttrs(Context context, AttributeSet attr) {
            TypedArray typedArray = context.obtainStyledAttributes(attr, R.styleable.RefreshLayout);
            if (HEADER_LAYOUTID == 0)
                HEADER_LAYOUTID = typedArray.getResourceId(R.styleable.RefreshLayout_headerID, builder.HEADER_LAYOUTID_DEFAULT);

            if (FOOTER_LAYOUTID == 0)
                FOOTER_LAYOUTID = typedArray.getResourceId(R.styleable.RefreshLayout_footerID, builder.FOOTER_LAYOUTID_DEFAULT);


            if (SCROLL_LAYOUT_ID == 0)
                SCROLL_LAYOUT_ID = typedArray.getResourceId(R.styleable.RefreshLayout_scrollID, builder.SCROLL_LAYOUT_ID_DEFAULT);

            if (CANHEADER == null)
                CANHEADER = typedArray.getBoolean(R.styleable.RefreshLayout_canHeader, builder.CANHEADER_DEFAULT);

            if (CANFOOTR == null)
                CANFOOTR = typedArray.getBoolean(R.styleable.RefreshLayout_canFooter, builder.CANFOOTR_DEFAULT);


            if (mMaxHeaderScroll == -1)
                mMaxHeaderScroll = (int) typedArray.getDimension(R.styleable.RefreshLayout_mMaxHeadertScroll, mMaxHeaderScroll);
            if (mMaxFooterScroll == -1)
                mMaxFooterScroll = (int) typedArray.getDimension(R.styleable.RefreshLayout_mMaxFooterScroll, mMaxFooterScroll);
            if (mHeaderRefreshPosition == -1)
                mHeaderRefreshPosition = (int) typedArray.getDimension(R.styleable.RefreshLayout_mHeaderRefreshPosition, mHeaderRefreshPosition);
            if (mFooterRefreshPosition == -1)
                mFooterRefreshPosition = (int) typedArray.getDimension(R.styleable.RefreshLayout_mFooterRefreshPosition, mFooterRefreshPosition);
            if (mHeaderRefreshCompletePosition == -1)
                mHeaderRefreshCompletePosition = (int) typedArray.getDimension(R.styleable.RefreshLayout_mHeaderRefreshCompletePosition, 0);
            if (mFooterLoadingCompletePosition == -1)
                mFooterLoadingCompletePosition = (int) typedArray.getDimension(R.styleable.RefreshLayout_mFooterLoadingCompletePosition, 0);
            if (mFlingmax == -1)
                mFlingmax = (int) typedArray.getDimension(R.styleable.RefreshLayout_mFlingmax, mFlingmax);
            if (delayCompleteTime == 800)
                delayCompleteTime = typedArray.getInt(R.styleable.RefreshLayout_delayCompleteTime, delayCompleteTime);

            if (PULLRATE == -1)
                PULLRATE = typedArray.getFloat(R.styleable.RefreshLayout_pullrate, builder.PULLRATE);

            typedArray.recycle();
        }

        public int getmMaxHeaderScroll() {
            return mMaxHeaderScroll;
        }

        public void setmMaxHeaderScroll(int mMaxHeaderScroll) {
            this.mMaxHeaderScroll = mMaxHeaderScroll;
        }

        public int getmMaxFooterScroll() {
            return mMaxFooterScroll;
        }

        public void setmMaxFooterScroll(int mMaxFooterScroll) {
            this.mMaxFooterScroll = mMaxFooterScroll;
        }

        public int getmHeaderRefreshPosition() {
            return mHeaderRefreshPosition;
        }

        public void setmHeaderRefreshPosition(int mHeaderRefreshPosition) {
            this.mHeaderRefreshPosition = mHeaderRefreshPosition;
        }

        public int getmFooterRefreshPosition() {
            return mFooterRefreshPosition;
        }

        public void setmFooterRefreshPosition(int mFooterRefreshPosition) {
            this.mFooterRefreshPosition = mFooterRefreshPosition;
        }

        public int getmHeaderRefreshCompletePosition() {
            return mHeaderRefreshCompletePosition;
        }

        public void setmHeaderRefreshCompletePosition(int mHeaderRefreshCompletePosition) {
            this.mHeaderRefreshCompletePosition = mHeaderRefreshCompletePosition;
        }

        public int getmFooterLoadingCompletePosition() {
            return mFooterLoadingCompletePosition;
        }

        public void setmFooterLoadingCompletePosition(int mFooterLoadingCompletePosition) {
            this.mFooterLoadingCompletePosition = mFooterLoadingCompletePosition;
        }

        public int getHEADER_LAYOUTID() {
            return HEADER_LAYOUTID;
        }

        public void setHEADER_LAYOUTID(int HEADER_LAYOUTID) {
            this.HEADER_LAYOUTID = HEADER_LAYOUTID;
        }

        public int getSCROLL_LAYOUT_ID() {
            return SCROLL_LAYOUT_ID;
        }

        public void setSCROLL_LAYOUT_ID(int SCROLL_LAYOUT_ID) {
            this.SCROLL_LAYOUT_ID = SCROLL_LAYOUT_ID;
        }

        public int getFOOTER_LAYOUTID() {
            return FOOTER_LAYOUTID;
        }

        public void setFOOTER_LAYOUTID(int FOOTER_LAYOUTID) {
            this.FOOTER_LAYOUTID = FOOTER_LAYOUTID;
        }

        public boolean getCANHEADER() {
            return CANHEADER;
        }

        public void setCANHEADER(Boolean CANHEADER) {
            this.CANHEADER = CANHEADER;
        }

        public boolean getCANFOOTR() {
            return CANFOOTR;
        }

        public void setCANFOOTR(Boolean CANFOOTR) {
            this.CANFOOTR = CANFOOTR;
        }

        public int getDelayCompleteTime() {
            return delayCompleteTime;
        }

        public void setDelayCompleteTime(int delayCompleteTime) {
            this.delayCompleteTime = delayCompleteTime;
        }

        public float getPULLRATE() {
            return PULLRATE;
        }

        public void setPULLRATE(float PULLRATE) {
            this.PULLRATE = PULLRATE;
        }
    }

    /**
     * 保存全局默认配置
     */
    public static class DefaultBuilder {
        private int HEADER_LAYOUTID_DEFAULT, SCROLL_LAYOUT_ID_DEFAULT, FOOTER_LAYOUTID_DEFAULT;
        private float PULLRATE = 2.5f;
        private boolean CANHEADER_DEFAULT = true, CANFOOTR_DEFAULT = true;
        private Class defaultRefreshWrap = RefreshOuterHanderImpl.class;

        public DefaultBuilder setBaseRefreshWrap(Class defaultRefreshWrap) {
            this.defaultRefreshWrap = defaultRefreshWrap;
            return this;
        }

        public DefaultBuilder setHeaderLayoutidDefault(int headerLayoutidDefault) {
            HEADER_LAYOUTID_DEFAULT = headerLayoutidDefault;
            return this;
        }

        public DefaultBuilder setScrollLayoutIdDefault(int scrollLayoutIdDefault) {
            SCROLL_LAYOUT_ID_DEFAULT = scrollLayoutIdDefault;
            return this;
        }

        public DefaultBuilder setFooterLayoutidDefault(int footerLayoutidDefault) {
            FOOTER_LAYOUTID_DEFAULT = footerLayoutidDefault;
            return this;
        }

        /**
         * 是否可以下啦
         *
         * @param canheaderDefault
         * @return
         */
        public DefaultBuilder setCanheaderDefault(boolean canheaderDefault) {
            CANHEADER_DEFAULT = canheaderDefault;
            return this;
        }

        /**
         * 是否可以上啦
         *
         * @param canfootrDefault
         * @return
         */
        public DefaultBuilder setCanfootrDefault(boolean canfootrDefault) {
            CANFOOTR_DEFAULT = canfootrDefault;
            return this;
        }


        public DefaultBuilder setPullRate(int rate) {
            if (rate > 0) {
                PULLRATE = rate;
            }
            return this;
        }

    }

    private BaseRefreshHeaderAndFooterHandler mHandler;

    public void setRefreshWrap(BaseRefreshHeaderAndFooterHandler handler) {
        this.mHandler = handler;
        if (!mHandler.isinit) {
            mHandler.initView(this);
            mHandler.isinit = true;
        }
    }

    public static abstract class BaseRefreshHeaderAndFooterHandler<T> {
        protected T data;
        boolean isinit = false;

        public abstract void onPullHeader(View view, int scrolls);

        public abstract void onPullFooter(View view, int scrolls);

        public abstract void OnStateChange(State state);

        protected void initView(RefreshLayout layout) {

        }

        protected void setData(Object data) {
            this.data = (T) data;
        }
    }


    public <T extends View> T getmHeader() {
        return (T) mHeader;
    }

    public <T extends View> T getmFooter() {
        return (T) mFooter;
    }

    public <T extends View> T getmScroll() {
        return (T) mScroll;
    }

    public <T extends View> T findInHeaderView(int id) {

        return (T) mHeader.findViewById(id);
    }

    public <T extends View> T findInScrollView(int id) {

        return (T) mScroll.findViewById(id);
    }

    public <T extends View> T findInFooterView(int id) {

        return (T) mScroll.findViewById(id);
    }

    public <T extends BaseRefreshHeaderAndFooterHandler> T getBaseRefreshWrap() {
        return (T) mHandler;
    }

    /**
     * 1+1/2+1/3+……+1/n=lnn+R
     * R为欧拉常数,约为0.5772.
     * 1 1/2 1/3 1/4 1/5
     *
     * @param current
     * @param base
     * @return
     */
    private int caculateZhangli(int current, int base) {
        float signum = Math.signum(current);
        int pullrate = Math.abs(current) / base;
        if (pullrate == 0) {
            return current;
        } else if (pullrate == 1)
            return (int) (signum * base + signum * (Math.abs(current) % base) / 3);
        else if (pullrate == 2) {
            return (int) (1.333333333f * signum * base + signum * (Math.abs(current) % base) / 4);
        } else if (pullrate == 3) {
            return (int) (1.583333333f * signum * base + signum * (Math.abs(current) % base) / 5);
        } else {
            return (int) (1.783333333f * signum * base + signum * (Math.abs(current) % base) / 6);
        }
    }

    public AttrsUtils getAttrsUtils() {
        return attrsUtils;
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }
}
