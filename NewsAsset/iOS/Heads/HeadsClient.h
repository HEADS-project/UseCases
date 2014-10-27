//
//  HeadsClient.h
//  Heads
//
//  Created by Kostas Giannakakis on 5/9/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "User.h"

@protocol HeadsClientDelegate

@optional
- (void) tagsWereReceived:(NSMutableDictionary *)tags;
- (void) loginWasSuccessful:(User *)user;
- (void) registrationSuccessful;
- (void) packagesListReceived:(NSMutableArray *)packages;
- (void) uploadCompleted:(NSInteger)packageId;
- (void) deleteCompleted;

@required
- (void) errorDidOccur:(NSString *)errorMessage;
@end


@interface HeadsClient : NSObject

@property (strong, nonatomic) id<HeadsClientDelegate> clientDelegate;

- (id) initWithAccessToken:(NSString *) accessToken;

-(void) register:(NSString *)username
        password:(NSString *)password;

-(void) login:(NSString *)username
     password:(NSString *)password;

-(void) requestTags;

-(void) requestUserPackages:(NSString *) userId;

-(void) searchWithTitle:(NSString *)title
        withDescription:(NSString *)description
           withLatitude:(double)latitude
          withLongitude:(double)longitude
             withRadius:(double)radius;

- (void) uploadPackageFromUser:(NSString *)user
                         title:(NSString *)title
                          tags:(NSArray *)tags
                      comments:(NSString *)comments
                      latitude:(double) latitude
                     longitude:(double)longitude
                     imageData:(NSData *)imageData;

- (void) deletePackageWithId:(NSString *) packageId;

- (NSString *) imageUrlForPackageWithId:(NSString *)packageId;

- (NSString *) thumbnailUrlForPackageWithId:(NSString *)packageId;

@end
