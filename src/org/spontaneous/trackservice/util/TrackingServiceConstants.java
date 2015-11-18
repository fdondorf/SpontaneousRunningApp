package org.spontaneous.trackservice.util;


/**
 * Constants for the tracking service
 * 
 */
public class TrackingServiceConstants {
	
   public static final String SATELLITE = "SATELLITE";
   public static final String SPEED = "showspeed";
   public static final String ALTITUDE = "showaltitude";
   public static final String DISTANCE = "showdistance";
   public static final String LOCATION = "LOCATION";
   public static final String START_LOCATION = "START_LOCATION";
   public static final String SPEEDSANITYCHECK = "speedsanitycheck";
   public static final String TRACK_DATA = "TRACK_DATA";
   public static final String TRACK_ID = "TRACK_ID";
   public static final String SEGMENT_ID = "SEGMENT_ID";
   public static final String WAYPOINT_ID = "WAYPOINT_ID";

   public static final String REQUEST_CODE = "REQUEST_CODE";
   
   /**
    * The state of the service is unknown
    */
   public static final int UNKNOWN = -1;

   /**
    * The service is actively logging, it has requested location update from the location provider.
    */
   public static final int LOGGING = 1;

   /**
    * The service is not active, but can be resumed to become active and store location changes as part of a new segment of the current track.
    */
   public static final int PAUSED = 2;

   /**
    * The service is not active and can not resume a current track but must start a new one when becoming active.
    */
   public static final int STOPPED = 3;

   /**
    * The precision of the GPS provider is based on the custom time interval and distance.
    */
   public static final int LOGGING_CUSTOM = 0;

   /**
    * The GPS location provider is asked to update every 10 seconds or every 5 meters.
    */
   public static final int LOGGING_FINE = 1;

   /**
    * The GPS location provider is asked to update every 15 seconds or every 10 meters.
    */
   public static final int LOGGING_NORMAL = 2;

   /**
    * The GPS location provider is asked to update every 30 seconds or every 25 meters.
    */
   public static final int LOGGING_COARSE = 3;

   /**
    * The radio location provider is asked to update every 5 minutes or every 500 meters.
    */
   public static final int LOGGING_GLOBAL = 4;

   /**
    * A distance between the last two waypoints in meters
    */
   public static final String EXTRA_DISTANCE = "org.spontaneous.trackservice.EXTRA_DISTANCE";
   /**
    * Total distance of the track in meters
    */
   public static final String TOTAL_DISTANCE = "org.spontaneous.trackservice.TOTAL_DISTANCE";
   /**
    * A time period in minutes
    */
   public static final String EXTRA_TIME = "org.spontaneous.trackservice.EXTRA_TIME";
   /**
    * The location that pushed beyond the set minimum time or distance
    */
   public static final String EXTRA_LOCATION = "org.spontaneous.trackservice.EXTRA_LOCATION";
   /**
    * The current speed
    */
   public static final String EXTRA_SPEED = "org.spontaneous.trackservice.EXTRA_SPEED";
   /**
    * The track that is being logged
    */
   public static final String EXTRA_TRACK = "org.spontaneous.trackservice.EXTRA_TRACK";
}
