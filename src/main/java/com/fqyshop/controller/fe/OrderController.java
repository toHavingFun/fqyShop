package com.fqyshop.controller.fe;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.fqyshop.common.Const;
import com.fqyshop.common.ResponseCode;
import com.fqyshop.common.ServerResponse;
import com.fqyshop.pojo.Order;
import com.fqyshop.pojo.User;
import com.fqyshop.service.OrderService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2019/1/25.
 */
@Controller
@RequestMapping("/order/")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private OrderService orderService;

    @RequestMapping("create.action")
    @ResponseBody
    public ServerResponse create(HttpSession session, Integer shippingId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.createOrder(user.getId(), shippingId);
    }

    @RequestMapping("cancel.action")
    @ResponseBody
    public ServerResponse cancel(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.cancel(user.getId(), orderNo);
    }

    @RequestMapping("getOrderCartProduct.action")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.getOrderCartProduct(user.getId());
    }


    @RequestMapping("pay.action")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String path = request.getSession().getServletContext().getRealPath("upload");
        return orderService.pay(orderNo, user.getId(), path);
    }

    @RequestMapping("detail.action")
    @ResponseBody
    public ServerResponse detail(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.getOrderDetail(user.getId(), orderNo);
    }

    @RequestMapping("list.action")
    @ResponseBody
    public ServerResponse list(HttpSession session,
                               @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize",defaultValue = "5") Integer pageSize
                               ) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.getOrderList(user.getId(),pageNum,pageSize);
    }

    @RequestMapping("alipayCallback.action")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request) {
        Map<String, String> params0 = Maps.newHashMap();
        Map params = request.getParameterMap();
        for (Iterator iter = params.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) params.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params0.put(name, valueStr);
        }
        logger.info("支付宝回调，sign:{},trade_status:{},参数：{}", params0.get("sign"), params0.get("trade_status"), params0.toString());

        params0.remove("sign_type");
        try {
            boolean b = AlipaySignature.rsaCheckV2(params0, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            if (!b) {
                return ServerResponse.responseErrMsg("非法请求！请停止请求！");
            }
        } catch (AlipayApiException e) {
            logger.error("验签异常", e);
        }
        //// TODO: 2019/1/25  验证各种数据
        ServerResponse response = orderService.aliCallback(params0);
        if (response.isSuccess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    @RequestMapping("queryOrderPayStatus.action")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse response = orderService.queryOderPayStatus(user.getId(), orderNo);
        if (response.isSuccess()) {
            return ServerResponse.responseData(true);
        }
        return ServerResponse.responseData(false);
    }

}
