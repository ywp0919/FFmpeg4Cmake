package com.wepon.ffmpeg4cmake;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceView;

public class FFVideoPlayer extends SurfaceView {
    public FFVideoPlayer(Context context) {
        this(context, null);
    }

    public FFVideoPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FFVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().setFormat(PixelFormat.RGBA_8888);
    }

    public void play(final String url) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                render(url, FFVideoPlayer.this.getHolder().getSurface());
            }
        }).start();
    }

    public native void render(String url, Surface surface);

}
