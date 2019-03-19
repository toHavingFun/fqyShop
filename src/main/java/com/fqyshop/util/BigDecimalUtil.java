package com.fqyshop.util;

import java.math.BigDecimal;

/**
 * Created by Administrator on 2019/1/22.
 */
public class BigDecimalUtil {
    private BigDecimalUtil() {
    }

    public static BigDecimal add(double a1, double a2) {
        BigDecimal v1 = new BigDecimal(Double.toString(a1));
        BigDecimal v2 = new BigDecimal(Double.toString(a2));
        return v1.add(v2);
    }
    public static BigDecimal sub(double a1, double a2) {
        BigDecimal v1 = new BigDecimal(Double.toString(a1));
        BigDecimal v2 = new BigDecimal(Double.toString(a2));
        return v1.subtract(v2);
    }
    public static BigDecimal mul(double a1, double a2) {
        BigDecimal v1 = new BigDecimal(Double.toString(a1));
        BigDecimal v2 = new BigDecimal(Double.toString(a2));
        return v1.multiply(v2);
    }
    public static BigDecimal div(double a1, double a2) {
        BigDecimal v1 = new BigDecimal(Double.toString(a1));
        BigDecimal v2 = new BigDecimal(Double.toString(a2));
        return v1.divide(v2,2,BigDecimal.ROUND_HALF_UP);
    }
}
