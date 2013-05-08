/**
 * 
 */
package fr.utc.nf33.ins.db;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * 
 * @author
 * 
 */
public class InsDbHelper extends SQLiteAssetHelper {
  //
  private static final String DATABASE_NAME = "ins";
  //
  private static final int DATABASE_VERSION = 1;

  /**
   * 
   * @param context
   */
  public InsDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);

    setForcedUpgradeVersion(DATABASE_VERSION);
  }
}
