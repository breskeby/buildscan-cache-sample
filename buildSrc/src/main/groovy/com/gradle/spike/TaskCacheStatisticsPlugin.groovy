package com.gradle.spike

import com.gradle.scan.plugin.BuildScanPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

public class TaskCacheStatisticsPlugin implements Plugin<Project> {


    public static final String GENERATE_CACHE_STATISTICS_TASKNAME = "generateCacheStatistics"

    @Override
    void apply(Project project) {
        if (project.rootProject != project) {
            throw new GradleException("Plugin must be applied on root project")
        }
        if (!project.gradle.startParameter.isTaskOutputCacheEnabled()) {
            return
        }


        project.plugins.withType(BuildScanPlugin) {
            project.buildScan.tag("Task Output Cache in use")

            project.gradle.startParameter.taskNames = project.gradle.startParameter.taskNames + GENERATE_CACHE_STATISTICS_TASKNAME

            TaskCacheStatusExecutionListener listener = new TaskCacheStatusExecutionListener(project.buildScan)
            GenerateCacheStatistics generateCacheStatisticsTask = project.tasks.create(GENERATE_CACHE_STATISTICS_TASKNAME, GenerateCacheStatistics)
            generateCacheStatisticsTask.conventionMapping.statisticsProvider = { listener }
            generateCacheStatisticsTask.conventionMapping.buildScan = { project.buildScan}
            project.gradle.taskGraph.addTaskExecutionListener(listener)

        }

    }
}
