package de.hs_rm.de.milefiz.game.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import de.hs_rm.de.milefiz.game.model.Meeple;
import de.hs_rm.de.milefiz.game.model.dto.MeepleDTO;

@Mapper(componentModel = "spring")
public interface MeepleMapper {

    MeepleMapper INSTANCE = Mappers.getMapper(MeepleMapper.class);

    @Mapping(target = "currentFieldId", source = "currentField.id")
    @Mapping(target = "lastFieldId", source = "lastField.id")
    @Mapping(target = "barrier", source = "barrier")
    MeepleDTO toDTO(Meeple meeple);
}
