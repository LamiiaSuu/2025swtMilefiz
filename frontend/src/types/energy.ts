
export interface EnergyCommand {
  playerId: string;
}

export interface FrontendEnergyEvent {
  sessionId: string;
  meepleId: string;
  targetField: string;
  remainingMoves?: number; 
}