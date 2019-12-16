package com.chien.knob;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.SoftReference;

/**
 * Created by Chien.Kung on 2019-12-04.
 */
public class RotaryView extends View {
    private static final String TAG = RotaryView.class.getSimpleName();
    private final int ERROR_RATE = 4;
    private final int INFO_X = 0;
    private final int INFO_Y = 1;
    private float mInitDegrees = 0;
    private float mTotalDegrees = 360;
    private float mStartDegrees = 0;
    private float mOnDegrees = 0;
    private State mState = State.NOTHING;
    private float[] mCenterInfo = new float[2];
    private float[] mTouchDownInfo = new float[2];
    private float[] mTouchMoveInfo = new float[2];
    private SoftReference<Bitmap> mEnableKnobPicture;
    private SoftReference<Bitmap> mDisableKnobPicture;
    private Matrix mMatrix = new Matrix();
    private Knob.OnRotateListener mOnRotateListener;

    private enum State {
        DOWN, UP, MOVE, NOTHING
    }

    public RotaryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        init();
    }

    private void init() {
        setOnTouchListener(mOnTouch);
    }

    private void addDegrees(float degrees) {
//        Log.v(TAG, "set degrees:" + degrees);
        float degreesNow = mStartDegrees + degrees;
        if (degreesNow > mTotalDegrees) {
            if (degrees > 0) {
                degreesNow = mTotalDegrees;
            }
        } else if (degreesNow < 0) {
            if (degrees < 0) {
                degreesNow = 0;
            }
        }
        mStartDegrees = sortDegrees(degreesNow);
//        Log.d(TAG, "degreesNow:" + degreesNow + " mTotalDegrees:" + mTotalDegrees);
        if (mOnDegrees != mStartDegrees) {
            mOnRotateListener.onRotate(this, mStartDegrees, degrees);
            mOnDegrees = mStartDegrees;
            invalidate();
        }
    }

    private float sortDegrees(float degress) {
        jump:
        while (true) {
            if (degress >= 0.0F)
                while (true) {
                    if (degress < 360.0F)
                        break jump;
                    degress -= 360.0F;
                }
            degress += 360.0F;
        }
        return degress;
    }

    private Bitmap cacheEnableDrawable(Drawable drawable) {
        if (this.mEnableKnobPicture == null || this.mEnableKnobPicture.get() == null)
            this.mEnableKnobPicture = new SoftReference(drawableToBitmap(drawable));
        return (Bitmap) this.mEnableKnobPicture.get();
    }

    private Bitmap cacheDisableDrawable(Drawable drawable) {
        if (this.mDisableKnobPicture == null || this.mDisableKnobPicture.get() == null)
            this.mDisableKnobPicture = new SoftReference(drawableToBitmap(drawable));
        return (Bitmap) this.mDisableKnobPicture.get();
    }

    public Bitmap drawableToBitmap(Drawable bitmapDrawable) {
        if (bitmapDrawable instanceof BitmapDrawable)
            return ((BitmapDrawable) bitmapDrawable).getBitmap();
        Bitmap bitmap = Bitmap.createBitmap(bitmapDrawable.getIntrinsicWidth(), bitmapDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        bitmapDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        bitmapDrawable.draw(canvas);
        return bitmap;
    }

    public void setEnableDrawableResource(int resId) {
        Bitmap knobPicture = cacheEnableDrawable(getContext().getDrawable(resId));
        mCenterInfo[INFO_X] = knobPicture.getWidth() / 2f;
        mCenterInfo[INFO_Y] = knobPicture.getHeight() / 2f;
        getLayoutParams().width = knobPicture.getWidth();
        getLayoutParams().height = knobPicture.getHeight();
    }

    public void setDisableDrawableResource(int resId) {
        cacheDisableDrawable(getContext().getDrawable(resId));
    }

    public void initDegrees(float start, float total) {
        mInitDegrees = start;
        mTotalDegrees = total;
    }

    private double getDegrees(float fromX, float fromY, float toX, float toY) {
        double angrad = Math.atan2((fromX - mCenterInfo[INFO_X]), (fromY - mCenterInfo[INFO_Y])) - Math.atan2((toX - mCenterInfo[INFO_X]), (toY - mCenterInfo[INFO_Y]));
        if (Math.abs(angrad) > ERROR_RATE) return 0;
        return Math.toDegrees(angrad);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (this.mEnableKnobPicture == null || this.mEnableKnobPicture.get() == null) return;
        Bitmap knobPicture = this.mEnableKnobPicture.get();

        mMatrix.reset();
        mMatrix.postRotate(mOnDegrees + mInitDegrees, mCenterInfo[INFO_X], mCenterInfo[INFO_Y]);
        canvas.drawBitmap(knobPicture, mMatrix, null);
        if (isInEditMode()) {
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setAlpha(64);
            RectF oval = new RectF(0, 0, knobPicture.getWidth(), knobPicture.getHeight());
            canvas.drawArc(oval, mOnDegrees + mInitDegrees - 90, mTotalDegrees, true, paint);
        }
        super.onDraw(canvas);
    }

    protected void setOnRotateListener(Knob.OnRotateListener listener) {
        mOnRotateListener = listener;
    }

    private OnTouchListener mOnTouch = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mState = State.DOWN;
                    mTouchDownInfo[INFO_X] = event.getX();
                    mTouchDownInfo[INFO_Y] = event.getY();
                    mTouchMoveInfo[INFO_X] = event.getX();
                    mTouchMoveInfo[INFO_Y] = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    mState = State.UP;
                    break;
                case MotionEvent.ACTION_MOVE:
                    mState = State.MOVE;
                    float x = event.getX();
                    float y = event.getY();
                    float degrees = (float) getDegrees(mTouchMoveInfo[INFO_X], mTouchMoveInfo[INFO_Y], x, y);
                    mTouchMoveInfo[INFO_X] = x;
                    mTouchMoveInfo[INFO_Y] = y;
//                    Log.w(TAG, "degrees:" + degrees);
                    addDegrees(degrees);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mState = State.NOTHING;
                default:
                    break;
            }
            return true;
        }
    };
}


/*
    假設三邊長分別為 10、5.26、7.03 求出角度

    CosA=(5.26^2+7.03^2－10^2)/(2*5.26*7.03)=－0.3098007
    A=ArcCos(－0.3098007)=108.047度

    CosB=(10^2+7.03^2－5.26^2)/(2*10*7.03)=0.8659551
    B=ArcCos(0.8659551)=30.003度

    CosC=(10^2+5.26^2－7.03^2)/(2*10*5.26)=0.7437899
    C=ArcCos(0.7437899)=41.944度
*/

/*
    (3，2)為A、(4，-6)為B 求AB之距離

   √((4-3)^2＋(-6-2)^2)

    兩點距離公式
    設兩點(X1,Y1) (X2,Y2)
    兩點距離為 √((X2-X1)^2+(Y2-Y1)^2)
*/
