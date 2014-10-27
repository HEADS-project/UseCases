//
//  UploadController.m
//  Heads
//
//  Created by Kostas Giannakakis on 5/20/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "UploadController.h"
#import <MobileCoreServices/MobileCoreServices.h>   // kUTTypeImage
#import "HeadsClient.h"
#import "HeadsPoint.h"
#import "HeadsContext.h"
#import "PackageViewController.h"

@interface UploadController () <UINavigationControllerDelegate, UIImagePickerControllerDelegate,
UIActionSheetDelegate, UITextFieldDelegate, HeadsClientDelegate>

@property (weak, nonatomic) UIImage *image;
@property (weak, nonatomic) IBOutlet UIImageView *imageView;
@property (weak, nonatomic) IBOutlet UITextField *titleTextField;
@property (weak, nonatomic) IBOutlet UITextField *commentsTextField;
@property (weak, nonatomic) IBOutlet UIButton *uploadButton;

@property (strong, nonatomic) HeadsClient *headsClient;

@property (nonatomic) BOOL isUploading;

@end

@implementation UploadController

#pragma mark - Properties

- (HeadsClient *) headsClient
{
    if (!_headsClient) {
        HeadsContext *context = [HeadsContext sharedInstance];
        _headsClient = [[HeadsClient alloc] initWithAccessToken:context.user.accessToken];
        _headsClient.clientDelegate = self;
    }
    return _headsClient;
}

#pragma mark - UploadController

- (UIImage *)imageWithImage:(UIImage *)image scaledToSize:(CGSize)newSize {
    //UIGraphicsBeginImageContext(newSize);
    // In next line, pass 0.0 to use the current device's pixel scaling factor (and thus account for Retina resolution).
    // Pass 1.0 to force exact pixel size.
    UIGraphicsBeginImageContextWithOptions(newSize, NO, 0.0);
    [image drawInRect:CGRectMake(0, 0, newSize.width, newSize.height)];
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return newImage;
}

- (UIImage *) getScaledImage:(UIImage *) image
{
    double maxDimension = 800;
    double scale;
    
    scale = MAX(image.size.width / maxDimension, image.size.height / maxDimension);
    if (scale > 1)
    {
        CGSize newSize = CGSizeMake(image.size.width/scale, image.size.height/scale);
        return [self imageWithImage:image scaledToSize:newSize];
    }
    return image;
}


