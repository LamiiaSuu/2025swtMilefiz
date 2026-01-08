/**
 * Asset-Konfiguration ausschließlich für das Standard-Board (UUID: b0ebd429-02b1-47f4-85e0-08162bebea04)
 *
 * Diese Assets werden nur geladen, wenn das Standard-Board aktiv ist.
 */

export interface BoardAsset {
  type: string // Ordner in /environment/
  variant: string // Dateiname ohne .glb
  position: [number, number, number]
  scale: number
  rotation: number // in Grad
}

/**
 * Dekorative Assets für das Standard-Board
 * Positioniert entlang der Ränder und strategischen Punkten
 */
export const standardBoardAssets: BoardAsset[] = [
  { type: 'mountains', variant: 'rainbow', position: [22, 18, 130], scale: 8, rotation: 205 },

  { type: 'easter_eggs', variant: 'supermarket', position: [-100, 0, -100], scale: 2, rotation: 0 },
  { type: 'easter_eggs', variant: 'bernd', position: [-1, 0, 30], scale: 0.05, rotation: 160 },

  { type: 'animals', variant: 'little_fox', position: [2, 0.2, 8], scale: 0.5, rotation: 0 },
  { type: 'animals', variant: 'deer', position: [14, 0, 4], scale: 0.5, rotation: 160 },
  { type: 'animals', variant: 'blue_jay', position: [11.9, 1.3, 28.5], scale: 0.1, rotation: 0 },

  { type: 'buildings', variant: 'cabin_1', position: [13, 0.7, 12.5], scale: 0.6, rotation: 200 },
  { type: 'buildings', variant: 'camp_1', position: [22, 0, 12], scale: 0.6, rotation: 180 },
  { type: 'buildings', variant: 'bonfire_1', position: [24, 0, 12], scale: 0.2, rotation: 0 },
  { type: 'buildings', variant: 'tree_house_1', position: [30, -0.1, 4], scale: 0.1, rotation: 270 },
  { type: 'buildings', variant: 'picnic_1', position: [-6, -0.15, 4], scale: 0.8, rotation: 90 },
  { type: 'buildings', variant: 'water_well_1', position: [12, 0, 24], scale: 0.2, rotation: 0 },

  { type: 'water', variant: 'pond_1', position: [6, 0.08, 16], scale: 2.5, rotation: 0 },
  { type: 'water', variant: 'pond_3', position: [18, 0.01, 16], scale: 0.8, rotation: 220 }
]

/**
 * UUID des Standard-Boards zur Identifikation
 */
export const STANDARD_BOARD_ID = 'b0ebd429-02b1-47f4-85e0-08162bebea04'
