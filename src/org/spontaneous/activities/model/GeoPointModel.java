package org.spontaneous.activities.model;

public class GeoPointModel {

	private Long id;
	private Long segmentId;
	private Double latitude;
	private Double longitude;
	private Long time;
	private Double speed;
	private Double accurracy;
	private Double altitude;
	private Double bearing;
	private Double distance;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getSegmentId() {
		return segmentId;
	}
	
	public void setSegmentId(Long segmentId) {
		this.segmentId = segmentId;
	}
	
	public Double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	
	public Double getLongitude() {
		return longitude;
	}
	
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	
	public Long getTime() {
		return time;
	}
	
	public void setTime(Long time) {
		this.time = time;
	}
	
	public Double getSpeed() {
		return speed;
	}
	
	public void setSpeed(Double speed) {
		this.speed = speed;
	}
	
	public Double getAccurracy() {
		return accurracy;
	}
	
	public void setAccurracy(Double accurracy) {
		this.accurracy = accurracy;
	}
	
	public Double getAltitude() {
		return altitude;
	}
	
	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}
	
	public Double getBearing() {
		return bearing;
	}
	
	public void setBearing(Double bearing) {
		this.bearing = bearing;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}	
	
}