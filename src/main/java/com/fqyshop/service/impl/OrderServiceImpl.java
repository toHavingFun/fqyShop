package com.fqyshop.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.fqyshop.common.Const;
import com.fqyshop.common.ServerResponse;
import com.fqyshop.dao.*;
import com.fqyshop.pojo.*;
import com.fqyshop.service.OrderService;
import com.fqyshop.util.BigDecimalUtil;
import com.fqyshop.util.DateTimeUtil;
import com.fqyshop.util.FTPUtil;
import com.fqyshop.util.PropertiesUtil;
import com.fqyshop.vo.OrderItemVo;
import com.fqyshop.vo.OrderProductVo;
import com.fqyshop.vo.OrderVo;
import com.fqyshop.vo.ShippingVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Administrator on 2019/1/25.
 *
 */

// TODO: 2019/1/25 有一个潜在的bug，可能会很致命！
@Service("orderService")
public class OrderServiceImpl implements OrderService {
    public static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;


    public ServerResponse pay(Long orderNo, Integer userId, String path) {
        Map<String, String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            ServerResponse.responseErrMsg("查找的订单不存在！");
        }
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));
        String outTradeNo = order.getOrderNo().toString();
        String subject = new StringBuilder().append("fqyshop扫码支付,订单号:").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品3件共20.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共：").append(totalAmount).toString();

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)
        String providerId = "2088100200300400500";
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId(providerId);
        String timeoutExpress = "5m";
        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo, userId);
        for (OrderItem o : orderItemList) {
            GoodsDetail goodsDetail = GoodsDetail.newInstance(o.getProductId().toString(), o.getProductName(),
                    BigDecimalUtil.mul(o.getCurrentUnitPrice().doubleValue(),
                            new Double(100).doubleValue()).longValue(),
                    o.getQuantity());
            goodsDetailList.add(goodsDetail);
        }
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setSellerId(sellerId).setBody(body)
                .setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);
                File folder = new File(path);
                if (!folder.exists()) {
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 需要修改为运行机器上的路径
                String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png", response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                File targetFile = new File(path, qrFileName);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("上传二维码异常", e);
                }
                logger.info("filePath:" + qrPath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName();
                resultMap.put("qrUrl", qrUrl);
                return ServerResponse.responseData(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.responseErrMsg("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("支持的交易状态，交易返回异常!!!");
                return ServerResponse.responseErrMsg("支持的交易状态，交易返回异常!!!");

            default:
                logger.error("支持的交易状态，交易返回异常!!!");
                return ServerResponse.responseErrMsg("支持的交易状态，交易返回异常!!!");
        }
    }

    public ServerResponse aliCallback(Map<String, String> params) {
        long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.responseErrMsg("非本网站的订单，忽略！");
        }
        if (order.getStatus() >= Const.OrderStatus.PAID.getCode()) {
            return ServerResponse.responseErrMsg("支付宝重复调用");
        }
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatus.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);
        payInfoMapper.insert(payInfo);
        return ServerResponse.response();
    }

    public ServerResponse queryOderPayStatus(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.responseErrMsg("用户没有该订单！");

        }
        if (order.getStatus() >= Const.OrderStatus.PAID.getCode()) {
            return ServerResponse.response();
        }
        return ServerResponse.responseError();
    }

    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        ServerResponse response = this.getCartOrderItem(userId, cartList);
        if (!response.isSuccess()) {
            return response;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);

        //生成订单
        Order order = assembleOrder(userId, shippingId, payment);
        if (order == null) {
            return ServerResponse.responseErrMsg("生成订单错误！");
        }
        if (CollectionUtils.isEmpty(orderItemList)) {
            return ServerResponse.responseErrMsg("购物车为空");
        }
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }
        orderItemMapper.batchInsert(orderItemList);
        this.reduceProductStock(orderItemList);
        this.cleanCart(cartList);
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        return ServerResponse.responseData(orderVo);
    }

    public ServerResponse<String> cancel(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.responseErrMsg("该用户订单不存在");
        }
        if (order.getStatus() != Const.OrderStatus.NO_PAY.getCode()) {
            return ServerResponse.responseErrMsg("无法取消该订单");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatus.CANCELED.getCode());
        int i = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (i > 0) {
            return ServerResponse.response();
        }
        return ServerResponse.responseError();
    }

    public ServerResponse getOrderCartProduct(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        ServerResponse serverResponse = this.getCartOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem o : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), o.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(o));
        }
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.responseData(orderProductVo);
    }

    public ServerResponse<PageInfo> getOrderList(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, userId);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.responseData(pageInfo);
    }

    public ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo, userId);
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            return ServerResponse.responseData(orderVo);
        }
        return ServerResponse.responseErrMsg("没有该订单");
    }

    //backend

    public ServerResponse<PageInfo> manageList(int pageNum, int pageSize) {
        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, null);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.responseData(pageResult);
    }

    public ServerResponse<OrderVo> manageDetail(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
            return ServerResponse.responseData(orderVo);
        }
        return ServerResponse.responseErrMsg("订单不存在");
    }

    public ServerResponse<PageInfo> manageSearch(Long orderNo, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
            PageInfo pageResult = new PageInfo(Lists.newArrayList(order));
            pageResult.setList(Lists.newArrayList(orderVo));
            return ServerResponse.responseData(pageResult);
        }
        return ServerResponse.responseErrMsg("订单不存在");
    }

    public ServerResponse<String> manageSendGoods(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            if (order.getStatus() == Const.OrderStatus.PAID.getCode()) {
                order.setStatus(Const.OrderStatus.SHIPPED.getCode());
                order.setSendTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
                return ServerResponse.responseMsg("发货成功");
            }
        }
        return ServerResponse.responseMsg("订单不存在");
    }

    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId) {
        List<OrderVo> orderVoList = Lists.newArrayList();

        for (Order o : orderList) {
            List<OrderItem> orderItemList = null;
            if (userId == null) {
// TODO: 2019/1/25 管理员
                orderItemList = orderItemMapper.getByOrderNo(o.getOrderNo());
            } else {
                orderItemList = orderItemMapper.getByOrderNoUserId(o.getOrderNo(), userId);
            }
            OrderVo orderVo = assembleOrderVo(o, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatus.codeOf(order.getStatus()).getValue());

        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }

        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shippingVo.getReceiverPhone());
        return shippingVo;
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }

    private void cleanCart(List<Cart> cartList) {
        for (Cart c : cartList) {
            cartMapper.deleteByPrimaryKey(c.getId());
        }
    }

    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem o : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(o.getProductId());
            product.setStock(product.getStock() - o.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        Order order = new Order();
        long orderNo = this.generateOrderNo();
        order.setOrderNo(orderNo);
        order.setStatus(Const.OrderStatus.NO_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPayment(payment);
        order.setUserId(userId);
        order.setShippingId(shippingId);
        int insert = orderMapper.insert(order);
        if (insert > 0) {
            return order;
        }
        return null;
    }

    private long generateOrderNo() {
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);
    }

    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem o : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), o.getTotalPrice().doubleValue());
        }
        return payment;

    }

    private ServerResponse<List<OrderItem>> getCartOrderItem(Integer userId, List<Cart> cartList) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.responseErrMsg("购物车为空");
        }
        for (Cart c : cartList) {
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(c.getProductId());
            if (Const.ProductStatus.ON_SAlE.getCode() != product.getStatus()) {
                return ServerResponse.responseErrMsg("产品不在售卖状态");
            }
            if (c.getQuantity() > product.getStock()) {
                return ServerResponse.responseErrMsg("产品" + product.getName() + "库存不足！");
            }
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(c.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), c.getQuantity()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.responseData(orderItemList);
    }

    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }
}
