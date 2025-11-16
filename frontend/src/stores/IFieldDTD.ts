/**
 * Datenstruktur für ein einzelnes Spielfeld.
 * Beschreibt Position, Nachbarn und Eigenschaften eines Feldes auf dem Spielbrett.
 */
export interface IFieldDTD{
    /** Eindeutige Feld-ID */
    id: number
    
    /** Verkettung zum jeweiligen Nachbarfeld (optional) */
    north?: number
    south?: number
    west?: number
    east?: number
    
    /** Art von Feld: Start von Farbe, Normal, Ziel */
    type: string
    /** Koordinaten des Felds */
    position: Array<number>
    /** Ob Feld durch Spieler oder Blockade blockiert ist */
    isBarrier: boolean
}