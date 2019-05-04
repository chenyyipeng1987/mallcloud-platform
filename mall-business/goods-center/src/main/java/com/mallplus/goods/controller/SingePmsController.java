package com.mallplus.goods.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.central.common.annotation.IgnoreAuth;
import com.central.common.annotation.SysLog;
import com.central.common.model.PmsProduct;
import com.central.common.utils.CommonResult;
import com.mallplus.goods.entity.PmsProductAttributeCategory;
import com.mallplus.goods.entity.PmsProductCategory;
import com.mallplus.goods.entity.PmsProductConsult;
import com.mallplus.goods.service.IPmsProductAttributeCategoryService;
import com.mallplus.goods.service.IPmsProductCategoryService;
import com.mallplus.goods.service.IPmsProductConsultService;
import com.mallplus.goods.service.IPmsProductService;
import com.mallplus.goods.vo.ConsultTypeCount;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: shenzhuan
 * @Date: 2019/4/2 15:02
 * @Description:
 */
@RestController
@Api(tags = "SingePmsController", description = "商品关系管理")
@RequestMapping("/api/single/pms")
public class SingePmsController  {

    @Autowired
    private IPmsProductConsultService pmsProductConsultService;
    @Resource
    private IPmsProductService pmsProductService;
    @Resource
    private IPmsProductAttributeCategoryService productAttributeCategoryService;
    @Resource
    private IPmsProductCategoryService productCategoryService;



    @SysLog(MODULE = "pms", REMARK = "查询商品详情信息")
    @IgnoreAuth
    @GetMapping(value = "/goods/detail")
    @ApiOperation(value = "查询商品详情信息")
    public Object queryProductDetail(@RequestParam(value = "id", required = false, defaultValue = "0") Long id) {
        PmsProduct productResult = pmsProductService.getById(id);
        return new CommonResult().success(productResult);
    }

    @IgnoreAuth
    @ApiOperation("获取某个商品的评价")
    @RequestMapping(value = "/consult/list", method = RequestMethod.GET)
    @ResponseBody
    public Object list(@RequestParam(value = "goodsId", required = false, defaultValue = "0") Long goodsId,
                       @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                       @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize) {

        PmsProductConsult productConsult = new PmsProductConsult();
        productConsult.setGoodsId(goodsId);
        List<PmsProductConsult> list =  pmsProductConsultService.list(new QueryWrapper<>(productConsult));

        int goods = 0;
        int general = 0;
        int bad = 0;
        ConsultTypeCount count = new ConsultTypeCount();
        for (PmsProductConsult consult : list) {
            if (consult.getStoreId() != null) {
                if (consult.getStoreId() == 1) {
                    goods++;
                }
                if (consult.getStoreId() == 2) {
                    general++;
                }
                if (consult.getStoreId() == 3) {
                    bad++;
                }
            }
        }
        count.setAll(goods + general + bad);
        count.setBad(bad);
        count.setGeneral(general);
        count.setGoods(goods);
        List<PmsProductConsult> productConsults = pmsProductConsultService.list(new QueryWrapper<>(productConsult));
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("list", productConsults);
        objectMap.put("count", count);
        return new CommonResult().success(objectMap);
    }
    @SysLog(MODULE = "pms", REMARK = "查询商品列表")
    @IgnoreAuth
    @ApiOperation(value = "查询商品列表")
    @GetMapping(value = "/goods/list")
    public Object goodsList(PmsProduct product,
                              @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
                              @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {
        product.setPublishStatus(1);
        product.setVerifyStatus(1);
        return new CommonResult().success(pmsProductService.page(new Page<PmsProduct>(pageNum, pageSize), new QueryWrapper<>(product)));
    }


    @SysLog(MODULE = "pms", REMARK = "查询商品分类列表")
    @IgnoreAuth
    @ApiOperation(value = "查询商品分类列表")
    @GetMapping(value = "/productCategory/list")
    public Object productCategoryList(PmsProductCategory productCategory,
                          @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
                          @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {
        return new CommonResult().success(productCategoryService.page(new Page<PmsProductCategory>(pageNum, pageSize), new QueryWrapper<>(productCategory)));
    }
    @SysLog(MODULE = "pms", REMARK = "查询商品分类列表")
    @IgnoreAuth
    @ApiOperation(value = "查询商品分类列表")
    @GetMapping(value = "/categoryAndGoodsList/list")
    public Object categoryAndGoodsList(PmsProductCategory productCategory) {
       List<PmsProductAttributeCategory> productAttributeCategoryList = productAttributeCategoryService.list(new QueryWrapper<>());
        for (PmsProductAttributeCategory gt : productAttributeCategoryList) {
            PmsProduct productQueryParam = new PmsProduct();
            productQueryParam.setProductAttributeCategoryId(gt.getId());
            productQueryParam.setPublishStatus(1);
            productQueryParam.setVerifyStatus(1);
            gt.setGoodsList(pmsProductService.list(new QueryWrapper<>(productQueryParam)));
        }
        return new CommonResult().success(productAttributeCategoryList);
    }

    @SysLog(MODULE = "pms", REMARK = "查询商品列表")
    @IgnoreAuth
    @ApiOperation(value = "查询首页推荐品牌")
    @GetMapping(value = "/recommendBrand/list")
    public Object getRecommendBrandList(
            @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {

        return new CommonResult().success(pmsProductService.getRecommendBrandList(1,1));
    }

    @SysLog(MODULE = "pms", REMARK = "查询商品列表")
    @IgnoreAuth
    @ApiOperation(value = "查询首页新品")
    @GetMapping(value = "/newProductList/list")
    public Object getNewProductList(
            @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {

        return new CommonResult().success(pmsProductService.getRecommendBrandList(1,1));
    }

    @SysLog(MODULE = "pms", REMARK = "查询商品列表")
    @IgnoreAuth
    @ApiOperation(value = "查询首页推荐商品")
    @GetMapping(value = "/hotProductList/list")
    public Object getHotProductList(
            @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {

        return new CommonResult().success(pmsProductService.getHotProductList(1,1));
    }




}
