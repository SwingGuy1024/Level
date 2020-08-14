/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.sensors;

import com.codename1.system.NativeInterface;
import com.neptunedreams.util.NotNull;

/**
 *
 * @author Chen
 */
public interface SensorsNative extends NativeInterface {

	boolean initSensor(int type);
	
	void registerListener(int type);
	
	void deregisterListener(int type);
	
	//    float getResolution(int type);
	
	//    @NotNull
	//    String getStringType(int type);
	
	int getLongInterval(int type);
	
	int getShortInterval(int type);
	
	void setInterval(int type, int delayMicroSeconds);
	
	boolean useEllipseWorkaround();

}
