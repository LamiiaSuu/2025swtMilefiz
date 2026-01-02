import { ERRORS_DE } from './de/error'
import { ERRORS_EN } from './en/error'
import { ERRORS_NL } from './nl/error'
import { UI_NL } from './nl/ui'
import { UI_DE } from './de/ui'
import { UI_EN } from './en/ui'
import type { UIKey } from './uiKeys'
import type { ErrorCode } from '@/errors/errorCodes'
import { ref } from 'vue'


/**
 * Zentrales Sprach-Wörterbuch.
 *
 * Enthält:
 * - UI-Texte
 * - Fehlermeldungen
 *
 * Keys werden aus den jeweiligen JSON-/TS-Dateien gemerged.
 */
const dictionaries = {
  de: { ...UI_DE, ...ERRORS_DE },
  en: { ...UI_EN, ...ERRORS_EN },
  nl: { ...UI_NL, ...ERRORS_NL }
}

/**
 * Aktuell aktive Sprache.
 *
 * Reaktiv, damit Vue-Komponenten automatisch re-rendern,
 * wenn die Sprache geändert wird.
 *
 * Default: "de"
 */
export const currentLocale = ref<'de' | 'en' | 'nl'>('de')

/**
 * Setzt die aktuelle Sprache.
 *
 * @param locale Ziel-Sprache (de | en | nl)
 */
export function setLocale(locale: 'de' | 'en' | 'nl') {
  currentLocale.value = locale
  
}

/**
 * Liefert UI-Texte anhand eines UI-Keys.
 * Fällt im Fehlerfall auf den Key selbst zurück.
 *
 * @param key UI-Key
 * @returns übersetzter String
 */
export function tUI(key: UIKey): string {
  return dictionaries[currentLocale.value][key] ?? key
}

/**
 * Liefert Fehlermeldungen anhand eines ErrorCodes.
 * Fällt im Fehlerfall auf den Code selbst zurück.
 *
 * @param code Fehlercode
 * @returns übersetzter Fehlertext
 */
export function tError(code: ErrorCode): string {
  return dictionaries[currentLocale.value][code] ?? code
}