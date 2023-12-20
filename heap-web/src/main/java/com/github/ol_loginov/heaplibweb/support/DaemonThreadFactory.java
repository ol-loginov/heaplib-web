package com.github.ol_loginov.heaplibweb.support;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

public class DaemonThreadFactory extends CustomizableThreadFactory {
    public DaemonThreadFactory() {
        super();
        setDaemon(true);
    }
}
