package com.vishalsingh.passwords;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.view.accessibility.*;
import android.accessibilityservice.*;
import java.io.*;
import java.util.*;
import android.content.*;
import android.widget.LinearLayout.*;
import android.content.res.*;
import android.util.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.ViewTreeObserver.*;

public class ShowOptions extends Activity implements Serializable {
   
	CustomizedExceptionHandler log = new CustomizedExceptionHandler("Crash Reports", "Passwords");
	MyAccessibilityService obj = MyAccessibilityService.getSharedInstance();
	LinearLayout layout;
	float dX, dY;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(log);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		if(getIntent().getBooleanExtra("showPassBox", false)){
			showPasswordBox(getIntent().getStringExtra("pass"));	
		}else{
		
		LinearLayout optionContainer = new LinearLayout(this);
		optionContainer.setOrientation(optionContainer.VERTICAL);
		
		if (obj != null) {
			String data = getIntent().getStringExtra("data");
			
			String[] accounts = data.split("##//##");
			for(final String account: accounts){
				final String[] info = account.split("\\*\\*//\\*\\*");
				LinearLayout option = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.dialog,null);
				TextView textView  = option.findViewById(R.id.dialogTextView1);
				textView.setText(MainActivity.decrypt(info[1]));
				TextView logo = option.findViewById(R.id.logo);
				Random rnd = new Random(); 
				int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
				((GradientDrawable)logo.getBackground()).setColor(color);
				logo.setText(MainActivity.decrypt(info[1]).substring(0,1));
				option.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View p1)
						{
							finish();
							if(getIntent().getBooleanExtra("google", false)){
								obj.workGoogle(info);
							}else if(getIntent().getBooleanExtra("onlyOne", false)){
								obj.workOnlyOne(info);
							}else{
								obj.workOpera(info[1], info[2]);
							}
						}
					});
				LinearLayout line = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.line,null);
				if(!accounts[0].equals(account)){optionContainer.addView(line);}
				optionContainer.addView(option);
			}
			layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.scroll, null);
			ScrollView scrollView = layout.findViewById(R.id.scrollScrollView1);
			scrollView.addView(optionContainer);
			
			//fixing height of scrollView if it becomes more than 300dp
			ViewTreeObserver vto = layout.getViewTreeObserver();
			vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener(){
					@Override
					public void onGlobalLayout()
					{
						if(layout.getHeight()>dpToPxl(ShowOptions.this,300)){
							FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
								LayoutParams.MATCH_PARENT,      
								dpToPxl(ShowOptions.this, 300)
							);
							layout.setLayoutParams(params);
						}
						ViewTreeObserver obs = layout.getViewTreeObserver();

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
							obs.removeOnGlobalLayoutListener(this);
						} else {
							obs.removeGlobalOnLayoutListener(this);
						}
					}
			});
			
			showSortPopup(getIntent().getBooleanExtra("google", false), layout);
		}
    	}
	}
	
	public void showSortPopup(final Boolean isGoogle,LinearLayout layout) 
	{
		Dialog popAddPost = new Dialog(this);
		popAddPost.setContentView(layout);
		popAddPost.setTitle("Choose Account");
		Objects.requireNonNull(popAddPost.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		popAddPost.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialogInterface) {
					finish();
					obj.pauseAccService(isGoogle);
				}
		});
		popAddPost.show();
	}
	
	
	
	public static int dpToPxl(Context context, int dp){
		Resources r = context.getResources();
		int pxl = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			dp, 
			r.getDisplayMetrics()
		);
		return pxl;
	}
	
	
	
	public void showPasswordBox(final String pass){
		TextView t = new TextView(this);
		t.setText("Paste Password");
		t.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8, this.getResources().getDisplayMetrics()));
		t.setPadding(dpToPxl(this, 10),dpToPxl(this,10),dpToPxl(this,10),dpToPxl(this,10));
		final LinearLayout l = new LinearLayout(this);
		l.setElevation(dpToPxl(this, 30));
		l.setBackgroundResource(R.drawable.paste_password_btn_bg);
		l.setForeground(this.getResources().getDrawable(R.drawable.ripple));
		l.addView(t);
		
		final WindowManager windowManager2 = (WindowManager)getSystemService(WINDOW_SERVICE);
		WindowManager.LayoutParams params=new WindowManager.LayoutParams(
			WindowManager.LayoutParams.WRAP_CONTENT,
			WindowManager.LayoutParams.WRAP_CONTENT,
			WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
			PixelFormat.OPAQUE
		);

		params.gravity=Gravity.CENTER|Gravity.CENTER;
		params.x=-150;
		params.y=150;
		windowManager2.addView(l, params);
		l.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					windowManager2.removeView(l);
					obj.pastePassword(pass);
				}
			});
		finish();
	}
	
	
}
