package android.hxs.com.customgifview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class GifViewActivity extends AppCompatActivity {

    private GifMovieView gifView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_view);
        init();
    }

    private void init(){
        gifView = (GifMovieView) findViewById(R.id.gifView);
        gifView.setScreenScal(0.5f);//设置gifView的宽度为默认屏幕的0.5倍
        gifView.setMovieResource(R.mipmap.gif_heart);//添加启动动画
        //第一个参数未0,则显示动画的周期时间;否则,以设置时间为准
        gifView.stopAnima(0, new GifMovieView.StopAnimator() {
            @Override
            public void onStopAnimator() {
                Toast.makeText(GifViewActivity.this,"动画结束",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gifView.clear();
    }
}
