package com.vishalsingh.passwords;

import android.app.*;
import android.content.*;
import android.content.ClipboardManager;
import android.content.pm.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.support.v4.content.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import android.widget.LinearLayout.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import android.util.Base64;
import android.support.v4.app.*;
import android.*;
import android.view.inputmethod.*;
import android.preference.*;
import android.graphics.*;
import android.webkit.*;
import android.view.inspector.*;
import javax.net.ssl.*;

public class MainActivity extends Activity 
{
	public static CustomizedExceptionHandler log = new CustomizedExceptionHandler("Crash Reports", "Passwords");
	public static SharedPreferences storage;
	LinearLayout accountsView, tabArea, homeBtn, settingsBtn, passGenBtn, home, settings, passgen, addAccount, editAccount, newLayout;
	Boolean doubleBackToExitPressedOnce = false, noOfAccountsChanged = false, activeSearch = false;
	String activeWindow = "home", UPDATE_DOWNLOADED = "false";
	ScrollView scrollView;
	PopupWindow popupWindow;
	AlertDialog progressDialog;
	AlertDialog.Builder builder;
	int VERSION_CODE, STORAGE_PERMISSION_CODE = 200, PICKFILE_REQUEST_CODE = 100;
	SharedPreferences variables, bitmapStorage;
	Handler handler = new Handler();
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(log);
        setContentView(R.layout.main);
		
