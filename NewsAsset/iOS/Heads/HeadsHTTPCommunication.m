//
//  HeadsHTTPCommunication
//
//  Created by Kostas Giannakakis on 5/9/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "HeadsHTTPCommunication.h"

@interface HeadsHTTPCommunication () <NSURLConnectionDataDelegate>
{
    NSMutableData *receivedData;
}

@property (nonatomic, copy) void (^success)(NSData *);
@property (nonatomic, copy) void (^error)(NSError *);

@end

@implementation HeadsHTTPCommunication

- (void)postURL:(NSURL *)url params:(NSDictionary *)params success:(void (^)(NSData *))success error:(void (^)(NSError *))error
{
    self.success = success;
    self.error = error;
    receivedData = [NSMutableData data];

    NSMutableArray *parametersArray = [NSMutableArray arrayWithCapacity:[params count]];
    for (NSString *key in params)
    {
        [parametersArray addObject:[NSString stringWithFormat:@"%@=%@", key, params[key]]];
    }

    NSString *postBodyString = [parametersArray componentsJoinedByString:@"&"];
    NSData *postBodyData = [NSData dataWithBytes:[postBodyString UTF8String]
                                          length:[postBodyString length]];
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];
    [request setHTTPMethod: @"POST"];
    [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"content-type"];
    [request setHTTPBody:postBodyData];

    NSURLConnection *urlConnection = [[NSURLConnection alloc] initWithRequest:request
                                                                     delegate:self];
    [urlConnection start];
}

- (void)postURL:(NSURL *)url withBody:(NSString *)postBodyString success:(void (^)(NSData *))success error:(void (^)(NSError *))error
{
    self.success = success;
    self.error = error;
    receivedData = [NSMutableData data];
    
    NSData *postBodyData = [NSData dataWithBytes:[postBodyString UTF8String]
                                          length:[postBodyString length]];
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];
    [request setHTTPMethod: @"POST"];
    [request setValue:@"application/json; charset=UTF-8" forHTTPHeaderField:@"content-type"];
    [request setValue:@"application-json" forHTTPHeaderField:@"Accept"];
    [request setHTTPBody:postBodyData];
    
    NSURLConnection *urlConnection = [[NSURLConnection alloc] initWithRequest:request
                                                                     delegate:self];
    [urlConnection start];
}

- (void)postURL:(NSURL *)url
    withHeaders:(NSDictionary *) headers
       withBody:(NSString *)postBodyString
        success:(void (^)(NSData *))success
          error:(void (^)(NSError *))error
{
    self.success = success;
    self.error = error;
    receivedData = [NSMutableData data];
    
    NSData *postBodyData = [NSData dataWithBytes:[postBodyString UTF8String]
                                          length:[postBodyString length]];
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];
    [request setHTTPMethod: @"POST"];
    
    for(id key in headers)
    {
        [request setValue:headers[key] forHTTPHeaderField:key];
    }
    
    [request setHTTPBody:postBodyData];
    
    NSURLConnection *urlConnection = [[NSURLConnection alloc] initWithRequest:request
                                                                     delegate:self];
    [urlConnection start];
}

- (void)retrieveURL:(NSURL *)url success:(void (^)(NSData *))success error:(void (^)(NSError *))error
{
    self.success = success;
    self.error = error;
    receivedData = [NSMutableData data];

    NSURLRequest *request = [[NSURLRequest alloc] initWithURL:url];
    NSURLConnection *urlConnection = [[NSURLConnection alloc] initWithRequest:request
                                                                     delegate:self];
    [urlConnection start];
}

- (void) deleteRequest:(NSURL *)url success:(void (^)(NSData *))success error:(void (^)(NSError *))error
{
    self.success = success;
    self.error = error;
    
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    [request setHTTPMethod: @"DELETE"];
    NSURLConnection *urlConnection = [[NSURLConnection alloc] initWithRequest:request
                                                                     delegate:self];
    [urlConnection start];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    [receivedData appendData:data];
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection
{
    self.success(receivedData);
}


- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
    [receivedData setLength:0];
    self.statusCode = [((NSHTTPURLResponse *) response) statusCode];
}

- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
    self.error(error);
}

@end
