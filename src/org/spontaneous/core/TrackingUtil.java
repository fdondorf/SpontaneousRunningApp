package org.spontaneous.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.spontaneous.R;
import org.spontaneous.activities.model.GeoPoint;
import org.spontaneous.activities.model.GeoPointModel;
import org.spontaneous.activities.model.SegmentModel;
import org.spontaneous.activities.model.SplitTimeModel;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.utility.Constants;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


public class TrackingUtil {

	private static final Float KILOMETER = 1000f;
	
	public static void drawCurrentTrackOnMap(Context ctx, List<GeoPointModel> geoPoints, GoogleMap map, GeoPoint lastGeoPoint) {
		
		if (!geoPoints.isEmpty()) {

			  // Draw line on map and add visual components like markers ...
			  //LatLng start = new LatLng(geoPoints.get(0).getLatitude(), geoPoints.get(0).getLongitude());
			  LatLng last = new LatLng(lastGeoPoint.getLatitude(), lastGeoPoint.getLongitude());

			  map.clear();
			  
			  for (PolylineOptions options : drawPolyLine(geoPoints, map, ctx.getResources())) {
				  map.addPolyline(options);
			  }
			  map.moveCamera(CameraUpdateFactory.newLatLngZoom(last, 20));
		  }
	}
	
	  /**
	   * Computes the average time in minutes per kilometer (m/km).
	   * @param totalDuration in Milliseconds (ms)
	   * @param totalDistance in meters (m)
	   * @return The average time per Km in Millis.
	   */
	  public static Long computeAverageTimePerKilometer(Long totalDuration, Float totalDistance) {

		  // Average speed in m/s
		  if (totalDistance == null || totalDistance <= 0F) {
			  return 0L;
		  }
		  if (totalDuration == null)
			  return 0L;
		  
		  Float totalDistInKm = totalDistance / KILOMETER;

		  BigDecimal averageTimeInMillisPerKm = new BigDecimal(totalDuration / totalDistInKm);
		  
		  if (averageTimeInMillisPerKm != null)
			  return averageTimeInMillisPerKm.longValue();
		  
		  return null;
	  }

	  /**
	   * Computes the spent calories
	   * @param weight in kilos (kg)
	   * @param totalDistance in meters (m)
	   * @return The spent calories
	   */
	  public static Long computeCalories(Integer weight, Float totalDistance) {

		  if (totalDistance == null || totalDistance <= 0F) {
			  return 0L;
		  }
		  if (weight == null)
			  return 0L;
		  
		  Float spentCalories = totalDistance * weight / Constants.KILOMETER;

		  return Math.round(Double.valueOf(spentCalories));
	  }
	  
