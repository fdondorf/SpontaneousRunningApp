package org.spontaneous.fragment;

import java.io.UnsupportedEncodingException;

import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.spontaneous.R;
import org.spontaneous.activities.LoginActivity;
import org.spontaneous.activities.MainActivity;
import org.spontaneous.activities.RegisterActivity;
import org.spontaneous.core.RestUrls;
import org.spontaneous.utility.Constants;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class LogoutFragment extends Fragment {

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";
    
	private Context mContext;
	
	// Progress Dialog Object
    ProgressDialog prgDialog;
    
	public static LogoutFragment newInstance(int sectionNumber, Activity parent) {
	     LogoutFragment fragment = new LogoutFragment();

	     Bundle args = new Bundle();
		 args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		 fragment.setArguments(args);

	     return fragment;
	}

	public LogoutFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_logout, container, false);
		
		mContext = this.getActivity();
		
//		try {
//			logout(null);
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		logout();
	    return rootView;
	}

	public void logout(RequestParams params) throws UnsupportedEncodingException, JSONException {
    	
    	// Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this.getActivity());
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false);
        
        // Show Progress Dialog
        prgDialog.show();

    	// Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        String adress = "http://" + RestUrls.IP_ADRESS.toString() + ":" + RestUrls.PORT.toString() + RestUrls.REST_SERVICE_LOGOUT.toString();
		
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this.getActivity().getApplicationContext());
		client.setCookieStore(myCookieStore);  
        client.get(mContext, adress, null, new AsyncHttpResponseHandler() {
        
            // When the response returned by REST has Http response code '200'
        	@Override
            public void onSuccess(int statusCode, String response) {
        		// Hide Progress Dialog
                prgDialog.hide();
                                
                Toast.makeText(mContext, "Successfully logged out!", Toast.LENGTH_LONG).show();
                clearCookieStore();
                navigatetoLoginActivity();
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

	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    ((MainActivity) activity).onSectionAttached(1);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	private void logout() {
		
		// 1. Delete SharedPrefs
		SharedPreferences sharedPrefs = getActivity().getSharedPreferences(Constants.PREFERENCES, getActivity().MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.clear();
        editor.commit();
		
		// 2. navigateToLoginscreen
		navigatetoLoginActivity();
	}
    /**
     * Method gets triggered when Register button is clicked
     *
     * @param view
     */
    public void navigatetoLoginActivity() {
        Intent loginIntent = new Intent(this.getActivity().getApplicationContext(), LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
        this.getActivity().finish();
    }
    
    private void clearCookieStore() {
    	PersistentCookieStore myStore = new PersistentCookieStore(this.getActivity().getApplicationContext());
    	myStore.clear();
	}
    
  	private boolean createWarnDialog() {

  		boolean result = false;

  		new AlertDialog.Builder(getActivity())
  			.setTitle(R.string.standardWarningHdr)
  			.setMessage(R.string.warningNoGPSSignalTxt)
  			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
  				public void onClick(DialogInterface dialog, int whichButton) {
  					dialog.cancel();
		        }
		    })
		    .setPositiveButton(R.string.cont, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	dialog.cancel();
		        	logout();
		        }
		    })
		    .create().show();

  		return result;
  	}
}
