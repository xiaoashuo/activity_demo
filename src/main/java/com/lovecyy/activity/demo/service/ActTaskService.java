package com.lovecyy.activity.demo.service;

public interface ActTaskService {
    /**
     * 开启流程实列
     * @param ProcessKey 流程定义的key
     * @return
     */
    public String startProcessInstance(String ProcessKey);

    /**
     * 判断进程状态
     * @param processInstanceId act_ru_task
     * @return true 流程未结束 false 流程已结束
     */
    public boolean isProcessing(String processInstanceId);
}
