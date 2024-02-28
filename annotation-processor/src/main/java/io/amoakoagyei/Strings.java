package io.amoakoagyei;

public class Strings {

    static boolean isNullOrEmpty(String value){
       return value == null || value.isEmpty();
    }

    public static boolean isNotNullOrEmpty(String value){
        return !isNullOrEmpty(value);
    }
}
