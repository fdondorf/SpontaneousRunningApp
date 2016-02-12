package org.spontaneous.core.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.spontaneous.activities.model.GeoPointModel;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.activities.model.UserModel;
import org.spontaneous.core.ITrackingService;
import org.spontaneous.core.RestUrls;
import org.spontaneous.db.GPSTracking.Tracks;
import org.spontaneous.db.GPSTracking.User;
import org.spontaneous.utility.Utility;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class TrackingServiceRESTImpl implements ITrackingService {

	private static final String TAG = "TrackingServiceRESTImpl";

	private PersistentCookieStore myCookieStore;

	private static final String IP_ADRESS = "192.168.178.38:8081";
	private static final String REST_SERVICE_TRACKS = "/oasp4j-sample-server/services/rest/trackmanagement/v1/tracks";
	private static final String REST_SERVICE_CSRFTOKEN = "/oasp4j-sample-server/services/rest/security/v1/csrftoken";
	private static final String REST_SERVICE_LOGIN = "/oasp4j-sample-server/services/rest/login";
	private static final String REST_SERVICE_CURRENTUSER = "/oasp4j-sample-server/services/rest/security/v1/currentuser/";
	
	private JSONObject mCsrfToken;
	
    private ProgressDialog prgDialog;
    private TextView errorMsg;

	private static ITrackingService instance;
	private static Activity mContext;

	private TrackingServiceRESTImpl(Activity context) {
		mContext = context;
		myCookieStore = new PersistentCookieStore(mContext);
	}

	public static ITrackingService getInstance(Activity context) {
		//if (instance == null || !context.equals(context)) {
			instance = new TrackingServiceRESTImpl(context);
		//}
		return instance;
	}

	@Override
	public TrackModel readTrackById(Long trackId) {

		TrackModel trackModel = null;

		return trackModel;
	}

	@Override
	public List<TrackModel> getAllTracks() {
		List<TrackModel> tracks = new ArrayList<TrackModel>();

		TrackModel trackModel = null;
		for (int i = 0; i < 10; i++) {
			 trackModel = new TrackModel(
					 Long.valueOf(i),
					 "Test_" + i,
					 3000f,
					 1000000L,
					 Long.valueOf(System.currentTimeMillis()),
					 1);
			 tracks.add(trackModel);
			 Log.i(TAG, "Created track entry No. " + i);
		}
        
		getCurrentUser(null);
		
		invokeWS(null);
		
		return tracks;
	}


    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */
    public void invokeWS(RequestParams params) {
    	
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
        String adress = "http://" + RestUrls.IP_ADRESS.toString() + ":" + RestUrls.PORT +
        		RestUrls.REST_SERVICE_TRACKS.toString();
        SharedPreferences sharedPref = mContext.getApplicationContext().getSharedPreferences("appPrefs", Context.MODE_PRIVATE);
        String csrfToken = sharedPref.getString("X-CSRF-TOKEN", "xyz");
        client.addHeader("X-CSRF-TOKEN", csrfToken);
        
        PersistentCookieStore myStore = new PersistentCookieStore(mContext.getApplicationContext());
        client.setCookieStore(myStore);
        
        client.get(adress, params, new JsonHttpResponseHandler() {

            // When the response returned by REST has Http response code '200'
        	@Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        		// Hide Progress Dialog
                prgDialog.hide();
                
                // When the JSON response has status boolean value assigned with true
                if (statusCode == 200) {
                	// Display successfully registered message using Toast
                	Toast.makeText(mContext, "Loaded successfully tracks via REST Service. " + response.toString(), Toast.LENGTH_LONG).show();
                }
                // Else display error message
                else{
                	Toast.makeText(mContext, "Loading of tracks failed.", Toast.LENGTH_LONG).show();
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
    
   public void getCurrentUser(RequestParams params) {
    	
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
        String adress = "http://" + IP_ADRESS + REST_SERVICE_CURRENTUSER;
        client.setCookieStore(myCookieStore);
        client.get(adress, params, new JsonHttpResponseHandler() {

        	@Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        		// Hide Progress Dialog
                prgDialog.hide();
                try {
                    // JSON Object
                	//JSONObject obj = new JSONObject(Utility.escape(response));
                    Toast.makeText(mContext, "Current User: " + response.getString("name") + ", " + 
                             response.getString("firstName"), Toast.LENGTH_LONG).show();
                        
                 } catch (JSONException e) {
                     // TODO Auto-generated catch block
                     Toast.makeText(mContext, "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                     e.printStackTrace();

                 }            }
        	
            // When the response returned by REST has Http response code '200'
//        	@Override
//            public void onSuccess(String response) {
//        		// Hide Progress Dialog
//                prgDialog.hide();
//                try {
//                    // JSON Object
//                	JSONObject obj = new JSONObject(Utility.escape(response));
//                    Toast.makeText(mContext, "Current User: " + obj.getString("name") + ", " + 
//                             obj.getString("firstName"), Toast.LENGTH_LONG).show();
//                        
//                 } catch (JSONException e) {
//                     // TODO Auto-generated catch block
//                     Toast.makeText(mContext, "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
//                     e.printStackTrace();
//
//                 }
//             }
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
   
    public void login(RequestParams params) throws UnsupportedEncodingException, JSONException {
    	
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
        String adress = "http://" + IP_ADRESS + REST_SERVICE_LOGIN;
		
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("j_username", "chief");
        jsonParams.put("j_password", "chief");
        StringEntity entity = new StringEntity(jsonParams.toString());
        
		client.setCookieStore(myCookieStore);  
        //TrackingRESTClient.post(mContext, REST_SERVICE_LOGIN, entity, "application/json", new AsyncHttpResponseHandler() {;
        client.post(mContext, adress, entity , "application/json", new AsyncHttpResponseHandler() {
        
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

                
                makeRESTCalls();
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
    
    private void makeRESTCalls () {
    	getCurrentUser(null);
    }

	@Override
	public UserModel login(String email, String password, boolean stayLoggedIn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkUserAlreadyLoggedin() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean register(UserModel user) {

		return false;
	}

	@Override
	public UserModel findUserByMail(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GeoPointModel> getGeoPointsByTrack(Long trackId) {
		// TODO Auto-generated method stub
		return null;
	}
}
