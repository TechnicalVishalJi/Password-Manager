package com.vishalsingh.passwords;
import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import android.view.View.*;
import android.content.pm.*;
import android.view.inputmethod.*;

public class PasswordActivity extends Activity
{
	CustomizedExceptionHandler log = new CustomizedExceptionHandler("Crash Reports", "Passwords");
	SharedPreferences storage;
	String inputType = "pass";
	LinearLayout layout, enterPass;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(log);
		setContentView(R.layout.password_layout);
		
		storage = getSharedPreferences("vishalsingh.masterpasswords", Context.MODE_PRIVATE);
		layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.password_set_layout, null);
		
		if(storage.getString("masterPass", null) != null){
			LinearLayout main = findViewById(R.id.mainLayout);
			main.removeAllViews();
			enterPass = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.password_enter_layout, null);
			main.addView(enterPass);
			EditText mainEdit =  enterPass.findViewById(R.id.passwordenterlayoutEditText1);
			mainEdit.requestFocus();
			final InputMethodManager inpmm = ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE));
			new Handler().postDelayed(new Runnable() {
					@Override
					public void run() 
					{
						if (inpmm != null)
						{
							inpmm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
						} 
					}
				}, 1000);
			
		}
		
		((EditText)enterPass.findViewById(R.id.passwordenterlayoutEditText1)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						enterPass.findViewById(R.id.passwordenterlayoutLinearLayout1).performClick();
						return true;
					}
					return false;
				}
			});
			
		((EditText)layout.findViewById(R.id.passwordsetlayoutEditText2)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						layout.findViewById(R.id.passwordsetlayoutLinearLayout1).performClick();
						return true;
					}
					return false;
				}
			});
	}
	
	
	
	public void showHidePassword(View view){
		RelativeLayout parent = ((RelativeLayout) view.getParent());
		if(inputType.equals("pass")){
			EditText text = (EditText)parent.getChildAt(0);
			text.setInputType(InputType.TYPE_CLASS_TEXT);
			text.setSelection(text.getText().length());
			((LinearLayout)view).getChildAt(0).setBackgroundResource(R.drawable.visibility_off);
			inputType = "text";
		}else{
			EditText text = (EditText)parent.getChildAt(0);
			text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			text.setSelection(text.getText().length());
			((LinearLayout)view).getChildAt(0).setBackgroundResource(R.drawable.visibility);
			inputType = "pass";
		}
	}
	
	
	
	public void setPassword(View view){
		int visibility = (((LinearLayout)view.getParent()).findViewById(R.id.passwordsetlayoutEditText3).getVisibility());
		if(visibility == View.VISIBLE){
			saveNewPassword(view);
			return;
		}
		EditText pass1 = layout.findViewById(R.id.passwordsetlayoutEditText1);
		EditText pass2 = layout.findViewById(R.id.passwordsetlayoutEditText2);
		if(!pass1.getText().toString().equals(pass2.getText().toString())){
			((TextView)layout.findViewById(R.id.passwordsetlayoutTextView1)).setVisibility(View.VISIBLE);
			return;
		}
		LinearLayout warningLayout = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.warning_box_layout, null);
		final Dialog popAddPost = new Dialog(this);
		popAddPost.setContentView(warningLayout);
		popAddPost.setTitle("Warning");
		Objects.requireNonNull(popAddPost.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		warningLayout.findViewById(R.id.warningboxlayoutTextView1).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					popAddPost.dismiss();
				}
			});
		warningLayout.findViewById(R.id.warningboxlayoutTextView2).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					popAddPost.dismiss();
					storage.edit().putString("masterPass", MainActivity.encrypt(((EditText)layout.findViewById(R.id.passwordsetlayoutEditText1)).getText().toString())).apply();
					Intent intent = new Intent(PasswordActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				}
			});
		popAddPost.show();
	}
	
	
	
	
	
	public void continueBtn(View view){
		LinearLayout main = findViewById(R.id.mainLayout);
		main.removeAllViews();
		main.addView(layout);
	}
	
	
	
	public void checkEnter(View view){
		String p = ((EditText)enterPass.findViewById(R.id.passwordenterlayoutEditText1)).getText().toString();
		if(MainActivity.decrypt(storage.getString("masterPass",null)).equals(p)){
			Intent intent = new Intent(PasswordActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}else{
			enterPass.findViewById(R.id.password_enter_layoutTextView).setVisibility(View.VISIBLE);
		}
	}
	
	
	
	public void goChangePassword(View view){
		LinearLayout main = findViewById(R.id.mainLayout);
		main.removeAllViews();
		final LinearLayout setPass = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.password_set_layout, null);
		((RelativeLayout)setPass.findViewById(R.id.passwordsetlayoutRelativeLayout1)).setVisibility(View.VISIBLE);
		main.addView(setPass);
		((EditText)setPass.findViewById(R.id.passwordsetlayoutEditText2)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						setPass.findViewById(R.id.passwordsetlayoutLinearLayout1).performClick();
						return true;
					}
					return false;
				}
			});
	}
	
	
	
	
	
	public void saveNewPassword(View view){
		LinearLayout parent = ((LinearLayout) view.getParent());
		parent.findViewById(R.id.passwordsetlayoutTextView2).setVisibility(View.GONE);
		parent.findViewById(R.id.passwordsetlayoutTextView1).setVisibility(View.GONE);
		String oldPass = ((EditText)(parent).findViewById(R.id.passwordsetlayoutEditText3)).getText().toString();
		String newPass = ((EditText)(parent).findViewById(R.id.passwordsetlayoutEditText1)).getText().toString();
		String confirmPass = ((EditText)(parent).findViewById(R.id.passwordsetlayoutEditText2)).getText().toString();
		
		String olderPass = storage.getString("masterPass",null);
		
		if(olderPass == null){
			return;
		}
		
		if(oldPass.equals(MainActivity.decrypt(olderPass))){
			if(newPass.equals(confirmPass)){
				storage.edit().putString("masterPass", MainActivity.encrypt(newPass)).apply();
				Toast.makeText(this, "Password Saved!", Toast.LENGTH_LONG).show();
				LinearLayout main = findViewById(R.id.mainLayout);
				main.removeAllViews();
				main.addView(enterPass);
			}else{
				parent.findViewById(R.id.passwordsetlayoutTextView1).setVisibility(View.VISIBLE);
			}
		}else{
			parent.findViewById(R.id.passwordsetlayoutTextView2).setVisibility(View.VISIBLE);
		}
	}
	
	
	
	
	
}
