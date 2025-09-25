package ai.duclo.scimtest.common.helper;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeUtil {

    public static final String MM_DD_YYYY_HH_MM_SS_SSS = "MM-dd-yyyy HH:mm:ss.SSS";

    /**
     * Get current Epoch time in milliseconds for UTC
     *
     * @return current Epoch time in milliseconds
     */
    public long getEpoch() {
        Instant instant = Instant.now();
        return instant.toEpochMilli();
    }

    public static String convertToDateFormat(long epochTime) {
        Instant instant = Instant.ofEpochMilli(epochTime);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(MM_DD_YYYY_HH_MM_SS_SSS);
        return localDateTime.format(formatter);
    }

    public static long convertToEpochTime(String dateString) {
        long epochTime = 0L;
        if (StringUtils.hasText(dateString)) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                    .ofPattern(MM_DD_YYYY_HH_MM_SS_SSS);
            LocalDateTime localDateTime = LocalDateTime.parse(dateString, dateTimeFormatter);
            if (localDateTime != null) {
                epochTime = localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
            }
        }
        return epochTime;
    }
}
