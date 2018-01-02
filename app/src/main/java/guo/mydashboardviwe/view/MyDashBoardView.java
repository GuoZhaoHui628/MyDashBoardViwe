package guo.mydashboardviwe.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.icu.text.DecimalFormat;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import guo.mydashboardviwe.R;

/**
 * Created by ${GuoZhaoHui} on 2017/12/28.
 * Email:guozhaohui628@gmail.com
 */

public class MyDashBoardView extends View {

    private static final String TAG = "MyDashBoardView";

    //整个view的长 宽
    private int mWidth,mHeight;

    /**
     * 开始弧度
     */
    private int mStartAngle = 135;
    /**
     * 圆弧扫过的弧度
     */
    private int mSweepAngle = 270;

    private Paint mArcPaint;

    /**
     * 圆弧外面矩形的长度
     */
    private float mRectWidth = 0;

    /**
     * 等分成十大份
     */
    private int largeSection = 10;

    /**
     * 每个大份中分为10个小份
     */
    private int smallSection = 10;

    /**
     * 长刻度的长度
     */
    private float longLength = dp2px(8);

    /**
     * 短刻度长度
     */
    private float shortLength = longLength/2;

    /**
     * 长刻度的间距角度
     */
    private float avgLargeAngle = mSweepAngle/largeSection;

    /**
     * 短刻度的间距角度
     */
    private float avgSmallAngle  = avgLargeAngle/smallSection;

    /**
     * 长刻度文本内容数组
     */
    private String[] textContent  = null;

    private float mMax = 100f;

    private float mMin = 0;

    /**
     * 内弧的外切矩形
     */
    private RectF inArcRect;

    /**
     * 内弧的path
     */
    private Path mInPath;

    /**
     * 长刻度文字矩形
     */
    private Rect textRect;

    /**
     * 表头
     */
    private String mHeadText = "";

    /**
     * 当前的实时读数
     */
    private float mCurrentValue = mMin;

    /**
     * 是否显示当前的实时读数
     */
    private boolean mIsShowCurrentValue;



    public MyDashBoardView(Context context) {
        super(context);
    }

