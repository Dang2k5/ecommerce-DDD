package com.dang.orderservice.infrastructure.persistence.jpa;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.outbox")
public class OutboxProperties {
    private boolean enabled = true;
    private long pollMs = 1000;
    private int batchSize = 100;
    private int maxRetry = 50;
    private boolean useSkipLocked = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public long getPollMs() { return pollMs; }
    public void setPollMs(long pollMs) { this.pollMs = pollMs; }

    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

    public int getMaxRetry() { return maxRetry; }
    public void setMaxRetry(int maxRetry) { this.maxRetry = maxRetry; }

    public boolean isUseSkipLocked() { return useSkipLocked; }
    public void setUseSkipLocked(boolean useSkipLocked) { this.useSkipLocked = useSkipLocked; }
}
