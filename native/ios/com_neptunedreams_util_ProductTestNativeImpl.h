#import <Foundation/Foundation.h>

@interface com_neptunedreams_util_ProductTestNativeImpl : NSObject {
}

-(NSString*)getPurchaseTestId;
-(NSString*)getUnavailableTestId;
-(NSString*)getCanceledTestId;
-(NSString*)getRefundedTestId;
-(BOOL)isSupported;
@end
