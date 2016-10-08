package com.gradle.spike

import com.google.common.base.Functions
import com.google.common.collect.Maps
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.internal.tasks.TaskExecutionOutcome
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.tasks.TaskState

class TaskCacheStatusExecutionListener implements TaskExecutionListener, TaskExecutionOutcomeStatisticsProvider {
    private int totalTasks
    private int cacheMissCount

    private final Map<TaskExecutionOutcome, Integer> taskCounts = Maps.newEnumMap(
            Maps.toMap(Arrays.asList(TaskExecutionOutcome.values()), Functions.constant(0))
    );
    private final def buildScans

    public TaskCacheStatusExecutionListener(def buildScans) {
        this.buildScans = buildScans
    }

    @Override
    void beforeExecute(Task task) {
    }

    @Override
    public void afterExecute(Task task, TaskState state) {
        totalTasks++

        TaskStateInternal stateInternal = (TaskStateInternal) state;
        TaskExecutionOutcome outcome = stateInternal.getOutcome();
        def counter = taskCounts.get(outcome)
        taskCounts.put(outcome, counter + 1);
        if (stateInternal.isCacheable() && outcome == TaskExecutionOutcome.EXECUTED) {
            buildScans.value("Task ${task.path}", "cache miss")
            cacheMissCount++
        }
    }

    public int getTotalTaskCount() {
        totalTasks
    }

    public int getExecutedTaskCount() {
        taskCounts.get(TaskExecutionOutcome.EXECUTED)
    }

    public int getSkippedTaskCount() {
        taskCounts.get(TaskExecutionOutcome.SKIPPED)
    }

    public int getUptodateTaskCount() {
        taskCounts.get(TaskExecutionOutcome.UP_TO_DATE)
    }

    public int getCachedTaskCount() {
        taskCounts.get(TaskExecutionOutcome.FROM_CACHE)
    }

    public int getCacheMissTaskCount() {
        cacheMissCount
    }

}
