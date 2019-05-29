package com.mallplus.common.feign.fallback;

import com.mallplus.common.entity.ums.UmsIntegrationConsumeSetting;
import com.mallplus.common.feign.MemberFeignClient;
import com.mallplus.common.entity.ums.UmsMember;
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
            public UmsMember findById(Long id) {
                return null;
            }

            @Override
            public Object saveUmsMember(UmsMember entity) {
                log.error("saveUmsMember异常:{}", entity, throwable);
                return new UmsMember();
            }

            @Override
            public UmsMember findByUsername(String username) {
                log.error("通过username查询会员异常:{}", username, throwable);
                return new UmsMember();
            }

            @Override
            public UmsMember findByMobile(String mobile) {
                log.error("通过mobile查询会员异常:{}", mobile, throwable);
                return new UmsMember();
            }

            @Override
            public void updateIntegration(Long id, int i) {

            }

            @Override
            public UmsIntegrationConsumeSetting selectIntegrationConsumeSettingById(long l) {
                return null;
            }
        };
    }
}
