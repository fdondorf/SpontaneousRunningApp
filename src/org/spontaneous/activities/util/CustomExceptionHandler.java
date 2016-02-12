package org.spontaneous.activities.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.print.PrintDocumentAdapter.WriteResultCallback;

public class CustomExceptionHandler implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler defaultUEH;
	private String localPath;
	private String url;
	
	public CustomExceptionHandler (String localPath, String url) {
		this.localPath = localPath;
		this.url = url;
		this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		String stacktrace = result.toString();
		printWriter.close();
		String filename = timestamp + ".txt";
		
		if(localPath != null) {
			writeToFile(stacktrace, filename);
		}
		
		if (url != null) {
			sendToServer(stacktrace, filename);
		}
		
		defaultUEH.uncaughtException(t, e);
	}
	
	private void writeToFile (String stacktrace, String filename) {
		try {
			File file = new File(localPath + "/");
			boolean suc = false;
			if (!file.exists()) {
				suc = file.mkdirs();
			}
			if (file.isDirectory()) {
				suc = true;
			}
			
			File outFile = new File(file, filename);
			// if file doesnt exists, then create it
			if (!outFile.exists()) {
				suc = outFile.createNewFile();
			}

			FileOutputStream out = new FileOutputStream(outFile);

			// get the content in bytes
			byte[] contentInBytes = stacktrace.getBytes();

			out.write(contentInBytes);
			out.flush();
			out.close();
			//BufferedWriter bos = new BufferedWriter(new FileWriter(localPath + "/" + filename));
			//bos.write(stacktrace);
			//bos.flush();
			//bos.close();
		} catch ( Exception e) {
			e.printStackTrace();
		}
	}

	private void sendToServer(String stacktrace, String filename) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("filename", filename));
		nvps.add(new BasicNameValuePair("stacktrace", stacktrace));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			httpClient.execute(httpPost);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
