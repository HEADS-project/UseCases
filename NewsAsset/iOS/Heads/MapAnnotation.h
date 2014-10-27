//
//  MapAnnotation.h
//  Heads
//
//  Created by Kostas Giannakakis on 5/21/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>

@interface MapAnnotation : NSObject <MKAnnotation>{
    
    CLLocationCoordinate2D coordinate;
    
}
- (id)initWithCoordinate:(CLLocationCoordinate2D)coord;
- (void)setCoordinate:(CLLocationCoordinate2D)newCoordinate;
@end