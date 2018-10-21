import 'dart:async';
import 'package:flutter/services.dart';

class CallLog {
  static const MethodChannel _channel = const MethodChannel('sk.fourq.call_log');

  /// Get all call history log entries. Permissions are handled automatically
  static Future<Iterable<CallLogEntry>> get() async {
    Iterable result = await _channel.invokeMethod('get', null);
    return result?.map((m) => CallLogEntry.fromMap(m));
  }

  /// Query call history log entries
  /// dateFrom: unix timestamp. precision in millis
  /// dateTo: unix timestamp. precision in millis
  /// durationFrom: minimal call length in seconds
  /// durationTo: minimal call length in seconds
  /// name: call participant name (present only if in contacts)
  /// number: call participant phone number
  /// type: value from [CallType] enum
  static Future<Iterable<CallLogEntry>> query({
    int dateFrom,
    int dateTo,
    int durationFrom,
    int durationTo,
    String name,
    String number,
    CallType type,
  }) async {
    var params = {
      "dateFrom": dateFrom?.toString(),
      "dateTo": dateTo?.toString(),
      "durationFrom": durationFrom?.toString(),
      "durationTo": durationTo?.toString(),
      "name": name,
      "number": number,
      "type": type?.index == null ? null : (type.index + 1).toString(),
    };
    Iterable records = await _channel.invokeMethod('query', params);
    return records?.map((m) => CallLogEntry.fromMap(m));
  }
}

/// PODO for one call log entry
class CallLogEntry {
  CallLogEntry({
    this.name,
    this.number,
    this.formattedNumber,
    this.callType,
    this.duration,
    this.timestamp,
  });

  String name;
  String number;
  String formattedNumber;
  CallType callType;
  int duration;
  int timestamp;

  CallLogEntry.fromMap(Map m) {
    name = m['name'];
    number = m['number'];
    formattedNumber = m['formattedNumber'];
    callType = CallType.values[m['callType'] - 1];
    duration = m['duration'];
    timestamp = m['timestamp'];
  }
}

/// All possible call types
enum CallType { incoming, outgoing, missed, voiceMail, rejected, blocked, answeredExternally }
