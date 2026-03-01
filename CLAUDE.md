# EthereumConnector Module — Ethereum Blockchain Integration

## Purpose
Provides Water Framework integration with Ethereum-compatible blockchains. Manages blockchain network configurations (`EthBlockchain`) and smart contract metadata (`EthSmartContract`) as JPA entities, and provides a pluggable `EthClient` abstraction for on-chain operations (account listing, balance queries, ETH transfers). The default implementation uses Web3J 4.12.2.

## Sub-modules

| Sub-module | Runtime | Key Classes |
|---|---|---|
| `EthereumConnector-api` | All | `BlockchainApi`, `BlockchainSystemApi`, `BlockchainRestApi`, `SmartContractApi`, `SmartContractSystemApi`, `SmartContractRestApi`, `EthClient`, `EthClientFactory`, `BlockchainRepository`, `SmartContractRepository` |
| `EthereumConnector-model` | All | `EthBlockchain`, `EthSmartContract`, `EthConstants` |
| `EthereumConnector-service` | Water/OSGi | Service impl, repositories, REST controllers |
| `EthereumConnector-web3j-client` | All | `EthWeb3JClient`, `EthWeb3JClientFactory` (Web3J implementation) |

## EthBlockchain Entity

```java
@Entity
@Table(name = "eth_blockchain",
       uniqueConstraints = @UniqueConstraint(columnNames = {"protocol", "host", "port"}))
@AccessControl(
    availableActions = {CrudActions.class},
    rolesPermissions = {
        @DefaultRoleAccess(roleName = "ethBlockChainManager", actions = {CrudActions.class}),
        @DefaultRoleAccess(roleName = "ethBlockChainViewer",  actions = {CrudActions.FIND, CrudActions.FIND_ALL}),
        @DefaultRoleAccess(roleName = "ethBlockChainEditor",  actions = {CrudActions.UPDATE, CrudActions.FIND, CrudActions.FIND_ALL})
    }
)
public class EthBlockchain extends AbstractJpaEntity implements ProtectedEntity {
    @NotNull @NoMalitiusCode
    private String name;                  // human-readable name (e.g., "Ethereum Mainnet")

    @NotNull @NoMalitiusCode
    private String protocol;              // "http" or "wss"

    @NotNull @NoMalitiusCode
    private String host;                  // RPC endpoint host

    @NotNull
    private int port;                     // RPC endpoint port

    @NoMalitiusCode
    private String description;

    // Composite unique key: protocol + host + port
}
```

## EthSmartContract Entity

```java
@Entity
@Table(name = "eth_smart_contract")
@AccessControl(
    availableActions = {CrudActions.class},
    rolesPermissions = {
        @DefaultRoleAccess(roleName = "ethereumSmartContractManager", actions = {CrudActions.class}),
        @DefaultRoleAccess(roleName = "ethereumSmartContractViewer",  actions = {CrudActions.FIND, CrudActions.FIND_ALL}),
        @DefaultRoleAccess(roleName = "ethereumSmartContractEditor",  actions = {CrudActions.UPDATE, CrudActions.FIND, CrudActions.FIND_ALL})
    }
)
public class EthSmartContract extends AbstractJpaEntity implements ProtectedEntity {
    @NotNull @NoMalitiusCode
    private String name;                  // contract identifier

    @NotNull @NoMalitiusCode
    private String contractClass;         // Web3J wrapper class (fully-qualified)

    @NotNull @NoMalitiusCode
    private String address;               // deployed contract address (0x...)

    @NotNull
    private long blockchainReference;     // FK to EthBlockchain.id
}
```

## EthClient Interface

```java
public interface EthClient {
    // Account management
    List<String> listAccounts();
    BigDecimal getBalanceOf(String address);             // in ETH

    // Transactions
    String sendEther(String toAddress, BigDecimal amount);   // returns tx hash

    // Credentials
    void setCredentials(String privateKey);
    void setCredentials(File walletFile, String walletPassword);

    // Gas configuration
    void setGasPrice(BigInteger gasPrice);
    void setGasLimit(BigInteger gasLimit);

    // Low-level
    Web3j getWeb3j();                                    // direct Web3J access
}
```

## EthClientFactory

Creates `EthClient` instances for a given `EthBlockchain` configuration:

