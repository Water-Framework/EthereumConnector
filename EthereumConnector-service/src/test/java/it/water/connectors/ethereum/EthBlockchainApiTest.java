package it.water.connectors.ethereum;

import it.water.connectors.ethereum.api.BlockchainApi;
import it.water.connectors.ethereum.api.BlockchainRepository;
import it.water.connectors.ethereum.api.BlockchainSystemApi;
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
class EthBlockchainApiTest implements Service {

    @Inject
    @Setter
    private ComponentRegistry componentRegistry;

    @Inject
    @Setter
    private BlockchainApi blockchainApi;

    @Inject
    @Setter
    private Runtime runtime;

    @Inject
    @Setter
    private BlockchainRepository blockChianRepository;

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
    private it.water.core.api.model.User adminUser;
    private it.water.core.api.model.User blockchainManagerUser;
    private it.water.core.api.model.User blockchainViewerUser;
    private it.water.core.api.model.User blockchainEditorUser;

    private Role blockchainManagerRole;
    private Role blockchainViewerRole;
    private Role blockchainEditorRole;


    @BeforeAll
    void beforeAll() {
        //getting user
        blockchainManagerRole = roleManager.getRole(EthBlockchain.DEFAULT_MANAGER_ROLE);
        blockchainViewerRole = roleManager.getRole(EthBlockchain.DEFAULT_VIEWER_ROLE);
        blockchainEditorRole = roleManager.getRole(EthBlockchain.DEFAULT_EDITOR_ROLE);
        Assertions.assertNotNull(blockchainManagerRole);
        Assertions.assertNotNull(blockchainViewerRole);
        Assertions.assertNotNull(blockchainEditorRole);
        //impersonate admin so we can test the happy path
        adminUser = userManager.findUser("admin");
        blockchainManagerUser = userManager.addUser("blockChainManager", "blockChainManager", "blockChainManager", "bcManager@a.com", "TempPassword1_", "salt", false);
        blockchainViewerUser = userManager.addUser("blockChainViewer", "blockChainViewer", "blockChainViewer", "bcViewer@a.com", "TempPassword1_", "salt", false);
        blockchainEditorUser = userManager.addUser("blockChainEditor", "blockChainEditor", "blockChainEditor", "bcEditor@a.com", "TempPassword1_", "salt", false);
        //starting with admin permissions
        roleManager.addRole(blockchainManagerUser.getId(), blockchainManagerRole);
        roleManager.addRole(blockchainViewerUser.getId(), blockchainViewerRole);
        roleManager.addRole(blockchainEditorUser.getId(), blockchainEditorRole);
        //default security context is admin
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
    }

    /**
     * Testing basic injection of basic component for ethereumconnector entity.
     */
    @Test
    @Order(1)
    void componentsInsantiatedCorrectly() {
        this.blockchainApi = this.componentRegistry.findComponent(BlockchainApi.class, null);
        Assertions.assertNotNull(this.blockchainApi);
        Assertions.assertNotNull(this.componentRegistry.findComponent(BlockchainSystemApi.class, null));
        this.blockChianRepository = this.componentRegistry.findComponent(BlockchainRepository.class, null);
        Assertions.assertNotNull(this.blockChianRepository);
    }

    /**
     * Testing simple save and version increment
     */
    @Test
    @Order(2)
    void saveOk() {
        EthBlockchain entity = createBlockchian(0);
        entity = this.blockchainApi.save(entity);
        Assertions.assertEquals(1, entity.getEntityVersion());
        Assertions.assertTrue(entity.getId() > 0);
        Assertions.assertEquals("host0", entity.getHost());
    }

    /**
     * Testing update logic, basic test
     */
    @Test
    @Order(3)
    void updateShouldWork() {
        Query q = this.blockChianRepository.getQueryBuilderInstance().createQueryFilter("host=host0");
        EthBlockchain entity = this.blockchainApi.find(q);
        Assertions.assertNotNull(entity);
        EthBlockchain updateEntity = new EthBlockchain(entity.getProtocol(), "hostUpdated", entity.getPort());
        updateEntity.setId(entity.getId());
        entity = this.blockchainApi.update(updateEntity);
        Assertions.assertEquals("hostUpdated", entity.getHost());
        Assertions.assertEquals(2, entity.getEntityVersion());
    }

    /**
     * Testing update logic, basic test
     */
    @Test
    @Order(4)
    void updateShouldFailWithWrongVersion() {
        Query q = this.blockChianRepository.getQueryBuilderInstance().createQueryFilter("host=hostUpdated");
        EthBlockchain errorEntity = this.blockchainApi.find(q);
        Assertions.assertEquals("hostUpdated", errorEntity.getHost());
        Assertions.assertEquals(2, errorEntity.getEntityVersion());
        errorEntity.setEntityVersion(1);
        Assertions.assertThrows(WaterRuntimeException.class, () -> this.blockchainApi.update(errorEntity));
    }

    /**
     * Testing finding all entries with no pagination
     */
    @Test
    @Order(5)
    void findAllShouldWork() {
        PaginableResult<EthBlockchain> all = this.blockchainApi.findAll(null, -1, -1, null);
        Assertions.assertFalse(all.getResults().isEmpty());
    }

