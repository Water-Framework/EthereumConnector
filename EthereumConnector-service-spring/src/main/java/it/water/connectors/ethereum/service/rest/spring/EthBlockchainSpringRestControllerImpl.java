
package it.water.connectors.ethereum.service.rest.spring;

import it.water.connectors.ethereum.model.EthBlockchain;
import it.water.connectors.ethereum.service.rest.EthBlockchainRestControllerImpl;
import it.water.core.api.model.PaginableResult;
import it.water.core.api.repository.query.Query;
import it.water.core.api.repository.query.QueryOrder;
import org.springframework.web.bind.annotation.RestController;


/**
 * @Generated by Water Generator
 * Rest Api Class for EthereumConnector entity. It just overrides method invoking super in order to let spring find web methods.
 */
@RestController
public class EthBlockchainSpringRestControllerImpl extends EthBlockchainRestControllerImpl implements EthBlockchainSpringRestApi {

    @Override
    @SuppressWarnings("java:S1185") //disabling sonar because spring needs to override this method
    public EthBlockchain save(EthBlockchain entity) {
        return super.save(entity);
    }

    @Override
    @SuppressWarnings("java:S1185") //disabling sonar because spring needs to override this method
    public EthBlockchain update(EthBlockchain entity) {
        return super.update(entity);
    }

    @Override
    @SuppressWarnings("java:S1185") //disabling sonar because spring needs to override this method
    public void remove(long id) {
        super.remove(id);
    }

    @Override
    @SuppressWarnings("java:S1185") //disabling sonar because spring needs to override this method
    public EthBlockchain find(long id) {
        return super.find(id);
    }

    @Override
    @SuppressWarnings("java:S1185") //disabling sonar because spring needs to override this method
    public PaginableResult<EthBlockchain> findAll(Integer delta, Integer page, Query filter, QueryOrder order) {
        return super.findAll(delta, page, filter, order);
    }

    @Override
    @SuppressWarnings("java:S1185") //disabling sonar because spring needs to override this method
    public PaginableResult<EthBlockchain> findAll() {
        return super.findAll();
    }
}
