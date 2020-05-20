package io.twillmott.synct.util;


import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static java.util.Objects.isNull;

public class SynctUtils {

    public static ZoneId utcZoneId() {
        return ZoneId.of("UTC");
    }

    /**
     * A convoluted way to convert to {@link OffsetDateTime} from {@link org.threeten.bp.OffsetDateTime} because trakt java
     * uses this silly library.
     */
    public static OffsetDateTime toOffsetDateTime(org.threeten.bp.OffsetDateTime threeTenDateTime) {
        if (isNull(threeTenDateTime)) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(threeTenDateTime.toEpochSecond()), utcZoneId());
    }

}
