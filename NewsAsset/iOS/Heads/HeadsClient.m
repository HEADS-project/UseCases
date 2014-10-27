//
//  HeadsClient.m
//  Heads
//
//  Created by Kostas Giannakakis on 5/9/14.
//  Copyright (c) 2014 ATC. All rights reserved.
//

#import "HeadsClient.h"
#import "HeadsHTTPCommunication.h"
#import "HeadsPoint.h"

@interface HeadsClient ()


@property (nonatomic, strong) NSString *loginUrl;
@property (nonatomic, strong) NSString *servicesUrl;
@property (nonatomic, strong) NSString* accessToken;

@end


@implementation HeadsClient

- (id) initWithAccessToken:(NSString *) accessToken
{
    self = [super init];
    if (self)
    {
        self.accessToken = accessToken;
    }
    return self;
}


- (NSString *) loginUrl
{
    return @"http://pc51.atc.gr/heads/Token";
}

- (NSString *) servicesUrl
{
    return @"http://pc51.atc.gr/heads";
}

- (NSString *) imageUrlForPackageWithId:(NSString *)packageId
{
    return [NSString stringWithFormat:@"%@/Photos/Full/%@", self.servicesUrl, packageId];
}

- (NSString *) thumbnailUrlForPackageWithId:(NSString *)packageId
{
    
    return [NSString stringWithFormat:@"%@/Photos/Thumbnail/%@", self.servicesUrl, packageId];
}

-(void) register:(NSString *)username password:(NSString *)password
{
    HeadsHTTPCommunication *http = [[HeadsHTTPCommunication alloc] init];
    NSURL *url = [NSURL URLWithString:[NSString stringWithFormat:@"%@/api/account/register", self.servicesUrl]];
    NSDictionary *params = @{@"username" : username, @"password": password,
                             @"confirmPassword": password};
    
    [http postURL:url
           params:params
          success:^(NSData *response) {
              if (http.statusCode == 200) {
                  [self.clientDelegate registrationSuccessful];
              }
              else {
                  NSError *error = nil;
                  NSDictionary *data = [NSJSONSerialization JSONObjectWithData:response options:0 error:&error];
                  if (!error)
                  {
                      if ([data objectForKey:@"ModelState"])
                      {
                          NSDictionary *modelState = data[@"ModelState"];
                          for(id key in modelState)
                          {
                              NSArray *messages = modelState[key];
                              if ([messages count])
                              {
                                  [self.clientDelegate errorDidOccur:messages[0]];
                                  return;
                              }
                          }
                      }
                      [self.clientDelegate errorDidOccur:@"Upload failed"];
                  }
                  else
                  {
                      [self.clientDelegate errorDidOccur:error.description];
                  }
              }
          }
            error:^(NSError *error) { [self.clientDelegate errorDidOccur:error.description];}];
}


-(void) login:(NSString *)username
     password:(NSString *)password
{
    HeadsHTTPCommunication *http = [[HeadsHTTPCommunication alloc] init];
    NSURL *url = [NSURL URLWithString:self.loginUrl];
    NSDictionary *params = @{@"username" : username, @"password": password,
                             @"confirmPassword": password, @"grant_type": @"password"};
    
    [http postURL:url
           params:params
          success:^(NSData *response) { [self loginResponseReceived:response];}
            error:^(NSError *error) { [self.clientDelegate errorDidOccur:error.description];}];
}

- (void) loginResponseReceived:(NSData *) response
{
    if ([response length] == 0)
    {
        [self.clientDelegate errorDidOccur:@"Server error"];
    }
    else
    {
        NSError *error = nil;
        NSDictionary *data = [NSJSONSerialization JSONObjectWithData:response options:0 error:&error];
        if (!error)
        {
            if ([data objectForKey:@"error"])
            {
                NSString *errorMessage = data[@"error_description"];
                [self.clientDelegate errorDidOccur:errorMessage];
            }
            else
            {
                self.accessToken = data[@"access_token"];
                
                //NSLog(@"%@", data);
                
                NSString *userName = data[@"userName"];
                
                User *user = [[User alloc] init];
                user.userId = userName;
                user.username = userName;
                user.fullname = userName;
                user.accessToken = self.accessToken;
                //user.email = resultData[@"user_email"];
                
                [self.clientDelegate loginWasSuccessful:user];
            }
        }
        else
        {
            [self.clientDelegate errorDidOccur:error.description];
        }
    }
}

