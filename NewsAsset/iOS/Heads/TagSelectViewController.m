//
//  TagSelectViewController.m
//  Heads
//
//  Created by Kostas Giannakakis on 5/16/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "TagSelectViewController.h"
#import "HeadsContext.h"

@interface TagSelectViewController ()

@property (strong, nonatomic) NSArray *tags;
@property (strong, nonatomic) NSDictionary *tagNameToTagIdMap;

@end


@implementation TagSelectViewController


- (void) setSelectedTagIds:(NSMutableArray *) selectedTagIds
{
    _selectedTagIds = selectedTagIds;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    
    HeadsContext *context = [HeadsContext sharedInstance];
    NSMutableDictionary *tagsDict = context.tags;
    self.tags = [[NSArray alloc] initWithArray:[tagsDict allValues]];
    self.tags = [_tags sortedArrayUsingSelector:@selector(localizedCaseInsensitiveCompare:)];
    
    self.tagNameToTagIdMap = [[NSMutableDictionary alloc] init];
    
    for(NSNumber *tagId in [tagsDict allKeys])
    {
        [self.tagNameToTagIdMap setValue:tagId forKey:[tagsDict objectForKey:tagId]];
    }
}

#pragma mark - UITableViewDelegate

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView cellForRowAtIndexPath:indexPath];
    
    NSString *tagName = [self.tags objectAtIndex:[indexPath row]];
    NSNumber *tagId = [self.tagNameToTagIdMap objectForKey:tagName];
    if(cell.accessoryType == UITableViewCellAccessoryNone)
    {
        cell.accessoryType = UITableViewCellAccessoryCheckmark;
        [self.selectedTagIds addObject:tagId];
    }
    else
    {
        cell.accessoryType = UITableViewCellAccessoryNone;
        [self.selectedTagIds removeObject:tagId];
    }
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

#pragma mark - UITableViewDataSource

- (NSInteger) tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.tags count];
}

- (UITableViewCell *) tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *cellIdentifier = @"Tag Cell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    
    NSString *tagName = [self.tags objectAtIndex:[indexPath row]];
    NSNumber *tagId = [self.tagNameToTagIdMap objectForKey:tagName];
    cell.textLabel.text = tagName;
    
    if([self.selectedTagIds containsObject:tagId])
    {
        cell.accessoryType = UITableViewCellAccessoryCheckmark;
    }
    else
    {
        cell.accessoryType = UITableViewCellAccessoryNone;
    }
    
    return cell;
}

@end
