# call_log

[![pub package](https://img.shields.io/pub/v/call_log.svg)](https://pub.dartlang.org/packages/call_log)

A Flutter plugin to access and query call history log.\
Support for Android only as iOS does not provide API for accessing call history

## Usage

To use this plugin, add `call_log` as a [dependency in your `pubspec.yaml` file](https://flutter.io/platform-plugins/).

You may add the following permission to your Android Manifest:

```xml
<uses-permission android:name="android.permission.READ_CALL_LOG" />
```

This plugin is able to handle checking and requesting permission automatically.\
Currently implemented query params are dateFrom, dateTo, durationFrom, durationTo, name and number.\
String params are queried using LIKE and '%' wildcard on both sides.

 ## Background execution

This plugin may be used in flutter background engine, via plugins like WorkManager. But please note that it is impossible for plugin to request permission while it is executed in background. You have to manually request permissions READ_CALL_LOG and READ_PHONE_STATE

## Note on iOS support

Unfortuynately iOS doesn't support queries of call log. More information here: https://stackoverflow.com/questions/33753644/is-there-an-ios-api-for-accessing-call-logs 

## Example

``` dart
// IMPORT PACKAGE
import 'package:call_log/call_log.dart';

// GET WHOLE CALL LOG
Iterable<CallLogEntry> entries = await CallLog.get();

// QUERY CALL LOG (ALL PARAMS ARE OPTIONAL)
var now = DateTime.now();
int from = now.subtract(Duration(days: 60)).millisecondsSinceEpoch;
int to = now.subtract(Duration(days: 30)).millisecondsSinceEpoch;
Iterable<CallLogEntry> entries = await CallLog.query(
      dateFrom: from,
      dateTo: to,
      durationFrom: 0,
      durationTo: 60,
      name: 'John Doe',
      number: '901700000',
      type: CallType.incoming,
    );
```

## Todo

- [x] query call log using most common fields, using AND as a logical gate
- [ ] query call log with full power of native API

## Contribution and Support

* Contributions are welcome!
* If you want to contribute code please create a PR
* If you find a bug or want a feature, please fill an issue
