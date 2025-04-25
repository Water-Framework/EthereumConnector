package it.water.connectors.ethereum.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EthConstants {
    public static final String ETH_CONNECTOR_CLIENT_FACTORY = "clientFactoryType";
    public static final String ETH_CONNECTOR_CLIENT_FACTORY_WEB3J = "Web3J";
}
