// // CommandServiceTest.java
// package com.gtcafe.app.service;

// import com.gtcafe.app.domain.Tenant;
// import com.gtcafe.app.enums.TenantStatus;
// import com.gtcafe.app.repository.TenantRepository;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.Mockito.*;

// class CommandServiceTest {
//     private CommandService commandService;
//     private TenantRepository tenantRepository;

//     @BeforeEach
//     void setUp() {
//         tenantRepository = Mockito.mock(TenantRepository.class);
//         commandService = new CommandService(tenantRepository);
//     }

//     @Test
//     void updateTenantStatusTest() {
//         Tenant tenant = new Tenant("123", TenantStatus.INITING);
//         when(tenantRepository.findById("123")).thenReturn(tenant);

//         commandService.updateTenantStatus("123", TenantStatus.ACTIVE);
//         assertEquals(TenantStatus.ACTIVE, tenant.getStatus());

//         verify(tenantRepository, times(1)).save(tenant);
//     }
// }
