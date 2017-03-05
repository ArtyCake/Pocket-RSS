package com.artycake.pocketrss.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.artycake.pocketrss.R;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;

/**
 * Created by artycake on 3/1/17.
 */

public class PicassoImageGetter implements Html.ImageGetter {
    private static int debuggingVar = 0;
    private final Resources resources;
    private final Picasso picasso;
    private final TextView textView;

    public PicassoImageGetter(final TextView textView, final Resources resources, Picasso picasso) {
        this.textView = textView;
        this.resources = resources;
        this.picasso = picasso;
    }

    @Override
    public Drawable getDrawable(final String source) {
        final BitmapDrawablePlaceHolder result = new BitmapDrawablePlaceHolder();

        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(final Void... meh) {
                Log.d("LSN", debuggingVar + " No. Task");
                try {
                    return picasso.load(source).get();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final Bitmap bitmap) {
                Log.d("LSN", debuggingVar++ + " No. Finished");
                try {

                    final BitmapDrawable drawable = new BitmapDrawable(resources, bitmap);
                    WindowManager wm = (WindowManager) textView.getContext().getSystemService(Context.WINDOW_SERVICE);
                    Display display = wm.getDefaultDisplay();
                    int deviceWidth;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                        Point size = new Point();
                        display.getSize(size);
                        deviceWidth = size.x;
                    } else {
                        deviceWidth = display.getWidth();
                    }
                    int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
                    int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    textView.measure(widthMeasureSpec, heightMeasureSpec);
                    int width;
                    int height;
                    if (drawable.getIntrinsicWidth() > textView.getMeasuredWidth()) {
                        double multiplier = (double) textView.getMeasuredWidth() / drawable.getIntrinsicWidth();
                        Logger.d("%d / %d = %f", textView.getMeasuredWidth(), drawable.getIntrinsicWidth(), multiplier);
                        width = Double.valueOf(drawable.getIntrinsicWidth() * multiplier).intValue();
                        height = Double.valueOf(drawable.getIntrinsicHeight() * multiplier).intValue();
                    } else {
                        width = drawable.getIntrinsicWidth();
                        height = drawable.getIntrinsicHeight();
                    }
                    drawable.setBounds(0, 0, width, height);
//                    drawable.mutate().setColorFilter(ContextCompat.getColor(textView.getContext(), R.color.colorPrimaryInverse), PorterDuff.Mode.MULTIPLY);
                    result.setDrawable(drawable);
                    result.setBounds(0, 0, width, height);

                    textView.setText(textView.getText()); // invalidate() doesn't work correctly...
                } catch (Exception e) {
            /* nom nom nom*/
                }
            }
        }.execute((Void) null);

        return result;
    }

    private static class BitmapDrawablePlaceHolder extends BitmapDrawable {

        protected Drawable drawable;

        @Override
        public void draw(final Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }
    }
}
