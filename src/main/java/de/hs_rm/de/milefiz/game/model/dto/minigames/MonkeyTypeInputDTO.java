package de.hs_rm.de.milefiz.game.model.dto.minigames;


import java.time.LocalDateTime;
import java.util.UUID;

public record MonkeyTypeInputDTO(UUID playerId, char typedChar, int position, LocalDateTime timestamp) {
    
}
