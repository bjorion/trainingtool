package org.jorion.trainingtool.mapper;

import org.jorion.trainingtool.dto.json.TrainingDTO;
import org.jorion.trainingtool.entity.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TrainingMapper {

    TrainingMapper INSTANCE = Mappers.getMapper(TrainingMapper.class);

    TrainingDTO toTrainingDTO(Training training);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    void updateTraining(Training source, @MappingTarget Training target);
}
