package com.xiaopo.flying.poiphoto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * DateUtil to beautify long millions
 * Created by Flying SnowBean on 16-4-4.
 */
public class DateUtil {
    public static String formatDate(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
        return simpleDateFormat.format(new Date(time));
    }
}
