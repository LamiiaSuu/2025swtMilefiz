/**
 * DTD für Spielbrett (Sammlung von Feldern)
 */
import type { IFieldDTD } from "./IFieldDTD";

export interface IBoardDTD{
    /** Eindeutige Board-ID */
    id: string
    fields: Array<IFieldDTD>
}