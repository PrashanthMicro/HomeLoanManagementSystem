package com.capgemini.homeloanmanagementapp.expection;

import com.capgemini.homeloanmanagementapp.exception.ForbiddenException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForbiddenExceptionTest {

    @Test
    void testForbiddenExceptionMessage() {
        ForbiddenException ex = new ForbiddenException("Access denied");

        assertNotNull(ex);
        assertEquals("Access denied", ex.getMessage());
    }
}