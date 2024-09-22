package com.vishalsingh.passwords;

import android.app.assist.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.preference.*;
import android.service.autofill.*;
import android.view.*;
import android.view.ViewStructure.*;
import android.view.autofill.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.regex.*;
import android.app.*;

public class MyAutofillService extends AutofillService
{
	CustomizedExceptionHandler log = new CustomizedExceptionHandler("Crash Reports", "Passwords");
	Map <String, AutofillId> info = new HashMap<>();
	String webDomain;
	SharedPreferences storage;
	Boolean foundToFill = false, datasetAlreadyAdded = false, accountCreation = false;

	@Override
	public void onSaveRequest(SaveRequest request, SaveCallback callback)
	{
		Thread.setDefaultUncaughtExceptionHandler(log);
		storage = PreferenceManager.getDefaultSharedPreferences(this);
	
		// Get the structure from the request
		List<FillContext> context = request.getFillContexts();
		AssistStructure structure = context.get(context.size() - 1).getStructure();

		// Traverse the structure looking for data to save
		Map <String, String> infoText = new HashMap<>();
		try{
			getUsernameAndPass(structure, infoText);
		}catch(Exception e){
			callback.onFailure("Some error occured! Can't Save.");
			return;
		}
		if(infoText.containsKey("username") & infoText.containsKey("pass") & webDomain != null){
			String username = infoText.get("username");
			String pass= infoText.get("pass");
			String account = storage.getString(webDomain, null);
			if(account == null){
				String data = MainActivity.getWebAppName(webDomain) + "**//**" + encrypt(username) + "**//**" + encrypt(pass) + "##//##";
				storage.edit().putString(webDomain, data).apply();
			}else{
				String[] array = account.split("##//##");
				for(String i : array){
					String acc = decrypt(i.split("\\*\\*//\\*\\*")[1]);
					if(acc.contains(username)){
						callback.onFailure("This account already exists!");
						return;
					}
				}
				String data = account + MainActivity.getWebAppName(webDomain) + "**//**" + encrypt( username ) + "**//**" + encrypt(pass) + "##//##";
				storage.edit().putString(webDomain, data).apply();
				callback.onSuccess();
			}
		}else{
			callback.onFailure("Can't Save!");
		}
	}

