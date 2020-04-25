package io.twillmott.synct.util;

import java.time.ZoneId;

public class SynctUtils {

    public static ZoneId utcZoneId() {
        return ZoneId.of("UTC");
    }
}
