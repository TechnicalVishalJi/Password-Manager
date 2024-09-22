package com.vishalsingh.passwords;
import android.app.*;
import android.content.*;
import android.os.*;
import android.service.quicksettings.*;
import android.provider.*;

public class MyTileService extends TileService {
	
	private static final String CHANNEL_ID = "Warning";
	// Called when the user adds your tile.
	@Override
	public void onTileAdded() {
		super.onTileAdded();
	}

	// Called when your app can update your tile.
	@Override
	public void onStartListening() {
		super.onStartListening();
	}

	// Called when your app can no longer update your tile.
	@Override
	public void onStopListening() {
		super.onStopListening();
	}

	// Called when the user taps on your tile in an active or inactive state.
	@Override
	public void onClick() {
		super.onClick();
		CustomizedExceptionHandler log = new CustomizedExceptionHandler("Crash Reports", "Passwords");
		Thread.setDefaultUncaughtExceptionHandler(log);
		
		MyAccessibilityService obj = MyAccessibilityService.getSharedInstance();
		//obj.getServiceInfo().flags = obj.getServiceInfo().FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
		if(obj != null){
			obj.show();
		}else{
			NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Notification notification;
			Notification.Builder notificationBuilder = new Notification.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setSubText("Can't autofill")
				.setContentText("Please turn off and on accessibility service by clicking on this notification.")
				.setStyle(new Notification.BigTextStyle()
						  .bigText("Please turn off and on accessibility service by clicking on this notification."))
				.setAutoCancel(true);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				notification = notificationBuilder.setChannelId(CHANNEL_ID).build();
				nm.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "Warning", NotificationManager.IMPORTANCE_HIGH));
			}else{
				notification = notificationBuilder.build();
			}
			
			Intent notificationIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, notificationIntent, 0);
			notification.contentIntent = contentIntent;
			
			nm.notify(1, notification);
		}
	}

	// Called when the user removes your tile.
	@Override
	public void onTileRemoved() {
		super.onTileRemoved();
	}
}