	@Override
	public void onFillRequest(FillRequest request, CancellationSignal cancellationSignal, final FillCallback callback) {
		Thread.setDefaultUncaughtExceptionHandler(log);
		
		storage = PreferenceManager.getDefaultSharedPreferences(this);
		
		// Get the structure from the request
		List<FillContext> context = request.getFillContexts();
		AssistStructure structure = context.get(context.size() - 1).getStructure();

		// Traverse the structure looking for nodes to fill out
		traverseStructure(structure);

		if(webDomain != null){
			//Customise the looks of autofill box
			final RemoteViews view = new RemoteViews(getPackageName(), R.layout.autofill_menu);

			final AutofillId focused = info.get("focused");
			final AutofillId usernameId = info.get("username");
			final AutofillId passId = info.get("pass");
			AutofillId mobileId = info.get("mobile");
			AutofillId stateId = info.get("state");
			AutofillId cityId = info.get("city");
			AutofillId countryId = info.get("country");
			AutofillId townId = info.get("town");
			AutofillId pincodeId = info.get("pincode");
			AutofillId addressId = info.get("address");


			final FillResponse.Builder builder = new FillResponse.Builder();
			final Dataset.Builder dBuilder = new Dataset.Builder();

			if(focused == usernameId | focused == passId){
				accountCreation = true;
				foundToFill = true;
				String account = storage.getString(webDomain, null);
				if(account != null){
					String[] array = account.split("##//##");
					for(String i : array){
						String[] acc = i.split("\\*\\*//\\*\\*");
						String username = decrypt(acc[1]);
						String password = decrypt(acc[2]);

						Dataset.Builder datasetBuilder = new Dataset.Builder();

						RemoteViews OptionMenu = new RemoteViews(getPackageName(), R.layout.autofill_menu);
						OptionMenu.setTextViewText(R.id.autofillmenuTextView1, username);

						Boolean foundAnyOneBox = false;

						if(usernameId != null){
							datasetBuilder.setValue(usernameId, AutofillValue.forText(username),OptionMenu);
							foundAnyOneBox = true;
						}
						if(passId != null){
							datasetBuilder.setValue(passId, AutofillValue.forText(password),OptionMenu);
							foundAnyOneBox = true;
						}

						if(foundAnyOneBox){
							builder.addDataset(datasetBuilder.build());
							datasetAlreadyAdded = true;

						}
					}
				}else{

				}
			}else if(mobileId != null & focused == mobileId){
				dBuilder.setValue(mobileId, AutofillValue.forText("7706073307"),view);
				foundToFill = true;
			}else if(stateId != null & focused == stateId){
				dBuilder.setValue(stateId, AutofillValue.forText("Uttar Pradesh"),view);
				foundToFill = true;
			}else if(cityId != null & focused == cityId){
				dBuilder.setValue(cityId, AutofillValue.forText("Kanpur"),view);
				foundToFill = true;
			}else if(countryId != null & focused == countryId){
				dBuilder.setValue(countryId, AutofillValue.forText("India"),view);
				foundToFill = true;
			}else if(townId != null & focused == townId){
				dBuilder.setValue(townId, AutofillValue.forText("Chaubepur"),view);
				foundToFill = true;
			}else if(pincodeId != null & focused == pincodeId){
				dBuilder.setValue(pincodeId, AutofillValue.forText("209203"),view);
				foundToFill = true;
			}else if(addressId != null & focused == addressId){
				dBuilder.setValue(addressId, AutofillValue.forText("Chaubepur, Kanpur, India"),view);
				foundToFill = true;
			}


			final RemoteViews customDescLayout = new RemoteViews(getPackageName(), R.layout.custom_desc);


			new Thread(new Runnable(){
					@Override
					public void run(){

						Bitmap favicon = getImageBitmap("https://t1.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=http://" + webDomain + "&size=64");
						if(favicon != null){
							customDescLayout.setImageViewBitmap(R.id.customdescImageView1, favicon);
						}
						customDescLayout.setTextViewText(R.id.customdescTextView3, "Your " + webDomain + " username and password will be saved in Encrypt Autofill.");


						CustomDescription.Builder cDescBuilder = new CustomDescription.Builder(customDescLayout);

						cDescBuilder.addChild(R.id.customdescinvisible, new CharSequenceTransformation
											  .Builder(passId, Pattern.compile("^.*(\\d\\d\\d\\d)$"), "...$1")
											  .build());

						cDescBuilder.addChild(R.id.customdescTextView1, new CharSequenceTransformation
											  .Builder(usernameId, Pattern.compile("$"), "$")
											  .build());


						if(foundToFill == true){
							if(accountCreation){
								builder.setSaveInfo(new SaveInfo.Builder(
														SaveInfo.SAVE_DATA_TYPE_GENERIC,
														new AutofillId[] {usernameId, passId})
													.setCustomDescription(cDescBuilder.build())
													.build());

								NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
								Notification notification;
								Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext())
									.setSmallIcon(R.drawable.ic_launcher)
									.setPriority(Notification.PRIORITY_MAX)
									.setSubText("Generate New Password")
									.setStyle(new Notification.BigTextStyle()
											  .bigText("Click here to generate and copy new password."))
									.setAutoCancel(false)
									.setTimeoutAfter(240000);
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
									notification = notificationBuilder.setChannelId("00").build();
									nm.createNotificationChannel(new NotificationChannel("00", "Imp", NotificationManager.IMPORTANCE_MAX));
								}else{
									notification = notificationBuilder.build();
								}

								Intent notificationIntent = new Intent(getApplicationContext(), PasswordGenReceiver.class);

								PendingIntent contentIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notificationIntent, 0);
								notification.contentIntent = contentIntent;

								nm.notify(1, notification);


								/*RemoteViews genPassView = new RemoteViews(getPackageName(), R.layout.autofill_menu_gen_pass);

								 Intent intent = new Intent(getApplicationContext(), PasswordGenReceiver.class);
								 PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
								 genPassView.setOnClickPendingIntent(R.id.autofillmenugenpassTextView1, pendingIntent);

								 builder.addDataset(new Dataset.Builder()
								 .setValue(passId, AutofillValue.forText("Generate Password"), genPassView)
								 .build());
								 */
							}else{
								builder.addDataset(dBuilder.build());
							}

							FillResponse fillResponse = builder.build();
							callback.onSuccess(fillResponse);
						}
					}
				}).start();
		}
	}

	public void traverseStructure(AssistStructure structure) {
		int nodes = structure.getWindowNodeCount();
		for (int i = 0; i < nodes; i++) {
			AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
			AssistStructure.ViewNode viewNode = windowNode.getRootViewNode();
			dumpNode("   " , viewNode);
		}
	}

	public void dumpNode(String prefix, AssistStructure.ViewNode node) {
		HtmlInfo htmlInfo = node.getHtmlInfo();
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
		 }

		 */

		if(node.getClassName().equals("android.widget.EditText") & node.getVisibility() ==0 & node.getIdPackage() == null){
			try{
				String tag = htmlInfo.getTag();
				String attr = htmlInfo.getAttributes().toString().toLowerCase();
				if(node.isFocused()){info.put("focused", node.getAutofillId());}
				if(tag.equals("input") & ( attr.contains(" username") | attr.contains(" name") | attr.contains("email") | attr.contains("/username") | attr.contains("/name"))){
					if(!info.containsKey("username")){
						info.put("username", node.getAutofillId());
						if(webDomain==null){
							webDomain = node.getWebDomain();
						}
					}
				}else if(tag.equals("input") & (attr.contains("password") | attr.contains("passwd") | attr.contains("pass"))){
					if(!info.containsKey("pass")){
						info.put("pass", node.getAutofillId());
						if(webDomain==null){
							webDomain = node.getWebDomain();
						}
					}
				}else if(tag.equals("input") & (attr.contains("tel") | attr.contains("mob") | attr.contains("phone"))){
					if(!info.containsKey("mobile")){
						info.put("mobile", node.getAutofillId());
						if(webDomain==null){
							webDomain = node.getWebDomain();
						}
					}
				}else if(tag.equals("input") & attr.contains("state")){
					if(!info.containsKey("state")){
						info.put("state", node.getAutofillId());
						if(webDomain==null){
							webDomain = node.getWebDomain();
						}
					}
				}else if(tag.equals("input") & (attr.contains("city"))){
					if(!info.containsKey("city")){
						info.put("city", node.getAutofillId());
						if(webDomain==null){
							webDomain = node.getWebDomain();
						}
					}
				}else if(tag.equals("input") & (attr.contains("country"))){
					if(!info.containsKey("country")){
						info.put("country", node.getAutofillId());
						if(webDomain==null){
							webDomain = node.getWebDomain();
						}
					}
				}else if(tag.equals("input") & (attr.contains("area") | attr.contains("town"))){
					if(!info.containsKey("town")){
						info.put("town", node.getAutofillId());
						if(webDomain==null){
							webDomain = node.getWebDomain();
						}
					}
				}else if(tag.equals("input") & (attr.contains("zip") | attr.contains("pin") | attr.contains("postal"))){
					if(!info.containsKey("pincode")){
						info.put("pincode", node.getAutofillId());
						if(webDomain==null){
							webDomain = node.getWebDomain();
						}
					}
				}else if(tag.equals("input") & (attr.contains("address"))){
					if(!info.containsKey("address")){
						info.put("address", node.getAutofillId());
						if(webDomain==null){
							webDomain = node.getWebDomain();
						}
					}
				}
			}catch (NullPointerException e){
			}
		}
		final String prefix2 = prefix + "  ";
		for (int i = 0; i < numberChildren; i++) {
			dumpNode(prefix2, node.getChildAt(i));
		}
	}


	public void getUsernameAndPass(AssistStructure structure, Map<String,String> infoText){
		int nodes = structure.getWindowNodeCount();
		for (int i = 0; i < nodes; i++) {
			AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
			AssistStructure.ViewNode viewNode = windowNode.getRootViewNode();
			getNodes(viewNode, infoText);
		}
	}


	public void getNodes(AssistStructure.ViewNode node, Map<String, String> infoText){
		HtmlInfo htmlInfo = node.getHtmlInfo();
		int numberChildren = node.getChildCount();

		if(node.getClassName().equals("android.widget.EditText") & node.getVisibility() ==0 & node.getIdPackage() == null & htmlInfo != null){
			String tag = htmlInfo.getTag();
			String attr = htmlInfo.getAttributes().toString().toLowerCase();

			if(tag.equals("input") & ( attr.contains(" username") | attr.contains(" name") | attr.contains("email") | attr.contains("/username") | attr.contains("/name"))){
				if(!infoText.containsKey("username")){
					infoText.put("username", node.getText().toString());
					if(webDomain==null){
						webDomain = node.getWebDomain();
					}
				}
			}else if(tag.equals("input") & (attr.contains("password") | attr.contains("passwd") | attr.contains("pass"))){
				if(!infoText.containsKey("pass")){
					infoText.put("pass", node.getText().toString());
					if(webDomain==null){
						webDomain = node.getWebDomain();
					}
				}
			}
		}
		for (int i = 0; i < numberChildren; i++) {
			getNodes(node.getChildAt(i), infoText);
		}
	}

	public static String encrypt(String string){
		return Base64.getEncoder().encodeToString(string.getBytes());
	}



	public static String decrypt(String string){
		return new String(Base64.getDecoder().decode(string));
	}


	public static String getAutofillTypeAsString(int type) {
        switch (type) {
            case View.AUTOFILL_TYPE_TEXT:
                return "TYPE_TEXT";
            case View.AUTOFILL_TYPE_LIST:
                return "TYPE_LIST";
            case View.AUTOFILL_TYPE_NONE:
                return "TYPE_NONE";
            case View.AUTOFILL_TYPE_TOGGLE:
                return "TYPE_TOGGLE";
            case View.AUTOFILL_TYPE_DATE:
                return "TYPE_DATE";
        }
        return "UNKNOWN_TYPE";
    }

	private Bitmap getImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
		} catch (IOException e) {}
		return bm;
    }


} 
