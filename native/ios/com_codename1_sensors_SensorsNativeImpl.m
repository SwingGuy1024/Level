#import "com_codename1_sensors_SensorsNativeImpl.h"
#import "com_codename1_sensors_SensorsManager.h"
#import <CoreMotion/CoreMotion.h>

@implementation com_codename1_sensors_SensorsNativeImpl

CMMotionManager *motionManager = nil;
const int GYRO = 1001;
const int ACCEL = 1002;
const int MAGNET = 1003;


-(BOOL) initSensor : (int) param {
    if(motionManager == nil) {
        motionManager = [[CMMotionManager alloc] init];
    }
    if (param == GYRO) {
        motionManager.gyroUpdateInterval = 0.2;
    } else if (param == ACCEL) {
        motionManager.accelerometerUpdateInterval = 0.2;
    } else if (param == MAGNET) {
        motionManager.magnetometerUpdateInterval = 0.2;
    } else {
        return NO;
    }
    return YES;
}

-(void) deregisterListener : (int) param {
    
    if (param == GYRO) {
        [motionManager stopGyroUpdates];
    } else if (param == ACCEL) {
        [motionManager stopAccelerometerUpdates];
    } else if (param == MAGNET) {
        [motionManager stopMagnetometerUpdates];
    }
}

//-(float)getResolution : (int) type {
//    return 0; // TO BE WRITTEN!
//}

//-(NSString*)getStringType{
////    if(motionManager == nil){
////        motionManager = [[CMMotionManager alloc] init];
////    }
//    return nil; // TO BE WRITTEN!
//}

-(void) registerListener : (int) param {
    if (param == GYRO) {
        if ([motionManager isGyroAvailable]) {
            NSOperationQueue *queue = [[NSOperationQueue alloc] init];
            
            [motionManager startGyroUpdatesToQueue : queue withHandler : ^(CMGyroData *gyroData, NSError * error) {
                
                dispatch_async(dispatch_get_main_queue(), ^ {
                    com_codename1_sensors_SensorsManager_onSensorChanged___int_float_float_float(CN1_THREAD_GET_STATE_PASS_ARG 1001,
                                                                                                 gyroData.rotationRate.x,
                                                                                                 gyroData.rotationRate.y,
                                                                                                 gyroData.rotationRate.z);
                });
            }
             ];
        }
        
    } else if (param == ACCEL) {
        if ([motionManager isAccelerometerAvailable]) {
            NSOperationQueue *queue = [[NSOperationQueue alloc] init];
            
            [motionManager startAccelerometerUpdatesToQueue : queue withHandler : ^(CMAccelerometerData *accelerometerData, NSError * error) {
                
                dispatch_async(dispatch_get_main_queue(), ^ {
                    com_codename1_sensors_SensorsManager_onSensorChanged___int_float_float_float(CN1_THREAD_GET_STATE_PASS_ARG 1002,
                                                                                                 accelerometerData.acceleration.x * -9.81,
                                                                                                 accelerometerData.acceleration.y * -9.81,
                                                                                                 accelerometerData.acceleration.z * -9.81);
                });
            }
             ];
        }
    } else if (param == MAGNET) {
        if ([motionManager isMagnetometerAvailable]) {
            NSOperationQueue *queue = [[NSOperationQueue alloc] init];
            
            [motionManager startMagnetometerUpdatesToQueue : queue withHandler : ^(CMMagnetometerData *magnetometerData, NSError * error) {
                
                dispatch_async(dispatch_get_main_queue(), ^ {
                    com_codename1_sensors_SensorsManager_onSensorChanged___int_float_float_float(CN1_THREAD_GET_STATE_PASS_ARG 1003,
                                                                                                 magnetometerData.magneticField.x,
                                                                                                 magnetometerData.magneticField.y,
                                                                                                 magnetometerData.magneticField.z);
                });
            }
             ];
        }
    }
    
}

-(BOOL) isSupported {
    return YES;
}

-(BOOL) useEllipseWorkaround {
    return YES;
}

-(int)getLongInterval:(int)type {
    return [self getShortInterval:type];
}

-(int)getShortInterval:(int)type {
    NSTimeInterval interval = 0;
    if (type == GYRO) {
        interval = motionManager.gyroUpdateInterval;
    } else if (type == ACCEL) {
        interval = motionManager.accelerometerUpdateInterval;
    } else if (type == MAGNET) {
        interval = motionManager.magnetometerUpdateInterval;
    }
    // Convert to microseconds.
    return (int)((interval * 1000000) + 0.5);
}

-(void)setInterval:(int)type param1:(int)delayMicroSeconds {
    // accelerometerUpdateInterval is in seconds.
    NSTimeInterval delaySeconds = delayMicroSeconds / 1000000.0;
    
    if (type == GYRO) {
        [motionManager setGyroUpdateInterval: delaySeconds];
    } else if (type == ACCEL) {
        [motionManager setAccelerometerUpdateInterval:delaySeconds];
    } else if (type == MAGNET) {
        [motionManager setMagnetometerUpdateInterval: delaySeconds];
    }
}

@end
