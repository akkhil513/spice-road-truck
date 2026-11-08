package com.spiceroad.service;

/** Kitchen progress of one order line: tapped once = TICKED, tapped again = CLEARED. */
public enum ItemStatus {
    PENDING, TICKED, CLEARED
}
