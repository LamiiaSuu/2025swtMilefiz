export interface Lobby {
  id: string
  lobbyName: string
  players: Player[]
  maxPlayers: number
}

export interface Player {
  id: string
  meeples: Meeple[]
  color: string
  leader: boolean,
  activeMeeple: Meeple | null
  remainingMoves: number
}

export interface Meeple {
  id: string
  playerId: string
  currentFieldId: string | null
}

export interface LobbyUpdateEvent {
  type: 'LOBBY_UPDATE'
  ownPlayerId: string
  lobby: Lobby
  playerToken: string
  msg: string | null
}