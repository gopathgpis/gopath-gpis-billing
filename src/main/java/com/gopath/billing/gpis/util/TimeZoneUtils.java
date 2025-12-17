package com.gopath.billing.gpis.util;

import org.apache.commons.lang.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class TimeZoneUtils {

    public static String formateByInstant(long time, String timeZone, String format){
        Instant instant = Instant.ofEpochMilli(time);

        if(StringUtils.isEmpty(timeZone)){
            timeZone = TimeZone.getDefault().getID();
        }
        //时区转换
        ZonedDateTime zoneDate = ZonedDateTime.ofInstant(instant, ZoneId.of(timeZone));
        String formateTime = zoneDate.format(DateTimeFormatter.ofPattern(format));

        return formateTime;
    }
}
