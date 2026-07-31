package com.poc.paymentengine;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.paymentengine.account.entity.Account;
import com.poc.paymentengine.account.enums.AccountStatus;
import com.poc.paymentengine.account.repository.AccountRepository;
import com.poc.paymentengine.transaction.dto.request.TransferRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class TransferControllerIntegrationTest extends BaseIntegrationTest {


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private AccountRepository accountRepository;



    @Test
    void shouldCreateTransferSuccessfully()
            throws Exception {


        Account sender = createAccount("700000001");


        Account receiver = createAccount("700000002");



        TransferRequest request =
                new TransferRequest(

                        "REQ-API-001",

                        sender.getId(),

                        receiver.getId(),

                        BigDecimal.valueOf(500)

                );



        mockMvc.perform(

                        post("/api/v1/transfers")

                                .contentType(MediaType.APPLICATION_JSON)

                                .content(objectMapper.writeValueAsString(request))

                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.success").value(true))

                .andExpect(jsonPath("$.message").exists())

                .andExpect(jsonPath("$.data.transactionReference").exists())

                .andExpect(jsonPath("$.timestamp").exists());

    }

    @Test
    void shouldRejectInvalidTransferAmount() throws Exception {


        Account sender = createAccount("710000001");

        Account receiver = createAccount("710000002");

        TransferRequest request =
                new TransferRequest(

                        "REQ-INVALID",

                        sender.getId(),

                        receiver.getId(),

                        BigDecimal.ZERO

                );



        mockMvc.perform(

                        post("/api/v1/transfers")

                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.success").value(false))

                .andExpect(jsonPath("$.message").exists())

                .andExpect(jsonPath("$.errorCode").exists());

    }





    @Test
    void shouldRetrieveAccountBalance()
            throws Exception {


        Account account =
                createAccount("720000001");



        mockMvc.perform(

                        get("/api/v1/accounts/{id}",
                                account.getId())

                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )

                .andExpect(
                        jsonPath("$.data.accountNumber")
                                .value("720000001")
                )

                .andExpect(
                        jsonPath("$.data.balance")
                                .value(10000)
                );

    }





    @Test
    void shouldReturn404WhenAccountDoesNotExist()
            throws Exception {


        mockMvc.perform(

                        get("/api/v1/accounts/{id}",
                                999999L)

                )

                .andExpect(
                        status().isNotFound()
                )

                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )

                .andExpect(
                        jsonPath("$.message")
                                .exists()
                )

                .andExpect(
                        jsonPath("$.errorCode")
                                .exists()
                );

    }




    private Account createAccount(
            String accountNumber) {


        return accountRepository.save(

                Account.builder()

                        .accountNumber(accountNumber)

                        .balance(BigDecimal.valueOf(10000))

                        .status(AccountStatus.ACTIVE)

                        .build()

        );
    }

}