package com.example.usersservices_mychatserver.integration.integration;

import com.example.usersservices_mychatserver.entity.request.ActiveAccountCodeData;
import com.example.usersservices_mychatserver.integration.integration.dbUtils.DatabaseActionUtilService;
import com.example.usersservices_mychatserver.integration.integration.exampleDataRequest.CorrectRequestData;
import com.example.usersservices_mychatserver.integration.integration.request_util.RequestUtil;
import com.example.usersservices_mychatserver.port.out.logic.GenerateRandomCodePort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.net.URISyntaxException;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class DefaultTestConfiguration {
    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected DatabaseActionUtilService databaseActionUtilService;

    @LocalServerPort
    protected int serverPort;

    @MockBean
    private GenerateRandomCodePort randomCodePort;

    protected RequestUtil createRequestUtil() {
        return new RequestUtil(serverPort);
    }

    @BeforeEach
    public void clearAllDatabaseInDatabaseBeforeRunTest() {
        databaseActionUtilService.clearAllUsersInDatabase();
        databaseActionUtilService.clearAllVerificationCodesInDatabase();
    }

    @AfterEach
    public void clearAllDataInDatabaseAfterRunTest() {
        databaseActionUtilService.clearAllDataInDatabase();
        databaseActionUtilService.clearAllVerificationCodesInDatabase();
    }


    void createUserAccount(boolean isActiveAccount) throws URISyntaxException {
        when(randomCodePort.generateCode()).thenReturn("000000");

        webTestClient.post().uri(createRequestUtil().createRequestRegister())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(CorrectRequestData.USER_REGISTER_DATA))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.correctResponse").isEqualTo("true");


        if (isActiveAccount) {
            ActiveAccountCodeData activeAccountCodeData = new ActiveAccountCodeData("000000", CorrectRequestData.USER_REGISTER_DATA.email());
            webTestClient.post().uri(createRequestUtil().createRequestActiveUserAccount())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(activeAccountCodeData))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.correctResponse").isEqualTo("true");
        }
    }


    void createActivatedUserAccount() throws URISyntaxException {
        createUserAccount(true);
    }

    void createUserAccountWithNotActiveAccount() throws URISyntaxException {
        createUserAccount(false);
    }


}
