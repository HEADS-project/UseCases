//
//  UserPackagesViewController.m
//  Heads
//
//  Created by Kostas Giannakakis on 5/13/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//
#import "UserPackagesViewController.h"
#import "HeadsClient.h"
#import "HeadsContext.h"
#import "HeadsPoint.h"


@implementation UserPackagesViewController 

- (void) loadData
{
    [self.packages removeAllObjects];
    
    HeadsContext *context = [HeadsContext sharedInstance];
    [self.headsClient requestUserPackages:context.user.userId];
    [self.refreshControl beginRefreshing];
}

@end
