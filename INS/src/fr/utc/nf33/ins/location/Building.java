/**
 * 
 */
package fr.utc.nf33.ins.location;

import java.util.List;

/**
 * 
 * @author
 * 
 */
public final class Building {
  //
  private final List<EntryPoint> mEntryPoints;
  //
  private final String mName;

  /**
   * 
   * @param name
   * @param entryPoints
   */
  public Building(String name, List<EntryPoint> entryPoints) {
    mName = name;
    mEntryPoints = entryPoints;
  }

  @Override
  public final boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Building other = (Building) obj;
    if (mEntryPoints == null) {
      if (other.mEntryPoints != null) return false;
    } else if (!mEntryPoints.equals(other.mEntryPoints)) return false;
    if (mName == null) {
      if (other.mName != null) return false;
    } else if (!mName.equals(other.mName)) return false;
    return true;
  }

  /**
   * 
   * @return
   */
  public final List<EntryPoint> getEntryPoints() {
    return mEntryPoints;
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
    result = prime * result + ((mEntryPoints == null) ? 0 : mEntryPoints.hashCode());
    result = prime * result + ((mName == null) ? 0 : mName.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "Building [mName=" + mName + ", mEntryPoints=" + mEntryPoints + "]";
  }
}
