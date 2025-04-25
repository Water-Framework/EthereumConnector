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

package it.water.connectors.ethereum.api;

import java.math.BigInteger;

/**
 * @Author Aristide Cittadino
 */
public interface EthTransactionReceipt {
    /**
     * @return
     */
    String getTransactionHash();

    /**
     * @return
     */
    BigInteger getTransactionIndex();

    /**
     * @return
     */
    String getBlockHash();

    /**
     * @return
     */
    BigInteger getBlockNumber();

    /**
     * @return
     */
    BigInteger getCumulativeGasUsed();

    /**
     * @return
     */
    BigInteger getGasUsed();

    /**
     * @return
     */
    String getContractAddress();

    /**
     * @return
     */
    String getRoot();

    /**
     * @return
     */
    String getStatus();

    /**
     * @return
     */
    String getFrom();

    /**
     * @return
     */
    String getTo();

    /**
     * @return
     */
    String getRevertReason();

    /**
     * @return
     */
    String getType();

    /**
     * @return
     */
    String getEffectiveGasPrice();
}
