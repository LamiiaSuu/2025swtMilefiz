package de.hs_rm.de.milefiz.game.service;

import java.util.UUID;

public interface CooldownService {
    public int getCooldown(UUID playerId);
    public void addCooldown(UUID playerId, int seconds);
    public void tick();
}
 