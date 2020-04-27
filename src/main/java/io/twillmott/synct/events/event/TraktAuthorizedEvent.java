package io.twillmott.synct.events.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TraktAuthorizedEvent extends ApplicationEvent {
    private boolean authorized;

    public TraktAuthorizedEvent(Object source, boolean authorized) {
        super(source);
        this.authorized = authorized;
    }
}
