package de.hs_rm.de.milefiz.game.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.model.dto.PlayerDTO;

/**
 * MapStruct Mapper für die Konvertierung zwischen Player und PlayerDTO.
 */
@Mapper(componentModel = "spring", uses = MeepleMapper.class)
public interface PlayerMapper {

    PlayerMapper INSTANCE = Mappers.getMapper(PlayerMapper.class);

    /**
     * Konvertiert einen Player zu einem PlayerDTO. Der playerToken wird nicht
     * übernommen.
     *
     * @param player die Player-Entity
     * @return das PlayerDTO ohne playerToken
     */
    PlayerDTO toDTO(Player player);

    /**
     * Konvertiert einen PlayerDTO zu einem Player.
     *
     * @param playerDTO das PlayerDTO
     * @return die Player-Entity
     */
    @Mapping(target = "playerToken", ignore = true)
    @Mapping(target = "meeples", ignore = true)
    @Mapping(target = "activeMeeple", ignore = true)
    Player toEntity(PlayerDTO playerDTO);
}
