package com.mallplus.common.feign;

import com.mallplus.common.constant.ServiceNameConstants;
import com.mallplus.common.entity.pms.PmsProduct;
import com.mallplus.common.entity.pms.PmsSkuStock;
import com.mallplus.common.feign.fallback.PmsFeignClientFallbackFactory;
import com.mallplus.common.vo.PromotionProduct;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author mall
 */
@FeignClient(name = ServiceNameConstants.GOODS_SERVICE, fallbackFactory = PmsFeignClientFallbackFactory.class, decode404 = true)
public interface PmsFeignClinent {
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

    @PostMapping(value = "/ums/UmsMember/updateSkuById")
    void updateSkuById(PmsSkuStock skuStock);
}
