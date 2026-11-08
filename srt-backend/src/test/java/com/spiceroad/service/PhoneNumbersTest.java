package com.spiceroad.service;

import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneNumbersTest {

    @Test
    void missingNumberIsAllowed() {
        assertNull(PhoneNumbers.normalize(null));
        assertNull(PhoneNumbers.normalize("   "));
    }

    @Test
    void normalizesCommonUsFormats() {
        assertEquals("+14045550142", PhoneNumbers.normalize("404 555 0142"));
        assertEquals("+14045550142", PhoneNumbers.normalize("(404) 555-0142"));
        assertEquals("+14045550142", PhoneNumbers.normalize("1-404-555-0142"));
        assertEquals("+14045550142", PhoneNumbers.normalize(" +1 404.555.0142 "));
    }

    @Test
    void keepsValidInternationalNumbers() {
        assertEquals("+919876543210", PhoneNumbers.normalize("+91 98765 43210"));
    }

    @Test
    void rejectsInvalidNumbers() {
        assertThrows(BadRequestException.class, () -> PhoneNumbers.normalize("12345"));
        assertThrows(BadRequestException.class, () -> PhoneNumbers.normalize("0045550142"));
        assertThrows(BadRequestException.class, () -> PhoneNumbers.normalize("404-555-01x2"));
        assertThrows(BadRequestException.class, () -> PhoneNumbers.normalize("+0 123456789"));
        assertThrows(BadRequestException.class, () -> PhoneNumbers.normalize("+1234567890123456"));
        assertThrows(BadRequestException.class, () -> PhoneNumbers.normalize("4".repeat(40)));
        assertThrows(BadRequestException.class, () -> PhoneNumbers.normalize("1 (123) 555-0142"));
    }
}
