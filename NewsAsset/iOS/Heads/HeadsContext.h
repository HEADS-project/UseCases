//
//  HeadsContext.h
//  Heads
//
//  Created by Kostas Giannakakis on 5/9/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "User.h"

@interface HeadsContext : NSObject

+(id)sharedInstance;

@property (nonatomic, strong) User* user;
@property (nonatomic, strong) NSMutableDictionary *tags;

@end
