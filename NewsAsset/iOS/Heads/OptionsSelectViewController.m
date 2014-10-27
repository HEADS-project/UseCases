//
//  OptionsSelectViewController.m
//  Heads
//
//  Created by Kostas Giannakakis on 5/21/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "OptionsSelectViewController.h"
#import "TagSelectViewController.h"
#import "LocationSelectController.h"
#import "HeadsContext.h"

@implementation OptionsSelectViewController

#pragma mark - Lifecycle

- (void) viewDidLoad
{
    [super viewDidLoad];
    
    self.tagIds = [[NSMutableArray alloc] init];
    
    self.locationManager = [[CLLocationManager alloc] init];
    self.locationManager.desiredAccuracy = kCLLocationAccuracyBest;
    self.locationManager.distanceFilter = 10.0f;
    self.locationManager.delegate = self;
    
    self.hasUserSelectedPointOnMap = false;
    
    self.geocoder = [[CLGeocoder alloc] init];
    
    // Hide keyboard when tap outside of textfield
    //[self.view addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self.view
    //                                                                        action:@selector(endEditing:)]];
}

- (void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    if (!self.hasUserSelectedPointOnMap)
    {
        [self.locationManager startUpdatingLocation];
    }
}

- (void) viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    [self.locationManager stopUpdatingLocation];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"Tag Select"])
    {
        TagSelectViewController *tagSelectVC;
        
        if ([segue.destinationViewController isKindOfClass:[TagSelectViewController class]])
        {
            tagSelectVC = (TagSelectViewController *)segue.destinationViewController;
        }
        else if ([segue.destinationViewController isKindOfClass:[UINavigationController class]])
        {
            tagSelectVC = (TagSelectViewController *) [segue.destinationViewController topViewController];
        }
        tagSelectVC.selectedTagIds = self.tagIds;
    }
    else if ([segue.identifier isEqualToString:@"Select Location"])
    {
        LocationSelectController *locationSelectController;
        
        if ([segue.destinationViewController isKindOfClass:[LocationSelectController class]])
        {
            locationSelectController = (LocationSelectController *)segue.destinationViewController;
        }
        else if ([segue.destinationViewController isKindOfClass:[UINavigationController class]])
        {
            locationSelectController = (LocationSelectController *) [segue.destinationViewController topViewController];
        }
        locationSelectController.location = self.location;
    }
}

- (IBAction)tagSelected:(UIStoryboardSegue *)segue
{    if ([self.tagIds count] == 0)
{
    self.tagsLabel.text = @"Tags";
}
else
{
    HeadsContext *context = [HeadsContext sharedInstance];
    NSMutableDictionary *tagsDict = context.tags;
    
    self.tagsLabel.text = [[tagsDict objectsForKeys:self.tagIds notFoundMarker:@"@"] componentsJoinedByString:@","];
}
}

- (IBAction)locationSelected:(UIStoryboardSegue *)segue
{
    self.hasUserSelectedPointOnMap = true;
    [self.locationManager stopUpdatingLocation];
    LocationSelectController *vc = (LocationSelectController *)segue.sourceViewController;
    self.location = vc.outLocation;
    [self updateLocationLabel];
}

#pragma mark - CLLocationManagerDelegate

- (void) locationManagerDidPauseLocationUpdates:(CLLocationManager *)manager
{
    self.locationLabel.text = @"Location unavailable";
}

- (void) locationManagerDidResumeLocationUpdates:(CLLocationManager *)manager
{
    
}

- (void) updateLocationLabel
{
    NSString *locationString = [NSString stringWithFormat:@"%f, %f",
                                self.location.coordinate.latitude,
                                self.location.coordinate.longitude];
    self.locationLabel.text = locationString;
    
    if (![self.geocoder isGeocoding])
    {
        [self.geocoder reverseGeocodeLocation:self.location
                            completionHandler:^(NSArray *placemarks, NSError *error) {
                                if (placemarks && [placemarks count] > 0 && error == nil)
                                {
                                    CLPlacemark *placemark = [placemarks lastObject];
                                    
                                    NSString *locationString = [NSString stringWithFormat:@"%@ %@, %@",
                                                                placemark.name,
                                                                placemark.subAdministrativeArea,
                                                                placemark.administrativeArea];
                                    
                                    self.locationLabel.text = locationString;
                                }
                            }];
    }
}

- (void) locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations
{
    if (!self.hasUserSelectedPointOnMap)
    {
        self.location = [locations lastObject];
        [self updateLocationLabel];
    }
}

@end
