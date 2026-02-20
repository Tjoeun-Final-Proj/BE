package com.tjoeun.boxmon.global.util;

public class AddressProcessor {
    public static String simplifiy(String address){
        String[] tokens = address.split(" ", 3);
        return tokens.length>=2?tokens[1]:null;
    }
}
