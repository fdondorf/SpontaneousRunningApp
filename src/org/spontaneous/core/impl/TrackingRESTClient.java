package org.spontaneous.core.impl;

import org.apache.http.HttpEntity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import android.content.Context;

public class TrackingRESTClient {
	
	  //private static final String BASE_URL = "https://api.twitter.com/1/";
	  private static final String BASE_URL = "http://192.168.178.38:8081";
	  
	  private static AsyncHttpClient client = new AsyncHttpClient();

	  public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
	      client.get(getAbsoluteUrl(url), params, responseHandler);
	  }

	  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
	      client.post(getAbsoluteUrl(url), params, responseHandler);
	  }
	  
	  public static void post(Context context, String url, HttpEntity httpEntity, String contentType, AsyncHttpResponseHandler responseHandler) {
		  
		  PersistentCookieStore myCookieStore = new PersistentCookieStore(context.getApplicationContext());
		  client.setCookieStore(myCookieStore);
	      client.post(context, getAbsoluteUrl(url), httpEntity, contentType, responseHandler);		  
	  }

	  private static String getAbsoluteUrl(String relativeUrl) {
	      return BASE_URL + relativeUrl;
	  }
	}
