package com.gtcafe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.gtcafe.rest.TenantController;
import com.gtcafe.rest.TenantDTO;

@SpringBootTest(
    classes = Main.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class TenantControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TenantController tenantController;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
            .bindToController(tenantController)
            .configureClient()
            .baseUrl("http://localhost:" + port)
            .build();
    }

    @Test
    void testCreateValidTenant() {
        TenantDTO validTenant = new TenantDTO(
            null, 
            "Test Tenant", 
            "test@example.com", 
            "Test Description"
        );

        webTestClient.post()
            .uri("/api/tenants")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(validTenant)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo("Test Tenant")
            .jsonPath("$.email").isEqualTo("test@example.com");
    }

    @Test
    void testCreateInvalidTenant() {
        TenantDTO invalidTenant = new TenantDTO(
            null, 
            "", 
            "invalid-email", 
            "Test Description"
        );

        webTestClient.post()
            .uri("/api/tenants")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidTenant)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.messages").exists();
    }

    @Test
    void testGetAllTenants() {
        // 先創建一些租戶用於測試
        TenantDTO tenant1 = new TenantDTO(null, "Tenant 1", "tenant1@example.com", "Description 1");
        TenantDTO tenant2 = new TenantDTO(null, "Tenant 2", "tenant2@example.com", "Description 2");

        // 創建租戶
        webTestClient.post()
            .uri("/api/tenants")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(tenant1)
            .exchange();

        webTestClient.post()
            .uri("/api/tenants")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(tenant2)
            .exchange();

        // 獲取所有租戶
        webTestClient.get()
            .uri("/api/tenants")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(TenantDTO.class)
            .hasSize(2)
            .contains(tenant1, tenant2);
    }

    @Test
    void testUpdateTenant() {
        // 先創建一個租戶
        TenantDTO originalTenant = new TenantDTO(null, "Original Tenant", "original@example.com", "Original Description");
        
        TenantDTO createdTenant = webTestClient.post()
            .uri("/api/tenants")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(originalTenant)
            .exchange()
            .expectStatus().isOk()
            .returnResult(TenantDTO.class)
            .getResponseBody()
            .blockFirst();

        // 更新租戶
        TenantDTO updatedTenant = new TenantDTO(
            createdTenant.id(), 
            "Updated Tenant", 
            "updated@example.com", 
            "Updated Description"
        );

        webTestClient.put()
            .uri("/api/tenants/{id}", createdTenant.id())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updatedTenant)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo("Updated Tenant")
            .jsonPath("$.email").isEqualTo("updated@example.com");
    }

    @Test
    void testDeleteTenant() {
        // 先創建一個租戶
        TenantDTO tenantToDelete = new TenantDTO(null, "Delete Tenant", "delete@example.com", "Delete Description");
        
        TenantDTO createdTenant = webTestClient.post()
            .uri("/api/tenants")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(tenantToDelete)
            .exchange()
            .expectStatus().isOk()
            .returnResult(TenantDTO.class)
            .getResponseBody()
            .blockFirst();

        // 刪除租戶
        webTestClient.delete()
            .uri("/api/tenants/{id}", createdTenant.id())
            .exchange()
            .expectStatus().isNoContent();

        // 確認租戶已被刪除
        webTestClient.get()
            .uri("/api/tenants/{id}", createdTenant.id())
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void testGetNonExistentTenant() {
        webTestClient.get()
            .uri("/api/tenants/9999")
            .exchange()
            .expectStatus().isNotFound();
    }
}