		storage = PreferenceManager.getDefaultSharedPreferences(this);
		bitmapStorage = getSharedPreferences("vishalsingh.bitmap", Context.MODE_PRIVATE);
		variables = getSharedPreferences("vishalsingh.variables", Context.MODE_PRIVATE);
		tabArea = findViewById(R.id.tabArea);
		settingsBtn = findViewById(R.id.settingsButton);
		homeBtn = findViewById(R.id.homeButton);
		passGenBtn = findViewById(R.id.passGenTabBtn);
		home = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.home, null);
		settings = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.settings, null);
		passgen = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.pass_gen, null);
		addAccount = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.add_account_layout, null);
		editAccount = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.edit_account_layout, null);
		newLayout = new LinearLayout(this);
		LayoutParams params = new LayoutParams(
			LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT
		);
		params.setMargins(0, ShowOptions.dpToPxl(this, 20),0,0);
		newLayout.setLayoutParams(params);
		newLayout.setOrientation(LinearLayout.VERTICAL);
		scrollView = new ScrollView(this);
		scrollView.addView(newLayout);
		accountsView = home.findViewById(R.id.accountsView);
		

		checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
		
		if(checkAccessibilityPermission()){
			((TextView)settings.findViewById(R.id.settingsTextView1)).setText("Activated");
		}else{
			if(!getSharedPreferences("vishalsingh.first", Context.MODE_PRIVATE).getBoolean("askedForAutofill", false)){
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle("Activate Autofill");
				alert.setMessage("Do you want to activate autofill?\nIt will help you to fill passwords automatically on the web");
				alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
							startActivityForResult(intent, 0);
							dialog.dismiss();
						}
					});
				alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
				alert.show();
				getSharedPreferences("vishalsingh.first", Context.MODE_PRIVATE).edit().putBoolean("askedForAutofill", true).apply();
			}
		}
		
		try
		{
			PackageManager packageManager = getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
			VERSION_CODE = packageInfo.versionCode;
		}
		catch (Exception e)
		{
			VERSION_CODE = 0;
			log.e(this, e);
		}
		
		homeTab(homeBtn);
		showAllAccounts();
		
		
		((EditText)home.findViewById(R.id.searchBar)).addTextChangedListener(new TextWatcher(){
				@Override
				public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4){}

				@Override
				public void onTextChanged(CharSequence s, int p2, int p3, int p4)
				{
					if(s.toString().isEmpty()){
						showAllAccounts();
						return;
					}
					showAllAccounts();
					for(int i = 0; i<accountsView.getChildCount(); i++){
						String name = (((TextView)((LinearLayout)accountsView.getChildAt(i)).findViewById(R.id.accountsoptionTextView1)).getText().toString()).toLowerCase();
						if(!name.contains(s.toString().toLowerCase())){
							accountsView.removeViewAt(i);
							activeSearch = true;
							i--;
						}
					}
				}
				
				@Override
				public void afterTextChanged(Editable p1){}
			});
			
			progressDialog = new AlertDialog.Builder(MainActivity.this)
												.setTitle("")
												.setView(R.layout.progress_dialog_layout)
												.setCancelable(false)
												.create();
			
																		
												
			checkUpdate();
		
			
		((EditText)editAccount.findViewById(R.id.password)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						editAccount.findViewById(R.id.editaccountlayoutLinearLayout1).performClick();
						return true;
					}
					return false;
				}
			});
			
		((EditText)addAccount.findViewById(R.id.password)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						addAccount.findViewById(R.id.addaccountlayoutLinearLayout1).performClick();
						return true;
					}
					return false;
				}
			});
			
			
			
    }
	
	
	
	
	
	public Boolean checkPermission(String permission, int requestCode)
	{

		// Checking if permission is not granted

		if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {

			ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);

		}else{
			return true;
		}
		return false;
	}
	
	
	
	
	public void copyUsernameAndPassword(View v){
		String url = ((TextView)((RelativeLayout)(v.getParent()).getParent()).findViewById(R.id.website)).getText().toString();
		String accounts = storage.getString(url, null);
		if(accounts != null){
			String[] acc = accounts.split("##//##")[0].split("\\*\\*//\\*\\*");
			String username = decrypt(acc[1]);
			String pass = decrypt(acc[2]);
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
			ClipData clip = ClipData.newPlainText("username", username);
			clipboard.setPrimaryClip(clip);
			ClipData clip1 = ClipData.newPlainText("password",  pass);
			clipboard.setPrimaryClip(clip1);
			Toast.makeText(this, "email and password copied!", Toast.LENGTH_SHORT).show();
		}else{		
			Toast.makeText(this, "Can't copy password! There is no account in this site", Toast.LENGTH_LONG).show();
		}
	}
	
	public void copyPasswordByLogo(View v){
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
		ClipData clip = ClipData.newPlainText("username", ((TextView)((RelativeLayout)v.getParent()).findViewById(R.id.accountoptionTextView1)).getText().toString());
		clipboard.setPrimaryClip(clip);
		ClipData clip1 = ClipData.newPlainText("password",  ((TextView)((RelativeLayout)v.getParent()).findViewById(R.id.hiddenPassword)).getText().toString());
		clipboard.setPrimaryClip(clip1);
		Toast.makeText(this, "Account copied!", Toast.LENGTH_SHORT).show();
	}
	
	
	
	
	public void copyPassword(View v){
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
		ClipData clip1 = ClipData.newPlainText("text",  ((EditText)((RelativeLayout)v.getParent()).getChildAt(0)).getText().toString());
		clipboard.setPrimaryClip(clip1);
		final LinearLayout icon = (LinearLayout)((LinearLayout)v).getChildAt(0);
		icon.setBackground(getResources().getDrawable(R.drawable.copy_done_icon));
		new java.util.Timer().schedule( new java.util.TimerTask() {@Override public void run() {handler.post(new Runnable() {public void run() {
			icon.setBackground(getResources().getDrawable(R.drawable.copy_icon));
		}});}}, 2000);
	}
	
	
	public void showHidePassword(View view){
		RelativeLayout parent = ((RelativeLayout) view.getParent());
		EditText text = (EditText)parent.getChildAt(0);
		int inptype = text.getInputType();
		if(inptype==InputType.TYPE_CLASS_TEXT){
			text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			text.setSelection(text.getText().length());
			((LinearLayout)view).getChildAt(0).setBackgroundResource(R.drawable.visibility);
		}else{
			text.setInputType(InputType.TYPE_CLASS_TEXT);
			text.setSelection(text.getText().length());
			((LinearLayout)view).getChildAt(0).setBackgroundResource(R.drawable.visibility_off);	
		}
	}
	
	
	public void homeTab(View view){
		tabArea.removeAllViews();
		tabArea.addView(home);
		homeBtn.setBackgroundResource(R.drawable.widgets_icon_blue);
		settingsBtn.setBackgroundResource(R.drawable.settings_icon);
		passGenBtn.setBackgroundResource(R.drawable.pass_gen_icon);
		activeWindow = "home";
	}
	
	
	
	public void settingsTab(View view){
		tabArea.removeAllViews();
		tabArea.addView(settings);
		if(checkAccessibilityPermission()){
			((TextView)settings.findViewById(R.id.settingsTextView1)).setText("Activated");
		}else{
			((TextView)settings.findViewById(R.id.settingsTextView1)).setText("Activate");
		}
		homeBtn.setBackgroundResource(R.drawable.widgets_icon);
		settingsBtn.setBackgroundResource(R.drawable.settings_icon_blue);
		passGenBtn.setBackgroundResource(R.drawable.pass_gen_icon);
		activeWindow = "settings";
	}
	
	
	public void passGen(View v){
		tabArea.removeAllViews();
		tabArea.addView(passgen);
		homeBtn.setBackgroundResource(R.drawable.widgets_icon);
		settingsBtn.setBackgroundResource(R.drawable.settings_icon);
		passGenBtn.setBackgroundResource(R.drawable.pass_gen_icon_blue);
		activeWindow = "passGenTab";
	}
	
	
	
	public void copyGenPass(View v){
		String generatedPassword = ((EditText)((LinearLayout)v.getParent()).getChildAt(0)).getText().toString();
		if(generatedPassword.length() == 0){
			((LinearLayout)((LinearLayout)v.getParent()).getChildAt(1)).performClick();
			generatedPassword = ((EditText)((LinearLayout)v.getParent()).getChildAt(0)).getText().toString();
		}
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
		ClipData clip = ClipData.newPlainText("text", generatedPassword);
		clipboard.setPrimaryClip(clip);
		Toast.makeText(this, "Copied Generated Password", Toast.LENGTH_SHORT).show();
	}
	
	
	public void generatePassword(View v){
		String generatedPass = PasswordGenReceiver.generatePassword(MainActivity.this, 14);
		((EditText)((LinearLayout)v.getParent()).getChildAt(0)).setText(generatedPass);
	}
	
	
	public void saveAccountWithPassword(View v){
		addAccount(v);
		EditText editText = passgen.findViewById(R.id.passgenEditText1);
		String generatedPassword = editText.getText().toString();
		if(generatedPassword.length()==0){
			((LinearLayout)((LinearLayout)v.getParent()).getChildAt(1)).performClick();
			generatedPassword = editText.getText().toString();
			Toast.makeText(this, "Created new Password!", Toast.LENGTH_SHORT).show();
		}
		editText.setText("");
		((EditText)addAccount.findViewById(R.id.password)).setText(generatedPassword);
	}
	
	
	public void showAllAccounts()
	{
		accountsView.removeAllViews();
		Map<String, ?> unsortedEntries = storage.getAll();
		if(unsortedEntries.size() == 0){
			TextView t= new TextView(this);
			t.setText("No Accounts!");
			t.setPadding(0, ShowOptions.dpToPxl(this, 40),0,0);
			t.setGravity(Gravity.CENTER_HORIZONTAL);
			accountsView.addView(t);
			return;
		}
		Map<String, String> allEntries = sortByFirstLetterOfValue(unsortedEntries);
		for (Map.Entry<String, String> entry : allEntries.entrySet()) {
			LinearLayout accountsOption = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.accounts_option, null);
			ImageView logo = accountsOption.findViewById(R.id.accountsoptionLinearLayout1);
			TextView websiteName = accountsOption.findViewById(R.id.accountsoptionTextView1);
			TextView noAccounts = accountsOption.findViewById(R.id.accountsoptionTextView2);
			TextView websiteUrl = accountsOption.findViewById(R.id.website);
			TextView usernameText = accountsOption.findViewById(R.id.accountsoptionUsername);	
			//String website = getWebAppName(entry.getKey().toString());
			String[] accounts = entry.getValue().toString().split("##//##");
			String[] firstAccount = accounts[0].split("\\*\\*//\\*\\*");
			usernameText.setText(decrypt(firstAccount[1]));
			websiteName.setText(firstAccount[0]);
			websiteUrl.setText(entry.getKey().toString());
			downloadAndSetIcon(logo, entry.getKey().toString());
			noAccounts.setText(accounts.length + " accounts");
			accountsView.addView(accountsOption);
		}
	}
	
	
	public static Map<String, String> sortByFirstLetterOfValue(Map<String, ?> unsortedMap) {
        List<Map.Entry<String, String>> list = new LinkedList<>(unsortedMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
				@Override
				public int compare(Map.Entry<String, String> e1, Map.Entry<String, String> e2) {
					return e1.getValue().substring(0, 1).compareTo(e2.getValue().substring(0, 1));
				}
			});
			
        Map<String, String> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
	
	
	
	public void downloadAndSetIcon(final ImageView logo, final String webDomain){
		String bm = bitmapStorage.getString(webDomain, null);
		if(bm != null){
			if(bm.equals("noBitmapAvailable")){
				logo.setBackgroundResource(R.drawable.logo_icon_bg_circle);
				((GradientDrawable) logo.getBackground()).setColor(getResources().getColor(R.color.lightBlue));
				TextView text = ((RelativeLayout)logo.getParent()).findViewById(R.id.logoText);
				text.setText(getWebAppName(webDomain).substring(0,1));
				text.setVisibility(View.VISIBLE);
			}else{
				byte[] imageAsBytes = Base64.decode(bm.getBytes(), Base64.DEFAULT);
				logo.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
			}
		}else{
			new Thread(new Runnable(){
					@Override
					public void run(){
						try{
							downloaAndSaveIcon(webDomain);
							String bm1 = bitmapStorage.getString(webDomain, null);
							if(bm1 != null){
								if(bm1.equals("noBitmapAvailable")){
									MainActivity.this.runOnUiThread(new Runnable(){
											@Override
											public void run(){
												logo.setBackgroundResource(R.drawable.logo_icon_bg_circle);
												((GradientDrawable) logo.getBackground()).setColor(getResources().getColor(R.color.lightBlue));
												TextView text = ((RelativeLayout)logo.getParent()).findViewById(R.id.logoText);
												text.setText(getWebAppName(webDomain).substring(0,1));
												text.setVisibility(View.VISIBLE);
											}
										});
								}else{
									final byte[] imageAsBytes1 = Base64.decode(bm1.getBytes(), Base64.DEFAULT);
									MainActivity.this.runOnUiThread(new Runnable(){
											@Override
											public void run(){
												logo.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes1, 0, imageAsBytes1.length));
												RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
													ShowOptions.dpToPxl(MainActivity.this, 40),      
													ShowOptions.dpToPxl(MainActivity.this, 40)
												);
												logo.setLayoutParams(params1);
												logo.setClipToOutline(true);
											}
										});
								}
							}
						} catch (Exception e) {log.e(e);}
					}
			}).start();
		}
	}
	
	
	public void downloaAndSaveIcon(String webDomain){
        try {
            URL aURL = new URL("https://t1.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=https://" + webDomain + "&size=128");
            URLConnection conn = aURL.openConnection();
			if(((HttpURLConnection)conn).getResponseCode() != HttpURLConnection.HTTP_OK){
				String webDomain1 = webDomain.substring(webDomain.indexOf(".") + 1);
				aURL = new URL("https://t1.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=https://" + webDomain1 + "&size=128");
				conn = aURL.openConnection();
				if(((HttpURLConnection)conn).getResponseCode() != HttpURLConnection.HTTP_OK){
					bitmapStorage.edit().putString(webDomain, "noBitmapAvailable").apply();
					return;
				}
			}
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            Bitmap bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
			byte[] byteArray = baos.toByteArray();
			String encodedBm = Base64.encodeToString(byteArray, Base64.DEFAULT);
			bitmapStorage.edit().putString(webDomain, encodedBm).apply();
		} catch (UnknownHostException e) {
		} catch (Exception e){
			log.e(e);
		}
		
    }
	
	
	/*public void setIcon(ImageView layout, String name){
		if(name.equals("Facebook")){
			layout.setForeground(getResources().getDrawable(R.drawable.facebook_icon));
		}else if(name.equals("Amazon")){
			layout.setForeground(getResources().getDrawable(R.drawable.amazon_icon));
		}else if(name.equals("Flipkart")){
			layout.setForeground(getResources().getDrawable(R.drawable.flipkart_icon));
		}else if(name.equals("Github")){
			layout.setForeground(getResources().getDrawable(R.drawable.github_icon));
		}else if(name.equals("Google")){
			layout.setForeground(getResources().getDrawable(R.drawable.google_icon));
		}else if(name.equals("Instagram")){
			layout.setForeground(getResources().getDrawable(R.drawable.instagram_icon));
		}else if(name.equals("Microsoft Office")){
			layout.setForeground(getResources().getDrawable(R.drawable.msoffice_icon));
		}else if(name.equals("Opera")){
			layout.setForeground(getResources().getDrawable(R.drawable.opera_icon));
		}else if(name.equals("Paypal")){
			layout.setForeground(getResources().getDrawable(R.drawable.paypal_icon));
		}else if(name.equals("Paytm")){
			layout.setForeground(getResources().getDrawable(R.drawable.paytm_icon));
		}else if(name.equals("Proton")){
			layout.setImageDrawable(getResources().getDrawable(R.drawable.protonvpn_icon));
		}else if(name.equals("Twitter")){
			layout.setForeground(getResources().getDrawable(R.drawable.twitter_icon));
		}else if(name.equals("WhatsApp")){
			layout.setForeground(getResources().getDrawable(R.drawable.whatsapp_icon));
		}else if(name.equals("Cloudflare")){
			layout.setForeground(getResources().getDrawable(R.drawable.cloudflare_icon));
		}else if(name.equals("Epicgames")){
			layout.setForeground(getResources().getDrawable(R.drawable.epicgames_icon));
		}else if(name.equals("Sendinblue")){
			layout.setForeground(getResources().getDrawable(R.drawable.sendinblue_icon));
		}else if(name.equals("Stackoverflow")){
			layout.setForeground(getResources().getDrawable(R.drawable.stackoverflow_icon));
		}else if(name.equals("Steam")){
			layout.setForeground(getResources().getDrawable(R.drawable.steam_icon));
		}else if(name.equals("Wordpress")){
			layout.setForeground(getResources().getDrawable(R.drawable.wordpress_icon));
		}else if(name.equals("Zerossl")){
			layout.setForeground(getResources().getDrawable(R.drawable.zerossl_icon));
		}else if(name.equals("Openai")){
			layout.setForeground(getResources().getDrawable(R.drawable.openai_icon));
		}else if(name.equals("Dropbox")){
			layout.setForeground(getResources().getDrawable(R.drawable.dropbox_icon));
		}else if(name.equals("Replit")){
			layout.setForeground(getResources().getDrawable(R.drawable.replit_icon));
		}else{
			//Random rnd = new Random(); 
			//int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
			layout.setBackgroundResource(R.drawable.logo_icon_bg_circle);
			((GradientDrawable) layout.getBackground()).setColor(getResources().getColor(R.color.lightBlue));
			TextView text = ((RelativeLayout)layout.getParent()).findViewById(R.id.logoText);
			text.setText(name.substring(0,1));
			text.setVisibility(View.VISIBLE);
			return;
		}
		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
			ShowOptions.dpToPxl(this, 40),      
			ShowOptions.dpToPxl(this, 40)
		);
		layout.setLayoutParams(params1);
		layout.setClipToOutline(true);
	}*/
	
	
	
	public static String getWebAppName(String address){
		String hostName = address;
		if(address.contains("app&&//&&")){
			String packageName = address.split("&&//&&")[1];
			if(packageName.equals("com.facebook.katana") | packageName.equals("com.facebook.lite")){
				return "Facebook";
			}else if(packageName.equals("in.amazon.mShop.android.shopping")){
				return "Amazon";
			}else if(packageName.equals("com.flipkart.android")){
				return "Flipkart";
			}else if(packageName.equals("com.github.android")){
				return "Github";
			}else if(packageName.equals("com.google.android.gms")){
				return "Google";
			}else if(packageName.equals("com.instagram.android")){
				return "Instagram";
			}else if(packageName.equals("com.microsoft.office.officehubrow")){
				return "Microsft Office";
			}else if(packageName.equals("com.opera.browser")){
				return "Opera";
			}else if(packageName.equals("com.paypal.android.p2pmobile")){
				return "Paypal";
			}else if(packageName.equals("net.one97.paytm")){
				return "Paytm";
			}else if(packageName.equals("ch.protonvpn.android")){
				return "Proton";
			}else if(packageName.equals("com.twitter.android")){
				return "Twitter";
			}else if(packageName.equals("com.whatsapp")){
				return "WhatsApp";
			}else{return packageName;}
		}else{
			try
			{
				URL uri = new URL("https://" + address);
				String[] host = uri.getHost().split("\\.");
				if(host.length == 2){
					hostName = host[0];
				}else if(host.length == 3 | host.length == 4){
					hostName = host[1];
				}
			}catch (Exception e)
			{
				log.e(e);
			}
		}
		hostName = hostName.substring(0, 1).toUpperCase() + hostName.substring(1);
		return hostName;
	}
	
	
	
	
	public void showMoreOptions(View view){
		
	}
	
	
	
	
	public void showMoreOptionsIn(View view){
		String website =((TextView) ((RelativeLayout)view.getParent()).findViewById(R.id.hiddenWebsite)).getText().toString();
		String email =((TextView) ((RelativeLayout)view.getParent()).findViewById(R.id.accountoptionTextView1)).getText().toString();
		String id =((TextView) ((RelativeLayout)view.getParent()).findViewById(R.id.optionId)).getText().toString();
		//LinearLayout menu = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.more_options_menu,null);
		// inflate the layout of the popup window
		LayoutInflater inflater = (LayoutInflater)
			getSystemService(LAYOUT_INFLATER_SERVICE);
		View popupView = inflater.inflate(R.layout.more_options_menu, null);

		// create the popup window
		int width = LinearLayout.LayoutParams.WRAP_CONTENT;
		int height = LinearLayout.LayoutParams.WRAP_CONTENT;
		boolean focusable = true; // lets taps outside the popup also dismiss it
		popupWindow = new PopupWindow(popupView, width, height, focusable);
		popupWindow.setAnimationStyle(R.style.more_menu_animation);
		// show the popup window
		// which view you pass in doesn't matter, it is only used for the window tolken
		popupWindow.showAsDropDown(view);//.showAtLocation(view, Gravity.CENTER, 0, 0);

		// dismiss the popup window when touched
		popupView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					popupWindow.dismiss();
					return true;
				}
			});
		((TextView)popupView.findViewById(R.id.hiddenemail)).setText(email);
		((TextView)popupView.findViewById(R.id.hiddenwebsite)).setText(website);
		((TextView)popupView.findViewById(R.id.hiddenId)).setText(id);
	}
	
	
	
	
	public void editAccount(View view){
		String password =((TextView) view.findViewById(R.id.hiddenPassword)).getText().toString();
		String email = ((TextView) view.findViewById(R.id.accountoptionTextView1)).getText().toString();
		String website = ((TextView) view.findViewById(R.id.hiddenWebsite)).getText().toString();
		String webName = ((TextView) view.findViewById(R.id.hiddenWebName)).getText().toString();
		((EditText)editAccount.findViewById(R.id.email)).setText(email);
		EditText text = editAccount.findViewById(R.id.password);
		text.setText(password);
		((TextView) editAccount.findViewById(R.id.previousEmail)).setText(email);
		((TextView) editAccount.findViewById(R.id.previousWebsite)).setText(website);
		((TextView) editAccount.findViewById(R.id.previousWebName)).setText(webName);
		((TextView)editAccount.findViewById(R.id.emailError)).setVisibility(View.GONE);
		((TextView)editAccount.findViewById(R.id.passwordError)).setVisibility(View.GONE);
		if(text.getInputType()==InputType.TYPE_CLASS_TEXT){
			editAccount.findViewById(R.id.editaccountlayoutLinearLayout2).performClick();
		}
		tabArea.removeAllViews();
		tabArea.addView(editAccount);
		activeWindow = "edit";
	}
	
	
	
	
	public void addAccount(View view){
		EditText text = addAccount.findViewById(R.id.password);
		if(text.getInputType()==InputType.TYPE_CLASS_TEXT){
			addAccount.findViewById(R.id.addaccountlayoutLinearLayout3).performClick();
		}
		tabArea.removeAllViews();
		if(activeWindow.equals("allAccounts")){
			((EditText)addAccount.findViewById(R.id.url)).setText(((TextView)((LinearLayout)newLayout.getChildAt(0)).getChildAt(0)).getText().toString());
			((EditText)addAccount.findViewById(R.id.webName)).setText(((TextView)((LinearLayout)newLayout.getChildAt(1)).findViewById(R.id.hiddenWebName)).getText().toString());
		}
		tabArea.addView(addAccount);
		activeWindow = "add";
	}
	
	
	public void saveAccount(View view){
		EditText edittext1 = addAccount.findViewById(R.id.url);
		String[] url1 = edittext1.getText().toString().split("https://");
		String url = url1[url1.length-1];
		if(url.isEmpty()){
			((TextView)addAccount.findViewById(R.id.urlError)).setVisibility(View.VISIBLE);
			return;
		}else{
			((TextView)addAccount.findViewById(R.id.urlError)).setVisibility(View.GONE);
		}
		EditText edittext2 = addAccount.findViewById(R.id.email);
		String email = edittext2.getText().toString();
		if(email.isEmpty()){
			((TextView)addAccount.findViewById(R.id.emailError)).setText("Email can't be empty");
			((TextView)addAccount.findViewById(R.id.emailError)).setVisibility(View.VISIBLE);
			return;
		}else{
			((TextView)addAccount.findViewById(R.id.emailError)).setVisibility(View.GONE);
		}
		EditText edittext3 = addAccount.findViewById(R.id.password);
		String password = edittext3.getText().toString();
		if(password.isEmpty()){
			((TextView)addAccount.findViewById(R.id.passwordError)).setVisibility(View.VISIBLE);
			return;
		}else{
			((TextView)addAccount.findViewById(R.id.passwordError)).setVisibility(View.GONE);
		}
		EditText edittext4 = addAccount.findViewById(R.id.webName);
		String webName = edittext4.getText().toString();
		if(webName.isEmpty()){
			((TextView)addAccount.findViewById(R.id.webNameError)).setVisibility(View.VISIBLE);
			return;
		}else{
			((TextView)addAccount.findViewById(R.id.webNameError)).setVisibility(View.GONE);
		}
		String account = storage.getString(url, null);
		if(account == null){
			String data = webName + "**//**" + encrypt(email) + "**//**" + encrypt( password) + "##//##";
			storage.edit().putString(url, data).apply();
		}else{
			String[] array = account.split("##//##");
			for(String i : array){
				String acc = decrypt(i.split("\\*\\*//\\*\\*")[1]);
				if(acc.contains(email)){
					((TextView)addAccount.findViewById(R.id.emailError)).setText("Email already exists");
					((TextView)addAccount.findViewById(R.id.emailError)).setVisibility(View.VISIBLE);
					return;
				}
			}
			String data = account + webName + "**//**" + encrypt( email ) + "**//**" + encrypt(password) + "##//##";
			storage.edit().putString(url, data).apply();
		}
		edittext1.setText("");
		edittext2.setText("");
		edittext3.setText("");
		edittext4.setText("");
		((TextView)addAccount.findViewById(R.id.emailError)).setVisibility(View.GONE);
		homeTab(homeBtn);
		showAllAccounts();
	}
	
	
	
	public void saveEdits(View view){
		TextView edittext1 = editAccount.findViewById(R.id.previousWebsite);
		String url = edittext1.getText().toString();
		EditText edittext2 = editAccount.findViewById(R.id.email);
		String email = edittext2.getText().toString();
		if(email.isEmpty()){
			((TextView)editAccount.findViewById(R.id.emailError)).setVisibility(View.VISIBLE);
			return;
		}else{
			((TextView)editAccount.findViewById(R.id.emailError)).setVisibility(View.GONE);
		}
		EditText edittext3 = editAccount.findViewById(R.id.password);
		String password = edittext3.getText().toString();
		if(password.isEmpty()){
			((TextView)editAccount.findViewById(R.id.passwordError)).setVisibility(View.VISIBLE);
			return;
		}else{
			((TextView)editAccount.findViewById(R.id.passwordError)).setVisibility(View.GONE);
		}
		String previousEmail = ((TextView) editAccount.findViewById(R.id.previousEmail)).getText().toString();
		String data = storage.getString(url, null);
		if(data == null){
			Toast.makeText(this, "Problem Saving Edits", Toast.LENGTH_SHORT).show();
		}else{
			String[] accounts = data.split("##//##");
			ArrayList<String> arrayList = new ArrayList<String>();
			for(String i : accounts){arrayList.add(i);}
			for(int i=0;i<accounts.length;i++){
				if(decrypt(arrayList.get(i).split("\\*\\*//\\*\\*")[1]).equals(previousEmail)){
					arrayList.remove(i);
					break;
				}
			}
			String webName = ((TextView)editAccount.findViewById(R.id.previousWebName)).getText().toString();
			String newAccount = webName + "**//**" + encrypt( email ) + "**//**" + encrypt( password);
			String newdata = "";
			for(String i : arrayList){
				newdata = newdata + i + "##//##";
			}
			newdata = newdata + newAccount + "##//##";
			storage.edit().putString(url, newdata).apply();
			edittext2.setText("");
			edittext3.setText("");
			homeTab(homeBtn);
		}
	}
	
	
	
	
	public void showItsAccounts(View view){
		String website = ((TextView) view.findViewById(R.id.website)).getText().toString();
		String data = storage.getString(website, null);
		tabArea.removeAllViews();
		newLayout.removeAllViews();
		LinearLayout linearlayout = ((LinearLayout)LayoutInflater.from(this).inflate(R.layout.textview_layout,null));
		((TextView)linearlayout.getChildAt(0)).setText(website);
		newLayout.addView(linearlayout);
		if(data !=null){	
			String[] array = data.split("##//##");
			int count = 0;
			for(String account : array){
				String[] accountArray= account.split("\\*\\*//\\*\\*");
				LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.account_option,null);
				((TextView)layout.findViewById(R.id.accountoptionTextView1)).setText(decrypt(accountArray[1]));
				((TextView)layout.findViewById(R.id.hiddenPassword)).setText(decrypt(accountArray[2]));
				((TextView)layout.findViewById(R.id.hiddenWebName)).setText(accountArray[0]);
				//Random rnd = new Random(); 
				//int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
				((GradientDrawable)layout.findViewById(R.id.accountoptionLinearLayout1).getBackground()).setColor(getResources().getColor(R.color.lightBlue));
				((TextView) layout.findViewById(R.id.logoText)).setText(decrypt(accountArray[1]).substring(0,1).toUpperCase());
				((TextView) layout.findViewById(R.id.hiddenWebsite)).setText(website);
				((TextView) layout.findViewById(R.id.optionId)).setText(count + "");
				newLayout.addView(layout);
				count++;
			}
			tabArea.addView(scrollView);
			activeWindow = "allAccounts";
		}
		
	}
	
	
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
			(keyCode == KeyEvent.KEYCODE_BACK)){
			if(activeWindow.equals("allAccounts")){
				homeTab(homeBtn);
				if(noOfAccountsChanged){
					showAllAccounts();
					noOfAccountsChanged = false;
				}
				activeWindow = "home";
				return false;
			}else if(activeWindow.equals("edit")){
				tabArea.removeAllViews();
				tabArea.addView(scrollView);
				activeWindow = "allAccounts";
				return false;
			}else if(activeWindow.equals("add")){
				EditText edittext1 = addAccount.findViewById(R.id.url);
				EditText edittext2 = addAccount.findViewById(R.id.email);
				EditText edittext3 = addAccount.findViewById(R.id.password);
				EditText edittext4 = addAccount.findViewById(R.id.webName);
				edittext1.setText("");
				edittext2.setText("");
				edittext3.setText("");
				edittext4.setText("");
				((TextView)addAccount.findViewById(R.id.emailError)).setVisibility(View.GONE);
				((TextView)addAccount.findViewById(R.id.urlError)).setVisibility(View.GONE);
				((TextView)addAccount.findViewById(R.id.passwordError)).setVisibility(View.GONE);
				((TextView)addAccount.findViewById(R.id.webNameError)).setVisibility(View.GONE);
				tabArea.removeAllViews();
				homeTab(homeBtn);
				activeWindow = "home";
				return false;
			}else if(activeWindow.equals("settings") | activeWindow.equals("passGenTab")){
				tabArea.removeAllViews();
				homeTab(homeBtn);
				activeWindow = "home";
				return false;
			}else if(activeSearch){
				EditText text = home.findViewById(R.id.searchBar);
				text.setText("");
				text.clearFocus();
				showAllAccounts();
				activeSearch = false;
				return false;
			}else if (doubleBackToExitPressedOnce) {
				return super.onKeyDown(keyCode, event);
			}

			this.doubleBackToExitPressedOnce = true;
			Toast.makeText(this, "Please press BACK again to exit", Toast.LENGTH_SHORT).show();

			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

					@Override
					public void run() {
						doubleBackToExitPressedOnce=false;                       
					}
				}, 2000);
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	
	
	public void deleteAccount(View view){
		String email = ((TextView)((LinearLayout)view.getParent()).findViewById(R.id.hiddenemail)).getText().toString();
		String website = ((TextView)((LinearLayout)view.getParent()).findViewById(R.id.hiddenwebsite)).getText().toString();
		String data = storage.getString(website,null);
		if(data != null){
			String[] array =data.split("##//##");
			String newData = "";
			for(String account : array){
				String info = decrypt(account.split("\\*\\*//\\*\\*")[1]);
				if(!info.contains(email)){
					newData = newData + account + "##//##";
				}
			}
			if(newData.isEmpty()){
				popupWindow.dismiss();
				storage.edit().remove(website).apply();
				homeTab(homeBtn);
				showAllAccounts();
				return;
			}else{
				storage.edit().putString(website, newData).apply();
			}
			popupWindow.dismiss();
			newLayout.removeViewAt(Integer.valueOf(((TextView)((LinearLayout)view.getParent()).findViewById(R.id.hiddenId)).getText().toString()));
		}else{
			log.i("delete is empty");
		}
		noOfAccountsChanged = true;
	}
	
	
	public void activateAutofill(final View v){
		if(checkAccessibilityPermission()){
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Deactivate Autofill?");
			alert.setMessage("Are you sure you want to deactivate autofill?");
			alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS); 
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
						startActivity(intent); 
						dialog.dismiss();
						homeTab(homeBtn);
					}
				});
			alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			alert.show();
		}else if(!Settings.canDrawOverlays(this)){
			askOverlayPermission();
		}else{
			askAccPerm();
			homeTab(homeBtn);
		}
	}
	
	
	
	
	public void askOverlayPermission(){
		if (!Settings.canDrawOverlays(this)) {
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
			startActivityForResult(intent, 0);
		}
	}
	
	
	
	
	
	
	public boolean checkAccessibilityPermission () { 
        int accessEnabled = 0; 
        try { 
            accessEnabled = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED); 
        } catch (Settings.SettingNotFoundException e) { 
            log.e(this, e); 
        }
		if(accessEnabled == 0){
			return false;
		}else{
			return true;
		}
	}
	
	
	
	
	public void askAccPerm(){
		Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS); 
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		startActivity(intent); 
	}
	
	
	
	
	public void exportPass(View v){
		Map<String, ?> allEntries = storage.getAll();
		String data = "";
		for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
			data = data + encrypt(entry.getKey().toString()) + "_//_" + entry.getValue().toString() + "__//__";
		}
		SimpleDateFormat formater = new SimpleDateFormat("HH:mm:ss,dd-MM-yyyy");
		Date date = new Date();
		File f = new File("storage/emulated/0/Backup/ExportedPasswords_" + formater.format(date) + ".vs");
		try
		{
			f.createNewFile();
		}
		catch (IOException e)
		{
			log.e(this,e);
		}
		if (f.exists()){
			try {
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(f));
				outputStreamWriter.write(data);
				outputStreamWriter.close();
				Toast.makeText(this, "Passwords saved in Backup folder", Toast.LENGTH_LONG).show();
			}
			catch (IOException e) {
				log.e(this, e);
			} 
		}
		
	}
	
	
	
	
	public void importPass(View v){
		Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
		chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
		chooseFile.setType("*/*");
		startActivityForResult(
			Intent.createChooser(chooseFile, "Choose a file"),
			PICKFILE_REQUEST_CODE
		);
	}
	
	

	
	
	
