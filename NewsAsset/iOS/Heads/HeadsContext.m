//
//  HeadsContext.m
//  Heads
//
//  Created by Kostas Giannakakis on 5/9/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "HeadsContext.h"

@implementation HeadsContext

-(NSMutableDictionary *) tags
{
    if (!_tags)
    {
        _tags = [[NSMutableDictionary alloc] init];
    }
    return _tags;
}


+(id)sharedInstance
{
    static dispatch_once_t pred;
    static HeadsContext *sharedInstance = nil;
    dispatch_once(&pred, ^{
        sharedInstance = [[HeadsContext alloc] init];
    });
    return sharedInstance;
}

- (void)dealloc
{
    // implement -dealloc & remove abort() when refactoring for
    // non-singleton use.
    abort();
}


@end
