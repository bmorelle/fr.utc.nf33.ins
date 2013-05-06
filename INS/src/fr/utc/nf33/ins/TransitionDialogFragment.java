/**
 * 
 */
package fr.utc.nf33.ins;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 *
 */
public class TransitionDialogFragment extends DialogFragment {
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(R.string.snr_above_limit_title);
    builder.setMessage(R.string.snr_above_limit_content);
    builder.setCancelable(false);
    return builder.create();
  }
}
