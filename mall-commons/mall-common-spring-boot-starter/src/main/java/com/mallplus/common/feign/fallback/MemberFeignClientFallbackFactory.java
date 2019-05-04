package com.mallplus.common.feign.fallback;

import com.mallplus.common.feign.MemberFeignClient;
import com.mallplus.common.model.UmsMember;
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
public class MemberFeignClientFallbackFactory implements FallbackFactory<MemberFeignClient> {
    @Override
    public MemberFeignClient create(Throwable throwable) {
        return new MemberFeignClient() {

            @Override
            public UmsMember findByOpenId(String openId) {
                log.error("通过openId查询会员异常:{}", openId, throwable);
                return new UmsMember();
            }

            @Override
            public Object saveUmsMember(UmsMember entity) {
                log.error("saveUmsMember异常:{}", entity, throwable);
                return new UmsMember();
            }
        };
    }
}
