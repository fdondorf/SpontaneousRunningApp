package org.spontaneous.core.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.core.ITrackingService;
import org.spontaneous.utility.Utility;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class TrackingServiceRESTImpl implements ITrackingService {

	private static final String TAG = "TrackingServiceRESTImpl";

	private static final String IP_ADRESS = "10.44.210.150:8080";

    private ProgressDialog prgDialog;
    private TextView errorMsg;

	private static ITrackingService instance;
	private static Activity mContext;

	private TrackingServiceRESTImpl(Activity context) {
		mContext = context;
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
					 Long.valueOf(System.currentTimeMillis()));
			 tracks.add(trackModel);
			 Log.i(TAG, "Created track entry No. " + i);
		}

		return tracks;
	}


    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */
    public void invokeWS(RequestParams params){
        // Show Progress Dialog
        //prgDialog.show();

    	// Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        String adress = "http://" + IP_ADRESS + "/SpontaneousRESTService/services/register/doregister";
        client.get(adress, params, new AsyncHttpResponseHandler() {
        //client.get(adress, new AsyncHttpResponseHandler() {

            // When the response returned by REST has Http response code '200'
        	@Override
            public void onSuccess(String response) {
        		// Hide Progress Dialog
                prgDialog.hide();
                try {
                          // JSON Object
                         JSONObject obj = new JSONObject(Utility.escape(response));
                         // When the JSON response has status boolean value assigned with true
                         if(obj.getBoolean("status")){
                             // Display successfully registered message using Toast
                             Toast.makeText(mContext, "You are successfully registered!", Toast.LENGTH_LONG).show();
                         }
                         // Else display error message
                         else{
                             errorMsg.setText(obj.getString("error_msg"));
                             Toast.makeText(mContext, obj.getString("error_msg"), Toast.LENGTH_LONG).show();
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
}
