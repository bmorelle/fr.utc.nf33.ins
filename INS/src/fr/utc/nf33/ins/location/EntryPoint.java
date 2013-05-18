/**
 * 
 */
package fr.utc.nf33.ins.location;

/**
 * 
 * @author
 * 
 */
public final class EntryPoint {
  //
  private final byte mFloor;
  //
  private final double mLatitude;
  //
  private final double mLongitude;
  //
  private final String mName;

  /**
   * 
   * @param name
   * @param floor
   * @param latitude
   * @param longitude
   */
  public EntryPoint(String name, byte floor, double latitude, double longitude) {
    mName = name;
    mFloor = floor;
    mLatitude = latitude;
    mLongitude = longitude;
  }

  @Override
  public final boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    EntryPoint other = (EntryPoint) obj;
    if (mFloor != other.mFloor) return false;
    if (Double.doubleToLongBits(mLatitude) != Double.doubleToLongBits(other.mLatitude))
      return false;
    if (Double.doubleToLongBits(mLongitude) != Double.doubleToLongBits(other.mLongitude))
      return false;
    if (mName == null) {
      if (other.mName != null) return false;
    } else if (!mName.equals(other.mName)) return false;
    return true;
  }

  /**
   * 
   * @return
   */
  public final byte getFloor() {
    return mFloor;
  }

  /**
   * 
   * @return
   */
  public final double getLatitude() {
    return mLatitude;
  }

  /**
   * 
   * @return
   */
  public final double getLongitude() {
    return mLongitude;
  }

  /**
   * 
   * @return
   */
  public final String getName() {
    return mName;
  }

  @Override
  public final int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + mFloor;
    long temp;
    temp = Double.doubleToLongBits(mLatitude);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(mLongitude);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((mName == null) ? 0 : mName.hashCode());
    return result;
  }

  @Override
  public final String toString() {
    return "EntryPoint [mName=" + mName + ", mFloor=" + mFloor + ", mLatitude=" + mLatitude
        + ", mLongitude=" + mLongitude + "]";
  }
}
