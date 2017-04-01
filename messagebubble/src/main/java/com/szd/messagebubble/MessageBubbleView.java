package com.szd.messagebubble;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by szd on 2017/3/28.
 */

public class MessageBubbleView extends View {
    private static final String TAG = "BezierView";
    Paint mPaint;
    Paint textPaint;
    Paint disappearPaint;
    Path mPath;
    float textMove;//字体垂直偏移量
    float centerRadius;//中心园半径
    float dragRadius;//拖拽圆半径
    int dragCircleX;
    int centerCircleX;
    int dragCircleY;
    int centerCircleY;
    float d;//两个圆的距离
    int mWidth;
    int mHeight;
    String mNumber;
    int maxDragLength;//最大可拖拽距离

    float textSize;//用户设定的字体大小
    int textColor;//用户设定的字体颜色
    int circleColor;//用户设定的圆圈颜色

    int[] disappearPic;
    Bitmap[] disappearBitmap;
    Rect bitmapRect;
    int bitmapIndex;//消失动画播放图片的index
    boolean startDisappear;//判断是否正在播放消失动画，防止死循环重复绘制

    ActionListener actionListener;

    //当前状态
    int curState;
    //原位
    public static int STATE_NORMAL = 0;
    //消失
    public static int STATE_DISAPPEAR = 1;
    //拖拽
    public static int STATE_DRAGING = 2;
    //移动（无粘连效果）
    public static int STATE_MOVE = 3;

    public MessageBubbleView(Context context) {
        super(context);
    }

