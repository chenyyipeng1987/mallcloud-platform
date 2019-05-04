package com.mallplus.cms.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.central.common.annotation.IgnoreAuth;
import com.central.common.annotation.SysLog;
import com.central.common.utils.CommonResult;
import com.mallplus.cms.entity.CmsSubject;
import com.mallplus.cms.entity.CmsSubjectCategory;
import com.mallplus.cms.entity.CmsSubjectComment;
import com.mallplus.cms.entity.CmsTopic;
import com.mallplus.cms.service.ICmsSubjectCategoryService;
import com.mallplus.cms.service.ICmsSubjectCommentService;
import com.mallplus.cms.service.ICmsSubjectService;
import com.mallplus.cms.service.ICmsTopicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Auther: shenzhuan
 * @Date: 2019/4/2 15:02
 * @Description:
 */
@RestController
@Api(tags = "SingeCmsController", description = "内容关系管理")
@RequestMapping("/api/single/cms")
public class SingeCmsController  {



    @Resource
    private ICmsSubjectCategoryService subjectCategoryService;
    @Resource
    private ICmsSubjectService subjectService;
    @Resource
    private ICmsSubjectCommentService commentService;
    @Resource
    private ICmsTopicService topicService;
    @IgnoreAuth
    @SysLog(MODULE = "cms", REMARK = "查询文章列表")
    @ApiOperation(value = "查询文章列表")
    @GetMapping(value = "/subject/list")
    public Object subjectList(CmsSubject subject,
                              @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
                              @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {
        return new CommonResult().success(subjectService.page(new Page<CmsSubject>(pageNum, pageSize), new QueryWrapper<>(subject)));
    }
    @SysLog(MODULE = "pms", REMARK = "查询商品详情信息")
    @IgnoreAuth
    @GetMapping(value = "/subject/detail")
    @ApiOperation(value = "查询商品详情信息")
    public Object queryProductDetail(@RequestParam(value = "id", required = false, defaultValue = "0") Long id) {
        CmsSubject productResult = subjectService.getById(id);
        return new CommonResult().success(productResult);
    }

    @SysLog(MODULE = "cms", REMARK = "查询文章分类列表")
    @IgnoreAuth
    @ApiOperation(value = "查询文章分类列表")
    @GetMapping(value = "/subjectCategory/list")
    public Object cateList(CmsSubjectCategory subjectCategory,
                          @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
                          @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {
        return new CommonResult().success(subjectCategoryService.page(new Page<CmsSubjectCategory>(pageNum, pageSize), new QueryWrapper<>(subjectCategory)));
    }

    @SysLog(MODULE = "cms", REMARK = "查询文章评论列表")
    @IgnoreAuth
    @ApiOperation(value = "查询文章评论列表")
    @GetMapping(value = "/subjectComment/list")
    public Object subjectList(CmsSubjectComment subjectComment,
                              @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
                              @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {
        return new CommonResult().success(commentService.page(new Page<CmsSubjectComment>(pageNum, pageSize), new QueryWrapper<>(subjectComment)));
    }

    @SysLog(MODULE = "pms", REMARK = "查询商品列表")
    @IgnoreAuth
    @ApiOperation(value = "查询首页推荐文章")
    @GetMapping(value = "/recommendSubjectList/list")
    public Object getRecommendSubjectList(
            @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {

        return new CommonResult().success(subjectService.getRecommendSubjectList(1,1));
    }
    @SysLog(MODULE = "cms", REMARK = "查询文章评论列表")
    @IgnoreAuth
    @ApiOperation(value = "查询文章评论列表")
    @GetMapping(value = "/topic/list")
    public Object subjectList(CmsTopic topic,
                              @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
                              @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {
        return new CommonResult().success(topicService.page(new Page<CmsTopic>(pageNum, pageSize), new QueryWrapper<>(topic)));
    }
    @SysLog(MODULE = "pms", REMARK = "查询商品详情信息")
    @IgnoreAuth
    @GetMapping(value = "/topic/detail")
    @ApiOperation(value = "查询商品详情信息")
    public Object topicDetail(@RequestParam(value = "id", required = false, defaultValue = "0") Long id) {
        CmsTopic productResult = topicService.getById(id);
        return new CommonResult().success(productResult);
    }
}
