package org.spontaneous.activities.util;

import java.util.Calendar;
import java.util.Locale;

import org.spontaneous.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogHelper {

	/**
	 * Create a standard AlertDialog with one Confirm-Button for the given activity and
	 * with the given title.
	 * @param activity Activity to create the dialog for
	 * @param titleResource resource-id of the title string
	 * @return
	 */
	public static Dialog createValidationDialog(Activity activity, int titleResource) {
		
		return new AlertDialog.Builder(activity)
        .setTitle(titleResource)
        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	dialog.cancel();
            }
        })
        .create();
	}
	
	public static DatePickerDialog createDatePickerDialog(Context context, 
			OnDateSetListener listener) {
		Calendar c = Calendar.getInstance(Locale.GERMAN);
        return new DatePickerDialog(context,
                    listener,
                    c.get(Calendar.YEAR), 
                    c.get(Calendar.MONTH), 
                    c.get(Calendar.DAY_OF_MONTH));
	}
	
	public static Dialog createStandardErrorDialog(Context context) {
		return createStandardErrorDialog(context, "");
	}
	
	public static Dialog createStandardErrorDialog(Context context, CharSequence message) {
		return new AlertDialog.Builder(context)
        .setTitle(R.string.standardErrorHdr)
        .setMessage(R.string.standardErrorTxt)
        .setMessage(message)
        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	dialog.cancel();
            }
        })
        .create();
	}
	
	public static Dialog createWarnDialog(Context context, CharSequence message) {
		return new AlertDialog.Builder(context)
        .setTitle(R.string.standardWarningHdr)
        .setMessage(R.string.warningNoGPSSignalTxt)
        .setMessage(message)
        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	dialog.cancel();
            }
        })
        .create();
	}
	
	public static Dialog createDeleteDialog(Context context, int resourceMsgId) {
		return new AlertDialog.Builder(context)
        .setTitle(R.string.standardWarningHdr)
        .setMessage(resourceMsgId)
        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	dialog.cancel();
            }
        })
         .setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	dialog.cancel();
            }
        })
        .create();
	}
	
	public static Dialog createValidationDialog(int resourceHdr, Context context, CharSequence message) {
		return new AlertDialog.Builder(context)
        .setTitle(resourceHdr)
        .setMessage(R.string.standardErrorTxt)
        .setMessage(message)
        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	dialog.cancel();
            }
        })
        .create();
	}
}
