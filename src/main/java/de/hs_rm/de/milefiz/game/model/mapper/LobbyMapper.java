package de.hs_rm.de.milefiz.game.model.mapper;

import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.dto.LobbyDTO;

/**
 * MapStruct Mapper für die Konvertierung zwischen Lobby und LobbyDTO. Nutzt
 * PlayerMapper für die Konvertierung der Player-Liste.
 */
@Mapper(componentModel = "spring", uses = PlayerMapper.class)
public interface LobbyMapper {

    LobbyMapper INSTANCE = Mappers.getMapper(LobbyMapper.class);

    /**
     * Konvertiert eine Lobby zu einem LobbyDTO. Die Player werden automatisch
     * über den PlayerMapper konvertiert.
     *
     * @param lobby die Lobby-Entity
     * @return das LobbyDTO
     */
    LobbyDTO toDTO(Lobby lobby);

    /**
     * Konvertiert einen LobbyDTO zu einer Lobby.
     *
     * @param lobbyDTO das LobbyDTO
     * @return Lobby-Entity
     */
    @Mapping(target = "board", ignore = true)
    Lobby toEntity(LobbyDTO lobbyDTO);

    /**
     * Konvertiert ein {@code Set<Lobby>} zu einem {@code Set<LobbyDTO>}.
 *
     *
     * @param lobbies Set von Lobby-Entitys
     * @return das Set von LobbyDTOs
     */
    Set<LobbyDTO> toDTOSet(Set<Lobby> lobbies);
    
    /**
     * Konvertiert ein {@code Set<LobbyDTO>} zu einem {@code Set<Lobby>}.
     *
     * @param lobbyDTOs Set von LobbyDTOs
     * @return das Set von Lobby-Entitys
     */
    Set<Lobby> toEntitySet(Set<LobbyDTO> lobbyDTOs);

}
