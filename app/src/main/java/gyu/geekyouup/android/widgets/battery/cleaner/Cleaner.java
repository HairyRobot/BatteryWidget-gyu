package gyu.geekyouup.android.widgets.battery.cleaner;

import android.app.Activity;
import android.app.WallpaperManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import gyu.geekyouup.android.widgets.battery.R;

public class Cleaner extends Activity {
    private RelativeLayout mView;
	private WallpaperManager mWPM;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cleaner);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
        
    	AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
        aa.setDuration(1000);
        mView.startAnimation(aa);
        finish();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();

        mView = (RelativeLayout) findViewById(R.id.mainview);
        mView.setFocusableInTouchMode(false);

        if(android.os.Build.VERSION.SDK_INT>= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }

        mWPM = WallpaperManager.getInstance(this);
    }
   
    float xPos = 0;
    float xWP = 0.5f;
    float yPos = 0;
    float yWP = 0.5f;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(event.getAction() == MotionEvent.ACTION_DOWN)
    	{
    		xPos = event.getX();
    		yPos = event.getY();
    		//kill app if touched in bottom right corner
    		if(xPos > mView.getWidth()-50 && event.getY()>mView.getHeight()-50) finish();
    		else
    		{
    			mWPM.sendWallpaperCommand(mView.getWindowToken(), WallpaperManager.COMMAND_TAP, (int) xPos, (int) event.getY(), 0, null);
    		}
    	}else if(event.getAction() == MotionEvent.ACTION_MOVE)
    	{
    		xWP = xWP + (xPos-event.getX())/500;
    		xPos = event.getX();
    		if(xWP < 0) xWP = 0;
    		if(xWP > 1) xWP = 1;
    		
    		yWP = yWP + (yPos-event.getY())/500;
    		yPos = event.getY();
    		if(yWP < 0) yWP = 0;
    		if(yWP > 1) yWP = 1;
    		mWPM.setWallpaperOffsets(mView.getWindowToken(), xWP, yWP);
    	}
    	
    	if(android.os.Build.VERSION.SDK_INT>= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    	{
    		mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    		//Log.d("Cleaner", "Lights out");
    	}
    	
    	
    	return super.onTouchEvent(event);
    }
}