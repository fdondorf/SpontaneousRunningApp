package org.spontaneous.activities;

import org.json.JSONException;
import org.json.JSONObject;
import org.spontaneous.R;
import org.spontaneous.activities.model.UserModel;
import org.spontaneous.core.ITrackingService;
import org.spontaneous.core.impl.TrackingServiceImpl;
import org.spontaneous.core.impl.TrackingServiceRESTImpl;
import org.spontaneous.utility.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
/**
 *
 * Register Activity Class
 */
public class RegisterActivity extends Activity {
	
	private static final String IP_ADRESS = "10.44.210.150:8080";
	
	private ITrackingService trackingService;
	
    private ProgressDialog prgDialog;
    
    private TextView errorMsg;

    private EditText firstnameET;
    private EditText lastnameET;
    private EditText emailET;
    private EditText pwdET;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        trackingService = TrackingServiceImpl.getInstance(this);
        
        errorMsg = (TextView)findViewById(R.id.register_error);
        
        firstnameET = (EditText) findViewById(R.id.registerFirstname);
        lastnameET = (EditText)findViewById(R.id.registerLastname);
        
        emailET = (EditText)findViewById(R.id.registerEmail);

        pwdET = (EditText)findViewById(R.id.registerPassword);
        
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false);
    }
 
    /**
     * Method gets triggered when Register button is clicked
     *
     * @param view
     */
    public void registerUser(View view) {
    	
        String firstname = firstnameET.getText().toString();
        String lastname = lastnameET.getText().toString();
        
        String email = emailET.getText().toString();
        String password = pwdET.getText().toString();
        
        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        // When Name Edit View, Email Edit View and Password Edit View have values other than Null
        if (Utility.isNotNull(firstname) && Utility.isNotNull(lastname) && 
        		Utility.isNotNull(email) && Utility.isNotNull(password)){
            // When Email entered is Valid
            if(Utility.validate(email)){
                // Put Http parameter name with value of Name Edit View control
                params.put("name", firstname);
                params.put("lastname", lastname);
                params.put("username", email);
                params.put("password", password);
                
                // Invoke RESTful Web Service with Http parameters
                //invokeWS(params);
                UserModel user = new UserModel(null, firstname, lastname, email, password);
                if (trackingService.register(user)) {
                    Toast.makeText(getApplicationContext(), "Deine Registrierung war erfolgreich!", Toast.LENGTH_LONG).show();
                }
                else{
                    errorMsg.setText("Fehler bei Registrierung!");
                }
            }
            // When Email is invalid
            else{
                Toast.makeText(getApplicationContext(), "Please enter valid email", Toast.LENGTH_LONG).show();
            }
        }
        // When any of the Edit View control left blank
        else{
            Toast.makeText(getApplicationContext(), "Please fill the form, don't leave any field blank", Toast.LENGTH_LONG).show();
        }
 
    }
 
    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */
    public void invokeWS(RequestParams params){
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        String adress = "http://" + IP_ADRESS + "/SpontaneousRESTService/services/register/doregister";
        client.get(adress, params, new AsyncHttpResponseHandler() {
        //client.get(adress, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
             @Override
             public void onSuccess(String response) {
                // Hide Progress Dialog
                 prgDialog.hide();
                 try {
                          // JSON Object
                         JSONObject obj = new JSONObject(Utility.escape(response));
                         // When the JSON response has status boolean value assigned with true
                         if(obj.getBoolean("status")){
                             // Set Default Values for Edit View controls
                             setDefaultValues();
                             // Display successfully registered message using Toast
                             Toast.makeText(getApplicationContext(), "You are successfully registered!", Toast.LENGTH_LONG).show();
                         }
                         // Else display error message
                         else{
                             errorMsg.setText(obj.getString("error_msg"));
                             Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                         }
                 } catch (JSONException e) {
                     // TODO Auto-generated catch block
                     Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                     e.printStackTrace();
 
                 }
             }
             // When the response returned by REST has Http response code other than '200'
             @Override
             public void onFailure(int statusCode, Throwable error,
                 String content) {
                 // Hide Progress Dialog
                 prgDialog.hide();
                 // When Http response code is '404'
                 if(statusCode == 404){
                     Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                 }
                 // When Http response code is '500'
                 else if(statusCode == 500){
                     Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                 }
                 // When Http response code other than 404, 500
                 else{
                     Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                 }
             }
         });
    }
 
    /**
     * Method which navigates from Register Activity to Login Activity
     */
    public void navigatetoLoginActivity(View view){
        Intent loginIntent = new Intent(getApplicationContext(),LoginActivity.class);
        // Clears History of Activity
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }
 
    /**
     * Set degault values for Edit View controls
     */
    public void setDefaultValues(){
        firstnameET.setText("");
    	lastnameET.setText("");
        emailET.setText("");
        pwdET.setText("");
    }

}
