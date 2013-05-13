/**
 * 
 */
package fr.utc.nf33.ins;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

/**
 *
 */
public class GpsDialogFragment extends DialogFragment {
  /*
   * The activity that creates an instance of this dialog fragment must implement this interface in
   * order to receive event callbacks. Each method passes the DialogFragment in case the host needs
   * to query it.
   */
  public interface GpsDialogListener {
    public void onGpsDialogCancel(DialogFragment dialog);

    public void onGpsDialogPositiveClick(DialogFragment dialog);
  }

  /**
   * 
   */
  public static final String NAME = "GpsDialogFragment";

  // Use this instance of the interface to deliver action events.
  private GpsDialogListener listener;

  // Override the Fragment.onAttach() method to instantiate the GpsDialogListener.
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    // Verify that the host activity implements the callback interface.
    try {
      // Instantiate the GpsDialogListener so we can send events to the host.
      listener = (GpsDialogListener) activity;
    } catch (ClassCastException e) {
      Log.d("GPSDialogFragment", "OnAttach");
      // The activity doesn't implement the interface, throw exception.
      throw new ClassCastException(activity.toString() + " must implement GpsDialogListener.");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Build the dialog and set up the button click handlers.
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(R.string.gps_dialog_title).setMessage(R.string.gps_dialog_content)
        .setPositiveButton(R.string.gps_dialog_ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            listener.onGpsDialogPositiveClick(GpsDialogFragment.this);
          }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            listener.onGpsDialogCancel(GpsDialogFragment.this);
          }
        });
    return builder.create();
  }
}
