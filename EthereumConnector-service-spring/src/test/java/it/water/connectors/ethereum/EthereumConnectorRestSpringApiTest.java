
package it.water.connectors.ethereum;

import com.intuit.karate.junit5.Karate;
import it.water.connectors.ethereum.api.BlockchainApi;
import it.water.connectors.ethereum.model.EthBlockchain;
import it.water.connectors.ethereum.service.EthereumConnectorApplication;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = EthereumConnectorApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "water.rest.security.jwt.validate.by.jws=false",
        "water.rest.security.jwt.validate=false",
        "water.testMode=true"
})
public class EthereumConnectorRestSpringApiTest {

    @Autowired
    private ComponentRegistry componentRegistry;

    @Autowired
    private BlockchainApi blockchainApi;

    @LocalServerPort
    private int serverPort;

    private EthBlockchain blockchain;

    @BeforeEach
    void impersonateAdmin() {
        //jwt token service is disabled, we just inject admin user for bypassing permission system
        //just remove this line if you want test with permission system working
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        blockchain = new EthBlockchain("http", "localhost", "8585");
        blockchainApi.save(blockchain);
    }

    @Karate.Test
    Karate restInterfaceTest() {
        return Karate.run("../EthereumConnector-service/src/test/resources/karate")
                .systemProperty("webServerPort", String.valueOf(serverPort))
                .systemProperty("host", "localhost")
                .systemProperty("protocol", "http")
                .systemProperty("blockchainId", String.valueOf(blockchain.getId()));
    }
}
