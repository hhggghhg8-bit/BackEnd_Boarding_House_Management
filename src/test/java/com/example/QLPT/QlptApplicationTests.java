package com.example.QLPT;

import com.example.QLPT.Config.Firebase.FcmNotificationService;
import com.example.QLPT.Enity.User;
import com.example.QLPT.Enity.Enum.Enum;
import com.example.QLPT.Repository.*;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import java.sql.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/** Uses a unique PostgreSQL schema, never the application's public tables. */
@SpringBootTest
@AutoConfigureMockMvc
class QlptApplicationTests {
    static final String SCHEMA = "qlpt_test_" + UUID.randomUUID().toString().replace("-", "");
    static String url, username, password;
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) throws Exception {
        var config = new Properties();
        try (var stream = QlptApplicationTests.class.getResourceAsStream("/application.properties")) { config.load(stream); }
        url = System.getenv().getOrDefault("TEST_DATABASE_URL", config.getProperty("spring.datasource.url"));
        username = System.getenv().getOrDefault("TEST_DATABASE_USERNAME", config.getProperty("spring.datasource.username"));
        password = System.getenv().getOrDefault("TEST_DATABASE_PASSWORD", config.getProperty("spring.datasource.password"));
        try (var c = DriverManager.getConnection(url, username, password); var s = c.createStatement()) { s.execute("CREATE SCHEMA " + SCHEMA); }
        registry.add("spring.datasource.url", () -> url + (url.contains("?") ? "&" : "?") + "currentSchema=" + SCHEMA);
        registry.add("spring.datasource.username", () -> username);
        registry.add("spring.datasource.password", () -> password);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }
    @AfterAll static void cleanup() throws Exception {
        try (var c = DriverManager.getConnection(url, username, password); var s = c.createStatement()) { s.execute("DROP SCHEMA " + SCHEMA + " CASCADE"); }
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired PropertyRepository properties;
    @Autowired RoomRepository rooms;
    @Autowired ContractRepository contracts;
    @Autowired InvoiceRepository invoices;
    @Autowired com.example.QLPT.Funsion.landlady.ServiceRoom.ServiceRoomService billing;
    @MockBean FcmNotificationService push;
    String landlord;
    @BeforeEach void setup() throws Exception {
        // Reset only this run's schema so tests do not depend on execution order.
        try (var c = DriverManager.getConnection(url, username, password);
             var query = c.prepareStatement("SELECT tablename FROM pg_tables WHERE schemaname = ?")) {
            query.setString(1, SCHEMA);
            var tables = new ArrayList<String>();
            try (var rows = query.executeQuery()) {
                while (rows.next()) tables.add("\"" + SCHEMA + "\".\"" + rows.getString(1).replace("\"", "\"\"") + "\"");
            }
            try (var statement = c.createStatement()) {
                statement.execute("TRUNCATE TABLE " + String.join(", ", tables) + " RESTART IDENTITY CASCADE");
            }
        }
        billing.initDefaultServices();
        landlord = UUID.randomUUID() + "@landlord.test";
        users.saveAndFlush(User.builder().email(landlord).password("unused").role(Enum.Role.LANDLORD).status(Enum.UserStatus.ACTIVE).build());
    }
    JsonNode call(String method, String path, Object body, String email, String role) throws Exception {
        var request = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(method), path);
        if (email != null) request.with(user(email).roles(role));
        if (body != null) request.contentType("application/json").content(json.writeValueAsBytes(body));
        var response = mvc.perform(request).andExpect(status().is2xxSuccessful()).andReturn().getResponse().getContentAsString();
        return response.startsWith("{") || response.startsWith("[") ? json.readTree(response) : json.getNodeFactory().textNode(response);
    }
    long property() throws Exception {
        call("POST", "/api/landlord/manage/properties", Map.of("name", "Test house", "address", "Test address", "billingDay", 1, "paymentDeadlineDay", 10), landlord, "LANDLORD");
        return properties.findByLandlordIdAndDeletedAtIsNull(users.findByEmail(landlord).orElseThrow().getId()).getFirst().getId();
    }
    long room(long property, String number) throws Exception {
        call("POST", "/api/landlord/manage/rooms", Map.of("propertyId", property, "roomNumber", number, "area", 20, "maxCapacity", 2, "basePrice", 1000000), landlord, "LANDLORD");
        return rooms.findByPropertyId(property).stream().filter(r -> number.equals(r.getRoomNumber())).findFirst().orElseThrow().getId();
    }
    @Test void completeRentalBillingAndPaymentWorkflow() throws Exception {
        String tenant = UUID.randomUUID() + "@tenant.test";
        call("POST", "/api/auth/register", Map.of("email", tenant, "password", "123456", "fullName", "Test tenant"), null, null);
        var login = call("POST", "/api/auth/login", Map.of("email", tenant, "password", "123456"), null, null);
        assertThat(login.toString()).contains("Bearer");
        mvc.perform(get("/api/profile/me").header("Authorization", "Bearer " + login.get("token").asText())).andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType("application/json").content(json.writeValueAsBytes(Map.of("email", tenant, "password", "wrong")))).andExpect(status().isBadRequest());
        call("PUT", "/api/tenant/update-profile", Map.of("fullName", "Updated tenant", "hometown", "Hanoi"), tenant, "TENANT");
        call("GET", "/api/profile/me", null, tenant, "TENANT");
        call("PUT", "/api/tenant/fcm-token", Map.of("fcmToken", "test-token"), tenant, "TENANT");
        call("POST", "/api/auth/logout-fcm", Map.of(), tenant, "TENANT");
        long p = property(), r = room(p, "A1");
        call("POST", "/api/landlord/manage/rooms/bulk", Map.of("propertyId", p, "numberOfFloors", 2, "roomsPerFloor", 2, "basePrice", 1000000), landlord, "LANDLORD");
        assertThat(rooms.findByPropertyId(p)).hasSize(5);
        call("PUT", "/api/landlord/manage/rooms/" + r, Map.of("area", 25), landlord, "LANDLORD");
        call("POST", "/api/landlord/manage/assign-tenant", Map.of("roomId", r, "tenantEmail", tenant, "startDate", "2026-01-01", "endDate", "2027-01-01", "deposit", 1000000), landlord, "LANDLORD");
        call("GET", "/api/landlord/properties/dashboard", null, landlord, "LANDLORD");
        call("GET", "/api/tenant/my-room", null, tenant, "TENANT");
        call("GET", "/api/landlord/room-services/catalog", null, landlord, "LANDLORD");
        call("GET", "/api/landlord/room-services/room/" + r, null, landlord, "LANDLORD");
        var data = new MockMultipartFile("data", "data.json", "application/json", json.writeValueAsBytes(Map.of("billingCycle", "01-2026", "oldElectricity", 0, "newElectricity", 10, "oldWater", 0, "newWater", 2)));
        var image = new MockMultipartFile("image", "meter.jpg", "image/jpeg", new byte[]{1,2,3});
        var result = mvc.perform(multipart("/api/landlord/room-services/room/" + r + "/record-utility").file(data).file(image).with(user(landlord).roles("LANDLORD"))).andExpect(status().isOk()).andReturn();
        String imageUrl = json.readTree(result.getResponse().getContentAsString()).get("meterImageUrl").asText();
        try {
            var contract = contracts.findByRoomIdAndStatus(r, Enum.ContractStatus.ACTIVE).orElseThrow();
            var invoice = invoices.findByContractIdAndBillingCycle(contract.getId(), "01-2026").orElseThrow();
            assertThat(invoice.getTotalAmount()).isEqualByComparingTo("1255000");
            call("GET", "/api/landlord/room-services/room/" + r + "/history", null, landlord, "LANDLORD");
            call("GET", "/api/tenant/my-invoices", null, tenant, "TENANT");
            call("GET", "/api/pay/vietqr/" + invoice.getId(), null, tenant, "TENANT");
            mvc.perform(put("/api/landlord/manage/rooms/" + r + "/checkout").with(user(landlord).roles("LANDLORD"))).andExpect(status().isBadRequest());
            call("PUT", "/api/landlord/room-services/invoice/" + invoice.getId() + "/pay-cash", null, landlord, "LANDLORD");
            call("GET", "/api/landlady/users/" + users.findByEmail(tenant).orElseThrow().getId() + "/history", null, landlord, "LANDLORD");
            call("PUT", "/api/landlord/manage/rooms/" + r + "/checkout", null, landlord, "LANDLORD");
            call("DELETE", "/api/landlord/properties/rooms/" + r, null, landlord, "LANDLORD");
            assertThat(rooms.findById(r)).isEmpty();
            // Regression: old invoices must remain readable after their room is soft deleted.
            assertThat(invoices.findById(invoice.getId()).orElseThrow().getContract().getRoom().getRoomNumber()).isEqualTo("A1");
            call("GET", "/api/landlady/users/" + users.findByEmail(tenant).orElseThrow().getId() + "/history", null, landlord, "LANDLORD");
            call("GET", "/api/landlady/users", null, landlord, "LANDLORD");
            long nextRoom = room(p, "A1");
            assertThat(nextRoom).isNotEqualTo(r);
            call("GET", "/api/landlord/room-services/room/" + nextRoom, null, landlord, "LANDLORD");
        } finally {
            java.nio.file.Files.deleteIfExists(java.nio.file.Path.of("uploads/meters", imageUrl.substring(imageUrl.lastIndexOf('/') + 1)));
        }
    }
    @Test void catalogAndUserManagementAndAlternativeRentalRoutes() throws Exception {
        var tenant = call("POST", "/api/landlady/users", Map.of("email", UUID.randomUUID() + "@test.test", "fullName", "New tenant"), landlord, "LANDLORD");
        long id = tenant.get("id").asLong();
        call("PUT", "/api/landlady/users/" + id, Map.of("fullName", "Changed", "role", "TENANT", "status", "ACTIVE"), landlord, "LANDLORD");
        call("GET", "/api/landlady/users", null, landlord, "LANDLORD");
        call("GET", "/api/landlady/users/" + id, null, landlord, "LANDLORD");
        long r = room(property(), "B1");
        call("PUT", "/api/landlord/properties/rooms/" + r, Map.of("roomNumber", "B2", "basePrice", 1200000), landlord, "LANDLORD");
        call("POST", "/api/landlord/properties/rooms/" + r + "/assign-tenant", Map.of("tenantId", id, "depositAmount", 500000), landlord, "LANDLORD");
        call("POST", "/api/landlord/properties/rooms/" + r + "/remove-tenant", null, landlord, "LANDLORD");
        String serviceName = "Extra-" + UUID.randomUUID();
        call("POST", "/api/landlord/room-services/catalog", Map.of("name", serviceName, "unit", "month", "defaultPrice", 100), landlord, "LANDLORD");
        var catalog = call("GET", "/api/landlord/room-services/catalog", null, landlord, "LANDLORD");
        long serviceId = 0;
        for (var s : catalog) if (serviceName.equals(s.get("name").asText())) serviceId = s.get("id").asLong();
        call("POST", "/api/landlord/room-services/room/" + r, Map.of("serviceId", serviceId), landlord, "LANDLORD");
        var summary = call("GET", "/api/landlord/room-services/room/" + r, null, landlord, "LANDLORD");
        long rsId = 0;
        for (var s : summary.get("services")) if (s.get("serviceId").asLong() == serviceId) rsId = s.get("roomServiceId").asLong();
        call("PUT", "/api/landlord/room-services/" + rsId + "/price?customPrice=200", null, landlord, "LANDLORD");
        call("DELETE", "/api/landlord/room-services/" + rsId, null, landlord, "LANDLORD");
    }
    @Test void malformedCreateDoesNotExposeSql() throws Exception {
        var result = mvc.perform(post("/api/landlady/users").contentType("application/json").content("{\"fullName\":\"Missing email\"}").with(user(landlord).roles("LANDLORD"))).andExpect(status().isBadRequest()).andReturn();
        assertThat(result.getResponse().getContentAsString()).doesNotContain("insert into", "constraint", "SQL", "null value in column");
    }
    @Test void invalidRoomPriceIsRejectedBeforeDatabaseWrite() throws Exception {
        long p = property();
        mvc.perform(post("/api/landlord/manage/rooms").contentType("application/json").content(json.writeValueAsBytes(Map.of("propertyId", p, "roomNumber", "bad", "basePrice", -1))).with(user(landlord).roles("LANDLORD"))).andExpect(status().isBadRequest());
        assertThat(rooms.findByPropertyId(p)).isEmpty();
    }
    @Test void longFcmTokenCanBeSaved() throws Exception {
        String email = UUID.randomUUID() + "@test.test";
        call("POST", "/api/auth/register", Map.of("email", email, "password", "123456"), null, null);
        call("PUT", "/api/tenant/fcm-token", Map.of("fcmToken", "t".repeat(300)), email, "TENANT");
        assertThat(users.findByEmail(email).orElseThrow().getFcmToken()).hasSize(300);
        call("POST", "/api/auth/login", Map.of("email", email, "password", "123456", "fcmToken", "t".repeat(400)), null, null);
        assertThat(users.findByEmail(email).orElseThrow().getFcmToken()).hasSize(400);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"/api/landlord/manage/properties", "/api/landlord/manage/rooms", "/api/landlord/manage/rooms/bulk", "/api/landlord/manage/assign-tenant", "/api/landlord/room-services/catalog"})
    void missingCreateFieldsReturn400(String path) throws Exception {
        mvc.perform(post(path).contentType("application/json").content("{}").with(user(landlord).roles("LANDLORD"))).andExpect(status().isBadRequest());
    }
    @Test void oversizedFieldsRejectedWithoutSqlErrors() throws Exception {
        mvc.perform(post("/api/auth/register").contentType("application/json").content(json.writeValueAsBytes(Map.of("email", "a".repeat(260) + "@test.test", "password", "123456")))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/landlord/manage/properties").contentType("application/json").content(json.writeValueAsBytes(Map.of("name", "n".repeat(256), "address", "address"))).with(user(landlord).roles("LANDLORD"))).andExpect(status().isBadRequest());
    }
    @Test void duplicatesAndBulkFailureLeaveNoPartialRooms() throws Exception {
        long p = property(); room(p, "102");
        mvc.perform(post("/api/landlord/manage/rooms/bulk").contentType("application/json").content(json.writeValueAsBytes(Map.of("propertyId", p, "numberOfFloors", 1, "roomsPerFloor", 2, "basePrice", 1000000))).with(user(landlord).roles("LANDLORD"))).andExpect(status().isBadRequest());
        assertThat(rooms.findByPropertyId(p)).hasSize(1);
        mvc.perform(post("/api/landlord/manage/rooms").contentType("application/json").content(json.writeValueAsBytes(Map.of("propertyId", p, "roomNumber", "102", "basePrice", 1000000))).with(user(landlord).roles("LANDLORD"))).andExpect(status().isBadRequest());
    }
    @Test void rolesProtectManagementEndpoints() throws Exception {
        mvc.perform(post("/api/landlord/manage/properties").contentType("application/json").content("{}").with(user("tenant").roles("TENANT"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/landlady/users")).andExpect(status().isForbidden());
    }
    @Test void bankPaymentHandlerAndReplay() throws Exception {
        long r = room(property(), "PAY");
        User tenant = users.saveAndFlush(User.builder().email(UUID.randomUUID() + "@test.test").password("unused").role(Enum.Role.TENANT).status(Enum.UserStatus.ACTIVE).build());
        call("POST", "/api/landlord/properties/rooms/" + r + "/assign-tenant", Map.of("tenantId", tenant.getId()), landlord, "LANDLORD");
        var invoice = invoices.saveAndFlush(com.example.QLPT.Enity.Invoice.builder().contract(contracts.findByRoomIdAndStatus(r, Enum.ContractStatus.ACTIVE).orElseThrow()).totalAmount(new java.math.BigDecimal("1000000")).status(Enum.InvoiceStatus.UNPAID).paymentCode("HD123ABC").billingCycle("02-2026").build());
        call("POST", "/api/pay/sepay-webhook", Map.of("transferAmount", 100, "content", "HD123ABC"), null, null);
        assertThat(invoices.findById(invoice.getId()).orElseThrow().getStatus()).isEqualTo(Enum.InvoiceStatus.UNPAID);
        call("POST", "/api/pay/sepay-webhook", Map.of("transferAmount", 1000000, "content", "HD123ABC"), null, null);
        call("POST", "/api/pay/sepay-webhook", Map.of("transferAmount", 1000000, "content", "HD123ABC"), null, null);
        assertThat(invoices.findById(invoice.getId()).orElseThrow().getStatus()).isEqualTo(Enum.InvoiceStatus.PAID);
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(value = Enum.InvoiceStatus.class, names = {"UNPAID", "PARTIALLY_PAID", "OVERDUE"})
    void bothCheckoutRoutesBlockEveryOutstandingStatus(Enum.InvoiceStatus status) throws Exception {
        long r = room(property(), "DEBT");
        User tenant = users.saveAndFlush(User.builder().email(UUID.randomUUID() + "@test.test").password("unused").role(Enum.Role.TENANT).status(Enum.UserStatus.ACTIVE).build());
        call("POST", "/api/landlord/manage/assign-tenant", Map.of("roomId", r, "tenantEmail", tenant.getEmail()), landlord, "LANDLORD");
        var contract = contracts.findByRoomIdAndStatus(r, Enum.ContractStatus.ACTIVE).orElseThrow();
        assertThat(contract.getStartDate()).isNotNull();
        invoices.saveAndFlush(com.example.QLPT.Enity.Invoice.builder().contract(contract).status(status).totalAmount(java.math.BigDecimal.TEN).build());
        mvc.perform(put("/api/landlord/manage/rooms/" + r + "/checkout").with(user(landlord).roles("LANDLORD"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/landlord/properties/rooms/" + r + "/remove-tenant").with(user(landlord).roles("LANDLORD"))).andExpect(status().isBadRequest());
        assertThat(rooms.findById(r).orElseThrow().getStatus()).isEqualTo(Enum.RoomStatus.RENTED);
        assertThat(contracts.findById(contract.getId()).orElseThrow().getStatus()).isEqualTo(Enum.ContractStatus.ACTIVE);
    }
    @Test void fcmMigrationPreservesExistingDataAndIsRepeatable() throws Exception {
        try (var c = DriverManager.getConnection(url, username, password); var s = c.createStatement()) {
            s.execute("CREATE TEMP TABLE users (fcm_token varchar(255))");
            s.execute("INSERT INTO users VALUES ('existing-token')");
            String migration = java.nio.file.Files.readString(java.nio.file.Path.of("maintenance/fix-fcm-token.sql"));
            s.execute(migration); s.execute(migration);
            try (var rows = s.executeQuery("SELECT fcm_token FROM users")) { rows.next(); assertThat(rows.getString(1)).isEqualTo("existing-token"); }
            s.execute("UPDATE users SET fcm_token = repeat('t', 400)");
            try (var rows = s.executeQuery("SELECT length(fcm_token) FROM users")) { rows.next(); assertThat(rows.getInt(1)).isEqualTo(400); }
        }
    }
}
