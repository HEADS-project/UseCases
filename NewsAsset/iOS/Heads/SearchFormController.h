//
//  SearchFormController.h
//  Heads
//
//  Created by Kostas Giannakakis on 5/15/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "OptionsSelectViewController.h"

@interface SearchFormController : OptionsSelectViewController

@property (weak, nonatomic) IBOutlet UITextField *pointTitle;
@property (weak, nonatomic) IBOutlet UITextField *pointDescription;

@end
