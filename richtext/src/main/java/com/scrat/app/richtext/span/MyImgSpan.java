package com.scrat.app.richtext.span;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.scrat.app.richtext.listener.OnImageClickListener;

/**
 * Created by yixuanxuan on 16/8/9.
 */
public class MyImgSpan extends ImageSpan {
    private String mUri;

    public MyImgSpan(Context context, Bitmap b, String path) {
        super(context, b);
        mUri = path;
    }

    @Override
    public String getSource() {
        return mUri;
    }

    public void onClick() {
        Log.d("MyImageSpan", mUri);
    }
}