// And then somewhere, in your activity:
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PICKFILE_REQUEST_CODE && resultCode == RESULT_OK){
			Uri content_describer = data.getData();
			if(!data.getData().getPath().endsWith(".vs")){
				new AlertDialog.Builder(this)
					.setTitle("Info")
					.setMessage("File did not contain any password")
					.setPositiveButton("OK", null)
					.setIcon(android.R.drawable.ic_dialog_info)
					.show();
				return;
			}
			BufferedReader reader = null;
			try {
				// open the user-picked file for reading:
				InputStream in = getContentResolver().openInputStream(content_describer);
				// now read the content:
				reader = new BufferedReader(new InputStreamReader(in));
				String line;
				StringBuilder builder = new StringBuilder();
				while ((line = reader.readLine()) != null){
					builder.append(line);
				}
				// Do something with the content in
				String accounts = builder.toString();
				String[] websites = accounts.split("__//__");
				for(String i : websites){
					String[] account = i.split("_//_");
					storage.edit().putString(decrypt(account[0]), account[1]).apply();
				}
				homeTab(homeBtn);
				showAllAccounts();
			} catch (Exception e) {
				log.e(this, e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						log.e(this, e);
					}
				}
			}
		}
	}
	
	
	
	
	
	
	public static String encrypt(String string){
		return java.util.Base64.getEncoder().encodeToString(string.getBytes());
	}
	
	
	
	public static String decrypt(String string){
		return new String(java.util.Base64.getDecoder().decode(string));
	}
	
	
	
	
	
	
	public void updateApp(View v){
		if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)){
			progressDialog.show();
			String fileUrl = variables.getString("updateUrl", null);
			if(fileUrl != null){
				final String path = "storage/emulated/0/Download/EncryptAutofillUpdate.apk";
				new DownloadFile(this, progressDialog).execute(fileUrl, path);
			}else{
				new AlertDialog.Builder(this)
					.setTitle("Error")
					.setMessage("Please restart the app to update it.")
					.setPositiveButton("OK", null)
					.show();
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	public void checkUpdate(){
		Thread thread = new Thread(new Runnable(){@Override public void run(){
		try {
			URL url = new URL("https://vishal.rf.gd/Apps/encryptautofill/update.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			String data = "";
			while ((line = reader.readLine()) != null) {
				data = data + line;
			}
			reader.close();
			final String version = data.split("##//##")[0];
			final String fileUrl = data.split("##//##")[1];
			final String fileSize = data.split("##//##")[2];
			runOnUiThread(new Runnable(){@Override public void run(){
						if(Integer.valueOf(version)>VERSION_CODE){
							new AlertDialog.Builder(MainActivity.this)
								.setTitle("Good News!")
								.setMessage("A new version of this app is available. Do you want to update this app? Update size : " + fileSize)
								.setPositiveButton("YES", new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface p1, int p2)
									{
										if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)){
											progressDialog.show();
											final String path = "storage/emulated/0/Download/EncryptAutofillUpdate.apk";
											new DownloadFile(MainActivity.this, progressDialog).execute(fileUrl, path);
										}
									}
								})
								.setNegativeButton("NO", null)
								.setIcon(android.R.drawable.ic_dialog_info)
								.show();
							variables.edit().putBoolean("updateAvailable?", true).apply();
							variables.edit().putString("updateUrl", fileUrl).apply();
							settings.findViewById(R.id.settingsLinearLayout1).setVisibility(View.VISIBLE);
						}else{
							variables.edit().remove("updateAvailable?").apply();
							variables.edit().remove("updateUrl").apply();
						}
			}});
		}catch (UnknownHostException e) {
		}catch(SSLHandshakeException e){
		}catch(Exception e){
			log.e(e);
		}
		}});

		thread.start();
	}

	
	
	
	

