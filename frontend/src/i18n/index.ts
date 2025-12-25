import { ERRORS_DE } from './de/error'
import { ERRORS_EN } from './en/error'
import { UI_DE } from './de/ui'
import { UI_EN } from './en/ui'
import type { UIKey } from './uiKeys'
import type { ErrorCode } from '@/errors/errorCodes'

const dictionaries = {
  de: { ...UI_DE, ...ERRORS_DE },
  en: { ...UI_EN, ...ERRORS_EN },
}

let currentLocale: 'de' | 'en' = 'de'

export function setLocale(locale: 'de' | 'en') {
  currentLocale = locale
}

export function tUI(key: UIKey): string {
  return dictionaries[currentLocale][key] ?? key
}

export function tError(code: ErrorCode): string {
  return dictionaries[currentLocale][code] ?? code
}