package org.spontaneous.trackservice;

import org.spontaneous.activities.model.GeoPoint;

import android.os.Parcel;
import android.os.Parcelable;

public class WayPointModel implements Parcelable {

	private Long trackId;
	private Long segmentId;
	private Long wayPointId;
	private Long startTime;
	private Long totalTime;
	private float totalDistance;
	private GeoPoint geopoint;
	
	WayPointModel(Parcel in) {
		this.trackId = in.readLong();
		this.segmentId = in.readLong();
		this.wayPointId = in.readLong();
        this.startTime = in.readLong();
        this.totalTime = in.readLong();
        this.totalDistance = in.readFloat();
        this.geopoint = (GeoPoint)in.readParcelable(GeoPoint.class.getClassLoader());
        //in.readTypedList(geopoints, GeoPoint.CREATOR);
    }
	
	public WayPointModel() {
		super();
	}

	public Long getTrackId() {
		return trackId;
	}

	public void setTrackId(Long trackId) {
		this.trackId = trackId;
	}

	public Long getSegmentId() {
		return segmentId;
	}

	public void setSegmentId(Long segmentId) {
		this.segmentId = segmentId;
	}

	public Long getWayPointId() {
		return wayPointId;
	}

	public void setWayPointId(Long wayPointId) {
		this.wayPointId = wayPointId;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public GeoPoint getGeopoint() {
		return geopoint;
	}

	public Long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(Long totalTime) {
		this.totalTime = totalTime;
	}

	public float getTotalDistance() {
		return totalDistance;
	}

	public void setTotalDistance(float totalDistance) {
		this.totalDistance = totalDistance;
	}
	
	public void setGeopoint(GeoPoint geopoint) {
		this.geopoint = geopoint;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(trackId);
		dest.writeLong(segmentId);
		dest.writeLong(wayPointId);
		dest.writeLong(startTime);
		dest.writeLong(totalTime);
		dest.writeFloat(totalDistance);
		dest.writeParcelable(geopoint, flags);
		
	}
	
	public static final Parcelable.Creator<WayPointModel> CREATOR
    	= new Parcelable.Creator<WayPointModel>() {

		public WayPointModel createFromParcel(Parcel in) {
			return new WayPointModel(in);
		}

	    public WayPointModel[] newArray(int size) {
	        return new WayPointModel[size];
	    }
	};

}
