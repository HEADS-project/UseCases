//
//  HeadsPackage+Annotation.m
//  Heads
//
//  Created by Kostas Giannakakis on 5/20/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "HeadsPoint+Annotation.h"
#import "HeadsContext.h"

@implementation HeadsPoint (Annotation)

- (CLLocationCoordinate2D)coordinate
{
    CLLocationCoordinate2D coordinate;
    
    coordinate.latitude = self.latitude;
    coordinate.longitude = self.longitude;
    
    return coordinate;
}

- (NSString *) subtitle
{
    HeadsContext *context = [HeadsContext sharedInstance];
    NSMutableDictionary *tags = context.tags;
    
    NSMutableArray *packageTagNames = [[NSMutableArray alloc] init];
    for(NSNumber *tagId in self.tags)
    {
        [packageTagNames addObject:[tags objectForKey:tagId]];
    }
    
    return [packageTagNames componentsJoinedByString:@","];
}


@end
