# EthereumConnector Module

## Module Goal

The EthereumConnector module provides a comprehensive integration layer for Ethereum blockchain operations within the Water Framework. It abstracts blockchain interactions through a standardized API, supporting multiple Ethereum networks, smart contract management, and transaction handling. The module enables applications to interact with Ethereum blockchains without being tied to specific client implementations, making it easy to switch between different Ethereum networks or client technologies.

## Module Technical Characteristics

### Core Technologies
- **Web3J Library**: Primary Ethereum client implementation using Web3J 4.12.2
- **JPA/Hibernate**: Entity persistence and database management
- **Water Framework Core**: Integration with Water's component system, permissions, and interceptors
- **REST API**: JAX-RS based REST endpoints for blockchain and smart contract management
- **Spring Integration**: Optional Spring framework support through dedicated service modules

### Architecture Components

#### API Layer (`EthereumConnector-api`)
- **EthClient**: Core interface for Ethereum blockchain operations
- **EthClientFactory**: Factory pattern for creating blockchain clients
- **EthTransactionReceipt**: Transaction result wrapper
- **BlockchainApi/SmartContractApi**: Entity management APIs
- **REST APIs**: JAX-RS interfaces for web service exposure

#### Model Layer (`EthereumConnector-model`)
- **EthBlockchain**: Represents blockchain network configurations
- **EthSmartContract**: Manages smart contract metadata and addresses
- **EthConstants**: Module-specific constants and configuration keys

#### Service Layer (`EthereumConnector-service`)
- **Service Implementations**: Business logic for blockchain and smart contract operations
- **Repository Implementations**: Data access layer using JPA
- **REST Controllers**: REST API implementations

#### Web3J Client (`EthereumConnector-web3j-client`)
- **EthWeb3JClient**: Web3J-based implementation of EthClient
- **EthWeb3JClientFactory**: Factory for creating Web3J clients
- **Transaction Management**: Gas management and transaction handling

### Key Features
- **Multi-Blockchain Support**: Connect to different Ethereum networks (mainnet, testnets, private networks)
- **Smart Contract Management**: Deploy, load, and interact with smart contracts
- **Transaction Handling**: Ether transfers and contract interactions
- **Account Management**: List accounts and check balances
- **Gas Management**: Configurable gas prices and limits
- **Credential Management**: Support for private keys and wallet files

## Permission and Security

### Role-Based Access Control
The module implements comprehensive permission system with predefined roles:

#### EthBlockchain Entity
- **ethBlockChainManager**: Full CRUD operations (SAVE, UPDATE, FIND, FIND_ALL, REMOVE)
- **ethBlockChainViewer**: Read-only access (FIND, FIND_ALL)
- **ethBlockChainEditor**: All operations except REMOVE (SAVE, UPDATE, FIND, FIND_ALL)

#### EthSmartContract Entity
- **ethereumSmartContractManager**: Full CRUD operations
- **ethereumSmartContractViewer**: Read-only access
- **ethereumSmartContractEditor**: All operations except REMOVE

### Security Features
- **@LoggedIn**: All REST endpoints require authentication
- **@AccessControl**: Entity-level permission annotations
- **@NoMalitiusCode**: Input validation to prevent malicious code injection
- **@NotNullOnPersist**: Validation for required fields
- **OwnedResource**: Smart contracts are owned by specific users

### Validation
- **Host/Port Validation**: Unique constraints on blockchain configurations
- **Contract Validation**: Unique constraints on contract names and addresses
- **Input Sanitization**: Protection against malicious input

## How to Use It

### 1. Module Import
Add the EthereumConnector module to your project dependencies:

```gradle
dependencies {
    implementation 'it.water.connectors.ethereum:EthereumConnector-api:${waterVersion}'
    implementation 'it.water.connectors.ethereum:EthereumConnector-model:${waterVersion}'
    implementation 'it.water.connectors.ethereum:EthereumConnector-service:${waterVersion}'
    implementation 'it.water.connectors.ethereum:EthereumConnector-web3j-client:${waterVersion}'
}
```

### 2. Basic Setup
```java
// Get the client factory from component registry
@Inject
private EthClientFactory ethereumClientFactory;

// Create blockchain configuration
EthBlockchain blockchain = new EthBlockchain("http", "localhost", "8545");

// Build client
EthClient client = ethereumClientFactory
    .withEthereumBlockChain(blockchain)
    .build();

// Set credentials
client.setCredentials("your-private-key");

// Perform operations
List<String> accounts = client.listAccounts();
BigInteger balance = client.getBalanceOf("0x...");
```

### 3. Smart Contract Operations
```java
// Deploy contract
EthSmartContract contract = new EthSmartContract();
contract.setName("MyContract");
contract.setContractClass("com.example.MyContract");
contract.setAddress("0x...");
contract.setBlockchain(blockchain);

// Save to database
smartContractApi.save(contract);

// Load and interact with contract
Web3j web3j = ((EthWeb3JClient) client).getWeb3j();
TransactionManager txManager = ((EthWeb3JClient) client).createNewTransactionManager(chainId);
ContractGasProvider gasProvider = ((EthWeb3JClient) client).createContractGasProvider(gasPrice, gasLimit, null, null);
```

