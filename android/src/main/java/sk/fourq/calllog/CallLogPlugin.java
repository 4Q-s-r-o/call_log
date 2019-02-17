package sk.fourq.calllog;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.CallLog;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@TargetApi(Build.VERSION_CODES.M)
public class CallLogPlugin implements MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {

    private static final String ALREADY_RUNNING = "ALREADY_RUNNING";
    private static final String PERMISSION_NOT_GRANTED = "PERMISSION_NOT_GRANTED";
    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private final Registrar registrar;
    private MethodCall request;
    private Result result;


    private CallLogPlugin(Registrar registrar) {
        this.registrar = registrar;
    }

    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "sk.fourq.call_log");
        final CallLogPlugin callLogPlugin = new CallLogPlugin(registrar);
        channel.setMethodCallHandler(callLogPlugin);
        registrar.addRequestPermissionsResultListener(callLogPlugin);
    }

    @Override
    public void onMethodCall(MethodCall c, Result r) {
        if (request != null) {
            r.error(ALREADY_RUNNING, "Method call was cancelled. One method call is already running", null);
        }

        request = c;
        result = r;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED == registrar.activity().checkSelfPermission(Manifest.permission.READ_CALL_LOG)) {
                handleCall();
            } else {
                String[] perm = {Manifest.permission.READ_CALL_LOG};
                registrar.activity().requestPermissions(perm, 0);
            }
        } else {
            handleCall();
        }
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] strings, int[] grantResults) {
        if (requestCode == 0 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (request != null) {
                handleCall();
            }
            return true;
        } else {
            if (result != null) {
                result.error(PERMISSION_NOT_GRANTED, null, null);
                cleanup();
            }
            return false;
        }
    }

    private void handleCall() {
        switch (request.method) {
            case "get":
                queryLogs(null);
                break;
            case "query":
                String dateFrom = request.argument("dateFrom");
                String dateTo = request.argument("dateTo");
                String durationFrom = request.argument("durationFrom");
                String durationTo = request.argument("durationTo");
                String name = request.argument("name");
                String number = request.argument("number");
                String type = request.argument("type");

                List<String> predicates = new ArrayList<>();
                if (dateFrom != null) {
                    predicates.add(CallLog.Calls.DATE + " > " + dateFrom);
                }
                if (dateTo != null) {
                    predicates.add(CallLog.Calls.DATE + " < " + dateTo);
                }
                if (durationFrom != null) {
                    predicates.add(CallLog.Calls.DURATION + " > " + durationFrom);
                }
                if (durationTo != null) {
                    predicates.add(CallLog.Calls.DURATION + " < " + durationTo);
                }
                if (name != null) {
                    predicates.add(CallLog.Calls.CACHED_NAME + " LIKE '%" + name + "%'");
                }
                if (number != null) {
                    predicates.add(CallLog.Calls.NUMBER + " LIKE '%" + number + "%'");
                }
                if (type != null) {
                    predicates.add(CallLog.Calls.TYPE + " = " + type);
                }

                if (predicates.size() == 0) {
                    queryLogs(null);
                } else {
                    StringBuilder whereCondition = new StringBuilder();
                    for (String predicate : predicates) {
                        whereCondition.append((whereCondition.length() == 0) ? "" : " AND ").append(predicate);
                    }
                    queryLogs(whereCondition.toString());
                }
                break;
            default:
                result.notImplemented();
                cleanup();
        }
    }

    private static final String[] PROJECTION = {
            CallLog.Calls.CACHED_FORMATTED_NUMBER,
            CallLog.Calls.CACHED_MATCHED_NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.CACHED_NAME
    };

    private void queryLogs(String query) {
        try (Cursor cursor = registrar.context().getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                PROJECTION,
                query,
                null,
                CallLog.Calls.DATE + " DESC"
        )) {
            List<HashMap<String, Object>> entries = new ArrayList<>();
            while (cursor != null && cursor.moveToNext()) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("formattedNumber", cursor.getString(0));
                map.put("number", cursor.getString(1));
                map.put("callType", cursor.getInt(2));
                map.put("timestamp", cursor.getLong(3));
                map.put("duration", cursor.getInt(4));
                map.put("name", cursor.getString(5));
                entries.add(map);
            }
            result.success(entries);
            cleanup();
        } catch (Exception e) {
            result.error(INTERNAL_ERROR, e.getMessage(), null);
            cleanup();
        }
    }

    private void cleanup() {
        request = null;
        result = null;
    }
}