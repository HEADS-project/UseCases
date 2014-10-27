//
//  RegisterViewController.m
//  Heads
//
//  Created by Kostas Giannakakis on 8/28/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "RegisterViewController.h"
#import "HeadsClient.h"
#import "HeadsContext.h"

@interface RegisterViewController () <HeadsClientDelegate, UITextFieldDelegate>

@property (strong, nonatomic) HeadsClient *headsClient;

@end

@implementation RegisterViewController

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
    self.label.text = @"";
    [self.activityIndicator stopAnimating];
}

#pragma mark UITextFieldDelegate

- (BOOL) textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    return YES;
}

- (void) textFieldDidEndEditing:(UITextField *)textField
{
    if (self.username.text.length && self.password.text.length && self.confirmPassword.text.length)
    {
        [self register:self.registerButton];
    }
}

- (IBAction)register:(UIButton *)sender
{
    if ([self.username.text length] == 0) {
        self.label.text = @"Username is required";
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
        self.label.text = @"Password is required";
        self.password.borderStyle = UITextBorderStyleRoundedRect;
        self.password.layer.borderColor = [[UIColor redColor] CGColor];
        self.password.layer.borderWidth = 1.0f;
        return;
    }
    self.password.borderStyle = UITextBorderStyleRoundedRect;
    self.password.layer.borderColor = [[UIColor clearColor] CGColor];
    self.password.layer.borderWidth = 1.0f;
    
    if ([self.confirmPassword.text length] == 0) {
        self.label.text = @"Password is required";
        self.confirmPassword.borderStyle = UITextBorderStyleRoundedRect;
        self.confirmPassword.layer.borderColor = [[UIColor redColor] CGColor];
        self.confirmPassword.layer.borderWidth = 1.0f;
        return;
    }
    self.confirmPassword.borderStyle = UITextBorderStyleRoundedRect;
    self.confirmPassword.layer.borderColor = [[UIColor clearColor] CGColor];
    self.confirmPassword.layer.borderWidth = 1.0f;
    
    if (![self.password.text isEqualToString:self.confirmPassword.text])
    {
        self.label.text = @"Passwords do not match";
        self.confirmPassword.borderStyle = UITextBorderStyleRoundedRect;
        self.confirmPassword.layer.borderColor = [[UIColor redColor] CGColor];
        self.confirmPassword.layer.borderWidth = 1.0f;
        self.password.borderStyle = UITextBorderStyleRoundedRect;
        self.password.layer.borderColor = [[UIColor redColor] CGColor];
        self.password.layer.borderWidth = 1.0f;
        return;
    }
    self.password.borderStyle = UITextBorderStyleRoundedRect;
    self.password.layer.borderColor = [[UIColor clearColor] CGColor];
    self.password.layer.borderWidth = 1.0f;
    self.confirmPassword.borderStyle = UITextBorderStyleRoundedRect;
    self.confirmPassword.layer.borderColor = [[UIColor clearColor] CGColor];
    self.confirmPassword.layer.borderWidth = 1.0f;
    
    self.label.text = @"";
    
    self.username.enabled = NO;
    self.password.enabled = NO;
    self.loginButton.enabled = NO;
    self.registerButton.enabled = NO;
    
    [self.activityIndicator startAnimating];
    
    [self.headsClient register:self.username.text
                     password:self.password.text];
}


#pragma  mark - HeadsClientDelegate

- (void) registrationSuccessful
{
    [self.headsClient login:self.username.text
                   password:self.password.text];
}

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
    
    self.label.text = errorMessage;
    [self.label sizeToFit];
}


@end
