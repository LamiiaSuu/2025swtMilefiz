/**
 * Datenstruktur für Baum-Objekt
 */

export interface ITreeDTD {
  treePosition: { x: number, y: number }
  treeType: Sizes
}

export enum Sizes {
  Small = "SMALL",
  Medium = "MEDIUM",
  Large = "LARGE",
  Bush = "BUSH",
  Mushroom = "MUSHROOM",
  Grass_Smol = "GRASS_SMOL"
}