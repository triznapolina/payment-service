package com.integration;

import com.dto.PaymentDto;
import com.entity.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.kafka.PaymentKafkaProducer;
import com.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Date;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class PaymentControllerTest {

    @Container
    protected static final MongoDBContainer mongoDBContainer = new MongoDBContainer(
            DockerImageName.parse("mongo:6.0"));

    @Container
    protected static final WireMockContainer wiremockContainer = new WireMockContainer(
            DockerImageName.parse("wiremock/wiremock:3.3.1"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
        registry.add("spring.data.mongodb.port", () -> mongoDBContainer.getMappedPort(27017));
        registry.add("spring.data.mongodb.database", () -> "paymentsDB");

        registry.add("payment.gateway.url",
                () -> "http://" + wiremockContainer.getHost() + ":" + wiremockContainer.getMappedPort(8080));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentKafkaProducer paymentKafkaProducer;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        WireMock.configureFor(wiremockContainer.getHost(), wiremockContainer.getMappedPort(8080));
        WireMock.reset();
    }


    @Test
    void createPayment() throws Exception {
        stubFor(post(urlEqualTo("/payments"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"SUCCESS\"}")));

        PaymentDto dto = new PaymentDto();
        dto.setId(1L);
        dto.setOrderId(100L);
        dto.setUserId(1L);
        dto.setPaymentAmount(500.0);
        dto.setTimestamp(new java.util.Date());
        dto.setStatus("PENDING");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/payments")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk());
    }



    @Test
    void getAllPayments_shouldReturnList() throws Exception {

        paymentRepository.saveAll(List.of(
                new Payment(1L, 1L, 1L, "SUCCESS", new Date(), 100.0),
                new Payment(2L, 2L, 2L, "FAILED", new Date(), 200.0)
        ));

        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getPaymentsByOrderId_shouldFilterCorrectly() throws Exception {
        paymentRepository.save(new Payment(1L, 555L, 1L, "SUCCESS", new Date(), 10.0));

        mockMvc.perform((RequestBuilder) get("/payments/order/555"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId").value(555));
    }

    @Test
    void getPaymentsByStatus_shouldReturnMatches() throws Exception {
        paymentRepository.save(new Payment(1L, 1L, 1L, "SUCCESS", new Date(), 10.0));
        paymentRepository.save(new Payment(2L, 2L, 1L, "PENDING", new Date(),10.0));

        mockMvc.perform((RequestBuilder) get("/payments/status/SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

}