package de.hs_rm.de.milefiz.messaging.commands;

public record UpdateLobbySettingsCommand(String newLobbyName, int maxPlayers) {

}
