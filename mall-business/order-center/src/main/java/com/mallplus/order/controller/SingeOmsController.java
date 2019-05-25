package com.mallplus.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mallplus.common.annotation.IgnoreAuth;
import com.mallplus.common.annotation.SysLog;
import com.mallplus.common.feign.MemberFeignClient;
import com.mallplus.common.feign.PmsFeignClinent;
import com.mallplus.common.model.UmsMember;
import com.mallplus.common.utils.CommonResult;
import com.mallplus.order.entity.OmsCartItem;
import com.mallplus.order.entity.OmsOrder;
import com.mallplus.order.service.IOmsOrderService;
import com.mallplus.order.vo.GroupAndOrderVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Auther: shenzhuan
 * @Date: 2019/4/2 15:02
 * @Description:
 */
@RestController
@Api(tags = "SingeOmsController", description = "订单管理系统")
@RequestMapping("/api/single/oms")
public class SingeOmsController  {


    @Resource
    private IOmsOrderService orderService;
    @Resource
    private MemberFeignClient memberFeignClient;
    @Resource
    private PmsFeignClinent pmsFeignClinent;
    @IgnoreAuth
    @SysLog(MODULE = "oms", REMARK = "查询订单列表")
    @ApiOperation(value = "查询订单列表")
    @GetMapping(value = "/order/list")
    public Object orderList(OmsOrder order,
                              @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
                              @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {
        return new CommonResult().success(orderService.page(new Page<OmsOrder>(pageNum, pageSize), new QueryWrapper<>(order)));
    }
    @IgnoreAuth
    @SysLog(MODULE = "oms", REMARK = "查询订单列表")
    @ApiOperation(value = "查询订单列表")
    @GetMapping(value = "/cart/list")
    public Object cartList(OmsOrder order,
                            @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
                            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {
        return new CommonResult().success(orderService.page(new Page<OmsOrder>(pageNum, pageSize), new QueryWrapper<>(order)));
    }


    /**
     * 提交订单
     * @param orderParam
     * @return
     */
    @ApiOperation("商品详情预览订单")
    @SysLog(MODULE = "order", REMARK = "商品详情预览订单")
    @GetMapping(value = "/preOrder")
    public Object preOrder(GroupAndOrderVo orderParam) {
        return orderService.preSingelOrder(orderParam);
    }
    /**
     * 提交订单
     * @param orderParam
     * @return
     */
    @ApiOperation("商品详情生成订单")
    @SysLog(MODULE = "order", REMARK = "商品详情生成订单")
    @PostMapping(value = "/bookOrder")
    public Object bookOrder(GroupAndOrderVo orderParam) {

        return orderService.generateSingleOrder(orderParam);
    }

    @ApiOperation("添加商品到购物车")
    @RequestMapping(value = "/addCart")
    @ResponseBody
    public Object addCart(@RequestParam(value = "id", defaultValue = "0") Long id,
                          @RequestParam(value = "userId", defaultValue = "0") Long userId,
                          @RequestParam(value = "count", defaultValue = "1") Integer count) {
        UmsMember umsMember = memberFeignClient.findById(userId);
        PmsSkuStock pmsSkuStock = pmsSkuStockService.getById(id);
        if (pmsSkuStock != null && umsMember != null && umsMember.getId() != null) {
            OmsCartItem cartItem = new OmsCartItem();
            cartItem.setPrice(pmsSkuStock.getPrice());
            cartItem.setProductId(pmsSkuStock.getProductId());
            cartItem.setProductSkuCode(pmsSkuStock.getSkuCode());
            cartItem.setQuantity(count);
            cartItem.setProductSkuId(id);
//            cartItem.setProductAttr(pmsSkuStock.getMeno1());
            cartItem.setProductPic(pmsSkuStock.getPic());
            cartItem.setSp1(pmsSkuStock.getSp1());
            cartItem.setSp2(pmsSkuStock.getSp2());
            cartItem.setSp3(pmsSkuStock.getSp3());
            OmsCartItem omsCartItem = cartItemService.addCart(cartItem);
            return new CommonResult().success(omsCartItem.getId());

        }
        return new CommonResult().failed();
    }
}