- (IBAction)upload:(UIButton *)sender
{
    if (self.titleTextField.text.length == 0) {
        [self showAlert:@"Title is required!"];
    }
    else if (!self.image) {
        [self showAlert:@"Please select an image."];
    }
    else if (!self.location) {
        [self showAlert:@"Please select a location."];
    }
    /*else if ([self.tagIds count] == 0) {
        [self showAlert:@"Please select at least one tag."];
    }*/
    else {
        NSString *title = self.titleTextField.text;
        NSString *comments = self.commentsTextField.text;
        double latitude = self.location.coordinate.latitude;
        double longitude = self.location.coordinate.longitude;
        NSData *imageData = UIImageJPEGRepresentation([self getScaledImage:self.image], 1.0);
        
        self.titleTextField.enabled = NO;
        self.commentsTextField.enabled = NO;
        self.uploadButton.enabled = NO;
        [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
        self.isUploading = YES;
        
        HeadsContext *context = [HeadsContext sharedInstance];
        
        [self.headsClient uploadPackageFromUser:context.user.userId
                                             title:title
                                              tags:self.tagIds
                                          comments:comments
                                          latitude:latitude
                                         longitude:longitude
                                         imageData:imageData];
    }
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
    else
    {
        [super prepareForSegue:segue sender:sender];
    }
}

-(BOOL) shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(id)sender
{
    return !self.isUploading;
}

- (void) showAlert:(NSString *)message
{
    UIAlertView *notPermitted = [[UIAlertView alloc]
                                 initWithTitle:@"Warning"
                                 message:message
                                 delegate:nil
                                 cancelButtonTitle:@"OK"
                                 otherButtonTitles:nil];
    [notPermitted show];
}

#pragma mark - HeadsClientDelegate

- (void) uploadCompleted:(NSInteger)packageId
{
    self.titleTextField.enabled = YES;
    self.commentsTextField.enabled = YES;
    self.uploadButton.enabled = YES;
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    self.isUploading = NO;
    
    HeadsPoint *package = [[HeadsPoint alloc] init];
    package.title = self.titleTextField.text;
    package.comments = self.commentsTextField.text;
    package.tags = self.tagIds;
    package.latitude = self.location.coordinate.latitude;
    package.longitude = self.location.coordinate.longitude;
    package.imageUrl = [self.headsClient imageUrlForPackageWithId:[NSString stringWithFormat:@"%ld", (long) packageId]];
    package.thumbnailUrl = [self.headsClient thumbnailUrlForPackageWithId:[NSString stringWithFormat:@"%ld", (long)packageId]];
    package.date = (long) ([[NSDate date] timeIntervalSince1970]*1000);
    
    // Clear form
    self.titleTextField.text = @"";
    self.commentsTextField.text = @"";
    self.imageView.image = nil;
    
    [self performSegueWithIdentifier:@"Package Select" sender:package];
}


- (void) errorDidOccur:(NSString *)errorMessage
{
    self.titleTextField.enabled = YES;
    self.commentsTextField.enabled = YES;
    self.uploadButton.enabled = YES;
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    self.isUploading = NO;
    
    UIAlertView *errorAlert = [[UIAlertView alloc]
                                 initWithTitle:@"Error"
                                 message:errorMessage
                                 delegate:nil
                                 cancelButtonTitle:@"OK"
                                 otherButtonTitles:nil];
    [errorAlert show];
}

#pragma mark - UITableViewDelegate

- (void) tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 3 && indexPath.row == 0)
    {
        UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:@"Select source"
                                                                 delegate:self
                                                        cancelButtonTitle:nil
                                                   destructiveButtonTitle:nil
                                                        otherButtonTitles:nil];
        
        [actionSheet addButtonWithTitle:@"Camera"];
        [actionSheet addButtonWithTitle:@"Gallery"];
        [actionSheet addButtonWithTitle:@"Cancel"]; // put at bottom (don't do at all on iPad)
        
        [actionSheet showInView:self.view]; // different on iPad
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 3 && indexPath.row == 1 && self.image)
    {
        return 289+6+6;
    }
    return 43.0;
}

#pragma mark - UIImagePickerControllerDelegate

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker
{
    [self dismissViewControllerAnimated:YES completion:NULL];
}

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
    UIImage *image = info[UIImagePickerControllerEditedImage];
    if (!image) image = info[UIImagePickerControllerOriginalImage];
    self.image = image;
    self.imageView.image = image;
    [self.tableView reloadData]; 
    [self dismissViewControllerAnimated:YES completion:NULL];
}

#pragma mark - UIActionSheetDelegate

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == 0) {
        UIImagePickerController *uiipc = [[UIImagePickerController alloc] init];
        uiipc.delegate = self;
        uiipc.mediaTypes = @[(NSString *)kUTTypeImage];
        uiipc.sourceType = UIImagePickerControllerSourceTypeCamera;
        uiipc.allowsEditing = YES;
        [self presentViewController:uiipc animated:YES completion:NULL];
    }
    else if (buttonIndex == 1) {
        UIImagePickerController *uiipc = [[UIImagePickerController alloc] init];
        uiipc.delegate = self;
        uiipc.mediaTypes = @[(NSString *)kUTTypeImage];
        uiipc.sourceType = UIImagePickerControllerSourceTypeSavedPhotosAlbum;
        uiipc.allowsEditing = YES;
        [self presentViewController:uiipc animated:YES completion:NULL];
    }
}

- (BOOL) textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    return YES;
}

@end
