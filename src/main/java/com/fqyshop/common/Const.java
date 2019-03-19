package com.fqyshop.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by Administrator on 2019/1/16.
 */
public class Const {
    public static final String CURRENT_USER = "current_user";

    public interface Role {
        int CUSTOMER = 0;
        int ADMIN = 1;
    }

    public interface Cart {
        int CHECKED = 1;
        int UNCHECKED = 0;
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    public interface ProductListOrderBy {
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc", "price_asc");
    }

    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    public enum ProductStatus {
        ON_SAlE("在售", 1);
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        ProductStatus(String value, int code) {
            this.value = value;
            this.code = code;
        }
    }

    public enum OrderStatus {
        CANCELED(0,"已取消"),

        NO_PAY(10,"未支付"),
        PAID(20,"已支付"),

        ORDER_SUCCESS(30,"订单完成"),

        SHIPPED(40,"已发货"),

        ORDER_CLOSED(50,"交易关闭");
        private int code;
        private String value;

        OrderStatus(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public static OrderStatus codeOf(int code) {
            for (OrderStatus orderStatus : values()) {
                if (orderStatus.getCode() == code) {
                    return orderStatus;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }


    public interface  AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum PayPlatformEnum{
        ALIPAY(1,"支付宝");

        PayPlatformEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }

    public enum PaymentTypeEnum{
        ONLINE_PAY(1,"在线支付");
        private String value;
        private int code;

        PaymentTypeEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public String getValue() {

            return value;
        }

        public int getCode() {
            return code;
        }

        public static PaymentTypeEnum codeOf(int code) {
            for (PaymentTypeEnum paymentTypeEnum : values()) {
                if (paymentTypeEnum.getCode() == code) {
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }

}
