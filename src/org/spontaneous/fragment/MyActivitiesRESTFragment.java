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
import org.spontaneous.core.impl.TrackingServiceRESTImpl;
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

public class MyActivitiesRESTFragment extends ListFragment {

	private static final String TAG = "MyActivitiesFragment";

	private static final int REQUEST_CODE_VALUE = 1;

	private static final int RESULT_DELETED = 3;

	private ITrackingService trackingService;

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	private List<TrackModel> mTracks = null;

	public static MyActivitiesRESTFragment newInstance(int sectionNumber, Activity parent) {

		MyActivitiesRESTFragment fragment = new MyActivitiesRESTFragment();

		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);

		return fragment;
	}

	public MyActivitiesRESTFragment() {
		;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	       View rootView = inflater.inflate(R.layout.list_layout, container, false);

	       setHasOptionsMenu(true);

	       trackingService = TrackingServiceRESTImpl.getInstance(this.getActivity());
	       getMyActivitiesList();

	       return rootView;
	}

	private void getMyActivitiesList() {

	    mTracks = new ArrayList<TrackModel>();

	    mTracks = trackingService.getAllTracks();

	    CustomArrayAdapter adapter = new CustomArrayAdapter(getActivity(), mTracks);
	    setListAdapter(adapter);
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

