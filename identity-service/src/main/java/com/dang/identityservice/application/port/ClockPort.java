package com.dang.identityservice.application.port;

import java.time.Instant;

public interface ClockPort {
    Instant now();
}
