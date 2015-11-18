package org.spontaneous.activities.model;

public class SplitTimeModel {

	private Integer unit;
	private Long time;
	
	public SplitTimeModel(Integer unit, Long time) {
		this.unit = unit;
		this.time = time;
	}
	
	public Integer getUnit() {
		return unit;
	}
	
	public void setUnit(Integer unit) {
		this.unit = unit;
	}
	
	public Long getTime() {
		return time;
	}
	
	public void setTime(Long time) {
		this.time = time;
	}

}
