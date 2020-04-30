## 2.0.3
* Add more CallLog Calls enums

## 2.0.2
* Fix CallLog.Calls.CACHED_MATCHED_NUMBER to CallLog.Calls.NUMBER

## 2.0.1
* Fix callType forced value to unknown in case it is not within the range of the available enum values

## 2.0.0
* Migration from ```android.support``` packages to ```androidx``` packages that allows this library to be used with flutter projects that use ```androidx```. If you need to stay on ```android.support``` for whatever reason, don't upgrade as it may break your build. [See more](https://flutter.io/docs/development/packages-and-plugins/androidx-compatibility).  
* Lowered minSdkVersion as requested per [#1](https://github.com/4Q-s-r-o/call_log/issues/1)

## 1.0.5
* bugfixes

## 1.0.4
* documentation modifications

## 1.0.3

* fixing NullPointerException when onRequestPermissionsResult was triggered by different plugin than this one

## 1.0.2

* more details in README.md

## 1.0.1

* documentation modifications based on flutter package analysis
* code reformat using flutter format
* Query log entries based on 'type' attribute

## 1.0.0

* Initial release
* Support for Android only as iOS does not provide API for call history
* Features
    * Check if access permissions are granted
    * Request access permissions
    * Get all call log entries
    * Query log entries
        * fromDate: get all entries from this date
        * toDate: get all entries until this date
        * name: get all entries for this contact name
        * number: get all entries for this telephone number
