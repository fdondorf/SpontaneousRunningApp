package org.spontaneous.fragment;

import java.util.ArrayList;
import java.util.List;

import org.spontaneous.R;
import org.spontaneous.activities.ActivitySummaryActivity;
import org.spontaneous.activities.MainActivity;
import org.spontaneous.activities.adapter.CustomArrayAdapter;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.core.ITrackingService;
import org.spontaneous.core.impl.TrackingServiceImpl;
import org.spontaneous.db.GPSTracking.Tracks;
import org.spontaneous.db.GPSTracking.TracksColumns;
import org.spontaneous.trackservice.util.TrackingServiceConstants;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class MyActivitiesFragmentOld extends ListFragment {

	private static final String TAG = "MyActivitiesFragment";

	private static final int REQUEST_CODE_VALUE = 1;

	private static final int RESULT_OK = 1;
	private static final int RESULT_CANCELLED = 2;
	private static final int RESULT_DELETED = 3;

	private ITrackingService trackingService = TrackingServiceImpl.getInstance(this.getActivity());

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	private List<TrackModel> mTracks = null;

	public static MyActivitiesFragmentOld newInstance(int sectionNumber, Activity parent) {

		MyActivitiesFragmentOld fragment = new MyActivitiesFragmentOld();

		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);

		return fragment;
	}

	public MyActivitiesFragmentOld() {
		;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	       View rootView = inflater.inflate(R.layout.list_layout, container, false);

	       setHasOptionsMenu(true);

	       getMyActivitiesList();

	       return rootView;
	}

	private void getMyActivitiesList() {

		// Defines a list of columns to retrieve from the Cursor and load into an output row
	    String[] mTrackListColumns = {
	    	   BaseColumns._ID,
	    	   TracksColumns.NAME,
	    	   TracksColumns.TOTAL_DISTANCE,
	    	   TracksColumns.TOTAL_DURATION,
	    	   TracksColumns.CREATION_TIME,
	    	   TracksColumns.USER_ID
	    };

	    String [] selectionArgs = { String.valueOf(((MainActivity)getActivity()).getUserId()) };
	    
		Cursor mTracksCursor = getActivity().getContentResolver().query(Tracks.CONTENT_URI, mTrackListColumns, null, selectionArgs, TracksColumns.CREATION_TIME + " DESC");

	    //mTracks = trackingService.getAllTracks();
	    mTracks = getTrackData(mTracksCursor);
	    CustomArrayAdapter adapter = new CustomArrayAdapter(getActivity(), mTracks);
	    setListAdapter(adapter);
	}

	private List<TrackModel> getTrackData(Cursor mTracksCursor) {

		List<TrackModel> tracks = new ArrayList<TrackModel>();
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
						//totalDuration,
						Long.valueOf(mTracksCursor.getString(mTracksCursor.getColumnIndex(Tracks.TOTAL_DURATION))),
						Long.valueOf(mTracksCursor.getString(mTracksCursor.getColumnIndex(Tracks.CREATION_TIME))),
						Integer.valueOf(mTracksCursor.getInt(mTracksCursor.getColumnIndex(Tracks.USER_ID))));
				tracks.add(trackModel);
			}
		}

		return tracks;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_DELETED) {
			getMyActivitiesList();
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent intent = new Intent();
    	intent.setClass(getActivity(), ActivitySummaryActivity.class);
    	intent.putExtra(TrackingServiceConstants.TRACK_ID, mTracks.get(position).getId());
    	intent.putExtra(TrackingServiceConstants.REQUEST_CODE, REQUEST_CODE_VALUE);
    	startActivityForResult(intent, REQUEST_CODE_VALUE);
	}

	@Override
	public void onAttach(Activity activity) {
	       super.onAttach(activity);
	       ((MainActivity) activity).onSectionAttached(1);
	}
}

