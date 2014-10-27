//
//  PackagesTableViewController.h
//  Heads
//
//  Created by Kostas Giannakakis on 5/15/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HeadsClient.h"

@interface PackagesTableViewController : UITableViewController <HeadsClientDelegate>

@property (strong, nonatomic) HeadsClient *headsClient;
@property (strong, nonatomic) NSMutableArray *packages;

- (void) loadData;
- (IBAction)refresh:(UIRefreshControl *)sender;

@end
