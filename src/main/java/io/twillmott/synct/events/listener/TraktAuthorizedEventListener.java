package io.twillmott.synct.events.listener;

import io.twillmott.synct.events.event.TraktAuthorizedEvent;
import io.twillmott.synct.service.TraktService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class TraktAuthorizedEventListener implements ApplicationListener<TraktAuthorizedEvent> {

    private final TraktService traktService;

    @Autowired
    TraktAuthorizedEventListener(TraktService traktService) {
        this.traktService = traktService;
    }

    @Async
    @Override
    public void onApplicationEvent(TraktAuthorizedEvent traktAuthorizedEvent) {
        if (traktAuthorizedEvent.isAuthorized()) {
            traktService.syncTraktLibrary();
        }
    }
}
