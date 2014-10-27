//
//  HeadsHTTPCommunication
//
//  Created by Kostas Giannakakis on 5/9/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HeadsHTTPCommunication : NSObject

@property (nonatomic) int statusCode;

- (void)retrieveURL:(NSURL *)url success:(void (^)(NSData *))success error:(void (^)(NSError *))error;
- (void)postURL:(NSURL *)url params:(NSDictionary *)params success:(void (^)(NSData *))success error:(void (^)(NSError *))error;
- (void)postURL:(NSURL *)url withBody:(NSString *)postBodyString success:(void (^)(NSData *))success error:(void (^)(NSError *))error;
- (void)postURL:(NSURL *)url
    withHeaders:(NSDictionary *) headers
       withBody:(NSString *)postBodyString
        success:(void (^)(NSData *))success
          error:(void (^)(NSError *))error;
- (void) deleteRequest:(NSURL *)url success:(void (^)(NSData *))success error:(void (^)(NSError *))error;

@end
