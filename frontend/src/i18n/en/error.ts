import type { ErrorCode } from '@/errors/errorCodes'

export const ERRORS_EN: Record<ErrorCode, string> = {
    ROLL_DICE_ERROR_MOVES_LEFT: 'Moves remaining - cannot roll the dice yet!',
    REMAINING_MOVES_LOST: 'No possible moves left - remaining moves lost!',
    CHEATED: 'You little cheater!',
    MEEPLE_SELECTION_REJECTED: 'Meeple already moved - switching not allowed!',
    MOVE_ERROR_INTO_START: 'Bases cannot be entered!',
    MOVE_ERROR_NO_FIELD_IN_DIRECTION: 'No space in that direction!',
    MOVE_ERROR_NO_MOVES_LEFT: 'No moves remaining!',
    MOVE_ERROR_CANT_CHANGE_DIRECTION: 'Changing direction not allowed!',
    MOVE_ERROR_TOO_MANY_MOVES_FOR_GOAL: 'Goal can only be reached with the last move!',
    MOVE_ERROR_OCCUPIED_BY_OWN_MEEPLE: 'Space already occupied by your own meeple!',
    MOVE_BARRIER_REJECTED_START_OR_END: 'Barrier cannot be placed on start or goal spaces',
    MOVE_BARRIER_OCCUPIED: 'Barrier cannot be placed on an occupied space',
    REJECTED_BY_BARRIER: 'Ouch! That looked painful...',
    SAVE_ENERGY_ERROR: 'Maximum energy reached or meeple already moved!',
    CONSUME_ENERGY_ERROR: 'Not enough energy for a jump!',
    NO_LOBBY_FOUND: 'Lobby not found!',
    NO_FILE_CHOSEN: 'No file selected!',
    BOARD_INVALID: 'Map validation failed: A map needs 4 start fields connected to at least one goal field.',
    BOARD_SUCCESSFULLY_IMPORTED: 'Map imported successfully!',
    BOARD_COULD_NOT_BE_IMPORTED: 'Map could not be imported: ',
    MEEPLE_IN_DUEL: 'The opposing meeple is already in a duel. Please wait until the duel is finished!',
    MOVE_ERROR_NO_VALID_FIELDS: 'There are no valid moves possible in this direction',
    MINIGAME_SLOT_JACKPOT_SUCCESS_MESSAGE: 'JACKPOT!!! Your energy has been replenished!',

}