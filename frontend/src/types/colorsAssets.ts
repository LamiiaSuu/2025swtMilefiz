/**
 * Zentrale Farb-Konfiguration für das Spiel.
 * 
 * Definiert konsistente Farben für verschiedene Spielelemente basierend
 * auf den Spielerfarben (RED, GREEN, YELLOW, BLUE).
 */

export type PlayerColor = 'RED' | 'GREEN' | 'YELLOW' | 'BLUE'

/**
 * Farbwerte für Start-Tiles (Häuser).
 */
export const startingbaseColors: Record<`START_${PlayerColor}`, string> = {
  START_RED: '#e24b4b',
  START_GREEN: '#3fc37a',
  START_YELLOW: '#ffd24d',
  START_BLUE: '#4da6ff'
}

/**
 * Gibt Hex-Farben für Body und Eyes basierend auf Spielerfarbe zurück.
 */
export const playerColors: Record<PlayerColor, { body: string; eyes: string }> = {
  RED: {
    body: '#ef2424',
    eyes: '#a9d8d5'
  },
  GREEN: {
    body: '#09af25',
    eyes: '#8a728aff'
  },
  YELLOW: {
    body: '#eccd06',
    eyes: '#555964'
  },
  BLUE: {
    body: '#1f89db',
    eyes: '#e9e1d8'
  }
}

/**
 * Holt Farben für eine Spielerfarbe.
 * Gibt default (weiß/schwarz) zurück wenn Farbe ungültig.
 */
export function getPlayerColors(color?: string): { body: string; eyes: string } {
  if (!color) return { body: '#ffffff', eyes: '#000000' }
  
  return playerColors[color as PlayerColor]
}