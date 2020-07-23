package org.apache.cordova.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class echoes a string called from JavaScript.
 */
public class Backlight extends CordovaPlugin {
  private final String TAG = Backlight.class.getSimpleName();
  private final String BACKLIGHT_FILE_PATH = "/sys/class/disp/disp/attr/lcd";

  // Prevent turning off backlight when app is in background mode
  private boolean active = false;
  private boolean on = true;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    this.on = this.isOn();

    Log.d(TAG, "is backlight on? " + this.on);
  }

  @Override
  public void onResume(boolean multitasking) {
    super.onResume(multitasking);
    this.active = true;

    if (!this.on) {
      this.off(null);
    }
  }

  @Override
  public void onPause(boolean multitasking) {
    this.active = false;

    if (!this.on) {
      this.on(null);
      // remember when resume
      this.on = false;
    }

    super.onPause(multitasking);
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals("on")) {
      this.on(callbackContext);
      return true;
    }
    else if (action.equals("off")) {
      this.off(callbackContext);
      return true;
    }
    else {
      return false;
    }
  }

  public void on(CallbackContext callbackContext) {
    this.update((byte) '0', callbackContext);
  }

  public void off(CallbackContext callbackContext) {
    // do not turn off when app is in background
    if (this.active) {
      this.update((byte) '1', callbackContext);
    }
  }

  private boolean isOn() {
    FileInputStream fis = null;

    try {
      File file = new File(BACKLIGHT_FILE_PATH);
      int length = (int) file.length();
      byte[] bytes = new byte[length];
      fis = new FileInputStream(file);
      fis.read(bytes);

      if (bytes[0] == (byte) '0') {
        return true;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      Log.e(TAG, e.getMessage());
    }
    finally {
      try {
        fis.close();
      }
      catch (Exception ignored) {
          // ignore exceptions generated by close()
      }
    }

    return false;
  }

  private void update(Byte aByte, CallbackContext callbackContext) {
    FileOutputStream os = null;
    try {
      os = new FileOutputStream(BACKLIGHT_FILE_PATH);
      os.write(aByte);
      os.flush();
      os.close();
      if (aByte == '0') {
        Log.d(TAG, "Backlight on");
        on = true;
      }
      else {
        on = false;
        Log.d(TAG, "Backlight off");
      }
      if (callbackContext != null) {
        callbackContext.success();
      }
    }
    catch (FileNotFoundException e) {
      Log.e(TAG, e.getMessage());
      if (callbackContext != null) {
        callbackContext.error(e.getMessage());
      }
    }
    catch (IOException e) {
      Log.e(TAG, e.getMessage());
      if (callbackContext != null) {
        callbackContext.error(e.getMessage());
      }
    }
    finally {
      try {
        os.close();
      }
      catch (Exception ignored) {
          // ignore exceptions generated by close()
      }
    }
  }
}
