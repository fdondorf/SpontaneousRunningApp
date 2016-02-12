package org.spontaneous.core;

public enum RestUrls {

	IP_ADRESS("192.168.178.38"),
	PORT("8081"),
	REST_SERVICE_TRACKS("/oasp4j-sample-server/services/rest/trackmanagement/v1/tracks"),
	REST_SERVICE_CSRFTOKEN("/oasp4j-sample-server/services/rest/security/v1/csrftoken"),
	REST_SERVICE_LOGIN("/oasp4j-sample-server/services/rest/login"),
	REST_SERVICE_LOGOUT("/oasp4j-sample-server/services/rest/logout"),
	REST_SERVICE_CURRENTUSER("/oasp4j-sample-server/services/rest/security/v1/currentuser/");
	
	private final String url;
	
	RestUrls(String url) {
		this.url = url;
	}
	
	private String getUrl() {
		return this.url;
	}
	
	public String toString () {
		return getUrl();
	}
}
