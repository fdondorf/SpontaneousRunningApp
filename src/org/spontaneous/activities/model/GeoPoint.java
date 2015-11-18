package org.spontaneous.activities.model;

import android.os.Parcel;
import android.os.Parcelable;

public class GeoPoint implements Parcelable {

	private double latitude;
	private double longitude;
	private float distance;
	private float speed;
	
	public GeoPoint () {
		super();
	}
	
	public GeoPoint (Parcel in) {
		this.latitude = in.readDouble();
		this.longitude = in.readDouble();
		this.distance = in.readFloat();
		this.speed = in.readFloat();
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeFloat(distance);
		dest.writeFloat(speed);
	}

	 public static final Parcelable.Creator<GeoPoint> CREATOR
     	= new Parcelable.Creator<GeoPoint>() {

		 public GeoPoint createFromParcel(Parcel in) {
			     return new GeoPoint(in);
			 }
			
		 public GeoPoint[] newArray(int size) {
			     return new GeoPoint[size];
			 }
	 };
}
