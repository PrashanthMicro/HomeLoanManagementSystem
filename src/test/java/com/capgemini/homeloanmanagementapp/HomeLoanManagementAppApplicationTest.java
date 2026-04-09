package com.capgemini.homeloanmanagementapp;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HomeLoanManagementAppApplicationTest {
    @Test
    void main_runsSpringApplicationSuccessfully() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {

            // Mock SpringApplication.run(...) so it does nothing
            mocked.when(() ->
                    SpringApplication.run(HomeLoanManagementAppApplication.class, new String[]{})
            ).thenReturn(null);

            // Nothing should throw
            assertDoesNotThrow(() ->
                    HomeLoanManagementAppApplication.main(new String[]{})
            );

            // Verify SpringApplication.r
            //
            // un(...) was invoked exactly once
            mocked.verify(() ->
                    SpringApplication.run(HomeLoanManagementAppApplication.class, new String[]{}),
                    times(1)
            );
        }
    }
}
