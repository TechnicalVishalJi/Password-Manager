package com.vishalsingh.passwords;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;
import android.widget.*;
import android.content.*;

public class CustomizedExceptionHandler implements UncaughtExceptionHandler {

	/*
	 Paste below code in your app mainactivity before setContentView() and add this file as class
	 Also add storage permission and change package name of this file
	 
	 CustomizedExceptionHandler log = new CustomizedExceptionHandler("Crash Reports", "App");
	 Thread.setDefaultUncaughtExceptionHandler(log);
	 
	 Now call log.e(this, error); for error
	 and log.i(message); for message
	 Crashes will be automatically added to log file
	 */
	
	
    private UncaughtExceptionHandler defaultUEH;
    private String localPath;
	private String appName;
    public CustomizedExceptionHandler(String localPath ,String appName) {
        this.localPath = localPath;
		this.appName = appName;
        //Getting the the default exception handler
        //that's executed when uncaught exception terminates a thread
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }
	

    public void uncaughtException(Thread t, Throwable e) {

        //Write a printable representation of this Throwable
        //The StringWriter gives the lock used to synchronize access to this writer.
        final Writer stringBuffSync = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringBuffSync);
        e.printStackTrace(printWriter);
        String stacktrace = stringBuffSync.toString();
        printWriter.close();

        if (localPath != null) {
            writeToFile(stacktrace);
        }

        //Used only to prevent from any code getting executed.
        // Not needed in this example
        defaultUEH.uncaughtException(t, e);
    }

    private void writeToFile(String currentStacktrace) {
        try {
            //Gets the Android external storage directory & Create new folder Crash_Reports
            File dir = new File(Environment.getExternalStorageDirectory(), localPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd-MM-yyyy, hh:mm:ss a");
            Date date = new Date();
            String comment = "---Logged at :  "+ dateFormat.format(date) + "   ---\n";

            // Write the file into the folder
            File reportFile = new File(dir, appName + ".log");
            FileWriter fileWriter = new FileWriter(reportFile, true);
            fileWriter.append(comment + currentStacktrace + "\n\n");
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e){
            Log.e("ExceptionHandler", e.getMessage());
        }
    }
	
	
	public void e(Context context, Exception message){
		final Writer stringBuffSync = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringBuffSync);
        message.printStackTrace(printWriter);
        String stacktrace = stringBuffSync.toString();
        printWriter.close();

        if (localPath != null) {
            writeToFile(stacktrace);
        }
		Toast.makeText(context, message.toString(), Toast.LENGTH_LONG).show();
	}
	
	public void i(String message){
		if (localPath != null) {
            writeToFile(message);
        }
	}
	
	public void e(Exception e){
		final Writer stringBuffSync = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringBuffSync);
        e.printStackTrace(printWriter);
        String stacktrace = stringBuffSync.toString();
        printWriter.close();

        if (localPath != null) {
            writeToFile(stacktrace);
        }
	}

}