    /**
     * Testing finding all entries with settings related to pagination.
     * Searching with 5 items per page starting from page 1.
     */
    @Test
    @Order(6)
    void findAllPaginatedShouldWork() {
        for (int i = 2; i < 11; i++) {
            EthBlockchain u = createBlockchian(i);
            this.blockchainApi.save(u);
        }
        PaginableResult<EthBlockchain> paginated = this.blockchainApi.findAll(null, 7, 1, null);
        Assertions.assertEquals(7, paginated.getResults().size());
        Assertions.assertEquals(1, paginated.getCurrentPage());
        Assertions.assertEquals(2, paginated.getNextPage());
        paginated = this.blockchainApi.findAll(null, 7, 2, null);
        Assertions.assertFalse(paginated.getResults().isEmpty());
        Assertions.assertEquals(2, paginated.getCurrentPage());
        Assertions.assertEquals(1, paginated.getNextPage());
    }

    /**
     * Testing removing all entities using findAll method.
     */
    @Test
    @Order(7)
    void removeAllShouldWork() {
        PaginableResult<EthBlockchain> paginated = this.blockchainApi.findAll(null, -1, -1, null);
        paginated.getResults().forEach(entity -> {
            this.blockchainApi.remove(entity.getId());
        });
        Assertions.assertEquals(0, this.blockchainApi.countAll(null));
    }

    /**
     * Testing failure on duplicated entity
     */
    @Test
    @Order(8)
    void saveShouldFailOnDuplicatedEntity() {
        EthBlockchain entity = createBlockchian(1);
        this.blockchainApi.save(entity);
        EthBlockchain duplicated = this.createBlockchian(1);
        //cannot insert new entity wich breaks unique constraint
        Assertions.assertThrows(DuplicateEntityException.class, () -> this.blockchainApi.save(duplicated));
        EthBlockchain secondEntity = createBlockchian(2);
        this.blockchainApi.save(secondEntity);
        EthBlockchain updateEntity = new EthBlockchain(entity.getProtocol(), "host2", "2");
        updateEntity.setId(entity.getId());
        //cannot update an entity colliding with other entity on unique constraint
        Assertions.assertThrows(DuplicateEntityException.class, () -> this.blockchainApi.update(updateEntity));
    }

    /**
     * Testing failure on validation failure for example code injection
     */
    @Test
    @Order(9)
    void updateShouldFailOnValidationFailure() {
        EthBlockchain newEntity = new EthBlockchain("<script>function(){alert('ciao')!}</script>", "host", "port");
        Assertions.assertThrows(ValidationException.class, () -> this.blockchainApi.save(newEntity));
    }

    /**
     * Testing Crud operations on manager role
     */
    @Order(10)
    @Test
    void managerCanDoEverything() {
        TestRuntimeInitializer.getInstance().impersonate(blockchainManagerUser, runtime);
        final EthBlockchain entity = createBlockchian(101);
        EthBlockchain savedEntity = Assertions.assertDoesNotThrow(() -> this.blockchainApi.save(entity));
        EthBlockchain updateEntity = new EthBlockchain(savedEntity.getProtocol(), "newHostUpdated", savedEntity.getPort());
        updateEntity.setId(savedEntity.getId());
        Assertions.assertDoesNotThrow(() -> this.blockchainApi.update(updateEntity));
        Assertions.assertDoesNotThrow(() -> this.blockchainApi.find(updateEntity.getId()));
        Assertions.assertDoesNotThrow(() -> this.blockchainApi.remove(updateEntity.getId()));
    }

    @Order(11)
    @Test
    void viewerCannotSaveOrUpdateOrRemove() {
        TestRuntimeInitializer.getInstance().impersonate(blockchainViewerUser, runtime);
        final EthBlockchain entity = createBlockchian(201);
        Assertions.assertThrows(UnauthorizedException.class, () -> this.blockchainApi.save(entity));
        Assertions.assertEquals(2, this.blockchainApi.findAll(null, -1, -1, null).getResults().size());
    }

    @Order(12)
    @Test
    void editorCannotRemove() {
        TestRuntimeInitializer.getInstance().impersonate(blockchainEditorUser, runtime);
        final EthBlockchain entity = createBlockchian(301);
        EthBlockchain savedEntity = Assertions.assertDoesNotThrow(() -> this.blockchainApi.save(entity));
        EthBlockchain updateEntity = new EthBlockchain(savedEntity.getProtocol(), "editorNewSavedEntity", savedEntity.getPort());
        updateEntity.setId(savedEntity.getId());
        Assertions.assertDoesNotThrow(() -> this.blockchainApi.update(updateEntity));
        Assertions.assertDoesNotThrow(() -> this.blockchainApi.find(updateEntity.getId()));
        long savedEntityId = savedEntity.getId();
        Assertions.assertThrows(UnauthorizedException.class, () -> this.blockchainApi.remove(savedEntityId));
    }

    private EthBlockchain createBlockchian(int seed) {
        EthBlockchain entity = new EthBlockchain("exampleField" + seed, "host" + seed, String.valueOf(seed));
        return entity;
    }
}
