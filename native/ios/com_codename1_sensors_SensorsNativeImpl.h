#import <Foundation/Foundation.h>

@interface com_codename1_sensors_SensorsNativeImpl : NSObject {
}

-(BOOL)initSensor:(int)param;
-(void)deregisterListener:(int)param;
-(void)registerListener:(int)param;
-(BOOL)isSupported;
-(int)getLongInterval:(int)type;
-(int)getShortInterval:(int)type;
-(void)setInterval:(int)type param1:(int)delayMicroSeconds;
-(BOOL)useEllipseWorkaround;

@end
