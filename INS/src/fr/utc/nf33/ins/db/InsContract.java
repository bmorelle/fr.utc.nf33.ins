/**
 * 
 */
package fr.utc.nf33.ins.db;

import android.provider.BaseColumns;

/**
 * 
 * @author
 * 
 */
public final class InsContract {
  /**
   * 
   * @author
   * 
   */
  public static abstract class Building implements BaseColumns {
    /**
     * 
     */
    public static final String COLUMN_NAME_ID_BUILDING = "idBuilding";
    /**
     * 
     */
    public static final String COLUMN_NAME_MAP_PATH = "mapPath";
    /**
     * 
     */
    public static final String COLUMN_NAME_NAME = "name";
    /**
     * 
     */
    public static final String TABLE_NAME = "Building";
  }

  /**
   * 
   * @author
   * 
   */
  public static abstract class EntryPoint implements BaseColumns {
    /**
     * 
     */
    public static final String COLUMN_NAME_BUILDING_ID_BUILDING = "Building_idBuilding";
    /**
     * 
     */
    public static final String COLUMN_NAME_FLOOR = "floor";
    /**
     * 
     */
    public static final String COLUMN_NAME_ID_ENTRY_POINT = "idEntryPoint";
    /**
     * 
     */
    public static final String COLUMN_NAME_LATITUDE = "latitude";
    /**
     * 
     */
    public static final String COLUMN_NAME_LONGITUDE = "longitude";
    /**
     * 
     */
    public static final String COLUMN_NAME_NAME = "name";
    /**
     * 
     */
    public static final String TABLE_NAME = "EntryPoint";
  }

  //
  private InsContract() {

  }
}
