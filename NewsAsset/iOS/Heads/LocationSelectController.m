//
//  LocationSelectController.m
//  Heads
//
//  Created by Kostas Giannakakis on 5/21/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "LocationSelectController.h"
#import <MapKit/MapKit.h>
#import "MapAnnotation.h"

@interface LocationSelectController () <MKMapViewDelegate>

@property (weak, nonatomic) IBOutlet MKMapView *mapView;

@end

@implementation LocationSelectController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    
    CLLocationCoordinate2D coordinate = CLLocationCoordinate2DMake(self.location.coordinate.latitude,
                                                                   self.location.coordinate.longitude);
    MKCoordinateRegion region = MKCoordinateRegionMakeWithDistance(coordinate, 3000, 3000);
    
    [self.mapView setRegion:region];
    self.mapView.delegate = self;
    
    MapAnnotation *annotation = [[MapAnnotation alloc] initWithCoordinate:coordinate];
    [self.mapView addAnnotation:annotation];
}


- (MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id<MKAnnotation>)annotation
{
    if ([annotation isKindOfClass:[MKUserLocation class]])
        return nil;
    
    static NSString *reuseId = @"pin";
    MKPinAnnotationView *pav = (MKPinAnnotationView *)[mapView dequeueReusableAnnotationViewWithIdentifier:reuseId];
    if (pav == nil)
    {
        pav = [[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:reuseId];
    }
    else
    {
        pav.annotation = annotation;
    }
    pav.draggable = YES;
    pav.animatesDrop = YES;
    pav.canShowCallout = NO;
    
    return pav;
}

- (void)mapView:(MKMapView *)mapView
 annotationView:(MKAnnotationView *)annotationView
didChangeDragState:(MKAnnotationViewDragState)newState
   fromOldState:(MKAnnotationViewDragState)oldState
{
    if (newState == MKAnnotationViewDragStateEnding)
    {
        CLLocationCoordinate2D droppedAt = annotationView.annotation.coordinate;
        NSLog(@"Pin dropped at %f,%f", droppedAt.latitude, droppedAt.longitude);
        self.outLocation = [[CLLocation alloc] initWithLatitude:droppedAt.latitude
                                                      longitude:droppedAt.longitude];
    }
}

- (IBAction)cancel:(UIBarButtonItem *)sender
{
    [self.presentingViewController dismissViewControllerAnimated:YES completion:NULL];
}

@end
