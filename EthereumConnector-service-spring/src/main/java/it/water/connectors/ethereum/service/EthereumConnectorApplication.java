package it.water.connectors.ethereum.service;


import it.water.implementation.spring.annotations.EnableWaterFramework;
import it.water.repository.jpa.spring.RepositoryFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableWaterFramework
@EnableJpaRepositories(basePackages = {"it.water", "it.water.connectors.ethereum.*"}, repositoryFactoryBeanClass = RepositoryFactory.class)
@EntityScan({"it.water","it.water.connectors.ethereum.*"})
@ComponentScan({"it.water", "it.water.connectors.ethereum.*"})
public class EthereumConnectorApplication {
    public static void main(String[] args) {
        SpringApplication.run(EthereumConnectorApplication.class, args);
    }

}
