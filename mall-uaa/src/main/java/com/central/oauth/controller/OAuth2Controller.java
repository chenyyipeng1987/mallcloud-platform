package com.central.oauth.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.central.common.constant.SecurityConstants;
import com.central.common.exception.ApiMallPlusException;
import com.central.common.exception.BusinessException;
import com.central.common.feign.MemberFeignClient;
import com.central.common.feign.UserService;
import com.central.common.model.Result;
import com.central.common.model.SysUser;
import com.central.common.model.UmsMember;
import com.central.common.utils.SpringUtil;
import com.central.oauth.config.WxAppletProperties;
import com.central.oauth.mobile.MobileAuthenticationToken;
import com.central.oauth.openid.OpenIdAuthenticationToken;
import com.central.oauth.service.impl.RedisClientDetailsService;
import com.central.util.CommonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2相关操作
 *
 * @author mall
 */
@Api(tags = "OAuth2相关操作")
@Slf4j
@RestController
public class OAuth2Controller {
    @Resource
    private MemberFeignClient memberFeignClient;
    @Resource
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Resource
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Resource
    private UserService userService;

    @ApiOperation(value = "用户名密码获取token")
    @PostMapping(SecurityConstants.PASSWORD_LOGIN_PRO_URL)
    public void getUserTokenInfo(@RequestBody SysUser umsAdminLoginParam,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (umsAdminLoginParam.getUsername() == null || "".equals(umsAdminLoginParam.getUsername())) {
            throw new UnapprovedClientAuthenticationException("用户名为空");
        }
        if (umsAdminLoginParam.getPassword() == null || "".equals(umsAdminLoginParam.getPassword())) {
            throw new UnapprovedClientAuthenticationException("密码为空");
        }
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(umsAdminLoginParam.getUsername(), umsAdminLoginParam.getPassword());
       SysUser user = userService.selectByUsername(umsAdminLoginParam.getUsername());
       if (user!=null){
           writerToken(request, response, token, "用户名或密码错误",user.getId());
       }else {
           exceptionHandler(response, "用户名或密码错误");
       }

    }

    @ApiOperation(value = "openId获取token")
    @PostMapping(SecurityConstants.OPENID_TOKEN_URL)
    public void getTokenByOpenId(
            @ApiParam(required = true, name = "openId", value = "openId") String openId,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        OpenIdAuthenticationToken token = new OpenIdAuthenticationToken(openId);
        UmsMember member = memberFeignClient.findByOpenId(openId);
        if (member!=null){
            writerToken(request, response, token, "openId错误",member.getId());
        }else {
            exceptionHandler(response, "openId错误");
        }

    }



