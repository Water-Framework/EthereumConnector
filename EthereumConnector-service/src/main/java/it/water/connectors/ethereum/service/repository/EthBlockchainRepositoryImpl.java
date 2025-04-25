package it.water.connectors.ethereum.service.repository;

import it.water.connectors.ethereum.api.BlockchainRepository;
import it.water.connectors.ethereum.model.EthBlockchain;
import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.repository.jpa.WaterJpaRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FrameworkComponent
public class EthBlockchainRepositoryImpl extends WaterJpaRepositoryImpl<EthBlockchain> implements BlockchainRepository {

    private static final String BLOCKCHIAN_CONTRACT_PERSISTENCE_UNIT = "ethereum-persistence-unit";
    @SuppressWarnings("java:S1068")
    private static Logger logger = LoggerFactory.getLogger(EthBlockchainRepositoryImpl.class);

    public EthBlockchainRepositoryImpl() {
        super(EthBlockchain.class, BLOCKCHIAN_CONTRACT_PERSISTENCE_UNIT);
    }

}
