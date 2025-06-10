package it.water.connectors.ethereum;

import it.water.connectors.ethereum.api.BlockchainApi;
import it.water.connectors.ethereum.api.SmartContractApi;
import it.water.connectors.ethereum.api.SmartContractRepository;
import it.water.connectors.ethereum.api.SmartContractSystemApi;
import it.water.connectors.ethereum.model.EthBlockchain;
import it.water.connectors.ethereum.model.EthSmartContract;
import it.water.core.api.bundle.Runtime;
import it.water.core.api.model.PaginableResult;
import it.water.core.api.model.Role;
import it.water.core.api.permission.PermissionManager;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.repository.query.Query;
import it.water.core.api.role.RoleManager;
import it.water.core.api.service.Service;
import it.water.core.api.user.UserManager;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.model.exceptions.ValidationException;
import it.water.core.model.exceptions.WaterRuntimeException;
import it.water.core.permission.exceptions.UnauthorizedException;
import it.water.core.testing.utils.bundle.TestRuntimeInitializer;
import it.water.core.testing.utils.junit.WaterTestExtension;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import it.water.repository.entity.model.exceptions.DuplicateEntityException;
import it.water.repository.entity.model.exceptions.NoResultException;
import lombok.Setter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Generated with Water Generator.
 * Test class for EthereumConnector Services.
 * <p>
 * Please use EthereumConnectorRestTestApi for ensuring format of the json response
 */
