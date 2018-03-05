package com.athul.nightwing.module;

import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by athul on 17/8/17.
 */

public class Tets implements IXposedHookZygoteInit,IXposedHookInitPackageResources ,IXposedHookLoadPackage{

    private static String MODULE_PATH = null;

    private static String packages="null";
    XSharedPreferences xSharedPreferences,appShared;

    public static final String GLOBAL_ACTION_KEY_POWER = "power";
    public static final String GLOBAL_ACTION_KEY_AIRPLANE = "airplane";
    public static final String GLOBAL_ACTION_KEY_SILENT = "silent";
    public static final String GLOBAL_ACTION_KEY_USERS = "users";
    public static final String GLOBAL_ACTION_KEY_SETTINGS = "settings";
    public static final String GLOBAL_ACTION_KEY_LOCKDOWN = "lockdown";

    //TODO external storage can be blocked on Environment class
    //TODO SIM card details should be blocked
    //TODO Un appropirate app launches should be blocked by finding category
    //TODO Youtube should be blocked, give it a try.
    //TODO Call blocker in an efficient way


    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam initPackageResourcesParam) throws Throwable {
        XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, initPackageResourcesParam.res);
        Utils.changeDrawerIcon(initPackageResourcesParam,modRes);


    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        //TODO
        XSharedPreferences pref = new XSharedPreferences("me.entri.entrime", "tab_settings");
        pref.makeWorldReadable();
        String text = pref.getString("TAB_MODE", "");

        if(text.isEmpty()||text==null){
            lockAll(loadPackageParam);
        }else {
            try {
                if(text.equals("MEENTRIENTRIME_UNLOCK_TABLET_FULL")){
                    //TODO Nothing
                }
                if(text.equals("MEENTRIENTRIME_LOCK_TABLET_LOCK")||
                        text.equals("MEENTRIENTRIME_UNLOCK_TABLET_PARTIAL")){

                    lockAll(loadPackageParam);
                }
            }catch (Exception e){

                lockAll(loadPackageParam);

            }
        }





    }

    private void lockAll(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if(loadPackageParam.packageName.equals("android")&&loadPackageParam.processName.equals("android")){
            final Class<?> packageParserClass = XposedHelpers.findClass(
                    "android.content.pm.PackageParser", loadPackageParam.classLoader);

          /* XposedBridge.hookAllMethods(packageParserClass, "loadApkIntoAssetManager",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                           try{
                               Log.e("WTKLV","CHECK FOR TMP");
                               if(param.args[1].toString().contains(".tmp")){

                                   xSharedPreferences=new XSharedPreferences("com.android.providers.media",Constants.sharedPreferenceName);
                                   xSharedPreferences.makeWorldReadable();
                                   if(xSharedPreferences.getString(Constants.downloadIdentifierKey,"error").contains("block")){
                                       param.args[2]=0;
                                       param.args[1]="podaa";
                                       Log.e("WTKLV","BLOCK FOUND");
                                   }
                                   if(xSharedPreferences.getString(Constants.downloadIdentifierKey,"error").contains("normal")){
                                       param.args[2]=0;
                                       param.args[1]="podaa";
                                       Log.e("WTKLV","NORMAL FOUND");
                                   }
                                   if(xSharedPreferences.getString(Constants.downloadIdentifierKey,"error").contains("error")){
                                       param.args[2]=0;
                                       param.args[1]="podaa";
                                       Log.e("WTKLV","ERROR FOUND");
                                   }
                                   if(xSharedPreferences.getString(Constants.downloadIdentifierKey,"error").contains("entri")){
                                       Log.e("WTKLV","ALLOWED APP FOUND");
                                   }

                               }
                           }catch (Exception e){
                               Log.e("WTKLV",e.getLocalizedMessage());
                           }
                        }
                    }); */

            XposedBridge.hookAllMethods(packageParserClass, "parsePackage", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if (param.args[0] instanceof File){

                         if(((File)param.args[0]).getAbsolutePath().endsWith(".tmp")){
                              xSharedPreferences=new XSharedPreferences("com.android.providers.downloads",Constants.sharedPreferenceName);
                              xSharedPreferences.makeWorldReadable();
                              if(xSharedPreferences.getString(Constants.downloadIdentifierKey,"error").contains("block")){
                                  Log.e("WTKLV","BLOCK");
                                    try{
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                              }
                              if(xSharedPreferences.getString(Constants.downloadIdentifierKey,"error").contains("normal")){
                                  Log.e("WTKLV","NORMAL");
                                  try{
                                  }catch (Exception e){
                                      e.printStackTrace();
                                  }

                              }
                              if(xSharedPreferences.getString(Constants.downloadIdentifierKey,"error").contains("error")){
                                  Log.e("WTKLV","ERROR");

                              }
                              if(xSharedPreferences.getString(Constants.downloadIdentifierKey,"error").contains("entri")){

                              }
                          }
                        }


                    }catch (Exception e){
                        Log.e("WTKLV",e.getMessage());
                    }

                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });

        }


        switch (loadPackageParam.packageName){

            case "com.android.launcher3":
                Utils.gsbHook(loadPackageParam);
                Utils.launcherHook(loadPackageParam);
                Utils.widgetHook(loadPackageParam);
                break;
            case "com.android.settings":
                Utils.removeFieldsFromSettings(loadPackageParam,loadPackageParam.classLoader);
                break;

            case "com.kingroot.kinguser":
                Utils.hookAppLaunching(loadPackageParam);
                break;

            case "com.android.packageinstaller":
                Utils.restrictAppUninstallation(loadPackageParam);
                break;

            case "com.google.android.youtube":
                Utils.hookYoutubeLaunching(loadPackageParam);
                break;

            case "android":
                Utils.USBMenuHook(loadPackageParam);
                break;

            case "com.android.dialer":
                Utils.phoneHook(loadPackageParam);
                break;

            case "com.android.incallui":
                //TODO this package needs a revision
                Utils.inCallHook(loadPackageParam);
                break;

            case "com.android.phone":
                Utils.incomingHook(loadPackageParam);
                break;

            case "com.android.contacts":
                Utils.contactsHook(loadPackageParam);
                break;

            case "com.android.camera":
                Utils.cameraAppHook(loadPackageParam);
                break;
            case "com.android.systemui":
                // Utils.recentsHook(loadPackageParam);
                Utils.usbStorageNotificationHook(loadPackageParam);
                break;
            case "com.android.vending":
                Utils.playStoreHook(loadPackageParam);
                break;
            case "com.android.providers.downloads":
                Utils.newDownloadHook(loadPackageParam);
                break;

            case "com.android.providers.media":
                xSharedPreferences=new XSharedPreferences("com.android.providers.media",Constants.sharedPreferenceName);
                xSharedPreferences.makeWorldReadable();
                break;

            case "com.athul.nightwing":
                appShared=new XSharedPreferences("com.athul.nightwing","my");
                appShared.makeWorldReadable();
                break;
            case "com.google.android.gm":
                Utils.notificationHook(loadPackageParam);
                Utils.hookAppLaunching(loadPackageParam);
                break;
            case "com.android.internal":
                Utils.hookScreenShot(loadPackageParam);
                break;
            case "com.google.android":
                Utils.talkHook(loadPackageParam);
                break;
            case "com.google.android.apps.uploader":
                Utils.galleryHook(loadPackageParam);
                break;
            case "com.google.android.talk":
                Utils.talkHook(loadPackageParam);
                break;
            case"com.android.gallery3d":
                Utils.gallery3d(loadPackageParam);
                Utils.notificationHook(loadPackageParam);
                break;
            case "com.opera.branding":
                Utils.notificationHook(loadPackageParam);
                break;
            case "kingoroot.supersu":
                Utils.notificationHook(loadPackageParam);
                break;
            case "com.myntra.android":
                Utils.notificationHook(loadPackageParam);
            case "com.opera.mini.android":
                Utils.notificationHook(loadPackageParam);
                break;
            case "com.google.android.street":
                Utils.notificationHook(loadPackageParam);
                Utils.hookAppLaunching(loadPackageParam);
                break;
            case "com.google.android.googlequicksearchbox":
                Utils.notificationHook(loadPackageParam);
                break;
            case "com.google.android.gms":
                Utils.notificationHook(loadPackageParam);
                break;
          /*
                case "com.athul.nightwing":
                Utils.notificationHook(loadPackageParam);
                break;

           */



        }

    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;


        //TODO
        try{
            ArrayList<String> itemsList = new ArrayList<String>();
            itemsList.add(GLOBAL_ACTION_KEY_POWER);
            String[] powerMenuItems = itemsList
                    .toArray(new String[itemsList.size()]);
            XResources.setSystemWideReplacement("android", "array",
                    "config_globalActionsList", powerMenuItems);
            int id=XResources.getFakeResId("config_globalActionsList");
            InputStream stream=XResources.getSystem().openRawResource(id);
        }catch (Exception e){
            Log.e("WTKLV",e.getLocalizedMessage());
        }


    }
    String convertToString(InputStream in){
        String resource = new Scanner(in).useDelimiter("\\Z").next();
        return resource;
    }




}
