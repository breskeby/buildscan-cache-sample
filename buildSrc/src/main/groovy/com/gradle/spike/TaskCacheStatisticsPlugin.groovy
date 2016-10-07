package com.gradle.spike

import com.gradle.scan.plugin.BuildScanExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener

public class TaskCacheStatisticsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (project.rootProject != project) {
            throw new GradleException("Plugin must be applied on root project")
        }
        if (!project.gradle.startParameter.isTaskOutputCacheEnabled()) {
            return
        }

        project.pluginManager.withPlugin("com.gradle.build-scan") {
            def buildScanExtension = project.getExtensions().getByType(BuildScanExtension)
            buildScanExtension.tag("Task Output Cache in use")
            project.gradle.taskGraph.addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
                @Override
                void graphPopulated(TaskExecutionGraph taskExecutionGraph) {
                    taskExecutionGraph.addTaskExecutionListener(new TaskCacheStatusExecutionListener(taskExecutionGraph, buildScanExtension))
                }
            })
        }

    }
}
