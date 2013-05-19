/**
 * 
 */
package fr.utc.nf33.ins.db;

import android.content.Context;
import android.database.Cursor;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * 
 * @author
 * 
 */
public final class InsDbHelper extends SQLiteAssetHelper {
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

  /**
   * 
   * @return
   */
  public final Cursor getEntryPoints() {
    return getReadableDatabase()
        .rawQuery(
            "SELECT b.idBuilding AS _id, b.name AS bName, ep.Name AS epName, ep.floor AS epFloor, ep.latitude AS epLatitude, ep.longitude AS epLongitude FROM Building b, EntryPoint ep WHERE b.idBuilding = ep.Building_idBuilding",
            null);
  }
}
