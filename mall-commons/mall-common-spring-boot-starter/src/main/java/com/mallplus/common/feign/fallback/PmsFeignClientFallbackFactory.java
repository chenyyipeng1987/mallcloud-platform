package com.mallplus.common.feign.fallback;

import com.mallplus.common.feign.PmsFeignClinent;
import com.mallplus.common.entity.pms.PmsProduct;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * userService降级工场
 *
 * @author mall
 * @date 2019/1/18
 */
@Slf4j
@Component
public class PmsFeignClientFallbackFactory implements FallbackFactory<PmsFeignClinent> {
    @Override
    public PmsFeignClinent create(Throwable throwable) {
        return new PmsFeignClinent() {

            @Override
            public PmsProduct selectById(Long id) {
                log.error("通过id查询商品异常:{}", id, throwable);
                return new PmsProduct();
            }
        };
    }
}
