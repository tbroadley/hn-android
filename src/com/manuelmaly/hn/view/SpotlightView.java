package com.manuelmaly.hn.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jmaltz on 12/23/13.
 */
public class SpotlightView extends View {

    private float mXStart;
    private float mYStart;
    private float mXEnd;
    private float mYEnd;

    private RadialGradient mRadialGradient;

    private final Paint mClearPaint;
    private Paint mGradientPaint;
    private final TextPaint mTextPaint;

    private Rect mClearRect;
    Bitmap mBitmap;

    private StaticLayout mTextLayout;

    public SpotlightView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        mClearPaint = new Paint();
        mClearPaint.setColor(Color.TRANSPARENT);
        mClearPaint.setAntiAlias(true);
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mClearPaint.setMaskFilter(new BlurMaskFilter(2 * getResources().getDisplayMetrics().density , Blur.NORMAL));

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize((float)(getResources().getDisplayMetrics().density * 20 + .5));
        mTextPaint.setShadowLayer(1.5f, 1, 1, Color.BLACK);
        // TODO make this better and actually read in the start and end attributes
    }

    public void setCoords(float xStart, float yStart, float xEnd, float yEnd) {
        mXStart = xStart;
        mYStart = yStart;
        mXEnd = xEnd;
        mYEnd = yEnd;

        float xCenter = mXStart + (mXEnd - mXStart)/2;
        float yCenter = mYStart + (mYEnd - mYStart)/2;
        // I want to basically make everything 0xFF000000 and I don't know how
        // to properly do that.  This solves that problem.
        mRadialGradient = new RadialGradient(xCenter, yCenter, 1,
                0xFF000000, 0xFF000000, TileMode.CLAMP);

        mGradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mGradientPaint.setColor(Color.BLACK);
        mGradientPaint.setAlpha(200);

        mClearRect = new Rect((int)mXStart, (int)mYEnd, (int)mXEnd, (int)mYStart);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        float density = getResources().getDisplayMetrics().density;
        mTextLayout = new StaticLayout("Clicking on the word \"comments\" will take you to the article viewer",
                mTextPaint, w - (int)(density * 20 + .5),
                Layout.Alignment.ALIGN_CENTER, 1, 0, false);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);
        canvas.drawRect(new Rect(0, 0, w, h), mGradientPaint);
        canvas.drawRect(mClearRect, mClearPaint);
        canvas.translate(0, mYEnd + (int)(density * 10 + .5));
        mTextLayout.draw(canvas);
    }
}
