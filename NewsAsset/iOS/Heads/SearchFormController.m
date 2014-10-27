//
//  SearchFormController.m
//  Heads
//
//  Created by Kostas Giannakakis on 5/15/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "SearchFormController.h"
#import "SearchResultsPTVC.h"
#import "TagSelectViewController.h"
#import "HeadsContext.h"

@interface SearchFormController() <UIActionSheetDelegate>

@property (weak, nonatomic) IBOutlet UILabel *range;
@property (nonatomic) NSArray *ranges;
@property (nonatomic) int selectedRange;

@end

@implementation SearchFormController


-(NSArray *) ranges
{
    if (!_ranges)
    {
        _ranges = @[@"1", @"2", @"5", @"10", @"100"];
    }
    return _ranges;
}

- (void) viewDidLoad
{
    [super viewDidLoad];
    self.selectedRange = 1;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"Search"])
    {
        if ([segue.destinationViewController isKindOfClass:[SearchResultsPTVC class]])
        {
            SearchResultsPTVC *srVC = (SearchResultsPTVC *)segue.destinationViewController;
            
            srVC.tags = @[@15];
            srVC.title = [self.pointTitle text];
            srVC.description = [self.pointDescription text];
            srVC.longitude = [[NSNumber alloc] initWithDouble:self.location.coordinate.longitude];
            srVC.latitude = [[NSNumber alloc] initWithDouble:self.location.coordinate.latitude];
            srVC.radius = [[NSNumber alloc] initWithInt:self.selectedRange];
        }
    }
    else
    {
        [super prepareForSegue:segue sender:sender];
    }
}

- (BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(id)sender
{
    if ([identifier isEqualToString:@"Search"])
    {
        /*BOOL segueShouldOccur = [self.tagIds count] > 0;
        if (!segueShouldOccur) {
            UIAlertView *notPermitted = [[UIAlertView alloc]
                                         initWithTitle:@"Warning"
                                         message:@"Please select at least one tag"
                                         delegate:nil
                                         cancelButtonTitle:@"OK"
                                         otherButtonTitles:nil];
            [notPermitted show];
            return NO;
        }*/
    }
    return YES;
}

#pragma mark - UITableViewDelegate

- (void) tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 1 && indexPath.row == 1)
    {
        UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:@"Select range"
                                                                 delegate:self
                                                        cancelButtonTitle:nil
                                                   destructiveButtonTitle:nil
                                                        otherButtonTitles:nil];
        for(id r in self.ranges)
        {
            [actionSheet addButtonWithTitle:r];
        }
        [actionSheet addButtonWithTitle:@"Cancel"]; // put at bottom (don't do at all on iPad)
        
        [actionSheet showInView:self.view]; // different on iPad
    }
}

#pragma mark - UIActionSheetDelegate

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex < [self.ranges count]) {
        self.range.text = [NSString stringWithFormat:@"Range: %@Km", self.ranges[buttonIndex]];
        self.selectedRange = [self.ranges[buttonIndex] intValue];
    }
}


@end