/*Install Apk*/
	public void installapk(File apkFile, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (apkFile.exists()) {
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    install.setDataAndType(Uri.fromFile(apkFile),
										   "application/vnd.android.package-archive");
                    Uri apkUri = FileProvider.getUriForFile(context, "com.vishalsingh.passwords.provider", apkFile);
                    
					install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    install.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    install.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    context.startActivity(install);
                } else {
                    install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    install.setDataAndType(Uri.fromFile(apkFile),
										   "application/vnd.android.package-archive");
                    context.startActivity(install);
                }
            } else {
                MainActivity.log.i("apk doesnot exist.");
            }
        }
    }
	
	
	
	
	
	
	
	
	
	
	
	
}








class DownloadFile extends AsyncTask<String, Integer, String> {

	Context context;
	AlertDialog dialog;
	
	public DownloadFile(Context con, AlertDialog alert){
		dialog = alert;
		context = con;
	}
	
    @Override
    protected String doInBackground(final String... sUrl) {
        try {
            URL url = new URL(sUrl[0]);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(sUrl[1]);

            byte data[] = new byte[2048];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
           MainActivity.log.i(e.toString());
        }
        return sUrl[1];
    }

	@Override
	protected void onPostExecute(String result)
	{
		MainActivity obj = new MainActivity();
		dialog.dismiss();
		obj.installapk(new File(result), context);
		super.onPostExecute(result);
	}


	
	
	
	
}







