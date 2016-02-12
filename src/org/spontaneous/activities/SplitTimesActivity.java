package org.spontaneous.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.spontaneous.R;
import org.spontaneous.activities.adapter.SplitTimeArrayAdapter;
import org.spontaneous.activities.model.GeoPointModel;
import org.spontaneous.activities.model.SplitTimeModel;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.core.ITrackingService;
import org.spontaneous.core.TrackingUtil;
import org.spontaneous.core.impl.TrackingServiceImpl;
import org.spontaneous.db.GPSTracking.Tracks;
import org.spontaneous.db.GPSTracking.Waypoints;
import org.spontaneous.db.GPSTracking.WaypointsColumns;
import org.spontaneous.trackservice.util.TrackingServiceConstants;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toolbar;

public class SplitTimesActivity extends Activity {

	private static final String TAG = "SplitTimesActivity";

	private static final int REQUEST_CODE_VALUE = 1;
	
	private Context mContext;
	
	private Toolbar toolbar;
	  
	private ITrackingService trackingService = TrackingServiceImpl.getInstance(this);

	private TrackModel mTrackModel = null;
	private List<GeoPointModel> geoPoints = null;

	private static final Float KILOMETER = 1000f;
	private static final Float MILES = 1600f;

	// GUI
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		 
		setContentView(R.layout.list_splittimes);

		mContext = this;
		
		// Toolbar		
		toolbar = (Toolbar) findViewById(R.id.tool_bar);
		toolbar.setTitle(R.string.btn_splittimes);
		toolbar.inflateMenu(R.menu.toolbar_menu);
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
		  
	    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
	    	@Override
	        public void onClick(View view) {
	            onBackPressed();
	        }
	    });
		setActionBar(toolbar);
		
		// Get track from db
		Bundle data = getIntent().getExtras();
		mTrackModel = trackingService.readTrackById(data.getLong(TrackingServiceConstants.TRACK_ID));

		// Get the geopoint for the track from db
		geoPoints = trackingService.getGeoPointsByTrack(data.getLong(TrackingServiceConstants.TRACK_ID));
		//readGeoPointsForTrack(data.getLong(TrackingServiceConstants.TRACK_ID));

		List<SplitTimeModel> splitTimes = TrackingUtil.computeAverageSpeedPerUnit(mTrackModel, geoPoints, KILOMETER);
		listView = (ListView) findViewById(R.id.splitTimesList);
		listView.setAdapter(new SplitTimeArrayAdapter(this, splitTimes));

  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

	  switch (item.getItemId()) {
	  case android.R.id.home:
		  onBackPressed();
		  return true;

	  default:
		  return super.onOptionsItemSelected(item);
	  }
  }


  private Map<Long, List<GeoPointModel>> createDataStructure(List<GeoPointModel> geoPoints) {

	  // Create data structure geoPoints by segments
	  Map<Long, List<GeoPointModel>> geoPointsBySegments = new TreeMap<Long, List<GeoPointModel>>();
	  for (GeoPointModel geoPointModel : geoPoints) {
		  if (geoPointsBySegments.get(geoPointModel.getSegmentId()) == null) {
			  geoPointsBySegments.put(geoPointModel.getSegmentId(), new ArrayList<GeoPointModel>());
		  }
		  geoPointsBySegments.get(geoPointModel.getSegmentId()).add(geoPointModel);
	  }

	  return geoPointsBySegments;
  }

  private List<GeoPointModel> readGeoPointsForTrack(Long trackId) {

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
	      Cursor mCursor = this.getContentResolver().query(wayPointsReadUri, mWayPointColumns, null, null, "waypoints."+Waypoints._ID);
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
	  getMenuInflater().inflate(R.menu.splittimes, menu);
	  return true;
  }

}