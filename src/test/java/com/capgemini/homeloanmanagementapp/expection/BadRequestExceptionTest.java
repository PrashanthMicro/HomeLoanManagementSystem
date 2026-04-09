package com.capgemini.homeloanmanagementapp.expection;

import com.capgemini.homeloanmanagementapp.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BadRequestExceptionTest {

    @Test
    void testBadRequestExceptionMessage() {
        BadRequestException ex = new BadRequestException("Invalid request");

        assertNotNull(ex);
        assertEquals("Invalid request", ex.getMessage());
    }
}