### 4. REST API Usage
```bash
# Create blockchain
POST /ethereum/blockchains
{
  "protocol": "http",
  "host": "localhost",
  "port": "8545"
}

# Create smart contract
POST /ethereum/smart-contracts
{
  "name": "MyContract",
  "contractClass": "com.example.MyContract",
  "address": "0x...",
  "blockchainId": 1
}

# List all blockchains
GET /ethereum/blockchains

# Find smart contract
GET /ethereum/smart-contracts/{id}
```

## Properties and Configurations

### Module Properties
- `clientFactoryType`: Type of client factory to use (default: "Web3J")
- `ethereum-persistence-unit`: JPA persistence unit name for blockchain entities

### Blockchain Configuration Properties
- **protocol**: Network protocol (http, https, ws, wss)
- **host**: Blockchain node hostname or IP
- **port**: Blockchain node port
- **chainId**: Network chain ID (optional, for transaction signing)

### Gas Configuration
- **gasPrice**: Default gas price in Wei
- **gasLimit**: Default gas limit
- **functionsGasPrice**: Per-function gas price overrides
- **functionsGasLimit**: Per-function gas limit overrides

### Credential Properties
- **privateKey**: Direct private key for account access
- **username/password**: Wallet file credentials
- **walletPath**: Path to wallet file (when using username/password)

### Test Configuration (from Web3JClientTest)
- **GANACHE_MNEMONIC**: "stereo consider quality wild fat farm symptom bundle laundry side one lemon"
- **GANACHE_PORT**: "7547"
- **CHAIN_ID**: 1337
- **GAS_PRICE**: 20000000000
- **GAS_LIMIT**: 6721975

## How to Customize Behaviours

### 1. Custom Client Implementation
Create a custom EthClient implementation:

```java
@FrameworkComponent(properties = "clientFactoryType=CustomClient")
public class CustomEthClient implements EthClient {
    // Implement all EthClient methods
}

@FrameworkComponent(properties = "clientFactoryType=CustomClient")
public class CustomEthClientFactory implements EthClientFactory {
    // Implement factory methods
}
```

### 2. Custom Gas Provider
```java
public class CustomGasProvider implements ContractGasProvider {
    @Override
    public BigInteger getGasPrice(String contractFunc) {
        // Custom gas price logic
    }
    
    @Override
    public BigInteger getGasLimit(String contractFunc) {
        // Custom gas limit logic
    }
}
```

### 3. Custom Transaction Manager
```java
public class CustomTransactionManager extends RawTransactionManager {
    // Custom transaction management logic
}
```

### 4. Custom Blockchain Validator
```java
@FrameworkComponent
public class CustomBlockchainValidator implements Validator<EthBlockchain> {
    @Override
    public void validate(EthBlockchain blockchain) {
        // Custom validation logic
    }
}
```

### 5. Custom Smart Contract Loader
```java
@FrameworkComponent
public class CustomContractLoader {
    public <T> T loadContract(String contractAddress, Class<T> contractClass, 
                             Web3j web3j, TransactionManager txManager, 
                             ContractGasProvider gasProvider) {
        // Custom contract loading logic
    }
}
```

### 6. Custom REST Endpoints
Extend the existing REST APIs or create new ones:

```java
@Path("/ethereum/custom")
@FrameworkRestController
public class CustomEthereumRestApi {
    @POST
    @Path("/deploy")
    public EthSmartContract deployContract(EthSmartContract contract) {
        // Custom deployment logic
    }
}
```

### 7. Custom Repository Queries
```java
@FrameworkComponent
public class CustomBlockchainRepository extends EthBlockchainRepositoryImpl {
    public List<EthBlockchain> findByProtocol(String protocol) {
        // Custom query implementation
    }
}
```

### 8. Custom Permission Handler
```java
@FrameworkComponent
public class CustomEthereumPermissionHandler implements PermissionHandler {
    @Override
    public boolean hasPermission(String action, String resource, String user) {
        // Custom permission logic
    }
}
```

### 9. Custom Interceptor
```java
@FrameworkComponent
public class EthereumTransactionInterceptor implements Interceptor {
    @Override
    public Object intercept(InvocationContext context) throws Exception {
        // Pre/post transaction processing
        return context.proceed();
    }
}
```

### 10. Custom Event Handlers
```java
@FrameworkComponent
public class EthereumEventHandler {
    @EventListener
    public void onBlockchainCreated(EthBlockchain blockchain) {
        // Handle blockchain creation events
    }
    
    @EventListener
    public void onContractDeployed(EthSmartContract contract) {
        // Handle contract deployment events
    }
}
```

The EthereumConnector module provides a robust foundation for Ethereum blockchain integration while maintaining flexibility for customization and extension according to specific application requirements.

