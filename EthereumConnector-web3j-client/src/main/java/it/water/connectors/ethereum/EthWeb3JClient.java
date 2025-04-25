/*
 * Copyright 2019-2023 HyperIoT
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.water.connectors.ethereum;

import it.water.connectors.ethereum.api.EthClient;
import it.water.connectors.ethereum.api.EthTransactionReceipt;
import it.water.core.model.exceptions.WaterRuntimeException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * @Author Aristide Cittadino
 * Ethereum Web3J Client implementation
 */
public class EthWeb3JClient implements EthClient {
    private static Logger logger = LoggerFactory.getLogger(EthWeb3JClient.class);
    @Getter
    private final Web3j web3j;
    private Credentials credentials;

    public EthWeb3JClient(Web3j web3j) {
        if (web3j == null)
            throw new IllegalArgumentException("web3j is null");
        this.web3j = web3j;
    }

    public TransactionManager createNewTransactionManager(long chainId) {
        return new RawTransactionManager(web3j, credentials, chainId);
    }

    public ContractGasProvider createContractGasProvider(final BigInteger gasPrice, final BigInteger gasLimit, final Map<String, BigInteger> functionsGasPrice, final Map<String, BigInteger> functionsGasLimit) {
        return new ContractGasProvider() {
            @Override
            public BigInteger getGasPrice(String contractFunc) {
                if (functionsGasPrice != null && functionsGasPrice.containsKey(contractFunc))
                    return functionsGasPrice.get(contractFunc);
                return gasPrice;
            }

            @Override
            public BigInteger getGasPrice() {
                return gasPrice;
            }

            @Override
            public BigInteger getGasLimit(String contractFunc) {
                if (functionsGasLimit != null && functionsGasLimit.containsKey(contractFunc))
                    return functionsGasLimit.get(contractFunc);
                return gasLimit;
            }

            @Override
            public BigInteger getGasLimit() {
                return gasLimit;
            }
        };
    }

    @Override
    public void setCredentials(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty())
            throw new IllegalArgumentException("username or password is null");
        try {
            this.credentials = WalletUtils.loadCredentials(username, password);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void setCredentials(String privateKey) {
        if (privateKey == null || privateKey.isEmpty())
            throw new IllegalArgumentException("privateKey is null");
        try {
            this.credentials = Credentials.create(privateKey);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public EthTransactionReceipt transferEther(String destination, BigDecimal amount) throws Exception {
        if (this.credentials == null)
            throw new IllegalStateException("credentials is null");
        TransactionReceipt receipt = Transfer.sendFunds(this.web3j, this.credentials, destination, amount, Convert.Unit.ETHER).send();
        return wrapEthereumTransactionReceipt(receipt);
    }

    @Override
    public List<String> listAccounts() {
        try {
            return this.web3j.ethAccounts().send().getAccounts();
        } catch (Exception e) {
            throw new WaterRuntimeException(e.getMessage());
        }
    }

    @Override
    public BigInteger getBalanceOf(String address) {
        try {
            EthGetBalance balance = this.web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            return balance.getBalance();
        } catch (IOException e) {
            throw new WaterRuntimeException(e.getMessage());
        }
    }

    private EthTransactionReceipt wrapEthereumTransactionReceipt(TransactionReceipt receipt) {
        return new EthTransactionReceipt() {
            @Override
            public String getTransactionHash() {
                return receipt.getTransactionHash();
            }

            @Override
            public BigInteger getTransactionIndex() {
                return receipt.getTransactionIndex();
            }

            @Override
            public String getBlockHash() {
                return receipt.getBlockHash();
            }

            @Override
            public BigInteger getBlockNumber() {
                return receipt.getBlockNumber();
            }

            @Override
            public BigInteger getCumulativeGasUsed() {
                return receipt.getCumulativeGasUsed();
            }

            @Override
            public BigInteger getGasUsed() {
                return receipt.getGasUsed();
            }

            @Override
            public String getContractAddress() {
                return receipt.getContractAddress();
            }

            @Override
            public String getRoot() {
                return receipt.getRoot();
            }

            @Override
            public String getStatus() {
                return receipt.getStatus();
            }

            @Override
            public String getFrom() {
                return receipt.getFrom();
            }

            @Override
            public String getTo() {
                return receipt.getTo();
            }

            @Override
            public String getRevertReason() {
                return receipt.getRevertReason();
            }

            @Override
            public String getType() {
                return receipt.getType();
            }

            @Override
            public String getEffectiveGasPrice() {
                return receipt.getEffectiveGasPrice();
            }
        };
    }
}
