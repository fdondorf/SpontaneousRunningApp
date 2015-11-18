package org.spontaneous.utility;

import org.spontaneous.enums.GPSSignal;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


public class GPSListener {

	private Context mContext;
	
	private LocationManager locationManager;
	private String provider;
	private Location currentLocation;
	
	private static final int ONE_MINUTE = 1000 * 60;

	private static GPSListener instance = null;
	
	public static GPSListener getInstance(Context context, LocationListener listener) {
		if (instance == null) {
			instance = new GPSListener(context, listener);
		}
		return instance;
	}
	
	private GPSListener (Context context, LocationListener listener) {
		
		this.mContext = context;

		 // Get the location manager
	    locationManager = (LocationManager) this.mContext.getSystemService(Context.LOCATION_SERVICE);
	    
	    // Define the criteria how to select the locatioin provider -> accuracy fine
	    Criteria criteria = new Criteria();
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
	    criteria.setAltitudeRequired(true);
	    
	    // Get best provide
	    provider = locationManager.getBestProvider(criteria, false);
	    //currentLocation = locationManager.getLastKnownLocation(provider);
	}

	public Location getBetterLocation(Location location) {
		
		if (location != null) {
			if (isBetterLocation(location, this.currentLocation)) {
				this.currentLocation = location;
				return location;
			}
		}
		return this.currentLocation;
	}
	
	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	private boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null && location != null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
	    boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than one minute since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than one minute older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

	public Location getCurrentLocation() {
		return currentLocation;
	}

	public String getProvider() {
		return provider;
	}
	
	public LocationManager getLocationManager() {
		return locationManager;
	}

	public GPSSignal getGPSSignal() {
		
		if (this.currentLocation == null) {
			return GPSSignal.NO_GPS_SIGNAL;
		}
		else if (this.currentLocation.getAccuracy() < 5f) {
			return GPSSignal.GPS_SIGNAL_GOOD;
		}
		else if (this.currentLocation.getAccuracy() >= 5f && this.currentLocation.getAccuracy() < 15f) {
			return GPSSignal.GPS_SIGNAL_MEDIUM;
		}
		else return GPSSignal.GPS_SIGNAL_BAD;
	}
	
}
