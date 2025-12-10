package de.hs_rm.de.milefiz.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import de.hs_rm.de.milefiz.game.model.dto.LobbyDTO;
import de.hs_rm.de.milefiz.game.model.dto.MeepleDTO;
import de.hs_rm.de.milefiz.game.model.dto.PlayerDTO;
import de.hs_rm.de.milefiz.game.model.mapper.LobbyMapperImpl;
import de.hs_rm.de.milefiz.game.model.mapper.PlayerMapper;

@ExtendWith(MockitoExtension.class)
class LobbyMapperTest {

    @Mock
    private PlayerMapper playerMapper;

    @InjectMocks
    private LobbyMapperImpl mapper;

    @Test
    void toDTO_mapsBasicFieldsAndPlayers() {
        Lobby lobby = new Lobby();
        UUID lobbyId = UUID.randomUUID();
        lobby.setId(lobbyId);
        lobby.setMaxPlayers(4);

        Player player = new Player(Color.RED);
        lobby.getPlayers().add(player);

        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setId(player.getId());
        playerDTO.setColor(Color.RED);
        playerDTO.setMeeples(new MeepleDTO[0]);
        when(playerMapper.toDTO(player)).thenReturn(playerDTO);

        LobbyDTO dto = mapper.toDTO(lobby);

        assertNotNull(dto);
        assertEquals(lobbyId, dto.getId());
        assertEquals(4, dto.getMaxPlayers());
        assertNotNull(dto.getPlayers());
        assertEquals(1, dto.getPlayers().size());
        assertEquals(Color.RED, dto.getPlayers().get(0).getColor());
    }

    @Test
    void toDTOSet_mapsAllLobbies() {
        Lobby lobby1 = new Lobby();
        Lobby lobby2 = new Lobby();

        Set<LobbyDTO> result = mapper.toDTOSet(Set.of(lobby1, lobby2));

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void toEntity_mapsBasicFieldsAndPlayers() {
        UUID lobbyId = UUID.randomUUID();
        LobbyDTO dto = new LobbyDTO();
        dto.setId(lobbyId);
        dto.setMaxPlayers(3);

        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setId(UUID.randomUUID());
        playerDTO.setColor(Color.GREEN);
        playerDTO.setMeeples(new MeepleDTO[0]);

        List<PlayerDTO> players = new ArrayList<>();
        players.add(playerDTO);
        dto.setPlayers(players);

        Player mappedPlayer = new Player(Color.GREEN);
        when(playerMapper.toEntity(playerDTO)).thenReturn(mappedPlayer);

        Lobby entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(lobbyId, entity.getId());
        assertEquals(3, entity.getMaxPlayers());
        assertNotNull(entity.getPlayers());
        assertEquals(1, entity.getPlayers().size());
        assertEquals(Color.GREEN, entity.getPlayers().get(0).getColor());
        assertNull(entity.getBoard()); // board is ignored on mapping
    }
}
