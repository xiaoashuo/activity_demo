package com.lovecyy.activity.demo.service.impl;

import com.lovecyy.activity.demo.service.ActTaskService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Auther: ys
 * @Date: 2019/5/13 13:50
 * @Description:
 */
@Service
public class ActTaskServiceImpl implements ActTaskService {

    @Autowired
    private RuntimeService runtimeService;


    @Override
    public String startProcessInstance(String ProcessKey) {
        try {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ProcessKey);
            return   processInstance.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean isProcessing(String processInstanceId) {
        ProcessInstance pi = runtimeService//表示正在执行的流程实例和执行对象
                .createProcessInstanceQuery()//创建流程实例查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        return pi==null?false:true;
    }


}
