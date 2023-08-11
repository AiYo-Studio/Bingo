package com.aiyostudio.bingo.cron;

import com.aiyostudio.bingo.cacheframework.manager.CacheManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

/**
 * @author AiYo Studio - Blank038
 */
public class CronJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        String jobKey = context.getMergedJobDataMap().getString("JobKey");
        CacheManager.getDataSource().loadJobResetData(jobKey);
        CacheManager.getDataSource().resetJobCache(jobKey, context.getFireTime());
    }
}
