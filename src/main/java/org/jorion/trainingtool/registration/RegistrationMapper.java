package org.jorion.trainingtool.registration;

import org.jorion.trainingtool.training.Training;
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
