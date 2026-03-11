package Diaz.Dev.BFlow.common.response;

import bflow.common.response.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testApiResponseSuccess() {
        ApiResponse<String> response = ApiResponse.success("Test message", "test_data", "/api/test");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Test message", response.getMessage());
        assertEquals("test_data", response.getData());
        assertEquals("/api/test", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testApiResponseSuccessNullData() {
        ApiResponse<Void> response = ApiResponse.success("Created successfully", null, "/api/resource");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNull(response.getData());
    }

    @Test
    void testApiResponseImmutable() {
        ApiResponse<String> response = ApiResponse.success("msg1", "data1", "/path1");
        ApiResponse<String> response2 = ApiResponse.success("msg2", "data2", "/path2");

        assertNotEquals(response.getMessage(), response2.getMessage());
        assertNotEquals(response.getData(), response2.getData());
        assertNotEquals(response.getPath(), response2.getPath());
    }
}