    public MyDashBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a =  context.obtainStyledAttributes(attrs, R.styleable.MyDashBoardView);
        String s = a.getString(R.styleable.MyDashBoardView_headText);
        /**
         * 是否显示实时读数 默认显示
         */
        mIsShowCurrentValue = a.getBoolean(R.styleable.MyDashBoardView_isShowCurrentValue, true);
        if(s!=null){
            mHeadText = s;
        }
        int color = a.getColor(R.styleable.MyDashBoardView_dashColor, context.getResources().getColor(R.color.colorAccent));
        initPaint(color);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureSpec(widthMeasureSpec);
        int height = measureSpec(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private void initPaint(int c) {
        mArcPaint = new Paint();
        mArcPaint.setColor(c);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStrokeWidth(dp2px(1));

        getTextContent();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        /**
         * 圆弧外切矩形的宽度，圆弧的半径即为它的一半
         */
        mRectWidth = (float) (Math.min(mWidth,mHeight)*0.9);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 将圆心移到控件中间
         */
        canvas.translate(mWidth / 2, mHeight / 2);
        drawArc(canvas);

        /**
         * 先画短刻度 在画长刻度，这样后者可以覆盖前者，毕竟其实所有的刻度都可以用短刻度表示出来
         */
        drawGraduation2(canvas);
        drawGraduation(canvas);
        drawText(canvas);
        drawHeadText(canvas);
        drawPointer(canvas);
        drawCurrentValue(canvas);
    }

    public float getmMax() {
        return mMax;
    }

    public void setmMax(float mMax) {
        this.mMax = mMax;
        getTextContent();
        invalidate();
    }

    /**
     * 画圆弧
     * @param canvas
     */
    private void drawArc(Canvas canvas){
        mArcPaint.setStyle(Paint.Style.STROKE);
        RectF rectFArc = new RectF(-mRectWidth / 2, -mRectWidth / 2, mRectWidth / 2, mRectWidth / 2);
        canvas.drawArc(rectFArc,mStartAngle,mSweepAngle,false,mArcPaint);
    }

    /**
     * 画长刻度
     * @param canvas
     */
    private void drawGraduation(Canvas canvas){
        canvas.restore();
        /**
         * 再次保存 为了以后的画布而言，拿出来时 还是最初的
         */
        canvas.save();

        /**
         * 将角度135 转成弧度 3pi/4
         */
        float startPi = (float) Math.toRadians(mStartAngle);
        /**
         * 刻度两个点的坐标
         */
        float[] point1 = new float[2];
        float[] point2 = new float[2];

        point1[0] = (float) (Math.cos(startPi)*mRectWidth/2);
        point1[1] = (float) (Math.sin(startPi) * mRectWidth / 2);

        point2[0] = (float) (Math.cos(startPi)*(mRectWidth/2-longLength));
        point2[1] = (float) (Math.sin(startPi) * (mRectWidth / 2 - longLength));

        for(float i=0;i<=270;i+=avgLargeAngle){
            canvas.drawLine(point2[0],point2[1],point1[0],point1[1],mArcPaint);
            canvas.rotate(avgLargeAngle);
        }
    }


    /**
     * 画短刻度
     * @param canvas
     */
    private void drawGraduation2(Canvas canvas){

        /**
         * 先将画布保存，等下画长度前拿出来，这样 画布相当于是最新的，不然 先画短刻度后 在画长刻度 画布的角度已经变了
         */
        canvas.save();
        /**
         * 将角度135 转成弧度 3pi/4
         */
        float startPi = (float) Math.toRadians(mStartAngle);
        /**
         * 刻度两个点的坐标
         */
        float[] point1 = new float[2];
        float[] point2 = new float[2];

        point1[0] = (float) (Math.cos(startPi)*mRectWidth/2);
        point1[1] = (float) (Math.sin(startPi) * mRectWidth / 2);

        /**
         * 半径减去短刻度的长度，然后算出三角函数
         */
        point2[0] = (float) (Math.cos(startPi)*(mRectWidth/2-shortLength));
        point2[1] = (float) (Math.sin(startPi) * (mRectWidth / 2 - shortLength));

        for(float i=0;i<=270;i+=avgSmallAngle){
            canvas.drawLine(point2[0],point2[1],point1[0],point1[1],mArcPaint);
            canvas.rotate(avgSmallAngle);
        }
    }

    /**
     * 获取长刻度 文字内容
     */
    private void getTextContent(){
        mMax  = getmMax();
        textContent = new String[largeSection + 1];
        for(int i=0;i<textContent.length;i++){
            float avgF = (mMax-mMin)/largeSection;
            float result = avgF*i;
            Log.d(TAG,"------result------    "+result);
            /**
             * 对值进行处理，保留一位小数
             */
            String lastResutl = new DecimalFormat("#.0").format(result);
            /**
             * 注意和上面的区别，上面的是 所有的值 始终保留一位小数，下面是 如果是整数则不保留，如果不是整数，则保留一位小数
             */
            //String lastResutl = new DecimalFormat("#.0").format(result);
            if(i==0){
                textContent[0] = String.valueOf(0.0);
            }else{
                textContent[i] = lastResutl;
            }
        }
    }


    public float getCurrentValue(){
        return mCurrentValue;
    }

    /**
     * 设置当前的实时参数值
     * @param currentValue
     */
    public void setCurrentValue(float currentValue){
        if(currentValue<mMin || currentValue>mMax || currentValue==mCurrentValue){
            return;
        }
        mCurrentValue = currentValue;
        postInvalidate();
    }

    /**
     * 画长刻度 文字
     * @param canvas
     */
    private void drawText(Canvas canvas){
        canvas.restore();
        canvas.save();
        inArcRect = new RectF(-mRectWidth / 2+longLength+longLength,-mRectWidth / 2+longLength+longLength,mRectWidth / 2-longLength-longLength,mRectWidth / 2-longLength-longLength);
        mInPath = new Path();

        textRect = new Rect();

        mArcPaint.setTextAlign(Paint.Align.LEFT);
        mArcPaint.setTextSize(sp2px(9));
        mArcPaint.setStyle(Paint.Style.FILL);

        for(int i=0;i<textContent.length;i++){
            mArcPaint.getTextBounds(textContent[i],0,textContent[i].length(),textRect);

            /**
             * 处理角度  这里暂时把文字的宽度当作弧长，然后通过弧长算出对应的角度，
             * 弧长公式  l(弧长) = n(圆心角) × 派 × r(半径)  / 180
             * 所以反推出  n = l*180/派×r
             */
            int θ = (int) ((textRect.width()/2*180)/(Math.PI*mRectWidth / 2-longLength-longLength));
            mInPath.reset();
            mInPath.addArc(inArcRect, mStartAngle + i * (mSweepAngle / largeSection)-θ, mSweepAngle);
            canvas.drawTextOnPath(textContent[i],mInPath,0,0,mArcPaint);
        }

    }

    /**
     * 画表头，如果表头不存在即不画
     * @param canvas
     */
    private void drawHeadText(Canvas canvas){
        if(!TextUtils.isEmpty(mHeadText)){
            canvas.restore();
            canvas.save();

            mArcPaint.setTextSize(sp2px(14));
            mArcPaint.setTextAlign(Paint.Align.CENTER);
            //mArcPaint.getTextBounds(mHeadText,0,mHeadText.length(),textRect);
            canvas.drawText(mHeadText,0,-mRectWidth/4,mArcPaint);
        }
    }

    /**
     * 画指针，因为这里并不是将 指针画出来 然后算出角度 让它旋转，这里是直接算出角度 然后画出来，所以我们画指针 首先就是算出指针角度
     */
    private void drawPointer(Canvas canvas){
        canvas.restore();
        canvas.save();

        float [] pointC = new float[2];
        float[] pointB = new float[2];
        float[] pointD = new float[2];

        mArcPaint.setStyle(Paint.Style.FILL);
        /**
         * 算出指针的弧度
         */
        float angle = (float) Math.toRadians((mCurrentValue/mMax)*mSweepAngle+mStartAngle);
        Log.d(TAG, "   ------------angle-----------    " + angle);

        /**
         * 指针的半径
         */
        float pointerRadius = mRectWidth/2-longLength-longLength-6;
        mInPath.reset();
        float d = 36;

        /**
         * C点坐标  指针最末点的位置坐标（远离圆心）
         */
        pointC[0] = (float) (Math.cos(angle)*pointerRadius);
        pointC[1] = (float) (Math.sin(angle) * pointerRadius);

        /**
         * B点坐标
         */
        pointB[0] = (float) (Math.cos(angle+Math.toRadians(30))*d);
        pointB[1] = (float) (Math.sin(angle + Math.toRadians(30)) * d);

        /**
         * D点坐标
         */
        pointD[0] = (float) (Math.cos(angle - Math.toRadians(30)) * d);
        pointD[1] = (float) (Math.sin(angle - Math.toRadians(30)) * d);

        mInPath.lineTo(pointB[0], pointB[1]);
        mInPath.lineTo(pointC[0], pointC[1]);
        mInPath.lineTo(pointD[0], pointD[1]);
        mInPath.close();
        canvas.drawPath(mInPath,mArcPaint);
    }

    /**
     * 如果设置需要实时读数，则画出来
     * @param canvas
     */
    private void drawCurrentValue(Canvas canvas){
        if(mIsShowCurrentValue){
            canvas.restore();
            canvas.save();

            mArcPaint.setTextAlign(Paint.Align.CENTER);
            mArcPaint.setTextSize(sp2px(18));
            mArcPaint.setStyle(Paint.Style.FILL);
            String currentValue = String.valueOf(mCurrentValue);
            canvas.drawText(currentValue,0,mRectWidth/4,mArcPaint);
        }
    }


    /**
     * 设置view的颜色
     * @param color
     */
    public void setDashColor(int color){
        mArcPaint.setColor(color);
        postInvalidate();
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                Resources.getSystem().getDisplayMetrics());
    }

    /**
     * 测量
     * @return
     */
    private int measureSpec(int widthMeasureSpec){
        int result = 0;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        if(specMode==MeasureSpec.EXACTLY){
            result = specSize;
        }else{
            result = dp2px(200);
            if(specMode==MeasureSpec.AT_MOST){
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

}
