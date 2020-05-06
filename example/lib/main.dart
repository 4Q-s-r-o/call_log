import 'package:call_log/call_log.dart';
import 'package:flutter/material.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  Iterable<CallLogEntry> _callLogEntries = [];

  @override
  Widget build(BuildContext context) {
    var mono = TextStyle(fontFamily: 'monospace');
    var children = <Widget>[];
    _callLogEntries.forEach((entry) {
      children.add(
        Column(
          children: <Widget>[
            Divider(),
            Text('F. NUMBER: ${entry.formattedNumber}', style: mono),
            Text('NUMBER   : ${entry.number}', style: mono),
            Text('NAME     : ${entry.name}', style: mono),
            Text('TYPE     : ${entry.callType}', style: mono),
            Text('DATE     : ${DateTime.fromMillisecondsSinceEpoch(entry.timestamp)}', style: mono),
            Text('DURATION :  ${entry.duration}', style: mono),
          ],
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.start,
        ),
      );
    });

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: Text('call_log example')),
        body: SingleChildScrollView(
          child: Column(
            children: <Widget>[
              Center(
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: RaisedButton(
                    onPressed: () async {
                      var result = await CallLog.query();
                      setState(() {
                        _callLogEntries = result;
                      });
                    },
                    child: Text("Get all"),
                  ),
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: Column(children: children),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
