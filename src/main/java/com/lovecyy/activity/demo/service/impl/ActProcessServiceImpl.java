package com.lovecyy.activity.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lovecyy.activity.demo.domain.ProcessDefinitionDto;
import com.lovecyy.activity.demo.service.ActProcessService;
import com.lovecyy.activity.demo.utils.AjaxResult;
import com.lovecyy.activity.demo.utils.TableDataInfo;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

/**
 * @Auther: ys
 * @Date: 2019/5/11 17:15
 * @Description:
 */
@Service
public class ActProcessServiceImpl implements ActProcessService {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * 将流程定义转换成模型
     *
     * @param processId 流程编号
     * @return 模型数据
     * @throws Exception
     */
    @Override
    public Model convertToModel(String processId) throws Exception {
        // 流程定义查询对象，用于查询流程定义表----act_re_procdef
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processId).singleResult();
        //根据部署id和资源名称 加载资源成流
        InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(),
                processDefinition.getResourceName());
        //xml读取工厂
        XMLInputFactory xif = XMLInputFactory.newInstance();
        //字符输入流
        InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
        //xml流读取器
        XMLStreamReader xtr = xif.createXMLStreamReader(in);
        //把读取到的资源转成BpmnModel
        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
        //将资源转为json
        BpmnJsonConverter converter = new BpmnJsonConverter();
        ObjectNode modelNode = converter.convertToJson(bpmnModel);
        //创建一个空的模型 并为之填充数据
        Model modelData = repositoryService.newModel();
        modelData.setKey(processDefinition.getKey());
        modelData.setName(processDefinition.getResourceName());
        modelData.setCategory(processDefinition.getCategory());
        modelData.setDeploymentId(processDefinition.getDeploymentId());
        modelData.setVersion(Integer.parseInt(
                String.valueOf(repositoryService.createModelQuery().modelKey(modelData.getKey()).count() + 1)));
        ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
        modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, processDefinition.getName());
        modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, modelData.getVersion());
        modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, processDefinition.getDescription());
        modelData.setMetaInfo(modelObjectNode.toString());
        repositoryService.saveModel(modelData);
        repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));
        return modelData;
    }

    @Override
    public InputStream findImageStream(String deploymentId, String imageName) throws Exception {
        return repositoryService.getResourceAsStream(deploymentId, imageName);
    }

    @Override
    public TableDataInfo selectProcessDefinitionList(ProcessDefinitionDto processDefinition) {
        TableDataInfo data = new TableDataInfo();
        ProcessDefinitionQuery pdQuery = repositoryService.createProcessDefinitionQuery();
        if (StringUtils.isNotEmpty(processDefinition.getKey())){
            pdQuery.processDefinitionKey(processDefinition.getKey());
        }
        if (StringUtils.isNotEmpty(processDefinition.getName())){
            pdQuery.processDefinitionName(processDefinition.getName());
        }
        if (StringUtils.isNotEmpty(processDefinition.getDeploymentId())){
            pdQuery.deploymentId(processDefinition.getDeploymentId());
        }
        data.setTotal(pdQuery.count());
        data.setRows(pdQuery.orderByDeploymentId().desc()
                .listPage(processDefinition.getPageNum(), processDefinition.getPageSize()).stream()
                .map(ProcessDefinitionDto::new).collect(Collectors.toList()));
        return data;
    }

    @Override
    public AjaxResult saveNameDeplove(InputStream is, String fileName, String category) {
        ZipInputStream zipInputStream = null;
        try
        {
            String extension = FilenameUtils.getExtension(fileName);
            if (extension.equals("zip"))
            {
                zipInputStream = new ZipInputStream(is);
                // 创建流程定义
                Deployment deployment = repositoryService.createDeployment().name(fileName)
                        .addZipInputStream(zipInputStream).deploy();
                ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deployment.getId()).singleResult();
                repositoryService.setProcessDefinitionCategory(processDefinition.getId(), category);
                return AjaxResult.success(String.format("部署成功，流程编号[{}]", processDefinition.getId()));
            }
            else
            {
                throw new Exception("不支持的文件类型：" + extension);
            }
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 根据流程部署id，删除流程定义
     *
     * @param ids 部署ids
     * @return 结果
     */
    @Override
    public boolean deleteProcessDefinitionByDeploymentIds(String ids)
    {
        boolean result = true;
        try
        {
            String[] deploymentIds = ids.split(",");
            for (String deploymentId : deploymentIds)
            {
                // 级联删除，不管流程是否启动，都能可以删除
                repositoryService.deleteDeployment(deploymentId, true);
            }
        }
        catch (Exception e)
        {
            result = false;
            throw e;
        }
        return result;
    }
}
