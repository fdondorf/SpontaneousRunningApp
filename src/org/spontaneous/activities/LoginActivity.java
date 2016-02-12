package org.spontaneous.activities;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.spontaneous.R;
import org.spontaneous.activities.model.UserModel;
import org.spontaneous.core.ITrackingService;
import org.spontaneous.core.RestUrls;
import org.spontaneous.core.impl.TrackingServiceImpl;
import org.spontaneous.utility.Constants;
import org.spontaneous.utility.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
/**
 *
 * Login Activity Class
 *
 */
public class LoginActivity extends Activity {
	
	private static final String IP_ADRESS = "10.44.210.150:8080";
	   
	private ITrackingService trackingService;

    // Progress Dialog Object
    private ProgressDialog prgDialog;
    // Error Msg TextView Object
    private TextView errorMsg;
    // Email Edit View Object
    private EditText emailET;
    // Passwprd Edit View Object
    private EditText pwdET;
    private CheckBox stayLoggedIn;
    
    private SharedPreferences sharedPrefs;
    
    private ToggleButton toggleBtn;
    
    private Context mContext;
    private PersistentCookieStore myCookieStore;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.login);
        
        trackingService = TrackingServiceImpl.getInstance(this);
        
        sharedPrefs = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        
        // Find Error Msg Text View control by ID
        errorMsg = (TextView)findViewById(R.id.login_error);
        // Find Email Edit View control by ID
        emailET = (EditText)findViewById(R.id.loginEmail);
        // Find Password Edit View control by ID
        pwdET = (EditText)findViewById(R.id.loginPassword);
    
        stayLoggedIn = (CheckBox) findViewById(R.id.cb_stayLoggedIn);
        
        toggleBtn = (ToggleButton) findViewById(R.id.server_login_enabled);
        
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        
        myCookieStore = new PersistentCookieStore(getApplicationContext());
        mContext = this;
        
        if (userAlreadyLoggedAndStaysLogged()) {
        	navigatetoHomeActivity();
        }
    }
 
    private boolean userAlreadyLoggedAndStaysLogged() {
    	SharedPreferences sharedPrefs = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
    	
    	if (sharedPrefs != null) {
    		Long userID = sharedPrefs.getLong(Constants.PREF_USERID, -1L);
    		if (userID > 0) {
    			boolean stayLogged = sharedPrefs.getBoolean(Constants.PREF_STAY_LOGGED, false);
    			return stayLogged;
    		}
    	}
    	return false;
    }
    
    private boolean userAlreadyLoggedIn() {
    	PersistentCookieStore myStore = new PersistentCookieStore(getApplicationContext());
    	String key = "JSESSIONID"+RestUrls.IP_ADRESS.toString();
    	if (myStore.getCookies().contains(key)) {
    		return true;
    	}
    	
    	for (Cookie cookie : myStore.getCookies()) {
    		if (cookie.getName().equals("JSESSIONID") && cookie.getDomain().equals(RestUrls.IP_ADRESS.toString())) {
    			return true;
    		}
    	}
    	return false;
	}

    
	@Override
	protected void onRestart() {
		super.onRestart();
		//this.finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//this.finish();
//		if (userAlreadyLoggedIn()) {
//			navigatetoHomeActivity();
//		}
	}

	/**
     * Method gets triggered when Login button is clicked
     *
     * @param view
     * @throws JSONException 
     * @throws UnsupportedEncodingException 
     */
    public void loginUser(View view) throws UnsupportedEncodingException, JSONException {
    	
    	if (!toggleBtn.isChecked()) {
    	
	        // Get Email Edit View Value
	        String email = emailET.getText().toString();
	        // Get Password Edit View Value
	        String password = pwdET.getText().toString();
	        
	        boolean stay = stayLoggedIn.isChecked();
	        
	        // Instantiate Http Request Param Object
	        RequestParams params = new RequestParams();
	        // When Email Edit View and Password Edit View have values other than Null
	        if (Utility.isNotNull(email) && Utility.isNotNull(password)){
	            // When Email entered is Valid
	            if (Utility.validate(email)){
	                // Put Http parameter username with value of Email Edit View control
	                params.put("j_username", email);
	                // Put Http parameter password with value of Password Edit Value control
	                params.put("j_password", password);
	                // Invoke RESTful Web Service with Http parameters
	                //login(params);
	                login(email, password, stay);
	            }
	            // When Email is invalid
	            //else{
	            //    Toast.makeText(getApplicationContext(), "Please enter valid email", Toast.LENGTH_LONG).show();
	            //}
	        } else{
	            Toast.makeText(getApplicationContext(), "Please fill the form, don't leave any field blank", Toast.LENGTH_LONG).show();
	        }
    	}
    	else {
    		navigatetoHomeActivity();
    	}
 
    }
 
    private void login (String email,  String password, boolean stayLogged) {
    	UserModel user = trackingService.login(email, password, stayLogged);
    	if (user != null) {
    		SharedPreferences.Editor editor = sharedPrefs.edit();
    		editor.putLong(Constants.PREF_USERID, user.getId());
    		editor.putString(Constants.PREF_FIRSTNAME, user.getFirstname());
    		editor.putString(Constants.PREF_LASTNAME, user.getLastname());
            editor.putString(Constants.PREF_EMAIL, email);
            editor.putBoolean(Constants.PREF_STAY_LOGGED, stayLogged);
            editor.commit();
    		navigatetoHomeActivity();
    	}
    	else {
            Toast.makeText(getApplicationContext(), "Fehler beim Anmelden!", Toast.LENGTH_LONG).show();

    	}
    }
    
    public void login(RequestParams params) throws UnsupportedEncodingException, JSONException {
    	
    	// Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        
        // Show Progress Dialog
        prgDialog.show();

    	// Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        String adress = "http://" + RestUrls.IP_ADRESS.toString() + ":" + RestUrls.PORT.toString() + RestUrls.REST_SERVICE_LOGIN.toString();
		
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("j_username", emailET.getText().toString());
        jsonParams.put("j_password", pwdET.getText().toString());
        StringEntity entity = new StringEntity(jsonParams.toString());
        
		client.setCookieStore(myCookieStore);  
        //TrackingRESTClient.post(mContext, REST_SERVICE_LOGIN, entity, "application/json", new AsyncHttpResponseHandler() {;
        client.post(this, adress, entity , "application/json", new AsyncHttpResponseHandler() {
        
            // When the response returned by REST has Http response code '200'
        	@Override
            public void onSuccess(int statusCode, String response) {
        		// Hide Progress Dialog
                prgDialog.hide();
                                
                StringBuilder builder = new StringBuilder();
                for (Cookie cookie : myCookieStore.getCookies()) {
                	builder.append("Name: ");
                	builder.append(cookie.getName() + ", ");
                	builder.append("Value: ");
                	builder.append(cookie.getValue() + ", ");
                	builder.append("Domain: ");
                	builder.append(cookie.getDomain() + ", ");
                }
                
                Toast.makeText(mContext, "Successfully logged in! Status Code = " + statusCode + 
                		"Cookies: " + builder.toString(), Toast.LENGTH_LONG).show();

                //getCurrentUser()
                getCSRFToken(null);
                navigatetoHomeActivity();
             }
             // When the response returned by REST has Http response code other than '200'
             @Override
             public void onFailure(int statusCode, Throwable error,
                 String content) {
                 // Hide Progress Dialog
                 prgDialog.hide();
                 // When Http response code is '404'
                 if(statusCode == 404){
                     Toast.makeText(mContext, "Requested resource not found", Toast.LENGTH_LONG).show();
                 }
                 // When Http response code is '500'
                 else if(statusCode == 500){
                     Toast.makeText(mContext, "Something went wrong at server end", Toast.LENGTH_LONG).show();
                 }
                 // When Http response code other than 404, 500
                 else{
                     Toast.makeText(mContext, "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                 }
             }
         });
    }
 
    public void getCSRFToken(RequestParams params) {
    	
    	// Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(mContext);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        
        // Show Progress Dialog
        prgDialog.show();

    	// Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        PersistentCookieStore myStore = new PersistentCookieStore(getApplicationContext());
        client.setCookieStore(myStore);
        
        String adress = "http://" + RestUrls.IP_ADRESS + ":" + RestUrls.PORT +
        		RestUrls.REST_SERVICE_CSRFTOKEN;
        client.get(adress, params, new JsonHttpResponseHandler() {

            // When the response returned by REST has Http response code '200'
        	@Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        		// Hide Progress Dialog
                prgDialog.hide();
                try {
                         // When the JSON response has status boolean value assigned with true
                         if(statusCode == 200){
                             // Display successfully registered message using Toast
                             Toast.makeText(mContext, "Got successfully CSRF-Token " + response.getString("headerName") + 
                            		 ":" + response.getString("token"), Toast.LENGTH_LONG).show();
                             printCookies();
                             SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("appPrefs", Context.MODE_PRIVATE);
                             SharedPreferences.Editor editor = sharedPref.edit();
                             editor.putString(response.getString("headerName"), response.getString("token"));
                             editor.commit();
                         }
                         else {
                             Toast.makeText(mContext, "Didn't get CSRF-Token ", Toast.LENGTH_LONG).show();
                         }
                 } catch (JSONException e) {
                     // TODO Auto-generated catch block
                     Toast.makeText(mContext, "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                     e.printStackTrace();

                 }
             }
             // When the response returned by REST has Http response code other than '200'
             @Override
             public void onFailure(int statusCode, Throwable error,
                 String content) {
                 // Hide Progress Dialog
                 prgDialog.hide();
                 // When Http response code is '404'
                 if(statusCode == 404){
                     Toast.makeText(mContext, "Requested resource not found", Toast.LENGTH_LONG).show();
                 }
                 // When Http response code is '500'
                 else if(statusCode == 500){
                     Toast.makeText(mContext, "Something went wrong at server end", Toast.LENGTH_LONG).show();
                 }
                 // When Http response code other than 404, 500
                 else{
                     Toast.makeText(mContext, "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                 }
             }
         });
    }
 
    /**
     * Method which navigates from Login Activity to Home Activity
     */
    public void navigatetoHomeActivity() {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        startActivity(mainIntent);
        finish();
    }
 
    /**
     * Method gets triggered when Register button is clicked
     *
     * @param view
     */
    public void navigatetoRegisterActivity(View view){
        Intent loginIntent = new Intent(getApplicationContext(),RegisterActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }
    
    private void printCookies() {
    	 StringBuilder builder = new StringBuilder();
         for (Cookie cookie : myCookieStore.getCookies()) {
         	builder.append("Name: ");
         	builder.append(cookie.getName() + ", ");
         	builder.append("Value: ");
         	builder.append(cookie.getValue() + ", ");
         	builder.append("Domain: ");
         	builder.append(cookie.getDomain() + ", ");
         }
         
         Toast.makeText(mContext, "Cookies: " + builder.toString(), Toast.LENGTH_LONG).show();

    }
}