package gyu.geekyouup.android.widgets.battery;

import java.lang.reflect.Method;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager; 
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.*;
import gyu.geekyouup.android.widgets.battery.cleaner.Cleaner;

/**
 * <h3>Fancy Blurred Background Activity</h3>
 */
public class TranslucentBlurActivity extends Activity implements OnClickListener {
	
	private Button mBtnDisplaySettings;
	private ToggleButton mBtnWifiSettings;
	private ToggleButton mBtnGPSSettings;
	private ToggleButton mBtnBluetoothSettings;
	private Button mBtnBatterySettings;
    private Button mBtnNetworkSettings;
    private Button mBtnShowDesktop;

    private Intent intentGPS;
    private Intent intentNetwork;

    private static final String LOG_TAG = "BatteryWidget";

    @Override
    protected void onCreate(Bundle icicle) {
        // Be sure to call the super class.
        super.onCreate(icicle);
        
        // Have the system blur any windows behind this one.
        if(!isLiveWallpaper(this) && BatteryWidget.SDK_VERSION<11) //no blur on honeycomb
        {
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
	                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        }
        
        setContentView(R.layout.settings);

        mBtnDisplaySettings = (Button) findViewById(R.id.displaySettings);
        mBtnDisplaySettings.setOnClickListener(this);
        
        mBtnWifiSettings = (ToggleButton) findViewById(R.id.wifiSettings);
        mBtnWifiSettings.setOnClickListener(this);
        
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        mBtnWifiSettings.setChecked(wifiManager.isWifiEnabled());

        mBtnGPSSettings = (ToggleButton) findViewById(R.id.gpsSettings);
        mBtnGPSSettings.setOnClickListener(this);
        
        mBtnBluetoothSettings = (ToggleButton) findViewById(R.id.bluetoothSettings);
        mBtnBluetoothSettings.setOnClickListener(this);

        mBtnNetworkSettings = (Button) findViewById(R.id.networkSettings);
        mBtnNetworkSettings.setOnClickListener(this);

        mBtnShowDesktop = (Button) findViewById(R.id.cleaner);
        mBtnShowDesktop.setOnClickListener(this);

        if (BatteryWidget.SDK_VERSION > 3)
        {
	        mBtnBatterySettings = (Button) findViewById(R.id.batterySettings);
	        mBtnBatterySettings.setOnClickListener(this);
        }
        
        try
        {
        	LocationManager locManager = (LocationManager)getSystemService(LOCATION_SERVICE);
            boolean gpsOn = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            mBtnGPSSettings.setChecked(gpsOn);
        	
        	int btStatus = Settings.Secure.getInt(getContentResolver(), android.provider.Settings.Secure.BLUETOOTH_ON);
        	mBtnBluetoothSettings.setChecked(btStatus == 1);
        }catch(Exception e)
        {
        	Log.d(LOG_TAG,"Failed to get Bluetooth status",e);
        }

        //disable the buttons that won't work
        intentGPS = new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
        if(!isIntentAvailable(this, intentGPS)) mBtnGPSSettings.setEnabled(false);

        intentNetwork = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        intentNetwork.addCategory("android.intent.category.DEFAULT");
        if(!isIntentAvailable(this, intentNetwork))
        {
            intentNetwork = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            if(!isIntentAvailable(this, intentNetwork))
            {
                mBtnNetworkSettings.setEnabled(false);
            }
        }

        ((LinearLayout) findViewById(R.id.settingsLayout)).startAnimation(AnimationUtils.loadAnimation(this, R.anim.launchanim));
    }
	
    protected void onPause() {
        super.onPause();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


	public void onClick(View v) {
		
		try
		{
			if(v==mBtnDisplaySettings)
			{
				Intent defineIntent2 = new Intent("com.android.settings.DISPLAY_SETTINGS");
				defineIntent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				defineIntent2.addCategory("android.intent.category.DEFAULT");
				startActivity(defineIntent2);
				finish();
			}else if(v== mBtnWifiSettings)
			{
				WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
				boolean enabled = wifiManager.isWifiEnabled();
				wifiManager.setWifiEnabled(!enabled);
				
				Toast.makeText(this, enabled?"Disabling Wifi":"Enabling Wifi", Toast.LENGTH_SHORT).show();
				finish();
			}else if(v==mBtnGPSSettings)
			{
				Intent defineIntent2 = new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
				defineIntent2.addCategory("android.intent.category.DEFAULT");
				defineIntent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(defineIntent2);
				finish();
			}else if(v==mBtnBluetoothSettings)
			{
                Intent defineIntent2 = new Intent();
                if (BatteryWidget.SDK_VERSION >= 14) {
                    defineIntent2.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
                } else {
                    defineIntent2.setAction("android.settings.WIRELESS_SETTINGS");
                }

				defineIntent2.addCategory("android.intent.category.DEFAULT");
				defineIntent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(defineIntent2);
				finish();
			}else if(v==mBtnBatterySettings)
			{
				Intent defineIntent2 = new Intent("android.intent.action.POWER_USAGE_SUMMARY");
                defineIntent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(defineIntent2);
				finish();
			} else if(v==mBtnNetworkSettings)
            {
                startActivity(intentNetwork);
                finish();
            } else if(v==mBtnShowDesktop)
            {
                Intent defineIntent2 = new Intent(this,Cleaner.class);
                defineIntent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(defineIntent2);
                finish();
            }
		}catch(Exception e){Log.e(LOG_TAG,"Settings Error",e);}
	}
	
    public static boolean isLiveWallpaper(Context context)
    {
    	boolean liveWallpaper = false;
   		try
   		{
	   		if(BatteryWidget.SDK_VERSION>=7)
	   		{
	   			//this version of code needs Android 2.0, but we want to run on 1.5+, so use reflection
	   			//this version of code works on all vers of android
		   		Class classWallpaperManager = Class.forName("android.app.WallpaperManager");
		   		if(classWallpaperManager != null)
	        	{
	        		Method methodGetInstance = classWallpaperManager.getDeclaredMethod("getInstance", Context.class);
	        		Object objWallpaperManager = methodGetInstance.invoke(classWallpaperManager, context);
	        		
	        		Method methodGetWallpaperInfo = objWallpaperManager.getClass().getMethod("getWallpaperInfo", null);
	        		Object objWallPaperInfo = methodGetWallpaperInfo.invoke(objWallpaperManager, null);
	        		if(objWallPaperInfo!=null)
	        		{
	        			liveWallpaper=true;
	        		}
	        	}
	   		}
   		}catch(Throwable t){}
   		
   		return liveWallpaper;
    }

    public static boolean isIntentAvailable(Context context, Intent action) {
        try
        {
            final PackageManager packageManager = context.getPackageManager();
            List list = packageManager.queryIntentActivities(action,
                            PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        }catch(Exception e)
        {
            Log.e(LOG_TAG, "Intent Checking Failed");
        }

        return true;
    }
}
