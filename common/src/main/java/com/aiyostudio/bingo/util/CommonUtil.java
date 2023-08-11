package com.aiyostudio.bingo.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Blank038
 */
public class CommonUtil {
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");

    public static String formatDate(Date date) {
        if (date == null) {
            return "197001010000";
        }
        return SIMPLE_DATE_FORMAT.format(date);
    }
}