    public String loginByWeixin(HttpServletRequest req) {
        try {
            String code = req.getParameter("code");
            if (StringUtils.isEmpty(code)) {
                throw new BusinessException("code is empty");
            }
            String userInfos = req.getParameter("userInfo");

            String signature = req.getParameter("signature");

            Map<String, Object> me = JSONUtil.parseObj(userInfos);
            if (null == me) {
                throw new BusinessException("登录失败");
            }

            Map<String, Object> resultObj = new HashMap<String, Object>();
            //
            //获取openid
            String requestUrl = this.getWebAccess(code);//通过自定义工具类组合出小程序需要的登录凭证 code

            JSONObject sessionData = CommonUtil.httpsRequest(requestUrl, "GET", null);

            if (null == sessionData || StringUtils.isEmpty(sessionData.getStr("openid"))) {
                throw new BusinessException("登录失败");
            }
            //验证用户信息完整性
            String sha1 = CommonUtil.getSha1(userInfos + sessionData.getStr("session_key"));
            if (!signature.equals(sha1)) {
                throw new BusinessException("登录失败");
            }
            UmsMember userVo = memberFeignClient.findByOpenId(sessionData.getStr("openid"));
            if (null == userVo) {
                UmsMember umsMember = new UmsMember();
                umsMember.setUsername("wxapplet");
                umsMember.setSourceType(1);
                umsMember.setPassword(passwordEncoder.encode("123456"));
                umsMember.setCreateTime(new Date());
                umsMember.setStatus(1);
                umsMember.setBlance(new BigDecimal(0));
                umsMember.setIntegration(0);
                umsMember.setHistoryIntegration(0);
                umsMember.setWeixinOpenid(sessionData.getStr("openid"));
                if (StringUtils.isEmpty(me.get("avatarUrl").toString())) {
                    //会员头像(默认头像)
                    umsMember.setIcon("/upload/img/avatar/01.jpg");
                } else {
                    umsMember.setIcon(me.get("avatarUrl").toString());
                }
                // umsMember.setGender(Integer.parseInt(me.get("gender")));
                umsMember.setNickname(me.get("nickName").toString());

                memberFeignClient.saveUmsMember(umsMember);
            }
          return   sessionData.getStr("openid");

        } catch (ApiMallPlusException e) {
            e.printStackTrace();
            throw new BusinessException("登录失败");
        }catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("登录失败");
        }

    }
    @ApiOperation(value = "mobile获取token")
    @PostMapping(SecurityConstants.MOBILE_TOKEN_URL)
    public void getTokenByMobile(
            @ApiParam(required = true, name = "mobile", value = "mobile") String mobile,
            @ApiParam(required = true, name = "password", value = "密码") String password,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        MobileAuthenticationToken token = new MobileAuthenticationToken(mobile, password);
        writerToken(request, response, token, "手机号或密码错误",1L);
    }
    @Autowired
    private WxAppletProperties wxAppletProperties;

    //替换字符串
    public String getCode(String APPID, String REDIRECT_URI, String SCOPE) {
        return String.format(wxAppletProperties.getGetCode(), APPID, REDIRECT_URI, SCOPE);
    }

    //替换字符串
    public String getWebAccess(String CODE) {

        return String.format(wxAppletProperties.getWebAccessTokenhttps(),
                wxAppletProperties.getAppId(),
                wxAppletProperties.getSecret(),
                CODE);
    }

    //替换字符串
    public String getUserMessage(String access_token, String openid) {
        return String.format(wxAppletProperties.getUserMessage(), access_token, openid);
    }
    private void writerToken(HttpServletRequest request, HttpServletResponse response, AbstractAuthenticationToken token
            , String badCredenbtialsMsg,Long userId) throws IOException {
        try {
            String clientId = request.getHeader("client_id");
            String clientSecret = request.getHeader("client_secret");
            if (clientId == null || "".equals(clientId)) {
                throw new UnapprovedClientAuthenticationException("请求头中无client_id信息");
            }

            if (clientSecret == null || "".equals(clientSecret)) {
                throw new UnapprovedClientAuthenticationException("请求头中无client_secret信息");
            }

            ClientDetails clientDetails = getClient(clientId, clientSecret, null);
            TokenRequest tokenRequest = new TokenRequest(MapUtils.EMPTY_MAP, clientId, clientDetails.getScope(), "customer");
            OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request(clientDetails);
            Authentication authentication = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);
            OAuth2AccessToken oAuth2AccessToken =  authorizationServerTokenServices.createAccessToken(oAuth2Authentication);
            oAuth2Authentication.setAuthenticated(true);

            writerObj(response, oAuth2AccessToken);
        } catch (BadCredentialsException | InternalAuthenticationServiceException e) {
            exceptionHandler(response, badCredenbtialsMsg);
            e.printStackTrace();
        } catch (Exception e) {
            exceptionHandler(response, e);
        }
    }

    private void exceptionHandler(HttpServletResponse response, Exception e) throws IOException {
        log.error("exceptionHandler-error:", e);
        exceptionHandler(response, e.getMessage());
    }

    private void exceptionHandler(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        writerObj(response, Result.failed(msg));
    }

    private void writerObj(HttpServletResponse response, Object obj) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        try (
                Writer writer = response.getWriter()
        ) {
            writer.write(objectMapper.writeValueAsString(obj));
            writer.flush();
        }
    }

    private ClientDetails getClient(String clientId, String clientSecret, RedisClientDetailsService clientDetailsService) {
        if (clientDetailsService == null) {
            clientDetailsService = SpringUtil.getBean(RedisClientDetailsService.class);
        }
        ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);

        if (clientDetails == null) {
            throw new UnapprovedClientAuthenticationException("clientId对应的信息不存在");
        } else if (!passwordEncoder.matches(clientSecret, clientDetails.getClientSecret())) {
            throw new UnapprovedClientAuthenticationException("clientSecret不匹配");
        }
        return clientDetails;
    }
}