	/**
	 * Computes the average speed per unit
	 * 
	 * @param geoPoints
	 * @param unitMarker
	 * @return
	 */
	public static List<SplitTimeModel> computeAverageSpeedPerUnit(TrackModel trackModel, List<GeoPointModel> geoPoints, Float unitMarker) {

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

				  segment = getSegmentById(entry.getKey(), trackModel);
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

					  // Wenn Unitgrenze zwischen den beiden Wegpunkten füge SplitTime hinzu
					  if (totalDistancePrevPoint <= kmDone && totalDistanceCurrentPoint >= kmDone) {

						  result.add(new SplitTimeModel(unitCounter, timeLastUnit));
						  kmDone += unitMarker;
						  unitCounter++;
						  timeLastUnit = 0L;
					  }
					  totalDistancePrevPoint = totalDistanceCurrentPoint;
				  }
			  }
		  }
		
		  // Last Unit
		  long distance = Math.round(totalDistanceCurrentPoint);
		  long done = Math.round(kmDone);
		  long result1 = done - distance;
		  long result2 = 1000/result1;
		  
		  Long timeFinalUnit = timeLastUnit * result2; //(1000/(done - distance));
		  result.add(new SplitTimeModel(unitCounter, timeFinalUnit));

		  return result;
	}
	
	public static Long computeTotalDuration(TrackModel trackModel) {
		
		Long totalDuration = 0L;
		
		totalDuration = System.currentTimeMillis() - Long.valueOf(trackModel.getCreationDate());
		
		Long segmentDuration = null;
		for (SegmentModel segModel : trackModel.getSegments()) {
			segmentDuration = segModel.getEndTimeInMillis() - segModel.getStartTimeInMillis();
			totalDuration += segmentDuration;
		}
		
		return totalDuration;
	};
	  
	private static Map<Long, List<GeoPointModel>> createDataStructure(List<GeoPointModel> geoPoints) {

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
	
	private static SegmentModel getSegmentById(Long segmentId, TrackModel trackModel) {
		if (segmentId != null) {
			for (SegmentModel segment : trackModel.getSegments()) {
				if (segmentId.equals(segment.getId())) {
					return segment;
				}
			}
		}
		return null;
	}
	
	private static List<PolylineOptions> drawPolyLine(List<GeoPointModel> geoPoints, GoogleMap map, Resources res) {

		  // Create data structure geoPoints by segments
		  Map<Long, List<GeoPointModel>> geoPointsBySegments = createDataStructure(geoPoints);

		  float kmMarker = KILOMETER;
		  int km = 1;
		  List<PolylineOptions> polyLineOptions = new ArrayList<PolylineOptions>();
		  boolean startOfFirstSegment = true;
		  Double totalDistanceCurrentPoint = 0D;
		  Double totalDistancePrevPoint = 0D;

		  GeoPointModel lastPointOfTrack = null;
		  for (Entry<Long, List<GeoPointModel>> entry : geoPointsBySegments.entrySet()) {
			  if (!entry.getValue().isEmpty()) {
				  PolylineOptions option = new PolylineOptions();
				  option.width(5).color(R.color.lightgreen);

				  boolean firstPointOfSegment = true;
				  for (GeoPointModel point : entry.getValue()) {
					  totalDistanceCurrentPoint += point.getDistance();
					  LatLng latLngPoint = new LatLng(point.getLatitude(), point.getLongitude());
					  if (startOfFirstSegment) {
						  map.addMarker(new MarkerOptions().position(latLngPoint)
									 .title("Stop").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
						  startOfFirstSegment = false;
						  firstPointOfSegment = false;
					  }
					  else if (firstPointOfSegment) {
						  map.addMarker(new MarkerOptions().position(latLngPoint)
									 .title("Stop").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
						  firstPointOfSegment = false;
					  }

					  if (totalDistancePrevPoint <= kmMarker && totalDistanceCurrentPoint >= kmMarker) {

						  createKmMarker(latLngPoint, km, map, res);
//						  map.addMarker(new MarkerOptions().position(latLngPoint)
//			     			        .title("Km " + kmMarker)
//			     			        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
						  kmMarker += KILOMETER;
						  km++;
					  }
					  option.add(latLngPoint);
					  lastPointOfTrack = point;
					  totalDistancePrevPoint = totalDistanceCurrentPoint;
				  }
				  polyLineOptions.add(option);
			  }
		  }

		  // Set Marker of last point
		  LatLng latLngPoint = new LatLng(lastPointOfTrack.getLatitude(), lastPointOfTrack.getLongitude());
		  map.addMarker(new MarkerOptions().position(latLngPoint)
			        .title("Ende")
			        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

		  return polyLineOptions;
	}
	
  	private static void createKmMarker(LatLng position, int km, GoogleMap map, Resources res) {

  		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
  		Bitmap bitmap = Bitmap.createBitmap(32, 37, conf);

	  	// paint defines the text color,
	  	// stroke width, size
	  	Paint color = new Paint();
	  	color.setTextSize(15);
	  	color.setColor(Color.WHITE);

  		Canvas canvas = new Canvas(bitmap);
  		canvas.drawBitmap(BitmapFactory.decodeResource(res,
  		    R.drawable.custom_marker), 0, 0, color);
  		canvas.drawText(km + " km", 3, 20, color);
  		MarkerOptions options = new MarkerOptions().position(position).
  				icon(BitmapDescriptorFactory.fromBitmap(bitmap)).anchor(0.5f, 1);
  		map.addMarker(options);
  	}
	
}
