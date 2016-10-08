package com.gradle.spike

import com.gradle.scan.plugin.BuildScanExtension
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.TaskAction

import java.text.NumberFormat
class GenerateCacheStatistics extends ConventionTask{

    TaskExecutionOutcomeStatisticsProvider statisticsProvider

    def buildScan;

    @TaskAction void generateCustomValues(){
        def provider = getStatisticsProvider()
        def totalTaskCount = provider.getTotalTaskCount()
        getBuildScan().value("Total number of tasks skipped", statisticsLine(provider.getSkippedTaskCount(), totalTaskCount))
        getBuildScan().value("Total number of tasks up-to-date", statisticsLine(provider.getUptodateTaskCount(), totalTaskCount))
        getBuildScan().value("Total number of tasks loaded from cache", statisticsLine(provider.getCachedTaskCount(), totalTaskCount))
        def cacheMissCount = provider.getCacheMissTaskCount()
        getBuildScan().value("Total number of tasks cache miss", statisticsLine(cacheMissCount, totalTaskCount))
        def allExecutedTasks = provider.getExecutedTaskCount()
        int nonCacheableExecutedTasks = allExecutedTasks - cacheMissCount;
        getBuildScan().value("Total number of tasks not cachable", statisticsLine(nonCacheableExecutedTasks, totalTaskCount))
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
