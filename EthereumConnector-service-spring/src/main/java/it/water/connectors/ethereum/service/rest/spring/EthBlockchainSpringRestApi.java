/*
 * Copyright 2024 Aristide Cittadino
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
 */

package it.water.connectors.ethereum.service.rest.spring;

import com.fasterxml.jackson.annotation.JsonView;
import it.water.connectors.ethereum.api.rest.BlockchainRestApi;
import it.water.connectors.ethereum.model.EthBlockchain;
import it.water.core.api.model.PaginableResult;
import it.water.core.api.service.rest.FrameworkRestApi;
import it.water.core.api.service.rest.WaterJsonView;
import it.water.service.rest.api.security.LoggedIn;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @Author Aristide Cittadino
 * Interface exposing same methods of its parent EthereumConnectorRestApi but adding Spring annotations.
 * Swagger annotation should be found because they have been defined in the parent EthereumConnectorRestApi.
 */
@RequestMapping("/ethereum/blockchains")
@FrameworkRestApi
public interface EthBlockchainSpringRestApi extends BlockchainRestApi {
    @LoggedIn
    @PostMapping
    @JsonView(WaterJsonView.Public.class)
    EthBlockchain save(@RequestBody EthBlockchain ethereumconnector);

    @LoggedIn
    @PutMapping
    @JsonView(WaterJsonView.Public.class)
    EthBlockchain update(@RequestBody EthBlockchain ethereumconnector);

    @LoggedIn
    @GetMapping("/{id}")
    @JsonView(WaterJsonView.Public.class)
    EthBlockchain find(@PathVariable("id") long id);

    @LoggedIn
    @GetMapping
    @JsonView(WaterJsonView.Public.class)
    PaginableResult<EthBlockchain> findAll();

    @LoggedIn
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @JsonView(WaterJsonView.Public.class)
    void remove(@PathVariable("id") long id);
}
