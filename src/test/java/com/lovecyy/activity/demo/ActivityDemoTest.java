package com.lovecyy.activity.demo;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipInputStream;

/**
 * @Auther: ys
 * @Date: 2019/5/13 08:58
 * @Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivityDemoTest {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    @Autowired
    HistoryService historyService;

    @Test
    public void testDeployProcessDef(){
        //获取流程定义与部署相关Service
        Deployment deployment =repositoryService
                .createDeployment()     //创建一个部署对象
                .name("helloworld入门程序")
                .addClasspathResource("diagrams/UserBmp.bpmn")//加载资源文件
                .deploy();//完成部署
        System.out.println(deployment.getId());
        System.out.println(deployment.getName());
    }
    /**部署流程定义（从zip）*/
    @Test
    public void deploymentProcessDefinition_zip(){
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("diagrams/helloworld.zip");
        ZipInputStream zipInputStream = new ZipInputStream(in);
        Deployment deployment = repositoryService//与流程定义和部署对象相关的Service
                .createDeployment()//创建一个部署对象
                .name("流程定义")//添加部署的名称
                .addZipInputStream(zipInputStream)//指定zip格式的文件完成部署
                .deploy();//完成部署
        System.out.println("部署ID："+deployment.getId());//
        System.out.println("部署名称："+deployment.getName());//
        //　通过流程定义的key启动流程实例，这时打开数据库act_ru_execution表，
        // ID_表示执行对象ID，PROC_INST_ID_表示流程实例ID，如果是单例流程
        // （没有分支和聚合），那么流程实例ID和执行对象ID是相同的。
    }

    /**
     * 启动流程实例
     * act-ru-execution 正在执行的执行对象表
     * act-hi-procinst 流程实列的历史表
     * act-ru-task 正在执行的任务表（只有节点是UserTask的时候 该表中存在数据）
     * act-hi-taskinst 任务历史表（只有节点是UserTask的时候 该表中存在数据）
     * act-hi-actinst 所有活动节点的历史表
     */
    @Test
    public void startProcessInstance(){
        //获取与正在执行的流程示例和执行对象相关的Service
        ProcessInstance processInstance = runtimeService
                //`act_re_procdef`的key值
                //使用流程定义的key启动实例，key对应bpmn文件中id的属性值，默认按照最新版本流程启动
                .startProcessInstanceByKey("myProcess_1");

        System.out.println(processInstance.getId());
        System.out.println(processInstance.getProcessDefinitionId());
    }

    /**
     * 查询当前的个人任务
     */
    @Test
    public void findPersonalTask(){
        //与正在执行的任务相关的Service  act-ru-task
        List<Task> list = taskService
                .createTaskQuery()  //创建查询任务对象
                .taskAssignee("王五")     //指定个人任务查询，指定办理人
                //                        .taskCandidateUser(candidateUser)//组任务的办理人查询
//                        .processDefinitionId(processDefinitionId)//使用流程定义ID查询
//                        .processInstanceId(processInstanceId)//使用流程实例ID查询
//                        .executionId(executionId)//使用执行对象ID查询
                /**排序*/
                .orderByTaskCreateTime().asc()//使用创建时间的升序排列
                /**返回结果集*/
//                        .singleResult()//返回惟一结果集
//                        .count()//返回结果集的数量
//                        .listPage(firstResult, maxResults);//分页查询
                .list(); //返回列表
        if(list != null && list.size() > 0){
            for(Task task : list){
                System.out.println(task.getId());
                System.out.println(task.getName());
                System.out.println(task.getCreateTime());
                System.out.println(task.getAssignee());
                System.out.println(task.getProcessInstanceId());
                System.out.println(task.getExecutionId());
                System.out.println(task.getProcessDefinitionId());
            }
        }
    }

    /**
     * 完成我的任务
     */
    @Test
    public void completePersonalTask(){
       taskService
                .complete("15002");
    }

    /**查询流程定义*/
    @Test
    public void findProcessDefinition(){
        List<ProcessDefinition> list = repositoryService//与流程定义和部署对象相关的Service
                .createProcessDefinitionQuery()//创建一个流程定义的查询
                /**指定查询条件,where条件*/
//                        .deploymentId(deploymentId)//使用部署对象ID查询
//                        .processDefinitionId(processDefinitionId)//使用流程定义ID查询
//                        .processDefinitionKey(processDefinitionKey)//使用流程定义的key查询
//                        .processDefinitionNameLike(processDefinitionNameLike)//使用流程定义的名称模糊查询

                /**排序*/
                .orderByProcessDefinitionVersion().asc()//按照版本的升序排列
//                        .orderByProcessDefinitionName().desc()//按照流程定义的名称降序排列

                /**返回的结果集*/
                .list();//返回一个集合列表，封装流程定义
//                        .singleResult();//返回惟一结果集
//                        .count();//返回结果集数量
//                        .listPage(firstResult, maxResults);//分页查询
        if(list!=null && list.size()>0){
            for(ProcessDefinition pd:list){
                System.out.println("流程定义ID:"+pd.getId());//流程定义的key+版本+随机生成数
                System.out.println("流程定义的名称:"+pd.getName());//对应helloworld.bpmn文件中的name属性值
                System.out.println("流程定义的key:"+pd.getKey());//对应helloworld.bpmn文件中的id属性值
                System.out.println("流程定义的版本:"+pd.getVersion());//当流程定义的key值相同的相同下，版本升级，默认1
                System.out.println("资源名称bpmn文件:"+pd.getResourceName());
                System.out.println("资源名称png文件:"+pd.getDiagramResourceName());
                System.out.println("部署对象ID："+pd.getDeploymentId());
                System.out.println("#########################################################");
            }
        }
    }
    /***附加功能：查询最新版本的流程定义*/
    @Test
    public void findLastVersionProcessDefinition(){
        List<ProcessDefinition> list = repositoryService//
                .createProcessDefinitionQuery()//
                .orderByProcessDefinitionVersion().asc()//使用流程定义的版本升序排列
                .list();
        /**
         * Map<String,ProcessDefinition>
         map集合的key：流程定义的key
         map集合的value：流程定义的对象
         map集合的特点：当map集合key值相同的情况下，后一次的值将替换前一次的值
         */
        Map<String, ProcessDefinition> map = new LinkedHashMap<String, ProcessDefinition>();
        if(list!=null && list.size()>0){
            for(ProcessDefinition pd:list){
                map.put(pd.getKey(), pd);
            }
        }
        List<ProcessDefinition> pdList = new ArrayList<ProcessDefinition>(map.values());
        if(pdList!=null && pdList.size()>0){
            for(ProcessDefinition pd:pdList){
                System.out.println("流程定义ID:"+pd.getId());//流程定义的key+版本+随机生成数
                System.out.println("流程定义的名称:"+pd.getName());//对应helloworld.bpmn文件中的name属性值
                System.out.println("流程定义的key:"+pd.getKey());//对应helloworld.bpmn文件中的id属性值
                System.out.println("流程定义的版本:"+pd.getVersion());//当流程定义的key值相同的相同下，版本升级，默认1
                System.out.println("资源名称bpmn文件:"+pd.getResourceName());
                System.out.println("资源名称png文件:"+pd.getDiagramResourceName());
                System.out.println("部署对象ID："+pd.getDeploymentId());
                System.out.println("#########################################################");
            }
        }
    }
    /**删除流程定义*/
    @Test
    public void deleteProcessDefinition(){
        //使用部署ID，完成删除
        String deploymentId = "10001";
        /**
         * 不带级联的删除
         *    只能删除没有启动的流程，如果流程启动，就会抛出异常
         */
//        processEngine.getRepositoryService()//
//                        .deleteDeployment(deploymentId);

        /**
         * 级联删除
         *       不管流程是否启动，都能可以删除
         */
        repositoryService//
                .deleteDeployment(deploymentId, true);
        System.out.println("删除成功！");
    }
    /**附加功能：删除流程定义（删除key相同的所有不同版本的流程定义）*/
    @Test
    public void deleteProcessDefinitionByKey(){
        //流程定义的key
        String processDefinitionKey = "helloworld";
        //先使用流程定义的key查询流程定义，查询出所有的版本
        List<ProcessDefinition> list = repositoryService//
                .createProcessDefinitionQuery()//
                .processDefinitionKey(processDefinitionKey)//使用流程定义的key查询
                .list();
        //遍历，获取每个流程定义的部署ID
        if(list!=null && list.size()>0){
            for(ProcessDefinition pd:list){
                //获取部署ID
                String deploymentId = pd.getDeploymentId();
                repositoryService//
                        .deleteDeployment(deploymentId, true);
            }
        }
    }
    /**查看流程图
     * @throws IOException */
    @Test
    public void viewPic() throws IOException {
        /**将生成图片放到文件夹下*/
        String deploymentId = "1";
        //获取图片资源名称
        List<String> list = repositoryService//
                .getDeploymentResourceNames(deploymentId);
        //定义图片资源的名称
        String resourceName = "";
        if(list!=null && list.size()>0){
            for(String name:list){
                if(name.indexOf(".png")>=0){
                    resourceName = name;
                }
            }
        }
        //获取图片的输入流
        InputStream in = repositoryService//
                .getResourceAsStream(deploymentId, resourceName);
        //将图片生成到D盘的目录下
        File file = new File("D:/"+resourceName);
        //将输入流的图片写到D盘下
        FileUtils.copyInputStreamToFile(in, file);
    }

    /**查询流程状态（判断流程正在执行，还是结束）*/
    @Test
    public void isProcessEnd(){
        String processInstanceId = "12501";
        ProcessInstance pi = runtimeService//表示正在执行的流程实例和执行对象
                .createProcessInstanceQuery()//创建流程实例查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        if(pi==null){
            System.out.println("流程已经结束");
        }
        else{
            System.out.println(pi.getDeploymentId());
            System.out.println("流程没有结束");
        }
    }

    /**查询历史任务*/
    @Test
    public void findHistoryTask(){
        String taskAssignee = "provider";
        List<HistoricTaskInstance> list =historyService//与历史数据（历史表）相关的Service
                .createHistoricTaskInstanceQuery()//创建历史任务实例查询
                .taskAssignee(taskAssignee)//指定历史任务的办理人
                .list();
        if(list!=null && list.size()>0){
            for(HistoricTaskInstance hti:list){
                System.out.println(hti.getId()+"    "+hti.getName()+"    "+hti.getProcessInstanceId()+"   "+hti.getStartTime()+"   "+hti.getEndTime()+"   "+hti.getDurationInMillis());
                System.out.println("################################");
            }
        }
    }
    /**查询历史流程实例*/
    @Test
    public void findHistoryProcessInstance(){
        String processInstanceId = "25001";
        HistoricProcessInstance hpi = historyService//与历史数据（历史表）相关的Service
                .createHistoricProcessInstanceQuery()//创建历史流程实例查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        System.out.println(hpi.getId()+"    "+hpi.getProcessDefinitionId()+"    "+hpi.getStartTime()+"    "+hpi.getEndTime()+"     "+hpi.getDurationInMillis());
    }

    /**
     * 流程变量
     * 涉及的表
     * act-ru-variable 正在执行的流程变量表
     * act-hi-varinst 历史的流程变量表
     */
    /**模拟设置和获取流程变量的场景 */
    @Test
    public void setAndGetVariables(){
        //使用执行对象ID设置
        //参数（executionId,variableName,value）设置单个变量 `act_ru_execution`的id
        runtimeService.setVariable("25001", "xiaoxi", "张三");
        //一次性设置多个变量  executionId   map<String,Object>
      //  runtimeService.setVariables("25001", variables);

        //使用任务ID设置
        //单个变量
        taskService.setVariable("27511", "zs", "1245");
        //多个变量
       // taskService.setVariables(taskId, variables);

        //启动流程实例的同时设置
        Map<String,Object> map=new HashMap<>();
        map.put("ws","xaaxa");
       // (processDefinitionKey, variables); 第一个参数 为`act_re_procdef` 的key第二个为map
        runtimeService.startProcessInstanceByKey("myProcess_1", map);

        //完成任务的同时设置
        taskService.complete("27511", map);

        /**获取流程变量*/
        //使用执行对象ID和流程变量的名称，获取流程变量的值executionId variableName
        runtimeService.getVariable("25001", "xiaoxi");
        //使用执行对象ID，获取所有的流程变量，将流程变量放置到Map集合中，map集合的key就是流程变量的名称，map集合的value就是流程变量的值
        runtimeService.getVariables("25001");
        //使用执行对象ID，获取流程变量的值，通过设置流程变量的名称存放到集合中，获取指定流程变量名称的流程变量的值，值存放到Map集合中
        List<String> names=new ArrayList<String>();
        names.add("xiaoxi");
        runtimeService.getVariables("25001", names);

        //使用任务ID和流程变量的名称，获取流程变量的值
        taskService.getVariable("27511", "xiaoxi");
        //使用任务ID，获取所有的流程变量，将流程变量放置到Map集合中，map集合的key就是流程变量的名称，map集合的value就是流程变量的值
        taskService.getVariables("27511");
        //使用任务ID，获取流程变量的值，通过设置流程变量的名称存放到集合中，获取指定流程变量名称的流程变量的值，值存放到Map集合中
        Map<String, Object> variables = taskService.getVariables("27511", names);
        Set<Map.Entry<String, Object>> entries = variables.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            System.out.println(entry.getValue());
        }
    }

    /**设置流程变量 */
    @Test
    public void setVariables(){

        //任务ID
        String taskId = "50004";
        //一、设置流程变量，使用基本数据类型
        taskService.setVariableLocal(taskId,"请假天数",3);//local与当前task绑定，下一个task不可见
        taskService.setVariable(taskId,"请假日期",new Date());
        taskService.setVariable(taskId,"请假原因","回家探亲");
        //二：设置流程变量，使用javabean类型
        /**
         * 当一个javabean（实现序列号）放置到流程变量中，要求javabean的属性不能再发生变化
         *    * 如果发生变化，再获取的时候，抛出异常
         *
         * 解决方案：在Person对象中添加：
         *         private static final long serialVersionUID = 6757393795687480331L;
         *      同时实现Serializable
         * */
       /* Person p = new Person();
        p.setId(20);
        p.setName("翠花");
        taskService.setVariable(taskId, "人员信息(添加固定版本)", p);*/

        System.out.println("流程变量设置成功");
    }

    /**获取流程变量 */
    @Test
    public void getVariables(){

        //任务ID
        String taskId = "55002";
        /**一：获取流程变量，使用基本数据类型*/
        Integer days = (Integer) taskService.getVariable(taskId, "请假天数");
        Date date = (Date) taskService.getVariable(taskId, "请假日期");
        String resean = (String) taskService.getVariable(taskId, "请假原因");
        System.out.println("请假天数："+days);
        System.out.println("请假日期："+date);
        System.out.println("请假原因："+resean);
        /**二：获取流程变量，使用javabean类型*/
      /*  Person p = (Person)taskService.getVariable(taskId, "人员信息(添加固定版本)");
        System.out.println(p.getId()+"        "+p.getName());*/
    }

    /**查询流程变量的历史表*/
    @Test
    public void findHistoryProcessVariables(){
        List<HistoricVariableInstance> list = historyService//
                .createHistoricVariableInstanceQuery()//创建一个历史的流程变量查询对象
                .variableName("ws")
                .list();
        if(list!=null && list.size()>0){
            for(HistoricVariableInstance hvi:list){
                System.out.println(hvi.getId()+"   "+hvi.getProcessInstanceId()+"   "+hvi.getVariableName()+"   "+hvi.getVariableTypeName()+"    "+hvi.getValue());
                System.out.println("###############################################");
            }
        }
    }
}
