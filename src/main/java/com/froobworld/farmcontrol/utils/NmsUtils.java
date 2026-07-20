package com.froobworld.farmcontrol.utils;

import org.bukkit.Bukkit;

import static org.joor.Reflect.*;

public class NmsUtils {
    private static final String NMS_PACKAGE_NAME = on(Bukkit.getServer()).call("getHandle")
            .type()
            .getPackage()
            .getName();
    private static final boolean PRE_1_17 = NMS_PACKAGE_NAME.matches(".*v1_(?:8|9|10|11|12|13|14|15|16)_.*");

    public static String getFullyQualifiedClassName(String className, String post1_17PackageName) {
        return (PRE_1_17 ? NMS_PACKAGE_NAME : ("net.minecraft." + post1_17PackageName)) + "." + className;
    }

    public static String getFieldOrMethodName(String pre1_17Name, String post1_17Name) {
        return PRE_1_17 ? pre1_17Name : post1_17Name;
    }

}
