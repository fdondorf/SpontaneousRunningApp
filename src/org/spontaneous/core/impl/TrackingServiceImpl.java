package org.spontaneous.core.impl;

import java.util.ArrayList;
import java.util.List;

import org.spontaneous.activities.model.GeoPointModel;
import org.spontaneous.activities.model.SegmentModel;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.activities.model.UserModel;
import org.spontaneous.core.ITrackingService;
import org.spontaneous.db.GPSTracking.Segments;
import org.spontaneous.db.GPSTracking.SegmentsColumns;
import org.spontaneous.db.GPSTracking.Tracks;
import org.spontaneous.db.GPSTracking.TracksColumns;
import org.spontaneous.db.GPSTracking.User;
import org.spontaneous.db.GPSTracking.UserColumns;
import org.spontaneous.db.GPSTracking.Waypoints;
import org.spontaneous.db.GPSTracking.WaypointsColumns;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class TrackingServiceImpl implements ITrackingService {

	private static final String TAG = "TrackingServiceImpl";

	private static ITrackingService instance;
	private static Activity mContext;

	private TrackingServiceImpl(Activity context) {
		mContext = context;
	}

	public static ITrackingService getInstance(Activity context) {
		//if (instance == null || !context.equals(context)) {
			instance = new TrackingServiceImpl(context);
		//}
		return instance;
	}

	@Override
	public TrackModel readTrackById(Long trackId) {

		TrackModel trackModel = null;

		if (trackId != null) {
		      String[] mTrackColumns = {
		    		Tracks._ID,
		   		   	Tracks.NAME,
		   		   	Tracks.TOTAL_DISTANCE,
		   		   	Tracks.TOTAL_DURATION,
		   		   	Tracks.USER_ID,
		   		   	Tracks.CREATION_TIME
		      };

		      // Read track
		      Uri trackReadUri = Uri.withAppendedPath(Tracks.CONTENT_URI, String.valueOf(trackId));
		      Cursor mCursor = mContext.getContentResolver().query(trackReadUri, mTrackColumns, null, null, Tracks.CREATION_TIME);
		      if (mCursor != null && mCursor.moveToNext()) {

			      trackModel = new TrackModel(
			    		Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks._ID))),
			    		mCursor.getString(mCursor.getColumnIndex(Tracks.NAME)),
						mCursor.getFloat(mCursor.getColumnIndex(Tracks.TOTAL_DISTANCE)),
						mCursor.getLong(mCursor.getColumnIndex(Tracks.TOTAL_DURATION)),
						mCursor.getLong(mCursor.getColumnIndex(Tracks.CREATION_TIME)),
						Integer.valueOf(mCursor.getColumnIndex(Tracks.USER_ID))
						);

			      // Read Segments and wayPoints
			      String[] mSegmentsColumns = {
			    		  BaseColumns._ID,
				   		  SegmentsColumns.TRACK,
				   		  SegmentsColumns.START_TIME,
				   		  SegmentsColumns.END_TIME
				      };
			      Uri segmentReadUri = Uri.withAppendedPath(Tracks.CONTENT_URI, String.valueOf(trackId) + "/segments/");
			      Cursor mSegmentsCursor = mContext.getContentResolver().query(segmentReadUri, mSegmentsColumns, null, null, Segments._ID);

			      if (mSegmentsCursor != null) {
				      SegmentModel segmentModel = null;
				      while (mSegmentsCursor.moveToNext()) {
				    	  segmentModel = new SegmentModel();
				    	  segmentModel.setId(Long.valueOf(mSegmentsCursor.getString(mSegmentsCursor.getColumnIndex(Segments._ID))));
				    	  segmentModel.setTrackId(Long.valueOf(mSegmentsCursor.getString(mSegmentsCursor.getColumnIndex(Segments.TRACK))));
				    	  segmentModel.setStartTimeInMillis(mSegmentsCursor.getLong(mSegmentsCursor.getColumnIndex(Segments.START_TIME)));
				    	  segmentModel.setEndTimeInMillis(mSegmentsCursor.getLong(mSegmentsCursor.getColumnIndex(Segments.END_TIME)));
				    	  trackModel.addSegment(segmentModel);
				      }
			      }
		      }
		}
		return trackModel;
	}

	@Override
	public List<TrackModel> getAllTracks() {
		List<TrackModel> tracks = new ArrayList<TrackModel>();

		// Defines a list of columns to retrieve from the Cursor and load into an output row
	    String[] mTrackListColumns = {
	    	   BaseColumns._ID,
	    	   TracksColumns.NAME,
	    	   TracksColumns.TOTAL_DISTANCE,
	    	   TracksColumns.TOTAL_DURATION,
	    	   TracksColumns.USER_ID,
	    	   TracksColumns.CREATION_TIME
	    };

		Cursor mTracksCursor = mContext.getContentResolver().query(Tracks.CONTENT_URI, mTrackListColumns, null, null, TracksColumns.CREATION_TIME + " DESC");


		if (mTracksCursor != null) {
			TrackModel trackModel = null;
			while (mTracksCursor.moveToNext()) {

				trackModel = new TrackModel(
						mTracksCursor.getLong(mTracksCursor.getColumnIndex(Tracks._ID)),
						mTracksCursor.getString(mTracksCursor.getColumnIndex(Tracks.NAME)),
						Float.valueOf(mTracksCursor.getString(mTracksCursor.getColumnIndex(Tracks.TOTAL_DISTANCE))),
						Long.valueOf(mTracksCursor.getString(mTracksCursor.getColumnIndex(Tracks.TOTAL_DURATION))),
						Long.valueOf(mTracksCursor.getString(mTracksCursor.getColumnIndex(Tracks.CREATION_TIME))),
						Integer.valueOf(mTracksCursor.getColumnIndex(Tracks.USER_ID)));
				tracks.add(trackModel);
			}
		}

		return tracks;
	}

	@Override
	public UserModel login(String email, String password, boolean stayLoggedIn) {
		if (email != null && password != null) {
		    Uri userReadUri = Uri.withAppendedPath(User.CONTENT_URI, "login");
		    String[] mColumns =
		        { User._ID, User.FIRSTNAME, User.LASTNAME, User.EMAIL, User.STAY_LOGGED_IN };

		    //TODO: Gehashtes Passwort abfragen
		    String [] selectionArgs = {email, password};
		    Cursor mCursor = mContext.getContentResolver().query(userReadUri, 
		    		mColumns, null, selectionArgs, User.CREATION_TIME);
		    
		    if (mCursor != null && mCursor.moveToNext()) {
		        UserModel userModel =
		            new UserModel(mCursor.getLong(mCursor.getColumnIndex(User._ID)), 
		          		  mCursor.getString(mCursor.getColumnIndex(User.FIRSTNAME)), 
		          		  mCursor.getString(mCursor.getColumnIndex(User.LASTNAME)), 
		          		  mCursor.getString(mCursor.getColumnIndex(User.EMAIL)), 
		          		  mCursor.getString(mCursor.getColumnIndex(User.STAY_LOGGED_IN))
		    		);
		        return userModel;
		    }
		    
		    // TODO: Wenn stayLoggedIn übergeben und aus DB unterschiedlich dann update
		}
		return null;
	}

	@Override
	public boolean checkUserAlreadyLoggedin() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean register(UserModel user) {
		if (user != null) {
			ContentValues args = new ContentValues();
		    args.put(UserColumns.FIRSTNAME, user.getFirstname());
		    args.put(UserColumns.LASTNAME, user.getLastname());
		    args.put(UserColumns.EMAIL, user.getEmail());
		    args.put(UserColumns.PASSWORD, user.getPassword());
		    
		    
		    //TOD: Test ob Email bereist existiert
		    Uri resgisteredUser = mContext.getContentResolver().insert(User.CONTENT_URI, args);
		    
		    Long userId = Long.valueOf(resgisteredUser.getLastPathSegment()).longValue();
		    if(userId > 0 ) {
		    	return true;
		    }
		}
		return false;
	}

	@Override
	public UserModel findUserByMail(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GeoPointModel> getGeoPointsByTrack(Long trackId) {
		List<GeoPointModel> geoPoints = new ArrayList<GeoPointModel>();
		  if (trackId != null) {
			  String[] mWayPointColumns = {
					"waypoints."+BaseColumns._ID,
					WaypointsColumns.LATITUDE,
		   		   	WaypointsColumns.LONGITUDE,
		   		   	WaypointsColumns.TIME,
		   		   	WaypointsColumns.SPEED,
		   		   	WaypointsColumns.ACCURACY,
		   		   	WaypointsColumns.ALTITUDE,
		   		   	WaypointsColumns.BEARING,
		   			WaypointsColumns.DISTANCE,
		   		   	WaypointsColumns.SEGMENT
		      };

		      // Read track
		      Uri wayPointsReadUri = Uri.withAppendedPath(Tracks.CONTENT_URI, String.valueOf(trackId) + "/waypoints/");
		      Cursor mCursor = mContext.getContentResolver().query(wayPointsReadUri, mWayPointColumns, null, null, "waypoints."+Waypoints._ID);
		      while (mCursor.moveToNext()) {
		    	  GeoPointModel geoPointModel = new GeoPointModel();
		    	  geoPointModel.setId(Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Waypoints._ID))));
		    	  geoPointModel.setLatitude(mCursor.getDouble(mCursor.getColumnIndex(Waypoints.LATITUDE)));
		    	  geoPointModel.setLongitude(mCursor.getDouble(mCursor.getColumnIndex(Waypoints.LONGITUDE)));
		    	  geoPointModel.setTime(mCursor.getLong(mCursor.getColumnIndex(Waypoints.TIME)));
		    	  geoPointModel.setSpeed(mCursor.getDouble(mCursor.getColumnIndex(Waypoints.SPEED)));
		    	  geoPointModel.setAccurracy(mCursor.getDouble(mCursor.getColumnIndex(Waypoints.ACCURACY)));
		    	  geoPointModel.setAltitude(mCursor.getDouble(mCursor.getColumnIndex(Waypoints.ALTITUDE)));
		    	  geoPointModel.setBearing(mCursor.getDouble(mCursor.getColumnIndex(Waypoints.BEARING)));
		    	  geoPointModel.setDistance(mCursor.getDouble(mCursor.getColumnIndex(Waypoints.DISTANCE)));
		    	  geoPointModel.setSegmentId(Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Waypoints.SEGMENT))));

		    	  geoPoints.add(geoPointModel);
		      }
		  }
		  return geoPoints;
	}

}
