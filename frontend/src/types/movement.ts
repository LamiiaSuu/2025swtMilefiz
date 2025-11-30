export type Direction = "NORTH" | "EAST" | "SOUTH" | "WEST";

export interface MovementCommand {
  meepleId: string;
  direction: Direction;
  remainingMoves?: number; 
}

export interface FrontendMoveEvent {
  sessionId: string;
  meepleId: string;
  targetField: string;
  remainingMoves?: number; 

}