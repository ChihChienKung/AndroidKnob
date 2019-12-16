package com.chien.knob;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;

/**
 * Created by Chien.Kung on 2019-12-04.
 */
public class Knob extends RelativeLayout {
    private final static String TAG = Knob.class.getSimpleName();
    private final int CASE_OF_BACK;
    private final int DISPLAY_OF_BACK;
    private final int ENABLE_KNOB;
    private final int DISABLE_KNOB;

    private ImageView mBack;
    private RotaryView mButton;

    private OnRotateListener mOnRotateListener;

    public Knob(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Knob);
        CASE_OF_BACK = a.getResourceId(R.styleable.Knob_caseOfBack, 0);
        DISPLAY_OF_BACK = a.getResourceId(R.styleable.Knob_displayOfBack, 0);
        ENABLE_KNOB = a.getResourceId(R.styleable.Knob_enableKnob, 0);
        DISABLE_KNOB = a.getResourceId(R.styleable.Knob_disableKnob, 0);
        float startDregrees = a.getFloat(R.styleable.Knob_startDegrees, 0f);
        float totalDegrees = a.getFloat(R.styleable.Knob_totalDegrees, 360f);
        a.recycle();

        if (CASE_OF_BACK != 0)
            mBack.setImageResource(CASE_OF_BACK);
        if (ENABLE_KNOB != 0)
            mButton.setEnableDrawableResource(ENABLE_KNOB);
        if (DISABLE_KNOB != 0)
            mButton.setDisableDrawableResource(DISABLE_KNOB);
        mButton.initDegrees(startDregrees, totalDegrees);
    }

    private void init() {
        final LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View contextView = layoutInflater.inflate(R.layout.main_knob, this, true);

        mBack = contextView.findViewById(R.id.knob_back);
        mButton = contextView.findViewById(R.id.knob_button);

        mButton.setOnRotateListener(new OnRotateListener() {
            @Override
            public void onRotate(View v, float degrees, float moveDegrees) {
//                Log.v(TAG, "degrees:" + degrees);
                if (mOnRotateListener != null) {
                    mOnRotateListener.onRotate(Knob.this, degrees, moveDegrees);
                }
            }
        });
    }

    public void setOnRotateListener(OnRotateListener listener) {
        mOnRotateListener = listener;
    }

    public interface OnRotateListener {
        public void onRotate(View v, float degrees, float moveDegrees);
    }
}
