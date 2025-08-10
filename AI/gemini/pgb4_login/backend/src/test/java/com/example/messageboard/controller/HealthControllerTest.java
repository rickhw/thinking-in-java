package com.example.messageboard.controller;

import com.gtcafe.messageboard.controller.HealthController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class HealthControllerTest {

    @MockBean
    private DataSource dataSource;

    @Test
    void testLivenessProbe() {
        HealthController healthController = new HealthController(dataSource);
        ResponseEntity<Map<String, Object>> response = healthController.liveness();
        
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("timestamp"));
        assertTrue(response.getBody().containsKey("checks"));
    }

    @Test
    void testReadinessProbeSuccess() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(5)).thenReturn(true);
        
        HealthController healthController = new HealthController(dataSource);
        ResponseEntity<Map<String, Object>> response = healthController.readiness();
        
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        
        verify(mockConnection).close();
    }

    @Test
    void testReadinessProbeFailure() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Database unavailable"));
        
        HealthController healthController = new HealthController(dataSource);
        ResponseEntity<Map<String, Object>> response = healthController.readiness();
        
        assertEquals(503, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("DOWN", response.getBody().get("status"));
    }

    @Test
    void testStartupProbe() {
        HealthController healthController = new HealthController(dataSource);
        ResponseEntity<Map<String, Object>> response = healthController.startup();
        
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("message"));
    }
}