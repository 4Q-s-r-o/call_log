#import "CallLogPlugin.h"

@implementation CallLogPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"sk.fourq.call_log"
            binaryMessenger:[registrar messenger]];
  CallLogPlugin* instance = [[CallLogPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"get" isEqualToString:call.method] ||
      [@"query" isEqualToString:call.method]) {
      result(nil);
  } else {
      result(FlutterMethodNotImplemented);
  }
}

@end
