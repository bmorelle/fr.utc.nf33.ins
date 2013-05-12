package fr.utc.nf33.ins;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

public class IndoorActivity extends Activity {
  
  @SuppressLint("NewApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_indoor);
  }
  
  @Override
  protected void onStop() {
    super.onStop();
  }

}
