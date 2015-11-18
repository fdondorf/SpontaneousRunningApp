package org.spontaneous.activities.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model containing the data of a track
 * @author fdondorf
 *
 */
public class TrackModel {

	private Long id;
	private String name;
	private Float totalDistance;
	private Long totalDuration;
	private Long creationTime;
	private List<SegmentModel> segments = new ArrayList<SegmentModel>();
	
	public TrackModel(Long id, String name, Float totalDistance, Long totalDuration, Long creationTime) {
		super();
		this.id = id;
		this.name = name;
		this.totalDistance = totalDistance;
		this.totalDuration = totalDuration;
		this.creationTime = creationTime;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Float getTotalDistance() {
		return totalDistance;
	}
	
	public void setTotalDistance(Float totalDistance) {
		this.totalDistance = totalDistance;
	}
	
	public Long getTotalDuration() {
		return totalDuration;
	}

	public void setTotalDuration(Long totalDuration) {
		this.totalDuration = totalDuration;
	}

	public Long getCreationDate() {
		return creationTime;
	}
	
	public void setCreationDate(Long creationTime) {
		this.creationTime = creationTime;
	}

	public List<SegmentModel> getSegments() {
		return segments;
	}

	public void setSegments(List<SegmentModel> segments) {
		this.segments = segments;
	}
	
	public boolean addSegment(SegmentModel segment) {
		if (this.segments == null) {
			this.segments = new ArrayList<SegmentModel>();
		}
		return this.segments.add(segment);
	}
}
