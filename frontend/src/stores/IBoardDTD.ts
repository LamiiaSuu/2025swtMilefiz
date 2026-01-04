/**
 * DTD für Spielbrett (Sammlung von Feldern)
 */
import type { IFieldDTD } from "./IFieldDTD";
import type { ITreeDTD } from "./ITreeDTD";


export interface IBoardDTD{
    fields: Array<IFieldDTD>
    trees: Array<ITreeDTD>
}