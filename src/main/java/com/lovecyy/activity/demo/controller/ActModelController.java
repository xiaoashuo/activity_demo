package com.lovecyy.activity.demo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lovecyy.activity.demo.domain.ModelEntityDto;
import com.lovecyy.activity.demo.service.ActModelService;
import com.lovecyy.activity.demo.utils.AjaxResult;
import com.lovecyy.activity.demo.utils.TableDataInfo;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.persistence.entity.ModelEntity;
import org.activiti.engine.repository.Model;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_DESCRIPTION;
import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_NAME;

/**
 * @Auther: ys
 * @Date: 2019/5/11 12:48
 * @Description:
 */
@Controller
@RequestMapping(value = "/activity/model")
public class ActModelController {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public String model()
    {
        return "model/model";
    }

    @Autowired
    private ActModelService actModelService;
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ModelEntityDto modelEntityDto)
    {
        return actModelService.selectModelList(modelEntityDto);
    }
    /**
     * 创建一个空的模型
     * @return
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/add")
    public String add() throws UnsupportedEncodingException
    {
        //创建一个空的model模型
        Model model = repositoryService.newModel();
        //ModelEntity model=new ModelEntity();
        //设置一些默认信息
        String name = "new-process";
        String description = "新的流程";
        int revision = 1;
        String key = "process";
        //创建对象节点  作为标签信息
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);

        model.setName(name);
        model.setKey(key);
        model.setMetaInfo(modelNode.toString());
        String modelId = actModelService.createModel(model);

        return "redirect:/modeler.html?modelId="+modelId;
    }

    /**
     * 保存更新model
     * @param modelId
     * @param name
     * @param description
     * @param json_xml
     * @param svg_xml
     * @throws IOException
     * @throws TranscoderException
     */
    @PutMapping(value = "/{modelId}/save")
    @ResponseBody
    public void save(@PathVariable String modelId,
                      String name, String description,
                      String json_xml, String svg_xml)
            throws IOException, TranscoderException{

        Model model = actModelService.getById(modelId);
        ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
        modelJson.put(MODEL_NAME, name);
        modelJson.put(MODEL_DESCRIPTION, description);
        model.setMetaInfo(modelJson.toString());
        model.setName(name);
        actModelService.update(model, json_xml, svg_xml);
    }

    @GetMapping("/{modelId}/json")
    @ResponseBody
    public ObjectNode getEditorJson(@PathVariable String modelId)
    {
        ObjectNode modelNode = actModelService.selectWrapModelById(modelId);
        return modelNode;
    }

    @GetMapping("/edit/{modelId}")
    public String edit(@PathVariable("modelId") String modelId)
    {
        return "redirect:/modeler.html?modelId="+modelId;
    }

    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return actModelService.deleteModelIds(ids) ? AjaxResult.success() : AjaxResult.error();

    }

    @GetMapping("/deploy/{modelId}")
    @ResponseBody
    public AjaxResult deploy(@PathVariable("modelId") String modelId) throws Exception
    {
        return actModelService.deployProcess(modelId);
    }

    @RequestMapping("/export/{id}")
    public void exportToXml(@PathVariable("id") String id, HttpServletResponse response)
    {
        try
        {
            org.activiti.engine.repository.Model modelData = actModelService.getById(id);
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            JsonNode editorNode = new ObjectMapper().readTree(actModelService.getModelEditorSource(modelData.getId()));
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
            byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);

            ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
            IOUtils.copy(in, response.getOutputStream());
            String filename = bpmnModel.getMainProcess().getId() + ".bpmn20.xml";
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            response.flushBuffer();
        }
        catch (Exception e)
        {
            throw new ActivitiException("导出model的xml文件失败，模型ID=" + id, e);
        }
    }
}
