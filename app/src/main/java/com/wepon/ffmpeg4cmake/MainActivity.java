package com.wepon.ffmpeg4cmake;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


    TextView tvInfo;
    private FFVideoPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_protocol).setOnClickListener(this);
        findViewById(R.id.btn_codec).setOnClickListener(this);
        findViewById(R.id.btn_filter).setOnClickListener(this);
        findViewById(R.id.btn_format).setOnClickListener(this);
        findViewById(R.id.btn_player).setOnClickListener(this);
        mPlayer = findViewById(R.id.ffVideoPlayer);

        tvInfo = findViewById(R.id.tv_info);
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_protocol:
                tvInfo.setText(urlprotocolinfo());
                break;
            case R.id.btn_format:
                tvInfo.setText(avformatinfo());
                break;
            case R.id.btn_codec:
                tvInfo.setText(avcodecinfo());
                break;
            case R.id.btn_filter:
                tvInfo.setText(avfilterinfo());
                break;
            case  R.id.btn_player:
                mPlayer.play("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
                break;
            default:
                break;
        }
    }

    public native String stringFromJNI();

    public native String urlprotocolinfo();

    public native String avformatinfo();

    public native String avcodecinfo();

    public native String avfilterinfo();




}