```java
public interface EthClientFactory {
    EthClient create(EthBlockchain blockchain);
    EthClient create(String protocol, String host, int port);
}
```

`EthWeb3JClientFactory` is the Web3J-backed implementation:
```java
@FrameworkComponent
public class EthWeb3JClientFactory implements EthClientFactory {
    @Override
    public EthClient create(EthBlockchain blockchain) {
        String rpcUrl = blockchain.getProtocol() + "://" + blockchain.getHost()
                        + ":" + blockchain.getPort();
        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        return new EthWeb3JClient(web3j);
    }
}
```

## Key Operations Flow

```java
// 1. Get blockchain config
EthBlockchain mainnet = blockchainApi.findByName("My Private Chain");

// 2. Create client
EthClient client = ethClientFactory.create(mainnet);
client.setCredentials("0xdeadbeef...");   // private key

// 3. Execute operations
List<String> accounts = client.listAccounts();
BigDecimal balance = client.getBalanceOf(accounts.get(0));
String txHash = client.sendEther("0xrecipient...", new BigDecimal("0.01"));

// 4. Interact with contract
EthSmartContract contractMeta = smartContractApi.findByName("MyToken");
MyToken contract = MyToken.load(contractMeta.getAddress(),
    ((EthWeb3JClient) client).getWeb3j(),
    credentials, gasPrice, gasLimit);
contract.transfer(recipient, amount).send();
```

## REST Endpoints

| Method | Path | Permission |
|---|---|---|
| `POST` | `/water/ethereum/blockchains` | ethBlockChainManager |
| `PUT` | `/water/ethereum/blockchains` | ethBlockChainManager / ethBlockChainEditor |
| `GET` | `/water/ethereum/blockchains/{id}` | ethBlockChainViewer |
| `GET` | `/water/ethereum/blockchains` | ethBlockChainViewer |
| `DELETE` | `/water/ethereum/blockchains/{id}` | ethBlockChainManager |
| `POST` | `/water/ethereum/smart-contracts` | ethereumSmartContractManager |
| `PUT` | `/water/ethereum/smart-contracts` | ethereumSmartContractManager / ethereumSmartContractEditor |
| `GET` | `/water/ethereum/smart-contracts/{id}` | ethereumSmartContractViewer |
| `GET` | `/water/ethereum/smart-contracts` | ethereumSmartContractViewer |
| `DELETE` | `/water/ethereum/smart-contracts/{id}` | ethereumSmartContractManager |

## Default Roles

| Role | Allowed Actions |
|---|---|
| `ethBlockChainManager` | Full CRUD on blockchains |
| `ethBlockChainViewer` | FIND, FIND_ALL |
| `ethBlockChainEditor` | UPDATE, FIND, FIND_ALL |
| `ethereumSmartContractManager` | Full CRUD on smart contracts |
| `ethereumSmartContractViewer` | FIND, FIND_ALL |
| `ethereumSmartContractEditor` | UPDATE, FIND, FIND_ALL |

## Dependencies
- `it.water.repository.jpa:JpaRepository-api` — `AbstractJpaEntity`
- `it.water.core:Core-permission` — `@AccessControl`, `CrudActions`
- `it.water.rest:Rest-persistence` — `BaseEntityRestApi`
- `org.web3j:core:4.12.2` — Ethereum Java client (in `EthereumConnector-web3j-client`)

## Testing
- Unit tests: `WaterTestExtension` — mock `EthClient` (use Ganache or Hardhat for integration testing)
- `EthBlockchain` and `EthSmartContract` CRUD: standard `ApiTest` pattern
- REST tests: **Karate only** — never JUnit direct calls to REST controllers
- For blockchain integration: use Hardhat local network or `EthereumTestContainers`

## Code Generation Rules
- NEVER store private keys in `EthBlockchain` entity — use a secrets manager (Vault, AWS Secrets Manager) and inject at runtime
- `EthClient` is stateful (credentials, gas config) — create a new instance per operation context, never share across threads
- `contractClass` in `EthSmartContract` refers to the Web3J-generated wrapper class name — generate wrappers with `web3j generate solidity` CLI
- REST controllers tested **exclusively via Karate**
- For new blockchain protocols (not Ethereum-compatible): implement `EthClient` and `EthClientFactory`, register as `@FrameworkComponent`
