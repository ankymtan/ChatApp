// wordChecker.aidl
package com.ankymtan.couplechat;

// Declare any non-default types here with import statements

interface WordChecker {

    String check( in String originalMessage);

    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
           // double aDouble, String aString);
}
