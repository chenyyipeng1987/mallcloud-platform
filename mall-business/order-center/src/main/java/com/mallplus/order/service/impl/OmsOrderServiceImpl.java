package com.mallplus.order.service.impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mallplus.common.feign.PmsFeignClinent;
import com.mallplus.common.model.PmsProduct;
import com.mallplus.common.utils.CommonResult;
import com.mallplus.common.utils.DateUtils;
import com.mallplus.order.config.WxAppletProperties;
import com.mallplus.order.entity.OmsOrder;
import com.mallplus.order.entity.OmsOrderOperateHistory;
import com.mallplus.order.entity.UmsMemberReceiveAddress;
import com.mallplus.order.mapper.OmsOrderMapper;
import com.mallplus.order.mapper.OmsOrderOperateHistoryMapper;
import com.mallplus.order.service.IOmsOrderOperateHistoryService;
import com.mallplus.order.service.IOmsOrderService;
import com.mallplus.order.service.IUmsMemberReceiveAddressService;
import com.mallplus.order.utils.applet.TemplateData;
import com.mallplus.order.utils.applet.WX_TemplateMsgUtil;
import com.mallplus.order.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author zscat
 * @since 2019-04-17
 */
@Slf4j
@Service
public class OmsOrderServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements IOmsOrderService {

    @Autowired
    private WxAppletProperties wxAppletProperties;
    @Resource
    private WechatApiService wechatApiService;

    @Resource
    private IUmsMemberReceiveAddressService addressService;
    @Resource
    private OmsOrderMapper orderMapper;

    @Resource
    private IOmsOrderOperateHistoryService orderOperateHistoryDao;
    @Resource
    private OmsOrderOperateHistoryMapper orderOperateHistoryMapper;
    @Resource
    private PmsFeignClinent pmsFeignClinent;


    @Override
    public int delivery(List<OmsOrderDeliveryParam> deliveryParamList) {
        //批量发货
        int count = orderMapper.delivery(deliveryParamList);
        //添加操作记录
        List<OmsOrderOperateHistory> operateHistoryList = deliveryParamList.stream()
                .map(omsOrderDeliveryParam -> {
                    OmsOrderOperateHistory history = new OmsOrderOperateHistory();
                    history.setOrderId(omsOrderDeliveryParam.getOrderId());
                    history.setCreateTime(new Date());
                    history.setOperateMan("后台管理员");
                    history.setOrderStatus(2);
                    history.setNote("完成发货");
                    return history;
                }).collect(Collectors.toList());
        orderOperateHistoryDao.saveBatch(operateHistoryList);
        return count;
    }

    @Override
    public int close(List<Long> ids, String note) {
        OmsOrder record = new OmsOrder();
        record.setStatus(4);
        int count = orderMapper.update(record, new QueryWrapper<OmsOrder>().eq("delete_status",0).in("id",ids));
        List<OmsOrderOperateHistory> historyList = ids.stream().map(orderId -> {
            OmsOrderOperateHistory history = new OmsOrderOperateHistory();
            history.setOrderId(orderId);
            history.setCreateTime(new Date());
            history.setOperateMan("后台管理员");
            history.setOrderStatus(4);
            history.setNote("订单关闭:" + note);
            return history;
        }).collect(Collectors.toList());
        orderOperateHistoryDao.saveBatch(historyList);
        return count;
    }
    @Override
    public int updateReceiverInfo(OmsReceiverInfoParam receiverInfoParam) {
        OmsOrder order = new OmsOrder();
        order.setId(receiverInfoParam.getOrderId());
        order.setReceiverName(receiverInfoParam.getReceiverName());
        order.setReceiverPhone(receiverInfoParam.getReceiverPhone());
        order.setReceiverPostCode(receiverInfoParam.getReceiverPostCode());
        order.setReceiverDetailAddress(receiverInfoParam.getReceiverDetailAddress());
        order.setReceiverProvince(receiverInfoParam.getReceiverProvince());
        order.setReceiverCity(receiverInfoParam.getReceiverCity());
        order.setReceiverRegion(receiverInfoParam.getReceiverRegion());
        order.setModifyTime(new Date());
        int count = orderMapper.updateById(order);
        //插入操作记录
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(receiverInfoParam.getOrderId());
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(receiverInfoParam.getStatus());
        history.setNote("修改收货人信息");
        orderOperateHistoryMapper.insert(history);
        return count;
    }

    @Override
    public int updateMoneyInfo(OmsMoneyInfoParam moneyInfoParam) {
        OmsOrder order = new OmsOrder();
        order.setId(moneyInfoParam.getOrderId());
        order.setFreightAmount(moneyInfoParam.getFreightAmount());
        order.setDiscountAmount(moneyInfoParam.getDiscountAmount());
        order.setModifyTime(new Date());
        int count = orderMapper.updateById(order);
        //插入操作记录
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(moneyInfoParam.getOrderId());
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(moneyInfoParam.getStatus());
        history.setNote("修改费用信息");
        orderOperateHistoryMapper.insert(history);
        return count;
    }

    @Override
    public int updateNote(Long id, String note, Integer status) {
        OmsOrder order = new OmsOrder();
        order.setId(id);
        order.setNote(note);
        order.setModifyTime(new Date());
        int count = orderMapper.updateById(order);
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(id);
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(status);
        history.setNote("修改备注信息：" + note);
        orderOperateHistoryMapper.insert(history);
        return count;
    }

