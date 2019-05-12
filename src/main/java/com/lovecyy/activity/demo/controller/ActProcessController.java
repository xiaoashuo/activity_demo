package com.lovecyy.activity.demo.controller;

import com.lovecyy.activity.demo.domain.ProcessDefinitionDto;
import com.lovecyy.activity.demo.service.ActProcessService;
import com.lovecyy.activity.demo.utils.AjaxResult;
import com.lovecyy.activity.demo.utils.TableDataInfo;
import org.activiti.engine.repository.Model;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * 流程管理
 * @Auther: ys
 * @Date: 2019/5/11 17:27
 * @Description:
 */
@Controller
@RequestMapping(value = "/activity/process")
public class ActProcessController {
    private String prefix = "process";

    @Autowired
    private ActProcessService actProcessService;

    @GetMapping("")
    public String process()
    {
        return prefix + "/process";
    }

    @PostMapping("list")
    @ResponseBody
    public TableDataInfo list(ProcessDefinitionDto processDefinitionDto)
    {
        return actProcessService.selectProcessDefinitionList(processDefinitionDto);
    }
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@RequestParam String category, @RequestParam("file") MultipartFile file)
            throws IOException
    {
        InputStream fileInputStream = file.getInputStream();
        String fileName = file.getOriginalFilename();
        return actProcessService.saveNameDeplove(fileInputStream, fileName, category);
    }

    @GetMapping(value = "/convertToModel/{processId}")
    @ResponseBody
    public AjaxResult convertToModel(@PathVariable("processId") String processId)
    {
        try
        {
            Model model = actProcessService.convertToModel(processId);
            return AjaxResult.success(String.format("转换模型成功，模型编号[{}]", model.getId()));
        }
        catch (Exception e)
        {
            return AjaxResult.error("转换模型失败" + e.getMessage());
        }
    }

    @GetMapping(value = "/resource/{imageName}/{deploymentId}")
    public void viewImage(@PathVariable("imageName") String imageName,
                          @PathVariable("deploymentId") String deploymentId, HttpServletResponse response)
    {
        try
        {
            InputStream in = actProcessService.findImageStream(deploymentId, imageName);
            for (int bit = -1; (bit = in.read()) != -1;)
            {
                response.getOutputStream().write(bit);
            }
            in.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return actProcessService.deleteProcessDefinitionByDeploymentIds(ids)? AjaxResult.success():AjaxResult.error();
    }
}
