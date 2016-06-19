package android.hxs.com.customgifview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

public class GifMovieView extends View {

	private static final int DEFAULT_MOVIEW_DURATION = 1000;//默认动画周期时间
	private long durations = 0;//存储动画周期时间
	private Movie mMovie;//装载动画对象
	private long mMovieStart;//开发播放的时间(帧)
	private int mCurrentAnimationTime = 0;//当前动画时间
	private float mScale;//缩放比例
	private int mMeasuredMovieWidth;//动画展示的宽度
	private int mMeasuredMovieHeight;//动画展示的高度
	private int screenWidth;//屏幕默认宽度
	private float screenScal;//gif展示的宽度为screenWidth*screenScal

	private volatile boolean mPaused = false;//本次动画完毕是否停止
	private boolean mVisible = true;//动画是否显示
	private StopAnimator stopAnimator;

	public GifMovieView(Context context) {
		this(context, null);
	}

	public GifMovieView(Context context, AttributeSet attrs) {
		this(context, attrs, R.styleable.CustomTheme_gifMoviewViewStyle);
	}

	public GifMovieView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init((Activity) context);
	}

	private void init(Activity context){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);//渲染
		}
		screenWidth = context.getWindowManager().getDefaultDisplay().getWidth();
	}

	//设置资源
	public void setMovieResource(int movieResId) {
		mMovie = Movie.decodeStream(getResources().openRawResource(movieResId));
		requestLayout();
	}

	public void setScreenScal(float screenScal) {
		this.screenScal = screenScal;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mMovie != null) {
			int movieWidth = mMovie.width();
			int movieHeight = mMovie.height();
			//在屏幕显示的宽与图片本身的宽的比例
			mScale = 1f*(screenWidth*screenScal)/movieWidth;
			//在屏幕显示的宽
			mMeasuredMovieWidth = (int) (movieWidth * mScale);
			//在屏幕显示的高
			mMeasuredMovieHeight = (int) (movieHeight * mScale);
			setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);
		} else {
			/*
			 * No movie set, just set minimum available size.
			 */
			setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		mVisible = getVisibility() == View.VISIBLE;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mMovie != null) {
			if (!mPaused) {
				updateAnimationTime();
				drawMovieFrame(canvas);
				invalidateView();
			} else {
				drawMovieFrame(canvas);
			}
		}
	}
	
	//刷新操作;代码兼容
	@SuppressLint("NewApi")
	private void invalidateView() {
		if(mVisible) {
			//Build.VERSION.SDK_INT:当前版本号  Build.VERSION_CODES.JELLY_BEAN:版本号16
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				postInvalidateOnAnimation();
			} else {
				invalidate();
			}
		}
	}

	/**
	 * Calculate current animation time
	 */
	private void updateAnimationTime() {
		//当前时间
		long now = android.os.SystemClock.uptimeMillis();
		//如果第一帧，记录起始时间
		if (mMovieStart == 0) {
			mMovieStart = now;
		}
		int dur = mMovie.duration();//gif动画的周期时间
		if (dur == 0) {
			dur = DEFAULT_MOVIEW_DURATION;
		}
		//算出需要显示第几帧
		mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
	}

	/**
	 * Draw current GIF frame
	 */
	private void drawMovieFrame(Canvas canvas) {
		//设置要显示的帧，绘制即可
		mMovie.setTime(mCurrentAnimationTime);
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(mScale, mScale);
		mMovie.draw(canvas, 0, 0);
		canvas.restore();
	}
	
	//duration时间后,执行onStopAnimator方法
	protected void stopAnima(long duration, StopAnimator stopAnimator){
		this.stopAnimator = stopAnimator;
		if(duration == 0){
			durations = mMovie.duration();
		}else{
			durations = duration;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(durations);
				} catch (Exception e) {
					// TODO: handle exception
				}
				handler.sendEmptyMessage(0);
			}
		}).start();
	}

	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			stopAnimator(true);//暂停动画
			if(stopAnimator != null){
				stopAnimator.onStopAnimator();
			}
		}
	};

	//暂停本次动画
	public void stopAnimator(boolean paused) {
		this.mPaused = paused;
		if (!paused) {
			mMovieStart = android.os.SystemClock.uptimeMillis() - mCurrentAnimationTime;
		}
		invalidate();
	}

	//清除
	public void clear(){
		mMovie = null;
	}

	protected interface StopAnimator{
		void onStopAnimator();
	}
}
