package com.mallplus.common.feign;

import com.mallplus.common.constant.ServiceNameConstants;
import com.mallplus.common.feign.fallback.PmsFeignClientFallbackFactory;
import com.mallplus.common.model.PmsProduct;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    @GetMapping(value = "/goods/{id}")
    PmsProduct selectById(@PathVariable("id") Long id);

}
