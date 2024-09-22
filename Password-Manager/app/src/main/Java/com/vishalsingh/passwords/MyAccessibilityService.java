package com.vishalsingh.passwords;
import android.accessibilityservice.*;
import android.annotation.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.view.accessibility.*;
import java.util.*;
import android.view.*;
import android.widget.*;
import android.preference.*;

public class MyAccessibilityService extends AccessibilityService
{
	CustomizedExceptionHandler log = new CustomizedExceptionHandler("Crash Reports", "Passwords");
	ArrayList<AccessibilityNodeInfo> textViewNodes = new ArrayList<AccessibilityNodeInfo>();
	SharedPreferences storage;
	Boolean run = false;
	Handler handler = new Handler();;
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event)
	{
		mainFunc();
	}
	
	
	public void mainFunc(){
		Thread.setDefaultUncaughtExceptionHandler(log);
		storage = PreferenceManager.getDefaultSharedPreferences(this);
	
		AccessibilityNodeInfo rootNode = getRootInActiveWindow();
		
		if(rootNode != null){
			String pkgName = rootNode.getPackageName().toString();
			run = true;
			
			if(pkgName.equals("com.opera.browser") | pkgName.equals("com.android.chrome") | pkgName.equals("com.brave.browser")){
				textViewNodes.clear();
				findChildViews(rootNode);
				
				if(textViewNodes.size()>2){
					AccessibilityNodeInfo website = textViewNodes.get(textViewNodes.size()-1);
					Map<String, ?> allEntries = storage.getAll();
					String data = null;
					for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
						if(website.getText().toString().contains(entry.getKey())){
							data = entry.getValue().toString();
							break;
						}
					}
					if(data != null){
						Intent intent = new Intent(this, ShowOptions.class);
						intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra("data", data);
						startActivity(intent);
					}
				/*}else if(textViewNodes.size()==2){
					if(textViewNodes.get(textViewNodes.size()-1).getText().toString().contains("accounts.google.com/v3/signin")){
						String data = storage.getString("accounts.google.com/v3/signin", null);
						if(data == null){
							data = storage.getString("accounts.google.com/v3/signin/", null);
						}
						if(data != null){
							Intent intent = new Intent(this, ShowOptions.class);
							intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
							intent.putExtra("data", data);
							intent.putExtra("google", true);
							startActivity(intent);
						}
					}*/
				}else if(textViewNodes.size()>1){
					AccessibilityNodeInfo website = textViewNodes.get(textViewNodes.size()-1);
					Map<String, ?> allEntries = storage.getAll();
					String data = null;
					for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
						if(website.getText().toString().contains(entry.getKey())){
							data = entry.getValue().toString();
							break;
						}
					}
					if(data != null){
						Intent intent = new Intent(this, ShowOptions.class);
						intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra("data", data);
						intent.putExtra("onlyOne", true);
						startActivity(intent);
					}
				}
			}
			else if(pkgName.equals("com.google.android.gms")){
				textViewNodes.clear();
				findChildViews(rootNode);	
				
				if(textViewNodes.size() == 1){
					String data = storage.getString("accounts.google.com/v3/signin", null);
					if(data != null){
						Intent intent = new Intent(this, ShowOptions.class);
						intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra("data", data);
						intent.putExtra("google", true);
						startActivity(intent);
					}
				}
			}
		}
		else{
			log.i("root null");
		}
	}
	
	
	
	
	@Override public void onInterrupt(){}
	
	
	
	
	public void pasteText(AccessibilityNodeInfo node, String text) {
        Bundle arguments = new Bundle();
        arguments.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
    }
	
	
	
	
	public void workOpera(String text, String pass){
		try{
			Thread.sleep(500);
			AccessibilityNodeInfo rootNode = getRootInActiveWindow();
			textViewNodes.clear();
			findChildViews(rootNode);
			AccessibilityNodeInfo emailField = textViewNodes.get(0);
			AccessibilityNodeInfo passField = textViewNodes.get(1);
			if(emailField != null & passField != null){
				pasteText(emailField, MainActivity.decrypt(text));
				pasteText(passField, MainActivity.decrypt(pass));
			}else{
				log.i("Problem while pasting text! Error: null nodeInfo");
			}
		}catch(Exception e){
			log.e(this, e);
		}
	}
	
	
	
	
	
	public void workOnlyOne(String[] info){
		try{
			Thread.sleep(500);
			AccessibilityNodeInfo rootNode = getRootInActiveWindow();
			textViewNodes.clear();
			findChildViews(rootNode);
			AccessibilityNodeInfo emailField = textViewNodes.get(0);
			if(emailField != null){
				pasteText(emailField, MainActivity.decrypt(info[1]));
				Intent intent = new Intent(this, ShowOptions.class);
				intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("pass", info[2]);
				intent.putExtra("showPassBox", true);
				startActivity(intent);
			}else{
				log.i("Problem while pasting email! Error: null nodeInfo");
			}
		}catch(Exception e){
			log.e(this, e);
		}
	}
	
	
	
	public void pastePassword(String pass){
		try{
			AccessibilityNodeInfo rootNode = getRootInActiveWindow();
			textViewNodes.clear();
			findChildViews(rootNode);
			AccessibilityNodeInfo passField = textViewNodes.get(0);
			if(passField != null){
				pasteText(passField, MainActivity.decrypt(pass));
			}else{
				log.i("Problem while pasting password! Error: null nodeInfo");
			}
		}catch(Exception e){
			log.e(e);
		}
	}
	
	
	
	
	
	public void workGoogle(String[] data){
		try{
			Thread.sleep(500);
			AccessibilityNodeInfo rootNode = getRootInActiveWindow();
			textViewNodes.clear();
			findChildViews(rootNode);
			AccessibilityNodeInfo emailField = textViewNodes.get(0);
			if(emailField != null){
				pasteText(emailField, MainActivity.decrypt(data[1]));
				AccessibilityNodeInfo rootNode1 = getRootInActiveWindow();
				textViewNodes.clear();
				findButtons(rootNode1);
				if(textViewNodes.size()>0){
					textViewNodes.get(textViewNodes.size()-1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
				}
				Thread.sleep(4000);
				AccessibilityNodeInfo rootNodeNew = getRootInActiveWindow();
				textViewNodes.clear();
				findChildViews(rootNodeNew);
				AccessibilityNodeInfo passField = textViewNodes.get(0);
				if(passField != null){
					pasteText(passField, MainActivity.decrypt(data[2]));
					AccessibilityNodeInfo rootNode2 = getRootInActiveWindow();
					textViewNodes.clear();
					findButtons(rootNode2);
					if(textViewNodes.size()>0){
						textViewNodes.get(textViewNodes.size()-1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
					}
				}else{
					log.i("Problem while pasting text! Error: null passField");
				}
			}else{
				log.i("Problem while pasting text! Error: null emailField");
			}
		}catch(Exception e){
			log.e(this, e);
		}
	}
	
	
	
	private void findChildViews(AccessibilityNodeInfo parentView) {
        if (parentView == null || parentView.getClassName() == null ) {
            return;
        }
		int childCount = parentView.getChildCount();
        if (childCount == 0 && (parentView.getClassName().toString().contentEquals("android.widget.EditText"))) {
            textViewNodes.add(parentView);
        } else {
            for (int i = 0; i < childCount; i++) {
                findChildViews(parentView.getChild(i));
            }
        }
    }
	
	
	
	
	private void findButtons(AccessibilityNodeInfo parentView) {
        if (parentView == null || parentView.getClassName() == null ) {
            return;
        }
		int childCount = parentView.getChildCount();
        if (childCount == 0 && (parentView.getClassName().toString().contentEquals("android.widget.Button"))) {
			textViewNodes.add(parentView);
        } else {
            for (int i = 0; i < childCount; i++) {
                findButtons(parentView.getChild(i));
            }
        }
    }
	

	
	
	
	
	
	public void show(){
		performGlobalAction(GLOBAL_ACTION_BACK);
		run = false;
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			log.e(e);
		}
		new java.util.Timer().schedule( 
			new java.util.TimerTask() {
				@Override
				public void run() {
					handler.post(new Runnable() {
							public void run() {
								if(run == false){
									log.i("not run");
								}
							}
						});
				}
			}, 
			4000 
		);
		mainFunc();
		
	}

	
	
	public void pauseAccService(Boolean isGoogle){
		try
		{
			if(isGoogle){
				Thread.sleep(10000);
			}else{
				Thread.sleep(5000);
			}
		}
		catch (InterruptedException e)
		{
			log.e(this, e);
		}
	}
	
	
	

	private static MyAccessibilityService sSharedInstance;

	protected void onServiceConnected() {
		sSharedInstance = this;
	}

	public boolean onUnbind(Intent intent) {
		sSharedInstance = null;
		return true;
	}

	public static MyAccessibilityService getSharedInstance() {
		return sSharedInstance;
	}
	
	public void dumpNode(AccessibilityNodeInfo node){
		String htmlInfo = node.getClassName().toString();
		log.i("ClassName : " + htmlInfo);
		int numberChildren = node.getChildCount();

		/*StringBuilder builder = new StringBuilder();
		 builder.append(prefix)
		 .append("autoFillId: ").append(node.getAutofillId())
		 .append("\tclassName: ").append(node.getClassName())
		 .append('\n');

		 builder.append(prefix)
		 .append("focused: ").append(node.isFocused())
		 .append("\tvisibility: ").append(node.getVisibility())
		 .append("\twebDomain: ").append(node.getWebDomain())
		 .append('\n');

		 if (htmlInfo != null) {
		 builder.append(prefix)
		 .append("HTML TAG: ").append(htmlInfo.getTag())
		 .append(" attrs: ").append(htmlInfo.getAttributes())
		 .append('\n');
		 }

		 String[] afHints = node.getAutofillHints();
		 builder.append(prefix).append("afType: ").append(getAutofillTypeAsString(node.getAutofillType()))
		 .append("\tafHints: ").append(afHints == null ? "N/A" : Arrays.toString(afHints))
		 .append("\tidPackage: ").append(node.getIdPackage() == null ? "N/A" : node.getIdPackage())
		 .append('\n');

		 builder.append(prefix).append("\ttext: ").append(node.getText())
		 .append('\n');

		 if(node.getClassName().equals("android.widget.EditText")){
		 log.i(builder.toString());
		 }*/
		 
		for (int i = 0; i < numberChildren; i++) {
			dumpNode(node.getChild(i));
		}
	}
	
	
}
