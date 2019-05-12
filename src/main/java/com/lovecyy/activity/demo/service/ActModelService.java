package com.lovecyy.activity.demo.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lovecyy.activity.demo.domain.ModelEntityDto;
import com.lovecyy.activity.demo.utils.AjaxResult;
import com.lovecyy.activity.demo.utils.TableDataInfo;
import org.activiti.engine.repository.Model;
import org.apache.batik.transcoder.TranscoderException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public interface ActModelService {
    /**
     * 查询模型列表
     *
     * @param modelEntityDto 模型信息
     * @return 模型集合
     */
    public TableDataInfo selectModelList(ModelEntityDto modelEntityDto);

    /**
     * 创建模型
     *
     * @param model 模型信息
     * @return 模型ID
     * @throws UnsupportedEncodingException
     */
    public String createModel(Model model) throws UnsupportedEncodingException;

    /**
     * 根据id获得模型
     * @param modelId
     * @return
     */
    public Model getById(String modelId);

    /**
     * 保存模型信息
     * @param model
     * @param json_xml
     * @param svg_xml
     * @throws IOException
     * @throws TranscoderException
     */
    public void update(Model model, String json_xml, String svg_xml) throws IOException, TranscoderException;
    /**
     * 查询模型编辑器
     *
     * @param modelId 模型ID
     * @return json信息
     */
    public ObjectNode selectWrapModelById(String modelId);

    /**
     * 批量删除模型信息
     *
     * @param ids 需要删除的数据ID
     * @return
     */
    public boolean deleteModelIds(String ids);

    /**
     * 发布模型为流程定义
     *
     * @param modelId 模型ID
     * @return
     * @throws Exception
     */
    public AjaxResult deployProcess(String modelId) throws IOException;

    /**
     * 获取资源文件信息
     *
     * @param modelId 模型ID
     * @return 资源文件信息
     */
    public byte[] getModelEditorSource(String modelId);
}
