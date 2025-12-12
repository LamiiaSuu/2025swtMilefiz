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
 * Farbwerte für GameCharacter body.
 */
export const characterBodyColors: Record<PlayerColor, string> = {
  RED: '#d63031',
  GREEN: '#3fc37a',
  YELLOW: '#f5bb0eff',
  BLUE: '#1d7ddcff'
}

/**
 * Farbwerte für GameCharacter eyes.
 */
export const characterEyeColors: Record<PlayerColor, string> = {
  RED: '#96c3c1ff',
  GREEN: '#1e0620ff',
  YELLOW: '#020b27ff',
  BLUE: '#d5bda6ff'
}

/**
 * Hilfsfunktion: Gibt Body- und Eye-Color für eine Spielerfarbe zurück.
 * 
 * @param color - Spielerfarbe (RED, GREEN, YELLOW, BLUE)
 * @returns Objekt mit bodyColor und eyeColor
 */
export function getCharacterColors(color: PlayerColor) {
  return {
    bodyColor: characterBodyColors[color],
    eyeColor: characterEyeColors[color]
  }
}