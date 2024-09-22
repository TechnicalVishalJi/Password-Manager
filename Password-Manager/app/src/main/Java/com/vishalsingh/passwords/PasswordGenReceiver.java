package com.vishalsingh.passwords;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.security.*;
import java.util.*;

public class PasswordGenReceiver extends BroadcastReceiver
{
	CustomizedExceptionHandler log = new CustomizedExceptionHandler("Crash Reports", "Passwords");
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Thread.setDefaultUncaughtExceptionHandler(log);

		String pass= generatePassword(context,14);
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE); 
		ClipData clip = ClipData.newPlainText("Text", pass);
		clipboard.setPrimaryClip(clip);

		NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
		Notification notification;
		Notification.Builder notificationBuilder = new Notification.Builder(context.getApplicationContext())
			.setSmallIcon(R.drawable.ic_launcher)
			.setPriority(Notification.PRIORITY_MAX)
			.setSubText("Generate New Password")
			.setStyle(new Notification.BigTextStyle()
					  .bigText("Click here to generate and copy new password.\nCopied Password :  " + pass))
			.setAutoCancel(false)
			.setTimeoutAfter(240000);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notification = notificationBuilder.setChannelId("00").build();
			nm.createNotificationChannel(new NotificationChannel("00", "Imp", NotificationManager.IMPORTANCE_MAX));
		}else{
			notification = notificationBuilder.build();
		}

		Intent notificationIntent = new Intent(context.getApplicationContext(), PasswordGenReceiver.class);

		PendingIntent contentIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, notificationIntent, 0);
		notification.contentIntent = contentIntent;

		nm.notify(1, notification);
	}


	public static String generatePassword(Context context,int length){
		char[] SYMBOLS = "^$*.[]{}()?-\"!@#%&/\\,><':;|_~`".toCharArray();
		char[] LOWERCASE = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		char[] UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		char[] NUMBERS = "0123456789".toCharArray();
		char[] ALL_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789^$*.[]{}()?-\"!@#%&/\\,><':;|_~`".toCharArray();
		Random rand = new SecureRandom(); 

        assert length >= 4;
        char[] password = new char[length];

        //get the requirements out of the way
        password[0] = LOWERCASE[rand.nextInt(LOWERCASE.length)];
        password[1] = UPPERCASE[rand.nextInt(UPPERCASE.length)];
        password[2] = NUMBERS[rand.nextInt(NUMBERS.length)];
        password[3] = SYMBOLS[rand.nextInt(SYMBOLS.length)];

        //populate rest of the password with random chars
        for (int i = 4; i < length; i++) {
            password[i] = ALL_CHARS[rand.nextInt(ALL_CHARS.length)];
        }

        //shuffle it up
        for (int i = 0; i < password.length; i++) {
            int randomPosition = rand.nextInt(password.length);
            char temp = password[i];
            password[i] = password[randomPosition];
            password[randomPosition] = temp;
        }

        return new String(password);

	}

}
