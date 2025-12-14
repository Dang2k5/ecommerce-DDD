package com.dang.identityservice.infrastructure.time;

import com.dang.identityservice.application.port.ClockPort;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SystemClockPort implements ClockPort {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
