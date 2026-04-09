package com.capgemini.homeloanmanagementapp.expection;

import com.capgemini.homeloanmanagementapp.exception.NotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotFoundExceptionTest {

    @Test
    void testNotFoundExceptionMessage() {
        NotFoundException ex = new NotFoundException("Record not found");

        assertNotNull(ex);
        assertEquals("Record not found", ex.getMessage());
    }
}
