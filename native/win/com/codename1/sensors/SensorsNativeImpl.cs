namespace com.codename1.sensors{

  using System;
  using System.Windows;
  
  public class SensorsNativeImpl {
    public void registerListener(int param) { }
 
    public float getResolution(int param) {
      return 0;
    }
 
    public bool initSensor(int param) {
      return false;
    }
 
    public String  getStringType(int param) {
      return null;
    }
 
    public void deregisterListener(int param) { }
 
    public bool isSupported() {
      return false;
    }

    /**
     * Returns the long interval in microseconds
     * @return The long interval in microseconds
     */
    public int getLongInterval(int type) {
      return 0;
    }
    
    /**
     * Returns the short interval in microseconds
     * @return The short interval in microseconds
     */
    public int getShortInterval(int type) {
      return 0;
    }

    public void setInterval(int type, int delayMicroSeconds) {
        // to be written
    }
  }
}
