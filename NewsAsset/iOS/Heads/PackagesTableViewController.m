//
//  PackagesTableViewController.m
//  Heads
//
//  Created by Kostas Giannakakis on 5/15/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "PackagesTableViewController.h"
#import "SearchResultsMapViewController.h"
#import "PackageViewController.h"

#import <SDWebImage/UIImageView+WebCache.h>
#import "UserPackagesViewController.h"
#import "HeadsContext.h"
#import "HeadsPoint.h"


@implementation PackagesTableViewController

#pragma mark - Properties

- (HeadsClient *) headsClient
{
    if (!_headsClient) {
        HeadsContext *context = [HeadsContext sharedInstance];
        _headsClient = [[HeadsClient alloc] initWithAccessToken:context.user.accessToken];
        _headsClient.clientDelegate = self;
    }
    return _headsClient;
}

- (NSMutableArray *) packages
{
    if (!_packages)
    {
        _packages = [[NSMutableArray alloc] init];
    }
    return _packages;
}

#pragma mark - Lifecycle

- (void) viewDidLoad
{
    [super viewDidLoad];
    self.edgesForExtendedLayout = UIRectEdgeNone;
    self.extendedLayoutIncludesOpaqueBars = NO;
    self.automaticallyAdjustsScrollViewInsets = NO;
    [self loadData];
}

- (IBAction)refresh:(UIRefreshControl *)sender
{
    [self loadData];
}

- (void) loadData
{
    [self.packages removeAllObjects];
}

- (void) prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"Show Map"])
    {
        if ([segue.destinationViewController isKindOfClass:[SearchResultsMapViewController class]])
        {
            SearchResultsMapViewController *srmvc = (SearchResultsMapViewController *)
            segue.destinationViewController;
            srmvc.packages = self.packages;
        }
    }
    else if ([segue.identifier isEqualToString:@"Package Select"])
    {
        if ([segue.destinationViewController isKindOfClass:[PackageViewController class]])
        {
            PackageViewController *packageViewController = (PackageViewController *)
            segue.destinationViewController;
            NSIndexPath *path = [self.tableView indexPathForSelectedRow];
            packageViewController.package = [self.packages objectAtIndex:path.row];
        }
    }
}

#pragma mark - UITableViewDataSource

- (NSInteger) tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.packages count];
}

- (UITableViewCell *) tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *cellIdentifier = @"User Package Cell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    HeadsPoint *package = [self.packages objectAtIndex:[indexPath row]];
    cell.textLabel.text = package.title;
    
    HeadsContext *context = [HeadsContext sharedInstance];
    NSMutableDictionary *tags = context.tags;
    NSMutableArray *packageTagNames = [[NSMutableArray alloc] init];
    
    for(NSNumber *tagId in package.tags)
    {
        [packageTagNames addObject:[tags objectForKey:tagId]];
    }
    
    cell.detailTextLabel.text = [packageTagNames componentsJoinedByString:@","];
    cell.imageView.image = [UIImage imageNamed:@"placeholder"];
    
    [cell.imageView sd_setImageWithURL:[NSURL URLWithString:package.thumbnailUrl]
                   placeholderImage:[UIImage imageNamed:@"placeholder"]];
    
    return cell;
}

- (BOOL) tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    return YES;
}

- (UITableViewCellEditingStyle) tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return UITableViewCellEditingStyleDelete;
}

- (void) tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete)
    {
        NSString *packageId = [self.packages[[indexPath row]] packageId];
        [self.packages removeObjectAtIndex:[indexPath row]];
        // Server-side deletion
        [self.headsClient deletePackageWithId:packageId];
        
        // Remove from table
        [self.tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }
}
#pragma  mark - HeadsClientDelegate

- (void) packagesListReceived:(NSMutableArray *)packages
{
    for(HeadsPoint *package in packages)
    {
        [self.packages addObject:package];
    }
    [self.tableView reloadData];
    [self.refreshControl endRefreshing];
}

- (void) deleteCompleted
{
    
}

- (void) errorDidOccur:(NSString *)errorMessage
{
    [self.refreshControl endRefreshing];
}


@end