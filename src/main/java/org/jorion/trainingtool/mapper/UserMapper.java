package org.jorion.trainingtool.mapper;

import org.jorion.trainingtool.dto.GroupInfraIdentityDTO;
import org.jorion.trainingtool.dto.json.UserDTO;
import org.jorion.trainingtool.entity.User;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Mapper for User objects.
 * <p>
 * Collection Mapping Strategy is set to IMMUTABLE because user's collections (roles, registrations) are unmodifiable,
 * and we don't want the implementation to clear them.
 */
@Mapper(uses = RegistrationMapper.class, collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "createdOn", dateFormat = "yyyy-MM-dd HH:mm")
    @Mapping(target = "modifiedOn", dateFormat = "yyyy-MM-dd HH:mm")
    UserDTO toUserDTO(User user);

    User toUser(UserDTO dto);

    @Mapping(target = "userName", source = "account")
    @Mapping(target = "phoneNumber", source = "mobileOrPhone")
    User toUser(GroupInfraIdentityDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pnr", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "sector", ignore = true)
    @Mapping(target = "registrations", ignore = true)
    void updateUserFromLdap(User source, @MappingTarget User target);

    List<UserDTO> toUserDTO(List<User> users);

}
