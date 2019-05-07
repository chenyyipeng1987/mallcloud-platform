package com.mallplus.goods.controller;


import com.mallplus.goods.service.IPmsProductAttributeCategoryService;
import com.mallplus.goods.service.IPmsProductCategoryService;
import com.mallplus.goods.service.IPmsProductConsultService;
import com.mallplus.goods.service.IPmsProductService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Auther: shenzhuan
 * @Date: 2019/4/2 15:02
 * @Description:
 */
@RestController
@Api(tags = "AuthPmsController", description = "商品关系管理")
@RequestMapping("/auth")
public class AuthPmsController {

    @Autowired
    private IPmsProductConsultService pmsProductConsultService;
    @Resource
    private IPmsProductService pmsProductService;
    @Resource
    private IPmsProductAttributeCategoryService productAttributeCategoryService;
    @Resource
    private IPmsProductCategoryService productCategoryService;





}
