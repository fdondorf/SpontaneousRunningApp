package org.spontaneous.core.impl;

import java.util.ArrayList;
import java.util.List;

import org.spontaneous.activities.model.SegmentModel;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.core.ITrackingService;
import org.spontaneous.db.GPSTracking.Segments;
import org.spontaneous.db.GPSTracking.SegmentsColumns;
import org.spontaneous.db.GPSTracking.Tracks;
import org.spontaneous.db.GPSTracking.TracksColumns;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

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
		   		   	Tracks.CREATION_TIME
		      };

		      // Read track
		      Uri trackReadUri = Uri.withAppendedPath(Tracks.CONTENT_URI, String.valueOf(trackId));
		      Cursor mCursor = mContext.getContentResolver().query(trackReadUri, mTrackColumns, null, null, Tracks.CREATION_TIME);
		      if (mCursor != null && mCursor.moveToNext()) {

		    	// TODO: Quickfix wieder entfernen
					Long totalDuration = 0L;
					String value = null;
					if (mCursor.getString(mCursor.getColumnIndex(Tracks.TOTAL_DURATION)) != null) {
						try {
							totalDuration = Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks.TOTAL_DURATION)));
						} catch (Exception exc) {
							value = String.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks.TOTAL_DURATION)));
							Log.i(TAG, "TotalDuration:" + value);
						}
					}


			      trackModel = new TrackModel(
			    		Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks._ID))),
			    		mCursor.getString(mCursor.getColumnIndex(Tracks.NAME)),
						Float.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks.TOTAL_DISTANCE))),
						totalDuration,
						//Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks.TOTAL_DURATION))),
						Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks.CREATION_TIME)))
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
			      //return trackModel;
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
	    	   TracksColumns.CREATION_TIME
	    };

		Cursor mTracksCursor = mContext.getContentResolver().query(Tracks.CONTENT_URI, mTrackListColumns, null, null, TracksColumns.CREATION_TIME + " DESC");


		if (mTracksCursor != null) {
			TrackModel trackModel = null;
			while (mTracksCursor.moveToNext()) {

				// TODO: Quickfix wieder entfernen
				Long totalDuration = 0L;
				String value = null;
				if (mTracksCursor.getString(mTracksCursor.getColumnIndex(Tracks.TOTAL_DURATION)) != null) {
					try {
						totalDuration = Long.valueOf(mTracksCursor.getString(mTracksCursor.getColumnIndex(Tracks.TOTAL_DURATION)));
						Log.i(TAG, value);
					} catch (Exception exc) {
						value = String.valueOf(mTracksCursor.getString(mTracksCursor.getColumnIndex(Tracks.TOTAL_DURATION)));
					}
				}

				trackModel = new TrackModel(
						mTracksCursor.getLong(mTracksCursor.getColumnIndex(Tracks._ID)),
						mTracksCursor.getString(mTracksCursor.getColumnIndex(Tracks.NAME)),
						Float.valueOf(mTracksCursor.getString(mTracksCursor.getColumnIndex(Tracks.TOTAL_DISTANCE))),
						totalDuration,
						//Long.valueOf(mTracksCursor.getString(mTracksCursor.getColumnIndex(Tracks.TOTAL_DURATION))),
						Long.valueOf(mTracksCursor.getString(mTracksCursor.getColumnIndex(Tracks.CREATION_TIME))));
				tracks.add(trackModel);
			}
		}

		return tracks;
	}

}
