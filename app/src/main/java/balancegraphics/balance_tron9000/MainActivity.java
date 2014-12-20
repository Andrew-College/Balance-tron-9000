package balancegraphics.balance_tron9000;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Menu;


public class MainActivity extends Activity implements SensorEventListener {

   /**
    * Accelerometer
    */
   private SensorManager senSensorManager;
   private Sensor senAccelerometer;
   private long lastUpdate = 100l, updateTimeout = 0;


   /**
    * opengl
    */

   private int[] textures = {-1};
   private GLSurfaceView mSurfaceView;
   private GLSurfaceView mGLView;

   private Renderer renderer;

   /**
    * 1st dimension is states(previous, difference, current)
    * 2nd dimension is values
    * e.g.
    * {
    * {1,2,3},
    * {1,0,0},
    * {0,2,3}
    * }
    */
   float[][] tiltAcceleration = new float[3][3];

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      renderer = new Renderer(getApplicationContext(), this);
      //setup accelerometer
      senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
      senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

      if (hasGLES30()) {
         mGLView = new GLSurfaceView(this);
         mGLView.setEGLContextClientVersion(2);
         mGLView.setPreserveEGLContextOnPause(true);
         mGLView.setRenderer(renderer);
      } else {
         // Time to get a new phone, OpenGL ES 2.0 not
         // supported.
      }

      setContentView(mGLView);
   }

   private boolean hasGLES30() {
      ActivityManager am = (ActivityManager)
         getSystemService(Context.ACTIVITY_SERVICE);
      ConfigurationInfo info = am.getDeviceConfigurationInfo();
      return info.reqGlEsVersion >= 0x20000;
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
//      getMenuInflater().inflate(R.menu.menu_main, menu);
      return true;
   }

   @Override
   protected void onPause() {
      super.onPause();
      senSensorManager.unregisterListener(this);
      if (mSurfaceView != null) {
         mSurfaceView.onPause();
      }
   }

   @Override
   protected void onResume() {
      super.onResume();
      senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
      if (mSurfaceView != null) {
         mSurfaceView.onResume();
      }
   }

   @Override
   public void onSensorChanged(SensorEvent event) {
      Sensor mySensor = event.sensor;

      if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
         long curTime = System.currentTimeMillis();

//         if ((curTime - lastUpdate) > updateTimeout) {

            //previous
            tiltAcceleration[0][0] = tiltAcceleration[2][0];
            tiltAcceleration[0][1] = tiltAcceleration[2][1];
            tiltAcceleration[0][2] = tiltAcceleration[2][2];
            //current
            tiltAcceleration[2][0] = event.values[0];
            tiltAcceleration[2][1] = event.values[1];
            tiltAcceleration[2][2] = event.values[2];
            //difference
            tiltAcceleration[1][0] = tiltAcceleration[0][0] - tiltAcceleration[2][0];
            tiltAcceleration[1][1] = tiltAcceleration[0][1] - tiltAcceleration[2][1];
            tiltAcceleration[1][2] = tiltAcceleration[0][2] - tiltAcceleration[2][2];

            lastUpdate = curTime;

//            renderer.setCoords(event.values);
            renderer.setCoords(tiltAcceleration[1]);

//           System.out.println("X" + tiltAcceleration[2][0] + "Y" + tiltAcceleration[2][1] + "Z" + tiltAcceleration[2][2]);

//         }
      }
   }

   @Override
   public void onAccuracyChanged(Sensor sensor, int accuracy) {

   }

}
