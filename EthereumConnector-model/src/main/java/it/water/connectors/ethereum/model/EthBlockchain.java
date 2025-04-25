package it.water.connectors.ethereum.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.water.core.api.permission.ProtectedEntity;
import it.water.core.permission.action.CrudActions;
import it.water.core.permission.annotations.AccessControl;
import it.water.core.permission.annotations.DefaultRoleAccess;
import it.water.core.validation.annotations.NoMalitiusCode;
import it.water.core.validation.annotations.NotNullOnPersist;
import it.water.repository.jpa.model.AbstractJpaEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"host","port"})})
@Access(AccessType.FIELD)
//Lombok
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Getter
@Setter(AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(of = {"id", "host","port"})
@AccessControl(availableActions = {CrudActions.SAVE, CrudActions.UPDATE, CrudActions.FIND, CrudActions.FIND_ALL, CrudActions.REMOVE}, rolesPermissions = {
        //Admin role can do everything
        @DefaultRoleAccess(roleName = EthBlockchain.DEFAULT_MANAGER_ROLE, actions = {CrudActions.SAVE, CrudActions.UPDATE, CrudActions.FIND, CrudActions.FIND_ALL, CrudActions.REMOVE}),
        //Viwer has read only access
        @DefaultRoleAccess(roleName = EthBlockchain.DEFAULT_VIEWER_ROLE, actions = {CrudActions.FIND, CrudActions.FIND_ALL}),
        //Editor can do anything but remove
        @DefaultRoleAccess(roleName = EthBlockchain.DEFAULT_EDITOR_ROLE, actions = {CrudActions.SAVE, CrudActions.UPDATE, CrudActions.FIND, CrudActions.FIND_ALL})})
public class EthBlockchain extends AbstractJpaEntity implements ProtectedEntity {

    public static final String DEFAULT_MANAGER_ROLE = "ethBlockChainManager";
    public static final String DEFAULT_VIEWER_ROLE = "ethBlockChainViewer";
    public static final String DEFAULT_EDITOR_ROLE = "ethBlockChainEditor";

    @NotEmpty
    @NotNullOnPersist
    @NoMalitiusCode
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    @NonNull
    private String protocol;

    @NonNull
    @Column(columnDefinition = "VARCHAR(1000) NOT NULL")
    private String host;

    @NotEmpty
    @NotNullOnPersist
    @NoMalitiusCode
    @NonNull
    @Column(columnDefinition = "VARCHAR(20) NOT NULL")
    private String port;

    @OneToMany(mappedBy = "blockchain", cascade = {CascadeType.PERSIST, CascadeType.REMOVE,CascadeType.MERGE})
    @JsonIgnore
    private Set<EthSmartContract> contracts = new HashSet<>();
}
