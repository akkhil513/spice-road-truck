package com.spiceroad.config;

import io.smallrye.config.ConfigMapping;

import java.math.BigDecimal;

/** Order rules that differ per environment or may change without a code release. */
@ConfigMapping(prefix = "srt.orders")
public interface OrderSettings {

    /** Sales tax as a fraction, e.g. 0.08 for 8%. */
    BigDecimal taxRate();

    /** IANA zone used to decide which "business day" an order belongs to. */
    String timezone();

    /** Maximum number of distinct line items in one order. */
    int maxLines();

    /** Maximum quantity of a single item in one order. */
    int maxQuantity();
}
