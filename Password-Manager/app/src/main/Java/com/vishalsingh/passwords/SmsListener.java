package com.vishalsingh.passwords;

import android.content.*;
import android.os.*;
import android.telephony.*;
import android.util.*;
import java.util.regex.*;

public class SmsListener extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();       
            SmsMessage[] msgs = null;
            if (bundle != null){
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        String msgBody = msgs[i].getMessageBody();
						if(msgBody.toLowerCase().contains("otp")){
							String regex = "(\\d{4,6})";
							Pattern pattern = Pattern.compile(regex); 
							Matcher matcher = pattern.matcher(msgBody);
							if (matcher.find()) {
								ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE); 
								ClipData clip = ClipData.newPlainText("OTP", matcher.group(1));
								clipboard.setPrimaryClip(clip);
							} else {}
						}
                    }
                }catch(Exception e){}
            }
        }
    }
}
