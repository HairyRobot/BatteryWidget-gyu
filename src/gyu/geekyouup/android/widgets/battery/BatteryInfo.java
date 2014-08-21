package gyu.geekyouup.android.widgets.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

public class BatteryInfo extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
        try
        {
			String action = intent.getAction();
	        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {       		
	            SharedPreferences settings = context.getSharedPreferences(BatteryWidget.PREFS_NAME, Context.MODE_PRIVATE);
	            if(settings !=null)
	            {
	                int prevLevel = settings.getInt(BatteryWidget.KEY_LEVEL, -1);
	                int prevStatus = settings.getInt(BatteryWidget.KEY_CHARGING, -1);
	                
	                //for 1.6-
	                int currentLevel = intent.getIntExtra("level", 0);//BatteryManager.EXTRA_LEVEL for 2.0+
	                int currentStatus = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);//BatteryManager.EXTRA_STATUS for 2.0+

	                // Only update display if something changed.
	                if (prevLevel != currentLevel || prevStatus != currentStatus)
	                {
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt(BatteryWidget.KEY_LEVEL, currentLevel);
	                    editor.putInt(BatteryWidget.KEY_CHARGING, currentStatus);
	                    int scale = intent.getIntExtra("scale", 100); //BatteryManager.EXTRA_SCALE for 2.0+
	                    editor.putInt(BatteryWidget.KEY_SCALE, scale);
	                    editor.commit();
	                    Intent forceUpIntent = new Intent(context, BatteryWidget.UpdateService.class);
	                    context.startService(forceUpIntent);
	               }
	            }
	        }
        }catch(Exception e){Log.e(BatteryWidget.LOG_TAG, "", e);}
	}
}
