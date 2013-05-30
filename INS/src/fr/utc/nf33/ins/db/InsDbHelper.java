/**
 * 
 */
package fr.utc.nf33.ins.db;

import android.content.Context;
import android.database.Cursor;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import fr.utc.nf33.ins.location.LocationHelper;

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
   * @param latitude
   * @param longitude
   * @return
   */
  public final Cursor getEntryPoints(double latitude, double longitude) {
    LocationHelper.DistancesApproximator distApp = LocationHelper.DEFAULT_DISTANCES_APPROXIMATOR;
    double latDiff = distApp.latitudeDifference(LocationHelper.MAX_DISTANCE, latitude, longitude);
    double lonDiff = distApp.longitudeDifference(LocationHelper.MAX_DISTANCE, latitude, longitude);

    double top = latitude + latDiff;
    if (top > 90.0) top = 90.0;
    double bottom = latitude - latDiff;
    if (bottom < -90.0) bottom = -90.0;
    double right = longitude + lonDiff;
    if (right > 180.0) right = 180.0;
    double left = longitude - lonDiff;
    if (left < -180.0) left = -180.0;

    StringBuilder sqlSb = new StringBuilder();
    sqlSb
        .append(
            "SELECT b.idBuilding AS _id, b.name AS bName, ep.Name AS epName, ep.floor AS epFloor, ep.latitude AS epLatitude, ep.longitude AS epLongitude FROM Building b, EntryPoint ep WHERE b.idBuilding = ep.Building_idBuilding")
        .append(" AND epLatitude <= ").append(top).append(" AND epLatitude >= ").append(bottom)
        .append(" AND epLongitude <= ").append(right).append(" AND epLongitude >= ").append(left)
        .append(" ORDER BY _id");

    return getReadableDatabase().rawQuery(sqlSb.toString(), null);
  }
}
