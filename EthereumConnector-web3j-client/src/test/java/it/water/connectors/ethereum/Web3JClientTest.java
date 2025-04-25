package it.water.connectors.ethereum;

import it.water.connectors.ethereum.api.EthClient;
import it.water.connectors.ethereum.api.EthClientFactory;
import it.water.connectors.ethereum.api.EthTransactionReceipt;
import it.water.connectors.ethereum.model.EthBlockchain;
import it.water.connectors.ethereum.model.EthSmartContract;
import it.water.core.api.service.Service;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.testing.utils.junit.WaterTestExtension;
import lombok.Setter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Web3JClientTest implements Service {
    private static Logger logger = LoggerFactory.getLogger(Web3JClientTest.class);
    private static final String CONTRACT_DEFAULT_NAME = "MY_DATA_CERTIFICATION";
    private static final String ACCOUNT_PRIVATE_KEY = "5c7a050c7b0e3a6896e9667a6dff3a6b389c665aaed218c352071890c05520ee";
    private static final String GANACHE_MNEMONIC = "stereo consider quality wild fat farm symptom bundle laundry side one lemon";
    private static final String GANACHE_PORT = "7547";
    private static final long CHAIN_ID = 1337;
    private static final long GAS_PRICE = 20000000000l;
    private static final long GAS_LIMIT = 6721975l;
    private static Process ganacheProcess;

    private EthClient ethereumClient;
    private EthBlockchain localBlockChain;
    private EthSmartContract contract;

    @Inject
    @Setter
    private EthClientFactory ethereumClientFactory;

    /**
     * This method initializes ganache and waits for it
     */
    @BeforeAll
    void initGanache() {
        localBlockChain = new EthBlockchain("http", "localhost", GANACHE_PORT);
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("ganache-cli", "-m", GANACHE_MNEMONIC, "-p", GANACHE_PORT);
        try {
            ganacheProcess = pb.start();
            logger.info("Waiting for ganache process to start...");
            await()
                    .atMost(10, TimeUnit.SECONDS)
                    .pollInterval(Duration.ofSeconds(1))
                    .ignoreExceptions()
                    .until(() -> ganacheProcess.isAlive());
            ethereumClient = ethereumClientFactory.withEthereumBlockChain(localBlockChain).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assertions.fail();
        }
    }

    /**
     * Closes ganache after all tests
     */
    @AfterAll
    void closeGanache() {
        ganacheProcess.destroy();
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptions()
                .until(() -> !ganacheProcess.isAlive());
        Assertions.assertFalse(ganacheProcess.isAlive());
    }

    /**
     * This methods retrieves the account list
     */
    @Test
    @Order(1)
    void getAccountListShouldWork() {
        List<String> accounts = ethereumClient.listAccounts();
        Assertions.assertTrue(accounts.size() > 0);
    }

    @Test
    @Order(2)
    void checkClient() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new EthWeb3JClient(null);
        });
    }

    /**
     * This methods tries to transfer funds from first account to the second
     */
    @Test
    @Order(3)
    void transferFundsShouldWork() {
        List<String> accounts = ethereumClient.listAccounts();
        try {
            String account1 = accounts.get(1);
            int etherAmount = 1;
            BigDecimal amount = BigDecimal.valueOf(etherAmount);
            Assertions.assertThrows(IllegalStateException.class, () -> {
                ethereumClient.transferEther(null,amount );
            });

            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                ethereumClient.setCredentials(null,null);
            });

            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                ethereumClient.setCredentials("","");
            });

            ethereumClient.setCredentials(ACCOUNT_PRIVATE_KEY);
            long oldBalanceAccount1 = ethereumClient.getBalanceOf(accounts.get(1)).longValue();
            EthTransactionReceipt receipt = ethereumClient.transferEther(account1, new BigDecimal(etherAmount));
            Assertions.assertNotNull(receipt);
            Assertions.assertEquals(receipt.getFrom(), accounts.get(0));
            Assertions.assertEquals(receipt.getTo(), accounts.get(1));
            Assertions.assertNotNull(receipt.getTransactionHash());
            Assertions.assertNotNull(receipt.getTransactionIndex());
            Assertions.assertNotNull(receipt.getBlockHash());
            Assertions.assertNotNull(receipt.getBlockNumber());
            Assertions.assertNotNull(receipt.getCumulativeGasUsed());
            Assertions.assertNull(receipt.getContractAddress());
            Assertions.assertNull(receipt.getRoot());
            Assertions.assertNotNull(receipt.getStatus());
            Assertions.assertNotNull(receipt.getEffectiveGasPrice());
            Assertions.assertNotNull(receipt.getType());
            Assertions.assertNull(receipt.getRevertReason());
            long newBalanceAccount1 = ethereumClient.getBalanceOf(accounts.get(1)).longValue();
            //creating the checkbalance variable equal to old value plus the relative wei amount
            long checkBalance = oldBalanceAccount1 + (etherAmount * 1000000000000000000l);
            Assertions.assertEquals(checkBalance, newBalanceAccount1);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Gets the balance of account 1
     */
    @Test
    @Order(4)
    void getBalanceShouldWork() {
        List<String> accounts = ethereumClient.listAccounts();
        String account1 = accounts.get(1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ethereumClient.setCredentials(null);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ethereumClient.setCredentials("");
        });
        ethereumClient.setCredentials(ACCOUNT_PRIVATE_KEY);
        BigInteger balance = ethereumClient.getBalanceOf(account1);
        Assertions.assertTrue(balance.longValue() > 0);
    }

    /**
     * Tries to deploy solidity contract and saving the address result to local database
     *
     * @throws Exception
     */
    @Test
    @Order(5)
    void deployContractShouldWork() throws Exception {
        EthWeb3JClient web3jClient = (EthWeb3JClient) ethereumClient;
        Web3j web3j = web3jClient.getWeb3j();
        BigInteger gasLimit = BigInteger.valueOf(GAS_LIMIT);
        BigInteger gasPrice = BigInteger.valueOf(GAS_PRICE);
        TransactionManager transactionManager = web3jClient.createNewTransactionManager(CHAIN_ID);
        ContractGasProvider gasProvider = web3jClient.createContractGasProvider(gasPrice, gasLimit, null, null);
        Assertions.assertNotNull(gasProvider.getGasPrice(null));
        Assertions.assertNotNull(gasProvider.getGasLimit(null));
        Map<String,BigInteger> functionGasPrice = new HashMap<>();
        functionGasPrice.put("notarizeDocument",new BigInteger("10"));
        Map<String,BigInteger> functionGasLimit = new HashMap<>();
        functionGasLimit.put("notarizeDocument",new BigInteger("10000"));
        gasProvider = web3jClient.createContractGasProvider(gasPrice, gasLimit, functionGasPrice, functionGasLimit);
        Assertions.assertNotNull(gasProvider.getGasPrice("notarizeDocument"));
        Assertions.assertNotNull(gasProvider.getGasPrice("notExisting"));
        Assertions.assertNotNull(gasProvider.getGasPrice());
        Assertions.assertNotNull(gasProvider.getGasLimit("notarizeDocument"));
        Assertions.assertNotNull(gasProvider.getGasLimit("notExisting"));
        Assertions.assertNotNull(gasProvider.getGasLimit());
        DataRegistry dataRegistryContract = DataRegistry.deploy(web3j, transactionManager, gasProvider).send();
        String contractAddress = dataRegistryContract.getContractAddress();
        Assertions.assertNotNull(dataRegistryContract);
        Assertions.assertNotNull(contractAddress);
        Assertions.assertTrue(contractAddress.length() > 0);
        contract = new EthSmartContract(DataRegistry.class.getName(), CONTRACT_DEFAULT_NAME, contractAddress, dataRegistryContract.getTransactionReceipt().get().toString(), localBlockChain, 0L);
        Assertions.assertNotNull(contract);
    }

    /**
     * Loads smart contract address from local database and tries to load the real contract from the blockchain
     */
    @Test
    @Order(6)
    void loadContractShouldWork() {
        EthWeb3JClient web3jClient = (EthWeb3JClient) ethereumClient;
        Web3j web3j = web3jClient.getWeb3j();
        TransactionManager transactionManager = web3jClient.createNewTransactionManager(CHAIN_ID);
        BigInteger gasLimit = BigInteger.valueOf(GAS_LIMIT);
        BigInteger gasPrice = BigInteger.valueOf(GAS_PRICE);
        ContractGasProvider gasProvider = web3jClient.createContractGasProvider(gasPrice, gasLimit, null, null);
        DataRegistry dataRegistryContract = DataRegistry.load(contract.getAddress(), web3j, transactionManager, gasProvider);
        Assertions.assertNotNull(dataRegistryContract);
    }

    /**
     * Loads the contract and invoke a transaction on it
     */
    @Test
    void interactWithContractShouldWork() {
        List<String> accounts = ethereumClient.listAccounts();
        EthWeb3JClient web3jClient = (EthWeb3JClient) ethereumClient;
        Web3j web3j = web3jClient.getWeb3j();
        BigInteger gasLimit = BigInteger.valueOf(GAS_LIMIT);
        BigInteger gasPrice = BigInteger.valueOf(GAS_PRICE);
        TransactionManager transactionManager = web3jClient.createNewTransactionManager(CHAIN_ID);
        ContractGasProvider gasProvider = web3jClient.createContractGasProvider(gasPrice, gasLimit, null, null);
        TransactionReceipt receipt = null;
        try {
            DataRegistry dataRegistryContract = DataRegistry.load(contract.getAddress(), web3j, transactionManager, gasProvider);
            String dataToSign = "This message must be signed and registered to the blockchain";
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(dataToSign.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest();
            receipt = dataRegistryContract.notarizeDocument(hash).send();
            Assertions.assertEquals(accounts.get(0), receipt.getFrom());
            List<DataRegistry.NotarizedEventResponse> notarizedEvents = dataRegistryContract.getNotarizedEvents(receipt);
            Assertions.assertEquals(new String(hash), new String(notarizedEvents.get(0)._dataHash));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assertions.fail();
        }
    }
}
