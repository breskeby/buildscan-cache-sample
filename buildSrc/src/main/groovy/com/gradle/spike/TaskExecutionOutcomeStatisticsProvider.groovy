package com.gradle.spike

interface TaskExecutionOutcomeStatisticsProvider {
    public int getTotalTaskCount();

    public int getExecutedTaskCount()

    public int getSkippedTaskCount()

    public int getUptodateTaskCount()

    public int getCachedTaskCount()

    public int getCacheMissTaskCount()
}