/**
 * 
 */
package fr.utc.nf33.ins.location;

import android.content.Intent;
import android.content.IntentFilter;

/**
 * 
 * @author
 * 
 */
public final class LocationIntent {
  /**
   * 
   * @author
   * 
   */
  public static final class NewLocation {
    /**
     * 
     */
    public static final String ACTION_NAME = "fr.utc.nf33.ins.NEW_LOCATION";

    /**
     * 
     */
    public static final String EXTRA_LATITUDE = "fr.utc.nf33.ins.LATITUDE";

    /**
     * 
     */
    public static final String EXTRA_LONGITUDE = "fr.utc.nf33.ins.LONGITUDE";

    /**
     * 
     * @param lat
     * @param lon
     * @return
     */
    public static final Intent newIntent(double lat, double lon) {
      Intent intent = new Intent(ACTION_NAME);
      intent.putExtra(EXTRA_LATITUDE, lat);
      intent.putExtra(EXTRA_LONGITUDE, lon);

      return intent;
    }

    /**
     * 
     * @return
     */
    public static final IntentFilter newIntentFilter() {
      return new IntentFilter(ACTION_NAME);
    }

    // Suppress default constructor for noninstantiability.
    private NewLocation() {

    }
  }

  /**
   * 
   * @author
   * 
   */
  public static final class NewSnr {
    /**
     * 
     */
    public static final String ACTION_NAME = "fr.utc.nf33.ins.NEW_SNR";

    /**
     * 
     */
    public static final String EXTRA_SNR = "fr.utc.nf33.ins.SNR";

    /**
     * 
     * @param snr
     * @return
     */
    public static final Intent newIntent(float snr) {
      Intent intent = new Intent(ACTION_NAME);
      intent.putExtra(EXTRA_SNR, snr);

      return intent;
    }

    /**
     * 
     * @return
     */
    public static final IntentFilter newIntentFilter() {
      return new IntentFilter(ACTION_NAME);
    }

    // Suppress default constructor for noninstantiability.
    private NewSnr() {

    }
  }

  /**
   * 
   * @author
   * 
   */
  public static final class Transition {
    /**
     * 
     */
    public static final String ACTION_NAME = "fr.utc.nf33.ins.TRANSITION";

    /**
     * 
     */
    public static final String EXTRA_NEW_STATE = "fr.utc.nf33.ins.NEW_STATE";

    /**
     * 
     * @param newState
     * @return
     */
    public static final Intent newIntent(String newState) {
      Intent intent = new Intent(ACTION_NAME);
      intent.putExtra(EXTRA_NEW_STATE, newState);

      return intent;
    }

    /**
     * 
     * @return
     */
    public static final IntentFilter newIntentFilter() {
      return new IntentFilter(ACTION_NAME);
    }

    // Suppress default constructor for noninstantiability.
    private Transition() {

    }
  }

  // Suppress default constructor for noninstantiability.
  private LocationIntent() {

  }
}
