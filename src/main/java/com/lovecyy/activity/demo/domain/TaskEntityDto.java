package com.lovecyy.activity.demo.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Auther: ys
 * @Date: 2019/5/13 14:01
 * @Description:
 */
@Data
public class TaskEntityDto implements Serializable {
    //任务id
    private String id;
    //任务名称
    private String name;
    //代理人
    private String assignee;
    //流程定义id
    private String processDefinitionId;
    //任务执行id
    private String executionId;
    //流程实列id
    private String processInstanceId;
    //所有者
    private String owner;
    //描述
    private String description;
    //任务创建时间
    private Date createTime;
}
