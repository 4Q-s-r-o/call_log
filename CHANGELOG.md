## 3.2.2
* Fixed incorrect permission handling on v2 embedding (re-ynd)

## 3.2.1
* Fixed incorrect query generation when using multiple query params (AR553)

## 3.2.0
* Migration to v2 embedding
* Minor native code refactoring

##3.1.0
* Fixed null pointer on some devices that will not return subscriptions list
* Added new DateTime parameters (dateTimeFrom/dateTimeTo) as an alternative to milisecond ones (dateFrom/dateTo)

## 3.0.3
* Correct detection of ``wifiIncomming`` and ``wifiOutgoing`` call types (sanjay23singh)
## 3.0.2
* Removed deprecated file

## 3.0.1
* Decreased lower bound for  dart

## 3.0.0
* Stable release of null safety

## 3.0.0-nullsafety
* Migration to null safety

## 2.2.1
* Fixed null pointer on android API level 22 and bellow (by ponnamkarthik)

## 2.2.0
* Background execution support. Please note that is impossible for plugin to ask user for permission while in background
* phoneAccountId in response (by asiriPiyajanaka)
* SIM display name in response (by balee)

## 2.1.0
* Added ```CACHED_MATCHED_NUMBER``` to query and response objects

## 2.0.2
* Multiple fixes and new features from Filipe Picoito Jorge
    * Fix callType forced value to unknown in case it is not within the range of the available enum values
    * Add the new CallLog Calls to the rest of the code logic
    * Add more CallLog Calls enums

## 2.0.1
* updated CallLog types

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
