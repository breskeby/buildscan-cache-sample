package com.gradle.spike

import com.google.common.base.Functions
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.gradle.scan.plugin.BuildScanExtension
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.internal.tasks.TaskExecutionOutcome
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.tasks.TaskState

import java.text.NumberFormat

class TaskCacheStatusExecutionListener implements TaskExecutionListener {
    private int totalTasks
    private int cacheMissCount

    private final Map<TaskExecutionOutcome, Integer> taskCounts = Maps.newEnumMap(
            Maps.toMap(Arrays.asList(TaskExecutionOutcome.values()), Functions.constant(0))
    );
    private final Set<Task> allTasks
    private final BuildScanExtension buildScans

    public TaskCacheStatusExecutionListener(TaskExecutionGraph graph, BuildScanExtension buildScans) {
        allTasks = Sets.newConcurrentHashSet(graph.getAllTasks())
        this.buildScans = buildScans

    }

    @Override
    void beforeExecute(Task task) {
    }

    @Override
    public void afterExecute(Task task, TaskState state) {
        allTasks.remove(task)
        totalTasks++

        TaskStateInternal stateInternal = (TaskStateInternal) state;
        TaskExecutionOutcome outcome = stateInternal.getOutcome();
        def counter = taskCounts.get(outcome)
        taskCounts.put(outcome, counter + 1);
        if (stateInternal.isCacheable() && outcome == TaskExecutionOutcome.EXECUTED) {
            buildScans.value("Cachable Task ${task.path}", "cache miss")
            cacheMissCount++
        }
        if (allTasks.isEmpty()) {
            // that was the last task to execute. let's push cache statistics to cs via custom values
            def allExecutedTasks = taskCounts.get(TaskExecutionOutcome.EXECUTED)
            buildScans.value("Total number of tasks skipped", statisticsLine(taskCounts.get(TaskExecutionOutcome.SKIPPED), totalTasks))
            buildScans.value("Total number of tasks up-to-date", statisticsLine(taskCounts.get(TaskExecutionOutcome.UP_TO_DATE), totalTasks))
            buildScans.value("Total number of tasks loaded from cache", statisticsLine(taskCounts.get(TaskExecutionOutcome.FROM_CACHE), totalTasks))
            buildScans.value("Total number of tasks cache miss", statisticsLine(cacheMissCount, totalTasks))

            int nonCacheableExecutedTasks = allExecutedTasks - cacheMissCount;
            buildScans.value("Total number of tasks not cachable", statisticsLine(nonCacheableExecutedTasks, totalTasks))
        }
    }

    private String statisticsLine(int fraction, int total) {
        int numberLength = Integer.toString(total).length();
        String percent = String.format("(%s)", roundedPercentOf(fraction, total));
        return String.format("%" + numberLength + "d %6s", fraction, percent);
    }

    private static String roundedPercentOf(int fraction, int total) {
        double out = total == 0 ? 0 : Math.round(100d * fraction / total) / 100d;
        return NumberFormat.getPercentInstance(Locale.US).format(out);
    }
}
