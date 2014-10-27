//
//  HeadsPoint.h
//  Heads
//
//  Created by Kostas Giannakakis on 5/13/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HeadsPoint : NSObject

@property (nonatomic) double latitude;
@property (nonatomic) double longitude;
@property (nonatomic) long date;
@property (nonatomic, strong) NSString *comments;
@property (nonatomic, strong) NSString *title;
@property (nonatomic, strong) NSArray *tags;
@property (nonatomic, strong) NSString *packageId;
@property (nonatomic, strong) NSString *thumbnailUrl;
@property (nonatomic, strong) NSString *imageUrl;

@end
