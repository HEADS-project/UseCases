//
//  SearchResultsMapViewController.m
//  Heads
//
//  Created by Kostas Giannakakis on 5/19/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "SearchResultsMapViewController.h"
#import "PackageViewController.h"
#import <MapKit/MapKit.h>
#import <SDWebImage/SDWebImageManager.h>
#import "HeadsPoint+Annotation.h"

@interface SearchResultsMapViewController () <MKMapViewDelegate>

@property (weak, nonatomic) IBOutlet MKMapView *mapView;

@end

@implementation SearchResultsMapViewController

- (void) viewDidLoad
{
    [super viewDidLoad];
    self.mapView.delegate = self;
    
    [self updateAnnotations];
}

- (void) setPackages:(NSMutableArray *) packages
{
    _packages = packages;
    
    [self updateAnnotations];
}

- (void) updateAnnotations
{
    [self.mapView removeAnnotations:self.mapView.annotations];
    
    BOOL isRegionSet = NO;
    for(HeadsPoint *package in self.packages)
    {        
        [self.mapView addAnnotation:package];
        
        if (!isRegionSet)
        {
            isRegionSet = YES;
            MKCoordinateRegion region = MKCoordinateRegionMakeWithDistance(package.coordinate, 5000, 5000);
            [self.mapView setRegion:region];
        }
    }
}

- (MKAnnotationView *) mapView:(MKMapView *)mapView viewForAnnotation:(id<MKAnnotation>)annotation
{
    MKAnnotationView *annotationView = [[MKPinAnnotationView alloc] initWithAnnotation:annotation
                                                                       reuseIdentifier:@"loc"];
    annotationView.canShowCallout = YES;
    annotationView.rightCalloutAccessoryView = [UIButton buttonWithType:UIButtonTypeInfoLight];
    
    __block UIImageView *imageView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 46, 46)];
    imageView.image = [UIImage imageNamed:@"placeholder"];
    annotationView.leftCalloutAccessoryView = imageView;
        
    SDWebImageManager *manager = [SDWebImageManager sharedManager];
    
    [manager downloadImageWithURL:[[NSURL alloc] initWithString:((HeadsPoint *) annotation).thumbnailUrl]
                          options:0
                         progress:^(NSInteger receivedSize, NSInteger expectedSize) { }
                        completed:^(UIImage *image, NSError *error, SDImageCacheType cacheType, BOOL finished, NSURL *imageURL) {
                            if (image)
                            {
                                imageView.image = image;
                            }
                        }];
    
    return annotationView;
}

- (void)mapView:(MKMapView *)mapView annotationView:(MKAnnotationView *)view calloutAccessoryControlTapped:(UIControl *)control
{
    [self performSegueWithIdentifier:@"Package Select" sender:view.annotation];
}

- (void) prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"Package Select"])
    {
        if ([segue.destinationViewController isKindOfClass:[PackageViewController class]])
        {
            PackageViewController *packageViewController = (PackageViewController *) segue.destinationViewController;
            packageViewController.package = sender;
        }
    }
}

@end
