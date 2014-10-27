//
//  LocationSelectController.h
//  Heads
//
//  Created by Kostas Giannakakis on 5/21/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreLocation/CoreLocation.h>

@interface LocationSelectController : UIViewController

@property (nonatomic, weak) CLLocation *location;
@property (nonatomic, strong) CLLocation *outLocation;

@end