-(void) requestTags
{
    NSURL *url = [NSURL URLWithString:[NSString stringWithFormat:@"%@/model", self.servicesUrl]];
    HeadsHTTPCommunication *http = [[HeadsHTTPCommunication alloc] init];
    
    [http retrieveURL:url
              success:^(NSData *response) { [self tagsResponseWasReceived:response];}
                error:^(NSError *error) { [self.clientDelegate errorDidOccur:error.description];}];
}


- (void) tagsResponseWasReceived:(NSData *) response
{

}

-(NSURL *) buildURL:(NSString *) url withQueryParameters:(NSDictionary *)params
{
    NSMutableCharacterSet * URLQueryPartAllowedCharacterSet; // possibly defined in class extension ...
    
    // ... and built in init or on first use
    URLQueryPartAllowedCharacterSet = [[NSCharacterSet URLQueryAllowedCharacterSet] mutableCopy];
    [URLQueryPartAllowedCharacterSet removeCharactersInRange:NSMakeRange('&', 1)]; // %26
    [URLQueryPartAllowedCharacterSet removeCharactersInRange:NSMakeRange('=', 1)]; // %3D
    [URLQueryPartAllowedCharacterSet removeCharactersInRange:NSMakeRange('?', 1)]; // %3F
    
    NSMutableString* queryParams = [[NSMutableString alloc] init];
    int i = 0;
    for(id key in params)
    {
        if (i > 0) {
            [queryParams appendString:@"&"];
        }
        [queryParams appendString:key];
        [queryParams appendString:@"="];
        [queryParams appendString:[params[key] stringByAddingPercentEncodingWithAllowedCharacters:URLQueryPartAllowedCharacterSet]];
        i++;
    }
    NSString *urlStr = [NSString stringWithFormat:@"%@?%@", url, queryParams];
    
    NSLog(@"%@", urlStr);
    
    return [[NSURL alloc] initWithString:urlStr];
}

-(void) requestUserPackages:(NSString *) userId
{
    NSAssert(self.accessToken, @"Not authenticated");
    
    NSURL *url = [self buildURL:[NSString stringWithFormat:@"%@/api/points/search", self.servicesUrl]
            withQueryParameters:@{@"username" : userId}];
    
                  
    HeadsHTTPCommunication *http = [[HeadsHTTPCommunication alloc] init];
    
    [http retrieveURL:url
              success:^(NSData *response) { [self userPackagesResponseWasReceived:response];}
                error:^(NSError *error) { [self.clientDelegate errorDidOccur:error.description];}];
}

- (void) userPackagesResponseWasReceived:(NSData *) response
{
    NSError *error = nil;
    NSDictionary *results = [NSJSONSerialization JSONObjectWithData:response options:0 error:&error];
    if (error)
    {
        [self.clientDelegate errorDidOccur:error.description];
        return;
    }
    
    
    NSMutableArray *packages = [[NSMutableArray alloc] init];
    for(NSDictionary *result in results)
    {
        HeadsPoint *package = [[HeadsPoint alloc] init];
        package.packageId = result[@"PointId"];
        
        package.latitude = [((NSNumber *) result[@"Latitude"]) doubleValue];
        package.longitude = [((NSNumber *) result[@"Longitude"]) doubleValue];
        package.date = [((NSNumber *) result[@"Uploaded"]) longValue];
        package.comments = result[@"Description"];
        package.title = result[@"Title"];
        package.imageUrl = [self imageUrlForPackageWithId:package.packageId];
        package.thumbnailUrl = [self thumbnailUrlForPackageWithId:package.packageId];
        //package.tags = packageData[@"tags"];
        
        [packages addObject:package];
    }
    [self.clientDelegate packagesListReceived:packages];
}

