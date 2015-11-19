package org.spontaneous.fragment;

import java.util.Map;

import org.spontaneous.R;
import org.spontaneous.activities.CurrentActivityActivity;
import org.spontaneous.activities.MainActivity;
import org.spontaneous.trackservice.util.TrackingServiceConstants;
import org.spontaneous.utility.GPSListener;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class StartFragmentMap extends Fragment implements LocationListener {

	private GPSListener gpsLocationListener;
	private Location mCurrentLocation;

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	//private GoogleMap map = null;
	private TextView signalView = null;
	private TextView latView = null;
	private TextView lngView = null;
	private TextView acView = null;
	private TextView altView = null;

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

		//map = ((MapFragment) getFragmentManager().findFragmentById(R.id.startMap)).getMap();

	    signalView = (TextView) rootView.findViewById(R.id.gpsSignalText);
	    signalView.setText(R.string.gpsSignalSearching);

	    latView = (TextView) rootView.findViewById(R.id.gpsLat);
	    lngView = (TextView) rootView.findViewById(R.id.gpsLng);
	    acView = (TextView) rootView.findViewById(R.id.gpsSignalAccurancy);
	    altView = (TextView) rootView.findViewById(R.id.gpsAltitude);

	    final Button button = (Button) rootView.findViewById(R.id.btn_startActivity);
	    button.setOnClickListener(mBtnStartActivityListener);

	    gpsLocationListener = GPSListener.getInstance(getActivity(), this);
	    gpsLocationListener.getLocationManager().requestLocationUpdates(gpsLocationListener.getProvider(), 500, 1, this);

	    mCurrentLocation = gpsLocationListener.getCurrentLocation();
	 	if (mCurrentLocation != null) {
	 		signalView.setText(R.string.gpsSignalBad);
	 		latView.setText(String.valueOf(mCurrentLocation.getLatitude()));
	 		lngView.setText(String.valueOf(mCurrentLocation.getLongitude()));
	 		acView.setText(String.valueOf(mCurrentLocation.getAccuracy()));
	 		altView.setText(String.valueOf(mCurrentLocation.getAltitude()));
	 	}
	 	else {
	 		signalView.setText(R.string.gpsSignalSearching);
	 	}

	    return rootView;
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

		signalView.setText(R.string.gpsSignalSearching);

		if (location != null) {

			mCurrentLocation = gpsLocationListener.getBetterLocation(location);

			switch (gpsLocationListener.getGPSSignal()) {
				case NO_GPS_SIGNAL:
					signalView.setText(R.string.gpsSignalSearching);
					break;

				case GPS_SIGNAL_BAD:
					signalView.setText(R.string.gpsSignalBad);
					break;

				case GPS_SIGNAL_MEDIUM:
					signalView.setText(R.string.gpsSignalMedium);
					break;

				case GPS_SIGNAL_GOOD:
					signalView.setText(R.string.gpsSignalGood);
					break;
			}

	 		latView.setText(String.valueOf(mCurrentLocation.getLatitude()));
	 		lngView.setText(String.valueOf(mCurrentLocation.getLongitude()));
	 		acView.setText(String.valueOf(mCurrentLocation.getAccuracy()));
	 		altView.setText(String.valueOf(mCurrentLocation.getAltitude()));

	 		CameraUpdate center=
	 		        CameraUpdateFactory.newLatLng(
	 		        		new LatLng(mCurrentLocation.getLatitude(),
	 		        				mCurrentLocation.getLongitude()));
	 		    CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

	 		    //map.moveCamera(center);
	 		    //map.animateCamera(zoom);

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
}
