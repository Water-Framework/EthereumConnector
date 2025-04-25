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
import it.water.connectors.ethereum.api.EthClientFactory;
import it.water.connectors.ethereum.model.EthBlockchain;
import it.water.connectors.ethereum.model.EthConstants;
import it.water.core.interceptors.annotations.FrameworkComponent;
import lombok.Getter;
import lombok.Setter;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

/**
 * @Author Aristide Cittadino
 * Ethereum Web3J Client Factory.
 */
@FrameworkComponent(properties = EthConstants.ETH_CONNECTOR_CLIENT_FACTORY+"="+EthConstants.ETH_CONNECTOR_CLIENT_FACTORY_WEB3J)
public class EthWeb3JClientFactory implements EthClientFactory {
    private EthBlockchain ethereumBlockChain;

    @Getter
    @Setter
    private final String clientFactoryType = EthConstants.ETH_CONNECTOR_CLIENT_FACTORY_WEB3J;

    @Override
    public EthClientFactory withEthereumBlockChain(EthBlockchain ethereumBlockChain) {
        if (ethereumBlockChain == null)
            throw new IllegalArgumentException("EthereumBlockChain cannot be null");
        this.ethereumBlockChain = ethereumBlockChain;
        return this;
    }

    @Override
    public EthClient build() {
        Web3j web3j = Web3j.build(new HttpService(ethereumBlockChain.getProtocol() + "://" + ethereumBlockChain.getHost() + ":" + ethereumBlockChain.getPort()));
        EthClient web3jClient = new EthWeb3JClient(web3j);
        this.reset();
        return web3jClient;
    }

    public void reset() {
        this.ethereumBlockChain = null;
    }

}
