package com.mallplus.common.feign;

import com.mallplus.common.constant.ServiceNameConstants;
import com.mallplus.common.entity.pms.PmsProduct;
import com.mallplus.common.entity.pms.PmsSkuStock;
import com.mallplus.common.entity.sms.SmsCouponHistory;
import com.mallplus.common.feign.fallback.PmsFeignClientFallbackFactory;
import com.mallplus.common.vo.CartPromotionItem;
import com.mallplus.common.vo.PromotionProduct;
import com.mallplus.common.vo.SmsCouponHistoryDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author mall
 */
@FeignClient(name = ServiceNameConstants.MARKING_SERVICE, fallbackFactory = PmsFeignClientFallbackFactory.class, decode404 = true)
public interface MarkingFeignClinent {
    /**
     * feign rpc访问远程/goods/{id}接口
     * 查询用户实体对象PmsProduct
     *
     * @param id
     * @return
     */


    @GetMapping(value = "/pms/PmsProduct/id", params = "id")
    PmsProduct selectById(@RequestParam("id") Long id);

    @GetMapping(value = "/pms/PmsSkuStock/id", params = "id")
    PmsSkuStock selectSkuById(@RequestParam("id") Long id);

    @GetMapping(value = "/notAuth/getPromotionProductList", params = "productIdList")
    List<PromotionProduct> getPromotionProductList(List<Long> productIdList);

    /**
     * 根据购物车信息获取可用优惠券
     */
    @GetMapping(value = "/notAuth/getPromotionProductList")
    List<SmsCouponHistoryDetail> listCart(List<CartPromotionItem> cartItemList, Integer type,Long memberId);


    List<SmsCouponHistory> listCouponHistory(SmsCouponHistory queryC);

    void updateCouponHistoryById(SmsCouponHistory couponHistory);
}
