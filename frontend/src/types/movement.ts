export type Direction = "NORTH" | "EAST" | "SOUTH" | "WEST";

export interface MovementCommand {
  meepleId: string;
  direction: Direction;
}

export interface FrontendMoveEvent {
  sessionId: string;
  meepleId: string;
  targetField: string;
}