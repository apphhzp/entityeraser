package net.apphhzp.entityeraser;

import apphhzp.lib.ClassHelper;

public final class AllReturn {
    public static boolean added=false;
    static {
		if (!added){
            ClassHelper.addExportImpl(AllReturn.class.getModule(),"net.apphhzp.entityeraser");
            added=true;
        }
		allReturn=false;
    }
    public static boolean allReturn;
}
