/**
 * DTD für Spielbrett (Sammlung von Feldern)
 */
import type { IFieldDTD } from "./IFieldDTD";

export interface IBoardDTD{
    /** Sammlung aller Felder die zu dem Spielbrett gehören */
    fields: Array<IFieldDTD>
}