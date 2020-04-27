package io.twillmott.synct.events.publisher;

import io.twillmott.synct.events.event.TraktAuthorizedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class TraktAuthorizedEventPublisher {

    ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public TraktAuthorizedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publish(Object source, final boolean authorized) {
        applicationEventPublisher.publishEvent(new TraktAuthorizedEvent(source, authorized));
    }
}
