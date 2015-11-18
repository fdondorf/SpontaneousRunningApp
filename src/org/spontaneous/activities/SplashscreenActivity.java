package org.spontaneous.activities;

import org.spontaneous.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

public class SplashscreenActivity extends Activity {

	private static final String TAG = "SplashscreenActivity";
	
	private Context ctx;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splashscreen);
        
        ViewGroup root = (ViewGroup) findViewById(R.layout.splashscreen);

        ctx = this;
        
        // Create a progress bar to display while Splashscreen is active
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        progressBar.setIndeterminate(true);
        //root.addView(progressBar);

        
        Thread timer = new Thread() {  
            public void run() {  
                try {  
                    sleep(3000); // wait 3 seconds  
                } catch (InterruptedException e) {  
                    e.printStackTrace();  
                } finally {  
                	Intent profileIntent = new Intent(ctx, MainActivity.class);  
                    startActivity(profileIntent);
                }  
            }  
        };  
        timer.start();  
    }  
  
    @Override  
    protected void onPause() {  
        super.onPause();  
        finish();  
    }
}
