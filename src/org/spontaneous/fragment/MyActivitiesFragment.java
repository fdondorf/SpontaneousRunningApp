package org.spontaneous.fragment;

import java.util.ArrayList;
import java.util.List;

import org.spontaneous.R;
import org.spontaneous.activities.MainActivity;
import org.spontaneous.activities.adapter.RVAdapter;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.activities.util.CustomExceptionHandler;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MyActivitiesFragment extends Fragment {

	private static final String TAG = "MyActivitiesFragment";

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	private List<TrackModel> mTracks = null;

	public static MyActivitiesFragment newInstance(int sectionNumber, Activity parent) {

		MyActivitiesFragment fragment = new MyActivitiesFragment();

		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);

		return fragment;
	}

	public MyActivitiesFragment() {
		;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	       View rootView = inflater.inflate(R.layout.card_layout, container, false);
	       setHasOptionsMenu(true);

	       RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.rv);
	       rv.setHasFixedSize(true);
	       
	       LinearLayoutManager llm = new LinearLayoutManager(this.getActivity());
	       rv.setLayoutManager(llm);
	       
	       RVAdapter adapter = new RVAdapter(getAllTracks());
	       rv.setAdapter(adapter);
	       
	       return rootView;
	}

	private List<TrackModel> getAllTracks() {
		List<TrackModel> tracks = new ArrayList<TrackModel>();

		TrackModel trackModel = null;
		for (int i = 0; i < 10; i++) {
			 trackModel = new TrackModel(
					 Long.valueOf(i),
					 "Test_" + i,
					 3000f,
					 1000000L,
					 Long.valueOf(System.currentTimeMillis()),
					 1);
			 tracks.add(trackModel);
			 Log.i(TAG, "Created track entry No. " + i);
		}
        
		return tracks;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public void onAttach(Activity activity) {
	       super.onAttach(activity);
	       ((MainActivity) activity).onSectionAttached(1);
	}
}

