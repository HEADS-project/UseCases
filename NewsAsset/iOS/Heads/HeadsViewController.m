//
//  HeadsViewController.m
//  Heads
//
//  Created by Kostas Giannakakis on 5/8/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import <QuartzCore/QuartzCore.h>
#import "HeadsViewController.h"
#import "HeadsClient.h"
#import "HeadsContext.h"

@interface HeadsViewController () <HeadsClientDelegate, UITextFieldDelegate>
@property (weak, nonatomic) IBOutlet UITextField *username;
@property (weak, nonatomic) IBOutlet UITextField *password;
@property (weak, nonatomic) IBOutlet UILabel *errorLabel;
- (IBAction)login:(UIButton *)sender;

@property (weak, nonatomic) IBOutlet UIButton *loginButton;
@property (weak, nonatomic) IBOutlet UIButton *registerButton;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *activityIndicator;

@property (strong, nonatomic) HeadsClient *headsClient;

@end

@implementation HeadsViewController

#pragma mark - Properties

- (HeadsClient *) headsClient
{
    if (!_headsClient) {
        _headsClient = [[HeadsClient alloc] init];
        _headsClient.clientDelegate = self;
    }
    return _headsClient;
}

#pragma mark - Lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    // Hide keyboard when tap outside of textfield
    [self.view addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self.view
                                                                            action:@selector(endEditing:)]];
}

- (void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    self.username.enabled = YES;
    self.password.enabled = YES;
    self.loginButton.enabled = YES;
    self.registerButton.enabled = YES;
    self.errorLabel.text = @"";
    [self.activityIndicator stopAnimating];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark UITextFieldDelegate

- (BOOL) textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    return YES;
}

- (void) textFieldDidEndEditing:(UITextField *)textField
{
    if (self.username.text.length && self.password.text.length)
    {
        [self login:self.loginButton];
    }
}

#pragma mark - Button handlers

- (IBAction)login:(UIButton *)sender
{
    if ([self.username.text length] == 0) {
        self.errorLabel.text = @"Username is required";
        self.username.borderStyle = UITextBorderStyleRoundedRect;
        self.username.layer.borderColor = [[UIColor redColor] CGColor];
        self.username.layer.borderWidth = 1.0f;
        //self.username.layer.cornerRadius = 8.0f;
        //self.username.layer.masksToBounds=YES;
        return;
    }
    self.username.borderStyle = UITextBorderStyleRoundedRect;
    self.username.layer.borderColor = [[UIColor clearColor] CGColor];
    self.username.layer.borderWidth = 1.0f;
    
    if ([self.password.text length] == 0) {
        self.errorLabel.text = @"Password is required";
        self.password.borderStyle = UITextBorderStyleRoundedRect;
        self.password.layer.borderColor = [[UIColor redColor] CGColor];
        self.password.layer.borderWidth = 1.0f;
        return;
    }
    self.password.borderStyle = UITextBorderStyleRoundedRect;
    self.password.layer.borderColor = [[UIColor clearColor] CGColor];
    self.password.layer.borderWidth = 1.0f;
    
    self.errorLabel.text = @"";
    
    self.username.enabled = NO;
    self.password.enabled = NO;
    self.loginButton.enabled = NO;
    self.registerButton.enabled = NO;
    
    [self.activityIndicator startAnimating];
    
    [self.headsClient login:self.username.text
                      password:self.password.text];
}


#pragma  mark - HeadsClientDelegate

- (void) loginWasSuccessful:(User *)user
{
    HeadsContext *context = [HeadsContext sharedInstance];
    context.user = user;
    [self performSegueWithIdentifier:@"login" sender:self];
    
    //[self.headsClient requestTags];
}


- (void) tagsWereReceived:(NSMutableDictionary *)tags
{
    HeadsContext *context = [HeadsContext sharedInstance];
    context.tags = tags;
    [self performSegueWithIdentifier:@"login" sender:self];
}

- (void) errorDidOccur:(NSString *)errorMessage
{
    self.username.enabled = YES;
    self.password.enabled = YES;
    self.loginButton.enabled = YES;
    self.registerButton.enabled = YES;
    
    [self.activityIndicator stopAnimating];
    
    self.errorLabel.text = errorMessage;
}


@end
