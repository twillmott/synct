package io.twillmott.synct.service;

import com.uwetrottmann.trakt5.TraktV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TraktService {

    private final TraktV2 trakt;

    @Autowired
    public TraktService(TraktV2 trakt) {
        this.trakt = trakt;
    }

    public void syncTraktLibrary() {
        log.info("Starting synchronisation of Trakt Account");
    }
}
