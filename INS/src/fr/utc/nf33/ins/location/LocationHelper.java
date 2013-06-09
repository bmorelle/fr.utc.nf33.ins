/**
 * 
 */
package fr.utc.nf33.ins.location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.database.Cursor;

/**
 * 
 * @author
 * 
 */
public final class LocationHelper {
  /**
   * 
   * @author
   * 
   */
  public enum DistancesApproximator {
    /**
     * 
     */
    PLANAR {
      @Override
      public double latitudeDifference(double distance, double latitude, double longitude) {
        return (distance / (RADIUS_OF_THE_EARTH * 1000)) / DEGREES_TO_RADIANS;
      }

      @Override
      public double longitudeDifference(double distance, double latitude, double longitude) {
        double cosLat = Math.cos(latitude * DEGREES_TO_RADIANS);
        if (cosLat == 0.0) return 0.0;
        if (cosLat < 0.0) cosLat *= -1.0;

        return (distance / (RADIUS_OF_THE_EARTH * 1000 * cosLat)) / DEGREES_TO_RADIANS;
      }

      @Override
      public double squaredDistanceBetween(double startLatitude, double startLongitude,
          double endLatitude, double endLongitude) {
        double sLat = startLatitude * DEGREES_TO_RADIANS;
        double sLon = startLongitude * DEGREES_TO_RADIANS;
        double eLat = endLatitude * DEGREES_TO_RADIANS;
        double eLon = endLongitude * DEGREES_TO_RADIANS;

        double diffLat = eLat - sLat;
        double diffLon = eLon - sLon;
        double meanLat = (sLat + eLat) / 2.0;

        double a = Math.cos(meanLat) * diffLon;

        return SQUARED_RADIUS_OF_THE_EARTH * ((diffLat * diffLat) + (a * a)) * 1000;
      }
    };

    /**
     * 
     * @param distance
     * @param latitude
     * @param longitude
     * @return
     */
    public abstract double latitudeDifference(double distance, double latitude, double longitude);

    /**
     * 
     * @param distance
     * @param latitude
     * @param longitude
     * @return
     * @throws IllegalArgumentException
     */
    public abstract double longitudeDifference(double distance, double latitude, double longitude);

    /**
     * Computes the approximate squared distance in meters between two locations.
     * 
     * @param startLatitude the starting latitude
     * @param startLongitude the starting longitude
     * @param endLatitude the ending latitude
     * @param endLongitude the ending longitude
     * @return the computed squared distance
     */
    public abstract double squaredDistanceBetween(double startLatitude, double startLongitude,
        double endLatitude, double endLongitude);
  }

  /**
   * 
   * @author
   * 
   */
  public enum ShouldGoIndoorResult {
    /**
     * 
     */
    ASK_USER,
    /**
     * 
     */
    NO,
    /**
     * 
     */
    YES;
  }

  /**
   * 
   */
  public static final DistancesApproximator DEFAULT_DISTANCES_APPROXIMATOR =
      DistancesApproximator.PLANAR;
  //
  private static final double DEGREES_TO_RADIANS = Math.PI / 180.0;
  // SPECIFICATION : POS_081
  public static final double MAX_DISTANCE = 12.0;
  //
  private static final double RADIUS_OF_THE_EARTH = 6371.009;
  // SPECIFICATION : TRS_020
  public static final byte SNR_THRESHOLD = 33;
  //
  private static final double SQUARED_MAX_DISTANCE = MAX_DISTANCE * MAX_DISTANCE;
  //
  private static final double SQUARED_RADIUS_OF_THE_EARTH = RADIUS_OF_THE_EARTH
      * RADIUS_OF_THE_EARTH;

