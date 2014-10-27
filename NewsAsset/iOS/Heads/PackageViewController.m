//
//  PackageViewController.m
//  Heads
//
//  Created by Kostas Giannakakis on 5/22/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "PackageViewController.h"
#import <SDWebImage/SDWebImageManager.h>

@interface PackageViewController ()

@property (weak, nonatomic) IBOutlet UIImageView *imageView;

@end

@implementation PackageViewController

-(void) viewDidLoad
{
    [super viewDidLoad];
    self.title = self.package.title;
    
    SDWebImageManager *manager = [SDWebImageManager sharedManager];
    [manager downloadImageWithURL:[[NSURL alloc] initWithString:self.package.imageUrl]
                          options:0
                         progress:^(NSInteger receivedSize, NSInteger expectedSize) { }
                        completed:^(UIImage *image, NSError *error, SDImageCacheType cacheType, BOOL finished, NSURL *imageURL) {
                            if (image)
                            {
                                self.imageView.image = image;
                            }
                        }];
}


@end
