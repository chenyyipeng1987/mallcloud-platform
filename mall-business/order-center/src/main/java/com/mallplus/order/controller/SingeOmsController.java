package com.mallplus.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.central.common.annotation.IgnoreAuth;
import com.central.common.annotation.SysLog;
import com.central.common.utils.CommonResult;
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


    @IgnoreAuth
    @SysLog(MODULE = "oms", REMARK = "查询订单列表")
    @ApiOperation(value = "查询订单列表")
    @GetMapping(value = "/order/list")
    public Object orderList(OmsOrder order,
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
}