  //
  static final List<Building> getCloseBuildings(final double latitude, final double longitude,
      Cursor cursor) {
    List<Building> closeBuildings = new ArrayList<Building>();

    int _idIdx = cursor.getColumnIndexOrThrow("_id");
    int bNameIdx = cursor.getColumnIndexOrThrow("bName");
    int epNameIdx = cursor.getColumnIndexOrThrow("epName");
    int epFloorIdx = cursor.getColumnIndexOrThrow("epFloor");
    int epLatitudeIdx = cursor.getColumnIndexOrThrow("epLatitude");
    int epLongitudeIdx = cursor.getColumnIndexOrThrow("epLongitude");

    Building currentBuilding = null;
    int currentId = -1;
    while (cursor.moveToNext()) {
      double epLatitude = cursor.getDouble(epLatitudeIdx);
      double epLongitude = cursor.getDouble(epLongitudeIdx);
      double sqDist =
          DEFAULT_DISTANCES_APPROXIMATOR.squaredDistanceBetween(latitude, longitude, epLatitude,
              epLongitude);
      if (sqDist <= SQUARED_MAX_DISTANCE) {
        int id = cursor.getInt(_idIdx);
        if (currentId != id) {
          currentId = id;
          String bName = cursor.getString(bNameIdx);
          currentBuilding = new Building(bName, new ArrayList<EntryPoint>());
          closeBuildings.add(currentBuilding);
        }

        String epName = cursor.getString(epNameIdx);
        byte epFloor = (byte) cursor.getInt(epFloorIdx);
        currentBuilding.getEntryPoints().add(
            new EntryPoint(epName, epFloor, epLatitude, epLongitude));
      }
    }

    for (Building b : closeBuildings) {
      List<EntryPoint> entryPoints = b.getEntryPoints();
      Collections.sort(entryPoints, new Comparator<EntryPoint>() {
        @Override
        public final int compare(EntryPoint lhs, EntryPoint rhs) {
          double lhsSqDist =
              DEFAULT_DISTANCES_APPROXIMATOR.squaredDistanceBetween(latitude, longitude,
                  lhs.getLatitude(), lhs.getLongitude());
          double rhsSqDist =
              DEFAULT_DISTANCES_APPROXIMATOR.squaredDistanceBetween(latitude, longitude,
                  rhs.getLatitude(), rhs.getLongitude());
          if (lhsSqDist < rhsSqDist)
            return -1;
          else if (lhsSqDist == rhsSqDist)
            return 0;
          else
            return 1;
        }
      });
    }
    Collections.sort(closeBuildings, new Comparator<Building>() {
      @Override
      public final int compare(Building lhs, Building rhs) {
        double lhsSqDist =
            DEFAULT_DISTANCES_APPROXIMATOR.squaredDistanceBetween(latitude, longitude, lhs
                .getEntryPoints().get(0).getLatitude(), lhs.getEntryPoints().get(0).getLongitude());
        double rhsSqDist =
            DEFAULT_DISTANCES_APPROXIMATOR.squaredDistanceBetween(latitude, longitude, rhs
                .getEntryPoints().get(0).getLatitude(), rhs.getEntryPoints().get(0).getLongitude());
        if (lhsSqDist < rhsSqDist)
          return -1;
        else if (lhsSqDist == rhsSqDist)
          return 0;
        else
          return 1;
      }
    });

    return closeBuildings;
  }

  // SPECIFICATION : TRS_011, TRS_030
  /**
   * 
   * @param snr
   * @param closeBuildings
   * @return
   */
  public static final ShouldGoIndoorResult shouldGoIndoor(float snr, List<Building> closeBuildings) {
    if ((closeBuildings == null) || (closeBuildings.size() == 0) || (snr >= SNR_THRESHOLD))
      return ShouldGoIndoorResult.NO;
    else if ((closeBuildings.size() > 1) || (closeBuildings.get(0).getEntryPoints().size() > 1))
      return ShouldGoIndoorResult.ASK_USER;
    else
      return ShouldGoIndoorResult.YES;
  }

  /**
   * 
   * @param snr
   * @return
   */
  public static final boolean shouldGoOutdoor(float snr) {
    return snr >= SNR_THRESHOLD;
  }

  // Suppress default constructor for noninstantiability.
  private LocationHelper() {

  }
}