@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EthSmartContractApiTest implements Service {

    @Inject
    @Setter
    private ComponentRegistry componentRegistry;

    @Inject
    @Setter
    private SmartContractApi smartContractApi;

    @Inject
    @Setter
    private BlockchainApi blockchainApi;

    @Inject
    @Setter
    private Runtime runtime;

    @Inject
    @Setter
    private SmartContractRepository ethereumConnectorRepository;

    @Inject
    @Setter
    //default permission manager in test environment;
    private PermissionManager permissionManager;

    @Inject
    @Setter
    //test role manager
    private UserManager userManager;

    @Inject
    @Setter
    //test role manager
    private RoleManager roleManager;

    //admin user
    @SuppressWarnings("unused")
    private it.water.core.api.model.User adminUser;
    private it.water.core.api.model.User smartContractManagerUser;
    private it.water.core.api.model.User smartContractViewerUser;
    private it.water.core.api.model.User smartContractEditorUser;

    private Role smartContractManagerRole;
    private Role smartContractViewerRole;
    private Role smartContractEditorRole;

    private EthBlockchain blockchain;

    @BeforeAll
    void beforeAll() {
        //getting user
        smartContractManagerRole = roleManager.getRole(EthSmartContract.DEFAULT_MANAGER_ROLE);
        smartContractViewerRole = roleManager.getRole(EthSmartContract.DEFAULT_VIEWER_ROLE);
        smartContractEditorRole = roleManager.getRole(EthSmartContract.DEFAULT_EDITOR_ROLE);
        Assertions.assertNotNull(smartContractManagerRole);
        Assertions.assertNotNull(smartContractViewerRole);
        Assertions.assertNotNull(smartContractEditorRole);
        //impersonate admin so we can test the happy path
        adminUser = userManager.findUser("admin");
        smartContractManagerUser = userManager.addUser("smartContracManager", "smartContracManager", "smartContracManager", "snManager@a.com", "TempPassword1_", "salt", false);
        smartContractViewerUser = userManager.addUser("smartContractViewer", "smartContractViewer", "smartContractViewer", "smViewer@a.com", "TempPassword1_", "salt", false);
        smartContractEditorUser = userManager.addUser("smartContractEditor", "smartContractEditor", "smartContractEditor", "smEditor@a.com", "TempPassword1_", "salt", false);
        //starting with admin permissions
        roleManager.addRole(smartContractManagerUser.getId(), smartContractManagerRole);
        roleManager.addRole(smartContractViewerUser.getId(), smartContractViewerRole);
        roleManager.addRole(smartContractEditorUser.getId(), smartContractEditorRole);
        //default security context is admin
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        blockchain = new EthBlockchain("http", "localhost", "8585");
        blockchainApi.save(blockchain);
    }

    /**
     * Testing basic injection of basic component for ethereumconnector entity.
     */
    @Test
    @Order(1)
    void componentsInsantiatedCorrectly() {
        this.smartContractApi = this.componentRegistry.findComponent(SmartContractApi.class, null);
        Assertions.assertNotNull(this.smartContractApi);
        Assertions.assertNotNull(this.componentRegistry.findComponent(SmartContractSystemApi.class, null));
        this.ethereumConnectorRepository = this.componentRegistry.findComponent(SmartContractRepository.class, null);
        Assertions.assertNotNull(this.ethereumConnectorRepository);
    }

    /**
     * Testing simple save and version increment
     */
    @Test
    @Order(2)
    void saveOk() {
        EthSmartContract entity = createSmartContract(0);
        entity = this.smartContractApi.save(entity);
        Assertions.assertEquals(1, entity.getEntityVersion());
        Assertions.assertTrue(entity.getId() > 0);
        Assertions.assertEquals("localhost0", entity.getAddress());
    }

    /**
     * Testing update logic, basic test
     */
    @Test
    @Order(3)
    void updateShouldWork() {
        Query q = this.ethereumConnectorRepository.getQueryBuilderInstance().createQueryFilter("address=localhost0");
        EthSmartContract entity = this.smartContractApi.find(q);
        Assertions.assertNotNull(entity);
        entity.setAddress("localhost0Updated");
        entity = this.smartContractApi.update(entity);
        Assertions.assertEquals("localhost0Updated", entity.getAddress());
        Assertions.assertEquals(2, entity.getEntityVersion());
    }

    /**
     * Testing update logic, basic test
     */
    @Test
    @Order(4)
    void updateShouldFailWithWrongVersion() {
        Query q = this.ethereumConnectorRepository.getQueryBuilderInstance().createQueryFilter("address=localhost0Updated");
        EthSmartContract errorEntity = this.smartContractApi.find(q);
        Assertions.assertEquals("localhost0Updated", errorEntity.getAddress());
        Assertions.assertEquals(2, errorEntity.getEntityVersion());
        errorEntity.setEntityVersion(1);
        Assertions.assertThrows(WaterRuntimeException.class, () -> this.smartContractApi.update(errorEntity));
    }

    /**
     * Testing finding all entries with no pagination
     */
    @Test
    @Order(5)
    void findAllShouldWork() {
        PaginableResult<EthSmartContract> all = this.smartContractApi.findAll(null, -1, -1, null);
        Assertions.assertEquals(1, all.getResults().size());
    }

    /**
     * Testing finding all entries with settings related to pagination.
     * Searching with 5 items per page starting from page 1.
     */
    @Test
    @Order(6)
    void findAllPaginatedShouldWork() {
        for (int i = 2; i < 11; i++) {
            EthSmartContract u = createSmartContract(i);
            this.smartContractApi.save(u);
        }
        PaginableResult<EthSmartContract> paginated = this.smartContractApi.findAll(null, 7, 1, null);
        Assertions.assertEquals(7, paginated.getResults().size());
        Assertions.assertEquals(1, paginated.getCurrentPage());
        Assertions.assertEquals(2, paginated.getNextPage());
        paginated = this.smartContractApi.findAll(null, 7, 2, null);
        Assertions.assertEquals(3, paginated.getResults().size());
        Assertions.assertEquals(2, paginated.getCurrentPage());
        Assertions.assertEquals(1, paginated.getNextPage());
    }

    /**
     * Testing removing all entities using findAll method.
     */
    @Test
    @Order(7)
    void removeAllShouldWork() {
        PaginableResult<EthSmartContract> paginated = this.smartContractApi.findAll(null, -1, -1, null);
        paginated.getResults().forEach(entity -> {
            this.smartContractApi.remove(entity.getId());
        });
        Assertions.assertEquals(0, this.smartContractApi.countAll(null));
    }

    /**
     * Testing failure on duplicated entity
     */
    @Test
    @Order(8)
    void saveShouldFailOnDuplicatedEntity() {
        EthSmartContract entity = createSmartContract(1);
        this.smartContractApi.save(entity);
        EthSmartContract duplicated = this.createSmartContract(1);
        //cannot insert new entity wich breaks unique constraint
        Assertions.assertThrows(DuplicateEntityException.class, () -> this.smartContractApi.save(duplicated));
        EthSmartContract secondEntity = createSmartContract(2);
        this.smartContractApi.save(secondEntity);
        entity.setAddress("localhost2");
        entity.setContractClass("exampleField2");
        //cannot update an entity colliding with other entity on unique constraint
        Assertions.assertThrows(DuplicateEntityException.class, () -> this.smartContractApi.update(entity));
        //second unique key is by name and blockchain id
        entity.setName("name2");
        entity.setAddress("localhostNew");
        entity.setContractClass("exampleFieldNew");
        Assertions.assertThrows(DuplicateEntityException.class, () -> this.smartContractApi.update(entity));
    }

    /**
     * Testing failure on validation failure for example code injection
     */
    @Test
    @Order(9)
    void updateShouldFailOnValidationFailure() {
        EthSmartContract newEntity = new EthSmartContract("<script>function(){alert('ciao')!}</script>", "name", "localhost", "transactionReceopt",blockchain, 0L);
        Assertions.assertThrows(ValidationException.class, () -> this.smartContractApi.save(newEntity));
    }

    /**
     * Testing Crud operations on manager role
     */
    @Order(10)
    @Test
    void managerCanDoEverything() {
        TestRuntimeInitializer.getInstance().impersonate(smartContractManagerUser, runtime);
        final EthSmartContract entity = createSmartContract(101);
        EthSmartContract savedEntity = Assertions.assertDoesNotThrow(() -> this.smartContractApi.save(entity));
        savedEntity.setAddress("newSavedEntity");
        Assertions.assertDoesNotThrow(() -> this.smartContractApi.update(entity));
        Assertions.assertDoesNotThrow(() -> this.smartContractApi.find(savedEntity.getId()));
        Assertions.assertDoesNotThrow(() -> this.smartContractApi.remove(savedEntity.getId()));

    }

    @Order(11)
    @Test
    void viewerCannotSaveOrUpdateOrRemove() {
        TestRuntimeInitializer.getInstance().impersonate(smartContractViewerUser, runtime);
        final EthSmartContract entity = createSmartContract(201);
        Assertions.assertThrows(UnauthorizedException.class, () -> this.smartContractApi.save(entity));
        //viewer can search
        Assertions.assertEquals(0,this.smartContractApi.findAll(null, -1, -1, null).getResults().size());

    }

    @Order(12)
    @Test
    void editorCannotRemove() {
        TestRuntimeInitializer.getInstance().impersonate(smartContractEditorUser, runtime);
        final EthSmartContract entity = createSmartContract(301);
        EthSmartContract savedEntity = Assertions.assertDoesNotThrow(() -> this.smartContractApi.save(entity));
        savedEntity.setAddress("editorNewSavedEntity");
        Assertions.assertDoesNotThrow(() -> this.smartContractApi.update(entity));
        Assertions.assertDoesNotThrow(() -> this.smartContractApi.find(savedEntity.getId()));
        long savedEntityId = savedEntity.getId();
        Assertions.assertThrows(UnauthorizedException.class, () -> this.smartContractApi.remove(savedEntityId));
    }

    @Order(13)
    @Test
    void ownedResourceShouldBeAccessedOnlyByOwner() {
        TestRuntimeInitializer.getInstance().impersonate(smartContractEditorUser, runtime);
        final EthSmartContract entity = createSmartContract(401);
        //saving as editor
        EthSmartContract savedEntity = Assertions.assertDoesNotThrow(() -> this.smartContractApi.save(entity));
        Assertions.assertDoesNotThrow(() -> this.smartContractApi.find(savedEntity.getId()));
        TestRuntimeInitializer.getInstance().impersonate(smartContractManagerUser, runtime);
        //find an owned entity with different user from the creator should raise an unauthorized exception
        long savedEntityId = savedEntity.getId();
        Assertions.assertThrows(NoResultException.class, () -> this.smartContractApi.find(savedEntityId));
    }

    private EthSmartContract createSmartContract(int seed) {
        EthSmartContract entity = new EthSmartContract("exampleField" + seed, "name"+seed, "localhost"+seed, "transactionReceopt"+seed,blockchain, 0L);
        return entity;
    }
}
