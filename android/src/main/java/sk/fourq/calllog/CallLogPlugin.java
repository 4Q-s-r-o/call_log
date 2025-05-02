package sk.fourq.calllog;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@TargetApi(Build.VERSION_CODES.M)
public class CallLogPlugin
        implements FlutterPlugin, ActivityAware, MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {

    private static final String TAG = "flutter/CALL_LOG";
    private static final String ALREADY_RUNNING = "ALREADY_RUNNING";
    private static final String PERMISSION_NOT_GRANTED = "PERMISSION_NOT_GRANTED";
    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    private static final String METHOD_GET = "get";
    private static final String METHOD_QUERY = "query";
    private static final String METHOD_DELETE = "delete";
    private static final String METHOD_EXPORT = "export";
    private static final String OPERATOR_LIKE = "LIKE";
    private static final String OPERATOR_GT = ">";
    private static final String OPERATOR_LT = "<";
    private static final String OPERATOR_EQUALS = "=";

    private static final String[] CURSOR_PROJECTION = {
            CallLog.Calls.CACHED_FORMATTED_NUMBER,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.CACHED_NUMBER_TYPE,
            CallLog.Calls.CACHED_NUMBER_LABEL,
            CallLog.Calls.CACHED_MATCHED_NUMBER,
            CallLog.Calls.PHONE_ACCOUNT_ID,
            CallLog.Calls._ID
    };

    private MethodCall request;
    private Result result;
    private ActivityPluginBinding activityPluginBinding;
    private Activity activity;
    private Context ctx;

    private void init(BinaryMessenger binaryMessenger, Context applicationContext) {
        Log.d(TAG, "init. Messanger:" + binaryMessenger + " Context:" + applicationContext);
        final MethodChannel channel = new MethodChannel(binaryMessenger, "sk.fourq.call_log");
        channel.setMethodCallHandler(this);
        ctx = applicationContext;
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        Log.d(TAG, "onAttachedToEngine");
        init(flutterPluginBinding.getBinaryMessenger(), flutterPluginBinding.getApplicationContext());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        // NO-OP
        Log.d(TAG, "onDetachedFromEngine");
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
        this.activityPluginBinding = activityPluginBinding;
        activityPluginBinding.addRequestPermissionsResultListener(this);
        activity = activityPluginBinding.getActivity();
        Log.d(TAG, "onAttachedToActivity");
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        Log.d(TAG, "onDetachedFromActivityForConfigChanges");
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {
        Log.d(TAG, "onReattachedToActivityForConfigChanges");
    }

    @Override
    public void onDetachedFromActivity() {
        Log.d(TAG, "onDetachedFromActivity");
        if (activityPluginBinding != null) {
            activityPluginBinding.removeRequestPermissionsResultListener(this);
            activityPluginBinding = null;
            activity = null;
        }
    }

    @Override
    public void onMethodCall(MethodCall c, Result r) {
        Log.d(TAG, "onMethodCall");
        if (request != null) {
            r.error(ALREADY_RUNNING, "Method call was cancelled. One method call is already running", null);
        }

        request = c;
        result = r;

        String[] perm = { Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_CALL_LOG };
        if (hasPermissions(perm)) {
            handleMethodCall();
        } else {
            if (activity != null) {
                ActivityCompat.requestPermissions(activity, perm, 0);
            } else {
                r.error("MISSING_PERMISSIONS",
                        "Permission READ_CALL_LOG, WRITE_CALL_LOG or READ_PHONE_STATE is required for plugin. Hovewer, plugin is unable to request permission because of background execution.",
                        null);
            }
        }
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] strings, int[] grantResults) {
        if (requestCode == 0) {
            // CHECK IF ALL REQUESTED PERMISSIONS ARE GRANTED
            for (int grantResult : grantResults) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (result != null) {
                        result.error(PERMISSION_NOT_GRANTED, null, null);
                        cleanup();
                    }
                    return false;
                }
            }
            if (request != null) {
                handleMethodCall();
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

    /**
     * Handler for flutter method call
     */
    private void handleMethodCall() {
        switch (request.method) {
            case METHOD_GET:
                queryLogs(null);
                break;
            case METHOD_QUERY:
                String dateFrom = request.argument("dateFrom");
                String dateTo = request.argument("dateTo");
                String durationFrom = request.argument("durationFrom");
                String durationTo = request.argument("durationTo");
                String name = request.argument("name");
                String number = request.argument("number");
                String type = request.argument("type");
                String cachedMatchedNumber = request.argument("cachedMatchedNumber");
                String phoneAccountId = request.argument("phoneAccountId");

                List<String> predicates = new ArrayList<>();
                generatePredicate(predicates, CallLog.Calls.DATE, OPERATOR_GT, dateFrom);
                generatePredicate(predicates, CallLog.Calls.DATE, OPERATOR_LT, dateTo);
                generatePredicate(predicates, CallLog.Calls.DURATION, OPERATOR_GT, durationFrom);
                generatePredicate(predicates, CallLog.Calls.DURATION, OPERATOR_LT, durationTo);
                generatePredicate(predicates, CallLog.Calls.CACHED_NAME, OPERATOR_LIKE, name);
                generatePredicate(predicates, CallLog.Calls.TYPE, OPERATOR_EQUALS, type);
                if (!StringUtils.isEmpty(number)) {
                    List<String> namePredicates = new ArrayList<>();
                    generatePredicate(namePredicates, CallLog.Calls.NUMBER, OPERATOR_LIKE, number);
                    generatePredicate(namePredicates, CallLog.Calls.CACHED_MATCHED_NUMBER, OPERATOR_LIKE, number);
                    generatePredicate(namePredicates, CallLog.Calls.PHONE_ACCOUNT_ID, OPERATOR_LIKE, number);
                    predicates.add("(" + StringUtils.join(namePredicates, " OR ") + ")");
                }
                queryLogs(StringUtils.join(predicates, " AND "));
                break;
            case METHOD_DELETE:
                String id = request.argument("id");
                deleteCallLog(id);
                break;
            case METHOD_EXPORT:
                exportCallLog(request.argument("filename"));
                break;
            default:
                result.notImplemented();
                cleanup();
        }
    }

    /**
     * Main query method
     *
     * @param query String with sql search condition
     */
    private void queryLogs(String query) {
        SubscriptionManager subscriptionManager = ContextCompat.getSystemService(ctx, SubscriptionManager.class);
        List<SubscriptionInfo> subscriptions = null;
        if (subscriptionManager != null) {
            subscriptions = subscriptionManager.getActiveSubscriptionInfoList();
        }
        try (Cursor cursor = ctx.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                CURSOR_PROJECTION,
                query,
                null,
                CallLog.Calls.DATE + " DESC")) {
            List<HashMap<String, Object>> entries = new ArrayList<>();
            while (cursor != null && cursor.moveToNext()) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("formattedNumber", cursor.getString(0));
                map.put("number", cursor.getString(1));
                map.put("callType", cursor.getInt(2));
                map.put("timestamp", cursor.getLong(3));
                map.put("duration", cursor.getInt(4));
                map.put("name", cursor.getString(5));
                map.put("cachedNumberType", cursor.getInt(6));
                map.put("cachedNumberLabel", cursor.getString(7));
                map.put("cachedMatchedNumber", cursor.getString(8));
                map.put("simDisplayName", getSimDisplayName(subscriptions, cursor.getString(9)));
                map.put("phoneAccountId", cursor.getString(9));
                map.put("_id", cursor.getString(10));
                entries.add(map);
            }
            result.success(entries);
            cleanup();
        } catch (Exception e) {
            result.error(INTERNAL_ERROR, e.getMessage(), null);
            cleanup();
        }
    }

    /**
     * Helper method that tries to obtian sim display name from accountId
     *
     * @param subscriptions Subscriptions - should represent sim cards
     * @param accountId     Id of account to search for
     * @return Name of the used sim card, null otherwise
     */
    private String getSimDisplayName(List<SubscriptionInfo> subscriptions, String accountId) {
        if (accountId != null && subscriptions != null) {
            for (SubscriptionInfo info : subscriptions) {
                if (Integer.toString(info.getSubscriptionId()).equals(accountId) ||
                        accountId.contains(info.getIccId())) {
                    return String.valueOf(info.getDisplayName());
                }
            }
        }
        return null;
    }

    /**
     * Helper method to check if permissions were granted
     *
     * @param permissions Permissions to check
     * @return false, if any permission is not granted, true otherwise
     */
    private boolean hasPermissions(String[] permissions) {
        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(ctx, perm)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method to generate new predicate
     *
     * @param predicates Generated predicate will be appended to this list
     * @param field      Field to search in
     * @param operator   Operator to use for comparision
     * @param value      Value to search for
     */
    private void generatePredicate(List<String> predicates, String field, String operator, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        String escapedValue;
        if (operator.equalsIgnoreCase(OPERATOR_LIKE)) {
            escapedValue = "'%" + value + "%'";
        } else {
            escapedValue = "'" + value + "'";
        }
        predicates.add(field + " " + operator + " " + escapedValue);
    }

    private void deleteCallLog(String id) {
        try {
            int rowsDeleted;
            if (id != null) {
                // Delete specific call log entry
                rowsDeleted = ctx.getContentResolver().delete(
                        CallLog.Calls.CONTENT_URI,
                        CallLog.Calls._ID + " = ?",
                        new String[] { id });
            } else {
                // Delete all call logs
                rowsDeleted = ctx.getContentResolver().delete(
                        CallLog.Calls.CONTENT_URI,
                        null,
                        null);
            }
            result.success(rowsDeleted);
        } catch (Exception e) {
            result.error(INTERNAL_ERROR, e.getMessage(), null);
        }
        cleanup();
    }

    private void exportCallLog(String filename) {
        try {
            // Get Downloads directory
            java.io.File downloadsDir = android.os.Environment
                    .getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
            java.io.File file = new java.io.File(downloadsDir, filename);
            List<HashMap<String, Object>> entries = new ArrayList<>();
            try (Cursor cursor = ctx.getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    CURSOR_PROJECTION,
                    null,
                    null,
                    CallLog.Calls.DATE + " DESC")) {
                while (cursor != null && cursor.moveToNext()) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("formattedNumber", cursor.getString(0));
                    map.put("number", cursor.getString(1));
                    map.put("callType", cursor.getInt(2));
                    map.put("timestamp", cursor.getLong(3));
                    map.put("duration", cursor.getInt(4));
                    map.put("name", cursor.getString(5));
                    map.put("cachedNumberType", cursor.getInt(6));
                    map.put("cachedNumberLabel", cursor.getString(7));
                    map.put("cachedMatchedNumber", cursor.getString(8));
                    map.put("phoneAccountId", cursor.getString(9));
                    map.put("_id", cursor.getString(10));
                    entries.add(map);
                }
            }

            // Write to file
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                Gson gson = new Gson();
                writer.write(gson.toJson(entries));
            }

            HashMap<String, Object> response = new HashMap<>();
            response.put("count", entries.size());
            response.put("path", file.getAbsolutePath());
            result.success(response);
        } catch (Exception e) {
            result.error(INTERNAL_ERROR, e.getMessage(), null);
        }
        cleanup();
    }

    /**
     * Helper method to cleanup after method call
     */
    private void cleanup() {
        request = null;
        result = null;
    }
}
