package com.mallplus.marking.controller;

import com.mallplus.common.annotation.SysLog;
import com.mallplus.common.utils.CommonResult;
import com.mallplus.common.vo.CartPromotionItem;
import com.mallplus.common.vo.SmsCouponHistoryDetail;
import com.mallplus.marking.service.ISmsCouponService;
import com.mallplus.marking.service.ISmsHomeAdvertiseService;
import com.mallplus.marking.service.ISmsRedPacketService;
import com.mallplus.marking.service.ISmsUserRedPacketService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 优惠卷表
 * </p>
 *
 * @author zscat
 * @since 2019-04-19
 */
@Slf4j
@RestController
@Api(tags = "AuthMarkingController", description = "")
@RequestMapping("/auth")
public class AuthMarkingController {
    @Resource
    private ISmsCouponService couponService;

    @Resource
    private ISmsUserRedPacketService userRedPacketService;
    @Resource
    private ISmsRedPacketService ISmsRedPacketService;
    @Resource
    private ISmsHomeAdvertiseService ISmsHomeAdvertiseService;

    @SysLog(MODULE = "sms", REMARK = "领取红包")
    @ApiOperation(value = "领取红包")
    @RequestMapping(value = "/redPacket/accept/{id}", method = RequestMethod.GET)
    public Object accept(@PathVariable("id") Integer id) {
        int count = ISmsRedPacketService.acceptRedPacket(id,1L);
        if (count == 1) {
            return new CommonResult().success("领取成功");
        } else {
            return new CommonResult().failed("你已经领取此红包");
        }
    }


    @RequestMapping(value = "/listCart", method = RequestMethod.GET)
    List<SmsCouponHistoryDetail> listCart(List<CartPromotionItem> cartItemList, Integer type,Long memberId){
        return couponService.listCart(cartItemList,type,memberId);
    }

}