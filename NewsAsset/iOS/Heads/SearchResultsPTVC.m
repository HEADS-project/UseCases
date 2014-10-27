//
//  SearcResultsPTVC.m
//  Heads
//
//  Created by Kostas Giannakakis on 5/15/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "SearchResultsPTVC.h"

@implementation SearchResultsPTVC

- (void) loadData
{
    [self.packages removeAllObjects];
    
    [self.headsClient searchWithTitle:self.title
                      withDescription:self.description
                         withLatitude:[self.latitude doubleValue]
                        withLongitude:[self.longitude doubleValue]
                           withRadius:[self.radius doubleValue]];
    
    [self.refreshControl beginRefreshing];
}

- (BOOL) tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    return NO;
}

@end