    @Override
    public Object preSingelOrder(GroupAndOrderVo orderParam) {
        ConfirmOrderResult result = new ConfirmOrderResult();
        result.setGroupAndOrderVo(orderParam);
        PmsProduct goods = pmsFeignClinent.selectById(orderParam.getGoodsId());
        result.setGoods(goods);
        //获取用户收货地址列表
        List<UmsMemberReceiveAddress> memberReceiveAddressList = addressService.list(new QueryWrapper<>());
        result.setMemberReceiveAddressList(memberReceiveAddressList);
        UmsMemberReceiveAddress address = addressService.getDefaultItem(orderParam.getUserId());

        result.setAddress(address);
        return result;
    }
    @Transactional
    @Override
    public Object generateSingleOrder(GroupAndOrderVo orderParam) {
        String type = orderParam.getType();
        orderParam.setMemberId(orderParam.getUserId());
        orderParam.setName(orderParam.getUsername());
        PmsProduct goods = pmsFeignClinent.selectById(orderParam.getGoodsId());

        if (goods.getStock() < 0) {
            return new CommonResult().failed("库存不足，无法下单");
        }


        //根据商品合计、运费、活动优惠、优惠券、积分计算应付金额
        OmsOrder order = new OmsOrder();
        order.setDiscountAmount(new BigDecimal(0));
        order.setTotalAmount(goods.getPrice());
        order.setPayAmount(goods.getPrice());
        order.setFreightAmount(new BigDecimal(0));
        order.setPromotionAmount(new BigDecimal(0));

        order.setSupplyId(goods.getSupplyId());
        order.setCouponAmount(new BigDecimal(0));

        order.setIntegration(0);
        order.setIntegrationAmount(new BigDecimal(0));


        order.setGoodsId(goods.getId());
        order.setGoodsName(order.getGoodsName());
        //转化为订单信息并插入数据库
        order.setMemberId(orderParam.getMemberId());
        order.setCreateTime(new Date());
        order.setMemberUsername(orderParam.getUsername());
        //支付方式：0->未支付；1->支付宝；2->微信
        order.setPayType(orderParam.getPayType());
        //订单来源：0->PC订单；1->app订单
        order.setSourceType(orderParam.getSourceType());
        //订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单
        order.setStatus(0);
        //订单类型：0->正常订单；1->秒杀订单
        order.setOrderType(orderParam.getOrderType());
        //收货人信息：姓名、电话、邮编、地址
        UmsMemberReceiveAddress address = addressService.getById(orderParam.getAddressId());
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhoneNumber());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());
        //0->未确认；1->已确认
        order.setConfirmStatus(0);
        order.setDeleteStatus(0);
        //计算赠送积分
        order.setIntegration(0);
        //计算赠送成长值
        order.setGrowth(0);
        //生成订单号
        order.setOrderSn(generateOrderSn(order));

        // TODO: 2018/9/3 bill_*,delivery_*
        //插入order表和order_item表
        this.save(order);


        if ("0".equals(type)) { // 0 下单 1 拼团 2 发起拼团

        }

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);

        if (orderParam.getSourceType() == 1) {
            push(orderParam, order, orderParam.getPage(), orderParam.getFormId());
        }
        return new CommonResult().success("下单成功", result);
    }
    /**
     * 生成18位订单编号:8位日期+2位平台号码+2位支付方式+6位以上自增id
     */
    private String generateOrderSn(OmsOrder order) {
        StringBuilder sb = new StringBuilder();
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        sb.append(date);
        sb.append(String.format("%02d", order.getSourceType()));
        sb.append(String.format("%02d", order.getPayType()));
        sb.append(order.getMemberId());
        return sb.toString();
    }
    /**
     * 推送消息
     */
    public void push(GroupAndOrderVo umsMember, OmsOrder order, String page, String formId) {
        log.info("发送模版消息：userId=" + umsMember.getMemberId() + ",orderId=" + order.getId() + ",formId=" + formId);
        if (StringUtils.isEmpty(formId)) {
            log.error("发送模版消息：userId=" + umsMember.getMemberId() + ",orderId=" + order.getId() + ",formId=" + formId);
        }
        String accessToken = null;
        try {
            accessToken = wechatApiService.getAccessToken();

            String templateId = wxAppletProperties.getTemplateId();
            Map<String, TemplateData> param = new HashMap<String, TemplateData>();
            param.put("keyword1", new TemplateData(DateUtils.format(order.getCreateTime(), "yyyy-MM-dd"), "#EE0000"));

            param.put("keyword2", new TemplateData(order.getGoodsName(), "#EE0000"));
            param.put("keyword3", new TemplateData(order.getOrderSn(), "#EE0000"));
            param.put("keyword3", new TemplateData(order.getPayAmount() + "", "#EE0000"));

            JSON jsonObject = JSONUtil.parseObj(param);
            //调用发送微信消息给用户的接口    ********这里写自己在微信公众平台拿到的模板ID
            WX_TemplateMsgUtil.sendWechatMsgToUser(umsMember.getWxid(), templateId, page + "?id=" + order.getId(),
                    formId, jsonObject, accessToken);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
