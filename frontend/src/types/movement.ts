export type Direction = "NORTH" | "EAST" | "SOUTH" | "WEST";

export interface MovementCommand {
  meepleId: string;
  direction: Direction;
}

export interface MoveBarrierCommand {
  barrierId: string,
  targetFieldId: string
}

export interface FrontendMoveEvent {
  sessionId: string;
  meepleId: string;
  targetField: string;
  remainingMoves?: number; 
  moved: boolean;
}

export interface RotationCommand{
  meepleId: string;
  rotation: number;
}