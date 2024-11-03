package org.jorion.trainingtool.mapper;

import org.jorion.trainingtool.dto.json.RegistrationDTO;
import org.jorion.trainingtool.entity.Registration;
import org.jorion.trainingtool.entity.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE)
public interface RegistrationMapper {

    RegistrationMapper INSTANCE = Mappers.getMapper(RegistrationMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainingId", source = "id")
    Registration toRegistration(Training rt);

    RegistrationDTO toRegistrationDTO(Registration reg);
}
