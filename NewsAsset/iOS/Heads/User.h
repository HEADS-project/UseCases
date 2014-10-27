//
//  User.h
//  Heads
//
//  Created by Kostas Giannakakis on 5/9/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface User : NSObject

@property (nonatomic, strong) NSString *username;
@property (nonatomic, strong) NSString *password;
@property (nonatomic, strong) NSString *userId;
@property (nonatomic, strong) NSString *fullname;
@property (nonatomic, strong) NSString *email;
@property (nonatomic, strong) NSString *accessToken;
@property (nonatomic) BOOL *isLoggedIn;

@end
