package gyu.geekyouup.android.widgets.battery;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Define a simple widget that shows a battery meter. To build
 * an update we spawn a background {@link Service} to perform the API queries.
 */
public class BatteryWidget extends AppWidgetProvider {
	
	public static final String LOG_TAG = BatteryWidget.class.getSimpleName();
	//public static final int SDK_VERSION = Integer.parseInt(android.os.Build.VERSION.SDK); //1.5 version
	public static final int SDK_VERSION = android.os.Build.VERSION.SDK_INT;
    public static final String KEY_SCALE = "KEY_SCALE";
	public static String PREFS_NAME="BATWIDG_PREFS";
	public static String KEY_LEVEL = "BATWIDG_LEVEL";
	public static String KEY_CHARGING = "BATWIDG_CHARGING";
	public static String KEY_VOLTAGE = "BATWIDG_VOLTAGE";
	
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		BatteryWidget.clearSettings(context);
	}
	
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		try
		{
			context.stopService(new Intent(context, UpdateService.class));//unregisterReceiver(mBI);
		}catch(Exception e){Log.d("BatteryWidget","Exception on disable: ",e);}
		BatteryWidget.clearSettings(context);
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		try
		{
			context.stopService(new Intent(context, UpdateService.class));//if(mBI != null) context.unregisterReceiver(mBI);
		}catch(Exception e){Log.d("BatteryWidget","Exception on delete: ",e);}
	}
	
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
        // To prevent any ANR timeouts, we perform the update in a service
    	context.startService(new Intent(context, UpdateService.class));
    }
    
    private static void clearSettings(Context context) {
        if (context != null)
        {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Editor editor = settings.edit();
            editor.remove(KEY_LEVEL);
            editor.remove(KEY_CHARGING);
            editor.remove(KEY_SCALE);
            editor.commit();
        }
    }
    
    public static class UpdateService extends Service {
    	
    	BatteryInfo mBI = null;

    	@Override
        public void onStart(Intent intent, int startId) {
        	
			if(mBI == null)
	        {
	        	mBI = new BatteryInfo();
	        	IntentFilter mIntentFilter = new IntentFilter();
	            mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
	            registerReceiver(mBI, mIntentFilter);
	        }
        	
        	// Build the widget update for today
        	RemoteViews updateViews = buildUpdate(this);
            if(updateViews != null)
            {
	            try
	            {
		            // Push update for this widget to the home screen
		            ComponentName thisWidget = new ComponentName(this, BatteryWidget.class);
		            if(thisWidget != null)
		            {
			            AppWidgetManager manager = AppWidgetManager.getInstance(this);
			            if(manager != null && updateViews != null)
			            {
			            	manager.updateAppWidget(thisWidget, updateViews);
			            }
		            }
		            
		            //stop the service, clear up memory, can't do this, need the Broadcast Receiver running
		            //stopSelf();
	            }catch(Exception e)
	            {
	            	Log.e("Widget", "Update Service Failed to Start", e);
	            }
            }
        }

    	@Override
    	public void onDestroy() {
    		super.onDestroy();
    		try{
    			if(mBI != null) unregisterReceiver(mBI);
    		}catch(Exception e)
    		{Log.e("Widget", "Failed to unregister", e);}
    	}
    	
        /**
         * Build a widget update to show the current Wiktionary
         * "Word of the day." Will block until the online API returns.
         */
        public RemoteViews buildUpdate(Context context) {           
            // Build an update that holds the updated widget contents
            RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget);
            try
            {
            	//Log.d("BatteryWidget","Updating Views");
	            int level = 0;
	            boolean charging = false;
	            SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	            if(settings !=null)
	            {
	            	level = settings.getInt(KEY_LEVEL, 0);
	            	
	            	//update level based on scale
	            	int scale = settings.getInt(KEY_SCALE, 100);
	            	if(scale != 100)
	            	{
			            if (scale <= 0) scale = 100;
			            level = (100 * level) / scale;
	            	}
	            	
		            charging = (settings.getInt(KEY_CHARGING, BatteryManager.BATTERY_STATUS_UNKNOWN)==BatteryManager.BATTERY_STATUS_CHARGING);
	            }
	            
	            if(level>20)
	            {
		            updateViews.setViewVisibility(R.id.bar100, level>80?View.VISIBLE:View.INVISIBLE);
		            updateViews.setViewVisibility(R.id.bar80, level>60?View.VISIBLE:View.INVISIBLE);
		            updateViews.setViewVisibility(R.id.bar60, level>40?View.VISIBLE:View.INVISIBLE);
		            updateViews.setViewVisibility(R.id.bar40, level>20?View.VISIBLE:View.INVISIBLE);
		            updateViews.setViewVisibility(R.id.bar20, View.VISIBLE);
		            updateViews.setImageViewResource(R.id.bar20, R.drawable.bar_green);
		            
		            updateViews.setViewVisibility(R.id.batterytext, View.VISIBLE);
	            }else
	            {
		            updateViews.setViewVisibility(R.id.bar100, View.INVISIBLE);
		            updateViews.setViewVisibility(R.id.bar80, View.INVISIBLE);
		            updateViews.setViewVisibility(R.id.bar60, View.INVISIBLE);
		            updateViews.setViewVisibility(R.id.bar40, View.INVISIBLE);
		            updateViews.setViewVisibility(R.id.bar20, View.VISIBLE);
		            updateViews.setImageViewResource(R.id.bar20, R.drawable.bar_red);
		            updateViews.setViewVisibility(R.id.batterytext, View.VISIBLE);
	            }
	
	            updateViews.setViewVisibility(R.id.charging, charging?View.VISIBLE:View.INVISIBLE);
	            
	            String levelText = level==100?"100":level+"%"; //100% too wide
	            if(level == 0) levelText=" 0%";
	            updateViews.setTextViewText(R.id.batterytext, levelText);
	        }catch(Exception e)
	        {
	        	Log.e("BatteryWidget","Error Updating Views",e);
	        }
            
    		try
    		{
	            Intent defineIntent2 = new Intent(context,TranslucentBlurActivity.class);
	            PendingIntent pendingIntent2 = PendingIntent.getActivity(context,0 /* no requestCode */, defineIntent2, 0 /* no flags */);
	            updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent2);
    		}catch(Exception e)
    		{
    			Log.e("BatteryWidget","Error Settings Intents",e);
    		}
            
            return updateViews;
        }
        
        @Override public IBinder onBind(Intent intent) {return null;}
    }

}
