/**
 * 
 */
package fr.utc.nf33.ins.db;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

/**
 * 
 * @author
 * 
 */
public abstract class DbCursorLoader extends AsyncTaskLoader<Cursor> {
  //
  private Cursor mCursor;
  //
  private final ForceLoadContentObserver mObserver;

  /**
   * 
   * @param context
   */
  public DbCursorLoader(Context context) {
    super(context);
    mObserver = new ForceLoadContentObserver();
  }

  /* Runs on the UI thread */
  @Override
  public void deliverResult(Cursor cursor) {
    if (isReset()) {
      // An async query came in while the loader is stopped
      if (cursor != null) {
        cursor.close();
      }
      return;
    }
    Cursor oldCursor = mCursor;
    mCursor = cursor;

    if (isStarted()) {
      super.deliverResult(cursor);
    }

    if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
      oldCursor.close();
    }
  }

  /**
   * 
   * @return
   */
  protected abstract Cursor getCursor();

  /* Runs on a worker thread */
  @Override
  public Cursor loadInBackground() {
    Cursor cursor = getCursor();
    if (cursor != null) {
      // Ensure the cursor window is filled
      cursor.getCount();
      registerContentObserver(cursor, mObserver);
    }
    return cursor;
  }

  @Override
  public void onCanceled(Cursor cursor) {
    if (cursor != null && !cursor.isClosed()) {
      cursor.close();
    }
  }

  @Override
  protected void onReset() {
    super.onReset();

    // Ensure the loader is stopped
    onStopLoading();

    if (mCursor != null && !mCursor.isClosed()) {
      mCursor.close();
    }
    mCursor = null;
  }

  /**
   * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
   * will be called on the UI thread. If a previous load has been completed and is still valid the
   * result may be passed to the callbacks immediately.
   * 
   * Must be called from the UI thread
   */
  @Override
  protected void onStartLoading() {
    if (mCursor != null) {
      deliverResult(mCursor);
    }
    if (takeContentChanged() || mCursor == null) {
      forceLoad();
    }
  }

  /**
   * Must be called from the UI thread
   */
  @Override
  protected void onStopLoading() {
    // Attempt to cancel the current load task if possible.
    cancelLoad();
  }

  /**
   * Registers an observer to get notifications from the content provider when the cursor needs to
   * be refreshed.
   */
  @SuppressWarnings("unused")
  void registerContentObserver(Cursor cursor, ContentObserver observer) {
    cursor.registerContentObserver(mObserver);
  }
}
