//
//  AccountViewController.m
//  Heads
//
//  Created by Kostas Giannakakis on 5/13/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "AccountViewController.h"
#import "HeadsContext.h"

@interface AccountViewController ()
@property (weak, nonatomic) IBOutlet UITextField *username;
@property (weak, nonatomic) IBOutlet UITextField *fullname;
@property (weak, nonatomic) IBOutlet UITextField *email;

@end

@implementation AccountViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    HeadsContext *context = [HeadsContext sharedInstance];
    self.username.text = context.user.userId;
    self.fullname.text = context.user.username;
    self.email.text = context.user.email;
}

@end
