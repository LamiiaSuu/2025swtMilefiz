/**
 * DTD für Spielbrett (Sammlung von Feldern)
 */
import type { IFieldDTD } from "./IFieldDTD";

export interface IBoardDTD{
    fields: Array<IFieldDTD>
}