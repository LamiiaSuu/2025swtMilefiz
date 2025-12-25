import type { ErrorCode } from '@/errors/errorCodes'

export const ERRORS_NL: Record<ErrorCode, string> = {
    ROLL_DICE_ERROR_MOVES_LEFT: 'Er zijn nog zetten over – je kunt de dobbelsteen nog niet gooien!',
    REMAINING_MOVES_LOST: 'Geen mogelijke zetten meer – resterende zetten verloren!',
    CHEATED: 'Je kleine valsspeler!',
    MEEPLE_SELECTION_REJECTED: 'Meeple al verplaatst – wisselen niet toegestaan!',
    MOVE_ERROR_INTO_START: 'Startvelden kunnen niet betreden worden!',
    MOVE_ERROR_NO_FIELD_IN_DIRECTION: 'Geen veld in die richting!',
    MOVE_ERROR_NO_MOVES_LEFT: 'Geen zetten meer over!',
    MOVE_ERROR_CANT_CHANGE_DIRECTION: 'Richting veranderen niet toegestaan!',
    MOVE_ERROR_TOO_MANY_MOVES_FOR_GOAL: 'Het doel kan alleen met de laatste zet bereikt worden!',
    MOVE_ERROR_OCCUPIED_BY_OWN_MEEPLE: 'Veld al bezet door je eigen meeple!',
    MOVE_BARRIER_REJECTED_START_OR_END: 'Barrier kan niet op start- of doelvelden geplaatst worden',
    MOVE_BARRIER_OCCUPIED: 'Barrier kan niet op een bezet veld geplaatst worden',
    REJECTED_BY_BARRIER: 'Oei! Dat zag pijnlijk uit – resterende zetten verloren!',
    SAVE_ENERGY_ERROR: 'Maximale energie bereikt of meeple al verplaatst!',
    CONSUME_ENERGY_ERROR: 'Niet genoeg energie voor een sprong!'
}
