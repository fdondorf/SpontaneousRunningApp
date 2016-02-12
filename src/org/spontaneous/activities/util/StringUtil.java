package org.spontaneous.activities.util;

import java.math.BigDecimal;

public class StringUtil {

	public static final Integer HOUR_IN_SECONDS = 3600;
	public static final Integer KM_IN_METERS = 1000;
	public static final String KM_PER_HOUR = " km/h";
	public static final String MIN_PER_KM = " Min/km";
	public static final String KILOMETER = " km";
	
	public static final int ROUND_2 = 2;
	
	/**
	 * Returns the given distance in meters as a String of type kilometers.
	 * @param distance Distance in meters (m)
	 * @return Distance-String in kilometers (km) (Example: 7,24 km)
	 */
    public static String getDistanceString(Float distance) {
    	BigDecimal distanceInKm = new BigDecimal(distance/KM_IN_METERS);
    	distanceInKm = distanceInKm.setScale(ROUND_2, BigDecimal.ROUND_HALF_UP);
        return distanceInKm + KILOMETER;
    }

	/**
	 * Returns the given distance in meters as a String of type kilometers.
	 * @param distance Distance in meters (m)
	 * @return Distance-String in kilometers (km) withot unit (Example: 7,24)
	 */
    public static String getDistanceStringWithoutUnit(Float distance) {
    	BigDecimal distanceInKm = new BigDecimal(distance/KM_IN_METERS);
    	distanceInKm = distanceInKm.setScale(ROUND_2, BigDecimal.ROUND_HALF_UP);
        return String.valueOf(distanceInKm);
    }
    
    /**
     * Returns the given speed in m/s as a String of the speed in km/h.
     * @param speed Speed in m/s
     * @return String of speed in km/h (Example: 8,56 km/h)
     */
    public static String getSpeedString(Float speed) {
    	BigDecimal speedInKmh = new BigDecimal(speed * HOUR_IN_SECONDS / KM_IN_METERS); // Compute km/h from m/s
    	speedInKmh = speedInKmh.setScale(ROUND_2, BigDecimal.ROUND_HALF_UP);
    	return speedInKmh + KM_PER_HOUR;
    }
    
    /**
     * Returns the given speed in m/s as a String of the speed in km/h.
     * @param speed Speed in m/s
     * @return String of speed in km/h without unit (Example: 8,56)
     */
    public static String getSpeedStringWithoutUnit(Float speed) {
    	BigDecimal speedInKmh = new BigDecimal(speed * HOUR_IN_SECONDS / KM_IN_METERS); // Compute km/h from m/s
    	speedInKmh = speedInKmh.setScale(ROUND_2, BigDecimal.ROUND_HALF_UP);
    	return String.valueOf(speedInKmh);
    }
    public static String getMinutesPerKmString(Float speed) {
    	BigDecimal speedBd = new BigDecimal(speed);
    	speedBd.setScale(ROUND_2, BigDecimal.ROUND_HALF_UP);
    	return speedBd + MIN_PER_KM;
    }
    
    public static String getMinutesPerKmString(Long value) {
    	BigDecimal bd = new BigDecimal(value);
    	bd.setScale(ROUND_2, BigDecimal.ROUND_HALF_UP);
    	return bd + MIN_PER_KM;
    }
}