-(void) searchWithTitle:(NSString *)title
        withDescription:(NSString *)description
          withLatitude:(double)latitude
         withLongitude:(double)longitude
            withRadius:(double)radius
{
    NSURL *url = [self buildURL:[NSString stringWithFormat:@"%@/api/points/search", self.servicesUrl]
            withQueryParameters:@{@"latitude" : [NSString stringWithFormat:@"%.6f", latitude],
                                  @"longitude" : [NSString stringWithFormat:@"%.6f", longitude],
                                  @"range" : [NSString stringWithFormat:@"%.1f", radius],
                                  @"title" : title,
                                  @"description" : description}];
    
    
    HeadsHTTPCommunication *http = [[HeadsHTTPCommunication alloc] init];
    
    [http retrieveURL:url
              success:^(NSData *response) { [self userPackagesResponseWasReceived:response];}
                error:^(NSError *error) { [self.clientDelegate errorDidOccur:error.description];}];
    
}

- (void) uploadPackageFromUser:(NSString *)user
                         title:(NSString *)title
                          tags:(NSArray *)tags
                      comments:(NSString *)comments
                      latitude:(double) latitude
                     longitude:(double)longitude
                     imageData:(NSData *)imageData
{
    NSAssert(self.accessToken, @"Not authenticated");
    
    NSMutableDictionary *point = [[NSMutableDictionary alloc] init];
    
    [point setObject:title forKey:@"Title"];
    [point setObject:comments forKey:@"Description"];
    [point setObject:@(latitude) forKey:@"Latitude"];
    [point setObject:@(longitude) forKey:@"Longitude"];
    [point setObject:[imageData base64EncodedStringWithOptions:0] forKey:@"Image"];
    
    // Convert to json string
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:point
                                                       options:0
                                                         error:&error];
    if (!jsonData) {
        [self.clientDelegate errorDidOccur:[error description]];
        return;
    }
    
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    // POST request
    NSURL *url = [NSURL URLWithString:[NSString stringWithFormat:@"%@/api/points", self.servicesUrl]];
    HeadsHTTPCommunication *http = [[HeadsHTTPCommunication alloc] init];
    
    [http postURL:url
      withHeaders:@{@"content-type" : @"application/json; charset=UTF-8",
                    @"Accept" : @"application/json",
                    @"Authorization" : [NSString stringWithFormat:@"Bearer %@", self.accessToken]}
         withBody:jsonString
          success:^(NSData *response) { [self uploadResponseWasReceived:response];}
            error:^(NSError *error) { [self.clientDelegate errorDidOccur:error.description];}];
}

- (void) uploadResponseWasReceived:(NSData *) response
{
    NSString *str = [[NSString alloc] initWithData:response encoding:NSUTF8StringEncoding];
    int pointId = [str intValue];
    if (pointId > 0)
    {
        [self.clientDelegate uploadCompleted:pointId];
    }
    else
    {
        [self.clientDelegate errorDidOccur:@"Upload failed"];
    }
}

- (void) deletePackageWithId:(NSString *) packageId
{
    NSAssert(self.accessToken, @"Not authenticated");
    
    NSURL *url = [NSURL URLWithString:[NSString stringWithFormat:@"%@/api/points/%@", self.servicesUrl, packageId]];
    
    //NSLog(@"%@", url);
    
    HeadsHTTPCommunication *http = [[HeadsHTTPCommunication alloc] init];
    [http deleteRequest:url
                success:^(NSData *respose) {
                    if (http.statusCode >= 200 && http.statusCode <=299) {
                        [self.clientDelegate deleteCompleted];
                    }
                    else {
                        [self.clientDelegate errorDidOccur:@"Delete failed"];
                    }
                }
                  error:^(NSError *error) {
        [self.clientDelegate errorDidOccur:error.description];
    }];
}

@end
