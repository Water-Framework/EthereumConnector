package it.water.connectors.ethereum.service.repository;

import it.water.connectors.ethereum.api.SmartContractRepository;
import it.water.connectors.ethereum.model.EthSmartContract;
import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.repository.jpa.WaterJpaRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FrameworkComponent
public class EthSmartContractRepositoryImpl extends WaterJpaRepositoryImpl<EthSmartContract> implements SmartContractRepository {

    private static final String SMART_CONTRACT_PERSISTENCE_UNIT = "ethereum-persistence-unit";
    @SuppressWarnings("java:S1068")
    private static Logger logger = LoggerFactory.getLogger(EthSmartContractRepositoryImpl.class);

    public EthSmartContractRepositoryImpl() {
        super(EthSmartContract.class, SMART_CONTRACT_PERSISTENCE_UNIT);
    }

}
