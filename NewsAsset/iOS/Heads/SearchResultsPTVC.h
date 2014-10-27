//
//  SearcResultsPTVC.h
//  Heads
//
//  Created by Kostas Giannakakis on 5/15/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "PackagesTableViewController.h"

@interface SearchResultsPTVC : PackagesTableViewController

@property (strong, nonatomic) NSArray *tags;
@property (strong, nonatomic) NSString *title;
@property (strong, nonatomic) NSString *description;
@property (nonatomic) NSNumber *latitude;
@property (nonatomic) NSNumber *longitude;
@property (nonatomic) NSNumber *radius;

@end