//To check browsers installed
//		selBrowser = findViewById(R.id.selBrowser);
//		defBrowser = findViewById(R.id.mainLinearLayout1);
//		
//		startService(new Intent(MainActivity.this, FloatingViewService.class));
//		final String defaultBrowser = storage.getString("defaultBrowser", null);
//		
//		PackageManager packageManager = this.getPackageManager();
//		
//		if(defaultBrowser == null)
//		{
//			Intent intent = new Intent(Intent.ACTION_VIEW);
//			intent.setData(Uri.parse("http://www.vishalsingh.ml"));
//			List<ResolveInfo> list = packageManager.queryIntentActivities(intent,PackageManager.MATCH_ALL);
//            for (final ResolveInfo info : list) {
//                final String name = info.loadLabel(packageManager).toString();				
//				Drawable icon = info.loadIcon(packageManager);
//				
//				LinearLayout option = (LinearLayout)LayoutInflater.from(MainActivity.this).inflate(R.layout.ask_layout, null);
//				option.findViewById(R.id.asklayoutImageView1).setBackground(icon);
//				((TextView) option.findViewById(R.id.asklayoutTextView1)).setText(name);
//				option.setOnClickListener(new View.OnClickListener(){
//						@Override
//						public void onClick(View p1)
//						{
//							selBrowser.setVisibility(View.GONE);
//							storage.edit().putString("defaultBrowser", info.activityInfo.packageName).apply();
//							Intent browser = getPackageManager().getLaunchIntentForPackage(info.activityInfo.packageName);
//							if (browser != null) { 
//								startActivity(browser);//null pointer check in case package name was not found
//							}
//						}
//				});
//				selBrowser.addView(option);
//		    }
//			selBrowser.setVisibility(View.VISIBLE);
//		}
//		else
//		{
//			Intent browser = getPackageManager().getLaunchIntentForPackage(defaultBrowser);
//			if (browser != null) { 
//				startActivity(browser);//null pointer check in case package name was not found
//			}
//		}
//		
//		Intent intent = new Intent(Intent.ACTION_VIEW);
//		intent.setData(Uri.parse("http://www.vishalsingh.ml"));
//		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,PackageManager.MATCH_ALL);
//		for (final ResolveInfo info : list) {
//			final String name = info.loadLabel(packageManager).toString();				
//			Drawable icon = info.loadIcon(packageManager);
//
//			LinearLayout option = (LinearLayout)LayoutInflater.from(MainActivity.this).inflate(R.layout.ask_layout, null);
//			option.findViewById(R.id.asklayoutImageView1).setBackground(icon);
//			((TextView) option.findViewById(R.id.asklayoutTextView1)).setText(name);
//			option.setOnClickListener(new View.OnClickListener(){
//					@Override
//					public void onClick(View p1)
//					{
//						selBrowser.setVisibility(View.GONE);
//						storage.edit().putString("defaultBrowser", info.activityInfo.packageName).apply();
//						Intent browser = getPackageManager().getLaunchIntentForPackage(info.activityInfo.packageName);
//						if (browser != null) { 
//							startActivity(browser);//null pointer check in case package name was not found
//						}
//					}
//				});
//			defBrowser.addView(option);
//		}
