/**
 * Datenstruktur für ein einzelnes Spielfeld.
 * Beschreibt Position, Nachbarn und Eigenschaften eines Feldes auf dem Spielbrett.
 */
export interface IFieldDTD{
    /** Eindeutige Feld-ID */
    id: string
    
    /** Verkettung zum jeweiligen Nachbarfeld (optional) */
    north?: string
    south?: string
    west?: string
    east?: string
    
    /** Art von Feld: Start von Farbe, Normal, Ziel */
    type: string
    /** Koordinaten des Felds */
    position: { x: number, y: number }
    /** Ob Feld durch Spieler oder Blockade blockiert ist */
    barrier: boolean
}