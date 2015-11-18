package org.spontaneous.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.spontaneous.R;
import org.spontaneous.activities.adapter.SplitTimeArrayAdapter;
import org.spontaneous.activities.model.GeoPointModel;
import org.spontaneous.activities.model.SegmentModel;
import org.spontaneous.activities.model.SplitTimeModel;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.core.ITrackingService;
import org.spontaneous.core.impl.TrackingServiceImpl;
import org.spontaneous.db.GPSTracking.Segments;
import org.spontaneous.db.GPSTracking.SegmentsColumns;
import org.spontaneous.db.GPSTracking.Tracks;
import org.spontaneous.db.GPSTracking.Waypoints;
import org.spontaneous.db.GPSTracking.WaypointsColumns;
import org.spontaneous.trackservice.util.TrackingServiceConstants;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class SplitTimesActivity extends Activity {

	private static final String TAG = "SplitTimesActivity";

	private static final int REQUEST_CODE_VALUE = 1;

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

		setContentView(R.layout.list_splittimes);

	  // Enable back naavigation in action bar
	  getActionBar().setDisplayHomeAsUpEnabled(true);

	  // Get track from db
	  Bundle data = getIntent().getExtras();
	  mTrackModel = trackingService.readTrackById(data.getLong(TrackingServiceConstants.TRACK_ID));

	  // Get the geopoint for the track from db
	  geoPoints = readGeoPointsForTrack(data.getLong(TrackingServiceConstants.TRACK_ID));

	  List<SplitTimeModel> splitTimes = computeAverageSpeedPerUnit(geoPoints, KILOMETER);
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

  // TODO: Auslagern in Helper f�r Activities, die it den Trackdaten rechnen (TrackingHelper)
  private List<SplitTimeModel> computeAverageSpeedPerUnit(List<GeoPointModel> geoPoints, Float unitMarker) {

	  List<SplitTimeModel> result = new ArrayList<SplitTimeModel>();

	  // Create data structure geoPoints by segments
	  Map<Long, List<GeoPointModel>> geoPointsBySegments = createDataStructure(geoPoints);

	  int unitCounter = 1;
	  boolean firstPointOfSegment = true;
	  Double totalDistanceCurrentPoint = 0D;
	  Double totalDistancePrevPoint = 0D;
	  Long timeLastUnit = 0L;
	  GeoPointModel lastPoint = null;
	  SegmentModel segment = null;
	  Float kmDone = unitMarker;


	  for (Entry<Long, List<GeoPointModel>> entry : geoPointsBySegments.entrySet()) {
		  if (!entry.getValue().isEmpty()) {

			  segment = getSegmentById(entry.getKey(), mTrackModel);
			  firstPointOfSegment = true;
			  for (GeoPointModel point : entry.getValue()) {

				  // Wenn erster Punkt des Segments, dann Zeit Punkt - Startzeit Segment
				  if (firstPointOfSegment) {
					  timeLastUnit += point.getTime() - segment.getStartTimeInMillis();
					  firstPointOfSegment = false;
				  }
				  // ...ansonsten Zeit aktueller Punkt - letzter Punkt
				  else {
					  if (lastPoint == null) {
						  lastPoint = point;
					  }
					  timeLastUnit += point.getTime() - lastPoint.getTime();
				  }
				  lastPoint = point;
				  totalDistanceCurrentPoint += point.getDistance();

				  // Wenn Unitgrenze zwischen den beiden Wegpunkten f�r SplitTime hinzu
				  if (totalDistancePrevPoint <= kmDone && totalDistanceCurrentPoint >= kmDone) {

					  result.add(new SplitTimeModel(unitCounter, timeLastUnit));
					  kmDone += unitMarker;
					  unitCounter++;
					  timeLastUnit = 0L;
				  }
				  totalDistancePrevPoint = totalDistanceCurrentPoint;
			  }
			  //lastPoint = null;
		  }
	  }

	  return result;
  	}

  private SegmentModel getSegmentById(Long segmentId, TrackModel trackModel) {
	if (segmentId != null) {
		for (SegmentModel segment : trackModel.getSegments()) {
			if (segmentId.equals(segment.getId())) {
				return segment;
			}
		}
	}
	return null;
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
	  getMenuInflater().inflate(R.menu.activity_summary, menu);
	  return true;
  }

}