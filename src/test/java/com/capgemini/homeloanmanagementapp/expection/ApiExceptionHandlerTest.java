package com.capgemini.homeloanmanagementapp.expection;

import com.capgemini.homeloanmanagementapp.exception.ApiExceptionHandler;
import com.capgemini.homeloanmanagementapp.exception.BadRequestException;
import com.capgemini.homeloanmanagementapp.exception.ForbiddenException;
import com.capgemini.homeloanmanagementapp.exception.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void testHandleNotFound() {
        NotFoundException ex = new NotFoundException("Not found");
        Map<String, String> response = handler.handleNotFound(ex);

        assertNotNull(response);
        assertEquals("Not found", response.get("error"));
    }

    @Test
    void testHandleForbidden() {
        ForbiddenException ex = new ForbiddenException("Access denied");
        Map<String, String> response = handler.handleForbidden(ex);

        assertNotNull(response);
        assertEquals("Access denied", response.get("error"));
    }

    @Test
    void testHandleBadRequest() {
        BadRequestException ex = new BadRequestException("Invalid input");
        Map<String, String> response = handler.handleBadRequest(ex);

        assertNotNull(response);
        assertEquals("Invalid input", response.get("error"));
    }

    @Test
    void testHandleGeneric() {
        RuntimeException ex = new RuntimeException("Server error");
        Map<String, String> response = handler.handleGeneric(ex);

        assertNotNull(response);
        assertEquals("Server error", response.get("error"));
    }
}