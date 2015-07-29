package com.ankymtan.couplechat.framework;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ankymtan.couplechat.WordChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by An on 3/6/2015.
 */
public class PluginManager extends Binder{
    public static final String ACTION_PICK_PLUGIN = "aexp.intent.action.PICK_PLUGIN";
    static final String KEY_PKG = "pkg";
    static final String KEY_SERVICENAME = "servicename";
    static final String KEY_ACTIONS = "actions";
    static final String KEY_CATEGORIES = "categories";
    static final String BUNDLE_EXTRAS_CATEGORY = "category";
    static final String LOG_TAG = "PluginManager";
    static final String CATEGORIES = "aexp.intent.category.WORD_CHECKER_PLUGIN";
    static final String BY_ME = "by me";
    private Context context;



    private PackageBroadcastReceiver packageBroadcastReceiver;
    private IntentFilter packageFilter;
    private ArrayList<HashMap<String, String>> services;
    private ArrayList<String> categories;

    private OpServiceConnection opServiceConnection;
    private WordChecker wordCheckerService;

    public PluginManager(Context context) {
        this.context = context;
        fillPluginList();

        packageBroadcastReceiver = new PackageBroadcastReceiver();
        packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addCategory(Intent.CATEGORY_DEFAULT);
        packageFilter.addDataScheme("package");

        bindOpService();

    }

    private void fillPluginList() {
        services = new ArrayList<HashMap<String, String>>();
        categories = new ArrayList<String>();
        PackageManager packageManager = context.getPackageManager();
        Intent baseIntent = new Intent(ACTION_PICK_PLUGIN);
        baseIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
        List<ResolveInfo> list = packageManager.queryIntentServices(baseIntent,
                PackageManager.GET_RESOLVED_FILTER);
        Log.d(LOG_TAG, "fillPluginList: " + list);
        for (int i = 0; i < list.size(); ++i) {
            ResolveInfo info = list.get(i);
            ServiceInfo sinfo = info.serviceInfo;
            IntentFilter filter = info.filter;
            Log.d(LOG_TAG, "fillPluginList: i: " + i + "; sinfo: " + sinfo + ";filter: " + filter);
            if (sinfo != null) {
                HashMap<String, String> item = new HashMap<String, String>();
                item.put(KEY_PKG, sinfo.packageName);
                item.put(KEY_SERVICENAME, sinfo.name);
                String firstCategory = null;
                if (filter != null) {
                    StringBuilder actions = new StringBuilder();
                    for (Iterator<String> actionIterator = filter.actionsIterator(); actionIterator.hasNext(); ) {
                        String action = actionIterator.next();
                        if (actions.length() > 0)
                            actions.append(",");
                        actions.append(action);
                    }
                    StringBuilder categories = new StringBuilder();
                    for (Iterator<String> categoryIterator = filter.categoriesIterator();
                         categoryIterator.hasNext(); ) {
                        String category = categoryIterator.next();
                        if (firstCategory == null)
                            firstCategory = category;
                        if (categories.length() > 0)
                            categories.append(",");
                        categories.append(category);
                    }
                    item.put(KEY_ACTIONS, new String(actions));
                    item.put(KEY_CATEGORIES, new String(categories));
                } else {
                    item.put(KEY_ACTIONS, "<null>");
                    item.put(KEY_CATEGORIES, "<null>");
                }
                if (firstCategory == null)
                    firstCategory = "";
                categories.add(firstCategory);
                services.add(item);
            }
        }
        Log.d(LOG_TAG, "services: " + services);
        Log.d(LOG_TAG, "categories: " + categories);
    }
    class PackageBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            services.clear();
            fillPluginList();
        }
    }

    public void onStart() {
        Log.d(LOG_TAG, "onStart");
        context.registerReceiver(packageBroadcastReceiver, packageFilter);
    }

    public void onStop() {
        Log.d(LOG_TAG, "onStop");
        context.unregisterReceiver(packageBroadcastReceiver);
        releaseOpService();
    }

    /////////////////////
    //take from InvokeOp
    ////////////////////////////////

    private void bindOpService() {
        opServiceConnection = new OpServiceConnection();
        Intent i = new Intent(this.ACTION_PICK_PLUGIN);
        i.addCategory(CATEGORIES);
        //android 5.0+ only accept explicit intent.
        context.bindService(createExplicitFromImplicitIntent(context, i), opServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void releaseOpService() {
        context.unbindService(opServiceConnection);
        opServiceConnection = null;
    }
    //main method that create the result
    public String check(String originalMessage) {

        Log.d(BY_ME, "checking message");
        try {
            return wordCheckerService.check(originalMessage);
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.d(BY_ME, "empty message");
            return null;
        }
    }

    public boolean isPositive(){
        try {
            return wordCheckerService.isPositive();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }


    //wordCheckerService is a instant of auto-gen class base on aidl file
    //all service are init within this class
    class OpServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder boundService) {
            wordCheckerService = WordChecker.Stub.asInterface(boundService);
            Log.d(LOG_TAG, "onServiceConnected");
        }

        public void onServiceDisconnected(ComponentName className) {
            wordCheckerService = null;
            Log.d(LOG_TAG, "onServiceDisconnected");
        }
    }
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        //Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        //Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        //Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        //Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        //Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    //get service here
}
