package com.mallplus.oauth.config;

import com.mallplus.db.config.DefaultMybatisPlusConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author mall
 * @date 2018/12/10
 */
@Configuration
@MapperScan({"com.mallplus.oauth.mapper*"})
public class MybatisPlusConfig extends DefaultMybatisPlusConfig {

}
