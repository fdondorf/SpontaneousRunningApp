package org.spontaneous.fragment;

import org.spontaneous.R;
import org.spontaneous.activities.CurrentActivityActivity;
import org.spontaneous.activities.MainActivity;
import org.spontaneous.trackservice.util.TrackingServiceConstants;
import org.spontaneous.utility.GPSListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

public class StartFragmentMap extends Fragment implements LocationListener {

	private GPSListener gpsLocationListener;
	private Location mCurrentLocation;

	private Thread mSignalViewTimer = null;
	
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	private GoogleMap map = null;
	private TextView gpsSearchTxt = null;
	private ImageView gpsSignalView = null;
	
	private TextView usernameView = null; 
	private TextView passwordView = null;
	
	public static StartFragmentMap newInstance(int sectionNumber, Activity parent) {
	     StartFragmentMap fragment = new StartFragmentMap();

	     Bundle args = new Bundle();
		 args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		 fragment.setArguments(args);

	     return fragment;
	}

	public StartFragmentMap() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_start, container, false);

		MapFragment mf = (MapFragment) getChildFragmentManager().findFragmentById(R.id.startMap);
		map = mf.getMap();
		map.setMyLocationEnabled(true);

		gpsSearchTxt = (TextView) rootView.findViewById(R.id.gpsSearchTxt);

	    gpsSignalView = (ImageView) rootView.findViewById(R.id.gpsImg);
	    
	    usernameView = (TextView) rootView.findViewById(R.id.currentUser_username);
	    passwordView = (TextView) rootView.findViewById(R.id.currentUser_password);
	    
	    final Button button = (Button) rootView.findViewById(R.id.btn_startActivity);
	    button.setOnClickListener(mBtnStartActivityListener);

	    gpsLocationListener = GPSListener.getInstance(getActivity(), this);
	    gpsLocationListener.getLocationManager().requestLocationUpdates(gpsLocationListener.getProvider(), 500, 1, this);

	    mCurrentLocation = gpsLocationListener.getCurrentLocation();
	 	if (mCurrentLocation != null) {
	 		gpsSignalView.setImageResource(R.drawable.ic_connection_excellent);
	 		onLocationChanged(mCurrentLocation);
	 	}
	 	else {
//	 		signalView.setText(R.string.gpsSignalSearching);
	 		gpsSignalView.setImageResource(R.drawable.ic_connection_gone);
	 		gpsSearchTxt.setText(R.string.gpsSignalSearching);
	 		updateGpsSignalImage(rootView, this);
	 	}

	 	getCurrentUser();
	 	
	    return rootView;
	}

	public static void updateGpsSignalImage(View rootView, Fragment fragment) {
		
		final ImageView gpsSignalView = (ImageView) rootView.findViewById(R.id.gpsImg);
		
		fragment.getActivity().runOnUiThread(new Thread() {
			int counter = 3;
			public void run() {
				 try {  
					 	Thread.sleep(500);
	                    if (counter == 3) {
	                    	counter = 0;
	                    }
	                    else {
	                    	counter++;
	                    }
	               } catch (InterruptedException e) {  
	                   e.printStackTrace();  
	               } finally {
	                	if (counter == 0)
	                		gpsSignalView.setImageResource(R.drawable.ic_connection_gone);
	                	else if (counter == 1) {
	                		gpsSignalView.setImageResource(R.drawable.ic_connection_bad);           		
	                	}
	                	else if (counter == 2) {
	                		gpsSignalView.setImageResource(R.drawable.ic_connection_good);                		
	                	}
	                	else if (counter == 3) {
	                		gpsSignalView.setImageResource(R.drawable.ic_connection_excellent);                		
	                	}
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
		gpsLocationListener.getLocationManager().removeUpdates(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		gpsLocationListener.getLocationManager().removeUpdates(this);
	}

	@Override
	public void onResume() {
		super.onResume();
        gpsLocationListener.getLocationManager().requestLocationUpdates(gpsLocationListener.getProvider(), 500, 1, this);
	}

	@Override
	public void onStop() {
		super.onStop();
        gpsLocationListener.getLocationManager().removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {

		if (location != null) {

			mCurrentLocation = gpsLocationListener.getBetterLocation(location);
			gpsSearchTxt.setText(R.string.gpsSignal);
			if (mSignalViewTimer != null) {
				//mSignalViewTimer.stop();
				mSignalViewTimer = null;
			}
			switch (gpsLocationListener.getGPSSignal()) {
				case NO_GPS_SIGNAL:
			 		gpsSignalView.setImageResource(R.drawable.ic_connection_gone);
					break;

				case GPS_SIGNAL_BAD:
			 		gpsSignalView.setImageResource(R.drawable.ic_connection_bad);
					break;

				case GPS_SIGNAL_MEDIUM:
			 		gpsSignalView.setImageResource(R.drawable.ic_connection_good);
					break;

				case GPS_SIGNAL_GOOD:
			 		gpsSignalView.setImageResource(R.drawable.ic_connection_excellent);
					break;
			}

	 		CameraUpdate center=
	 		        CameraUpdateFactory.newLatLng(
	 		        		new LatLng(mCurrentLocation.getLatitude(),
	 		        				mCurrentLocation.getLongitude()));
	 		CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

	 		map.moveCamera(center);
	 		map.animateCamera(zoom);

 	    }
	}

	/****************************************
  	 * Listener
  	 * *************************************/

  	private OnClickListener mBtnStartActivityListener = new OnClickListener() {

	    public void onClick(View v) {
	    	if (gpsLocationListener == null || gpsLocationListener.getCurrentLocation() == null)
	    		createWarnDialog();
	    	else {
	    		startIntentForCurrentActivity();
	    	}
	    }
  	};

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
		        	startIntentForCurrentActivity();
		        }
		    })
		    .create().show();

  		return result;
  	}

  	private void startIntentForCurrentActivity() {
  		Intent intent = new Intent();
        intent.setClass(getActivity(), CurrentActivityActivity.class);
        intent.putExtra(TrackingServiceConstants.START_LOCATION, mCurrentLocation);
        startActivity(intent);
  	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
	    Toast.makeText(getActivity(), "Enabled new provider " + provider,
		        Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderDisabled(String provider) {
	    Toast.makeText(getActivity(), "Disabled provider " + provider,
		        Toast.LENGTH_SHORT).show();
	}
	
	private void getCurrentUser() {
		//TODO: REST-Call
	}
}