    public MessageBubbleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MessageBubble);
        circleColor = ta.getColor(R.styleable.MessageBubble_circleColor, Color.RED);
        textColor = ta.getColor(R.styleable.MessageBubble_textColor, Color.WHITE);
        textSize = ta.getDimension(R.styleable.MessageBubble_textSize, 30);
        centerRadius = ta.getDimension(R.styleable.MessageBubble_radius, 30);
        mNumber = ta.getString(R.styleable.MessageBubble_number);
        ta.recycle();
    }

    public MessageBubbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }

    private void init() {
        //画圆的Paint
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(circleColor);
        //画数字的Paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(textSize);
        //画消失图片的Paint
        disappearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        disappearPaint.setFilterBitmap(false);
        startDisappear = false;

        Paint.FontMetrics textFontMetrics = textPaint.getFontMetrics();
        textMove = -textFontMetrics.ascent - (-textFontMetrics.ascent + textFontMetrics.descent) / 2; // drawText从baseline开始
        // ，baseline的值为0，baseline的上面为负值，baseline的下面为正值，即这里ascent为负值，descent为正值,比如ascent为-20
        // ，descent为5，那需要移动的距离就是20 - （20 + 5）/ 2

        mPath = new Path();
        if (centerRadius <= 2) {//如果不是第一次创建，上次的拖动删除会因为中心圆半径随着拖放变为零
            centerRadius = dragRadius;
        } else {
            dragRadius = centerRadius;
        }

        maxDragLength = (int) (4 * dragRadius);

        //设定圆的初始位置为View正中心
        centerCircleX = getWidth() / 2;
        centerCircleY = getHeight() / 2;
        //防止被拖动圆因上一次拖动而未回到原位
        dragCircleX = centerCircleX;
        dragCircleX = centerCircleX;

        if (disappearPic == null) {
            disappearPic = new int[]{R.drawable.explosion_one, R.drawable.explosion_two, R.drawable.explosion_three
                    , R.drawable.explosion_four, R.drawable.explosion_five};
        }
        disappearBitmap = new Bitmap[disappearPic.length];
        for (int i = 0; i < disappearPic.length; i++) {
            disappearBitmap[i] = BitmapFactory.decodeResource(getResources(), disappearPic[i]);
        }
        curState = STATE_NORMAL;

    }


    @Override
    protected void onMeasure(int widthMeasure, int heightMeasure) {
        int widthMode = MeasureSpec.getMode(widthMeasure);
        int widthSize = MeasureSpec.getSize(widthMeasure);
        int heightMode = MeasureSpec.getMode(heightMeasure);
        int heightSize = MeasureSpec.getSize(heightMeasure);

        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        } else {
            mWidth = getPaddingLeft() + 400 + getPaddingRight();
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        } else {
            mHeight = getPaddingTop() + 400 + getPaddingBottom();
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                if (curState != STATE_DISAPPEAR) {
                    //计算点击位置与气泡的距离
                    d = (float) Math.hypot(centerCircleX - event.getX(), centerCircleY - event.getY());
                    if (d < centerRadius + 10) {
                        curState = STATE_DRAGING;
                    } else {
                        curState = STATE_NORMAL;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                dragCircleX = (int) event.getX();
                dragCircleY = (int) event.getY();
                if (curState == STATE_DRAGING) {//拖拽状态下计算拖拽距离，超出後不再計算
                    d = (float) Math.hypot(centerCircleX - event.getX(), centerCircleY - event.getY());
                    if (d <= maxDragLength - maxDragLength / 7) {
                        centerRadius = dragRadius - d / 4;
                        if (actionListener != null) {
                            actionListener.onDrag();
                        }
                    } else {
                        centerRadius = 0;
                        curState = STATE_MOVE;
                    }
                } else if (curState == STATE_MOVE) {//超出最大拖拽距离，则中间的圆消失
                    if (actionListener != null) {
                        actionListener.onMove();
                    }
                }
                invalidate();

                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                //当正在拖动时，抬起手指才会做响应的处理
                if (curState == STATE_DRAGING || curState == STATE_MOVE) {
                    d = (float) Math.hypot(centerCircleX - event.getX(), centerCircleY - event.getY());
                    if (d > maxDragLength) {//如果拖拽距离大于最大可拖拽距离，则消失
                        curState = STATE_DISAPPEAR;
                        startDisappear = true;
                        disappearAnim();
                    } else {//小于可拖拽距离，则复原气泡位置
                        restoreAnim();
                    }
                    invalidate();
                }
                break;

        }
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (curState == STATE_NORMAL) {
            //画初始圆
            canvas.drawCircle(centerCircleX, centerCircleY, centerRadius, mPaint);
            //画数字（要在画完贝塞尔曲线之后绘制，不然会被挡住）
            canvas.drawText(mNumber, centerCircleX, centerCircleY + textMove, textPaint);
        }
        //如果开始拖拽，则画dragCircle
        if (curState == STATE_DRAGING) {
            //画初始圆
            canvas.drawCircle(centerCircleX, centerCircleY, centerRadius, mPaint);
            //画被拖拽的圆
            canvas.drawCircle(dragCircleX, dragCircleY, dragRadius, mPaint);
            drawBezier(canvas);
            canvas.drawText(mNumber, dragCircleX, dragCircleY + textMove, textPaint);
        }

        if (curState == STATE_MOVE) {
            canvas.drawCircle(dragCircleX, dragCircleY, dragRadius, mPaint);
            canvas.drawText(mNumber, dragCircleX, dragCircleY + textMove, textPaint);
        }

        if (curState == STATE_DISAPPEAR && startDisappear) {
            if (disappearBitmap != null) {
                canvas.drawBitmap(disappearBitmap[bitmapIndex], null, bitmapRect, disappearPaint);
            }
        }

    }


    /**
     * 气泡消失动画
     */
    private void disappearAnim() {
        bitmapRect = new Rect(dragCircleX - (int) dragRadius, dragCircleY - (int) dragRadius,
                dragCircleX + (int) dragRadius, dragCircleY + (int) dragRadius);
        ValueAnimator disappearAnimator = ValueAnimator.ofInt(0, disappearBitmap.length);
        disappearAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                bitmapIndex = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        disappearAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startDisappear = false;
                if (actionListener != null) {
                    actionListener.onDisappear();
                }
            }
        });
        disappearAnimator.setInterpolator(new LinearInterpolator());
        disappearAnimator.setDuration(500);
        disappearAnimator.start();
    }

    /**
     * 气泡复原动画
     */
    private void restoreAnim() {
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new MyPointFEvaluator(), new PointF(dragCircleX, dragCircleY), new PointF(centerCircleX, centerCircleY));
        valueAnimator.setDuration(200);
        valueAnimator.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                float f = 0.571429f;
                return (float) (Math.pow(2, -4 * input) * Math.sin((input - f / 4) * (2 * Math.PI) / f) + 1);
            }
        });
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF pointF = (PointF) animation.getAnimatedValue();
                dragCircleX = (int) pointF.x;
                dragCircleY = (int) pointF.y;
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //复原了
                centerRadius = dragRadius;
                curState = STATE_NORMAL;
                if (actionListener != null) {
                    actionListener.onRestore();
                }
            }
        });
        valueAnimator.start();
    }

    private void drawBezier(Canvas canvas) {
        float controlX = (centerCircleX + dragCircleX) / 2;//贝塞尔曲线控制点X坐标
        float controlY = (dragCircleY + centerCircleY) / 2;//贝塞尔曲线控制点Y坐标
        //计算曲线的起点终点
        d = (float) Math.hypot(centerCircleX - dragCircleX, centerCircleY - dragCircleY);
        float sin = (centerCircleY - dragCircleY) / d;
        float cos = (centerCircleX - dragCircleX) / d;
        float dragCircleStartX = dragCircleX - dragRadius * sin;
        float dragCircleStartY = dragCircleY + dragRadius * cos;
        float centerCircleEndX = centerCircleX - centerRadius * sin;
        float centerCircleEndY = centerCircleY + centerRadius * cos;
        float centerCircleStartX = centerCircleX + centerRadius * sin;
        float centerCircleStartY = centerCircleY - centerRadius * cos;
        float dragCircleEndX = dragCircleX + dragRadius * sin;
        float dragCircleEndY = dragCircleY - dragRadius * cos;

        mPath.reset();
        mPath.moveTo(centerCircleStartX, centerCircleStartY);
        mPath.quadTo(controlX, controlY, dragCircleEndX, dragCircleEndY);
        mPath.lineTo(dragCircleStartX, dragCircleStartY);
        mPath.quadTo(controlX, controlY, centerCircleEndX, centerCircleEndY);
        mPath.close();

        canvas.drawPath(mPath, mPaint);
    }


    /**
     * 重置
     */
    public void resetBezierView() {
        init();
        invalidate();
    }

    /**
     * 设置显示的消息数量(超过99需要自己定义为"99+")
     *
     * @param number 消息的数量
     */
    public void setNumber(String number) {
        mNumber = number;
        invalidate();
    }

    /**
     * 设置消失动画
     *
     * @param disappearPic
     */
    public void setDisappearPic(int[] disappearPic) {
        if (disappearPic != null) {
            this.disappearPic = disappearPic;
        }
    }

    public interface ActionListener {
        /**
         * 被拖动时
         */
        void onDrag();

        /**
         * 消失后
         */
        void onDisappear();

        /**
         * 拖动距离不足，气泡回到原位后
         */
        void onRestore();

        /**
         * 拖动时超出了最大粘连距离，气泡单独移动时
         */
        void onMove();
    }

    public void setOnActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    /**
     * PointF动画估值器(复原时的振动动画)
     */
    public class MyPointFEvaluator implements TypeEvaluator<PointF> {

        @Override
        public PointF evaluate(float fraction, PointF startPointF, PointF endPointF) {
            float x = startPointF.x + fraction * (endPointF.x - startPointF.x);
            float y = startPointF.y + fraction * (endPointF.y - startPointF.y);
            return new PointF(x, y);
        }
    }

}
