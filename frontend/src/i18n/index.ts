import { ERRORS_DE } from './de/error'
import { ERRORS_EN } from './en/error'
import type { ErrorCode } from '@/errors/errorCodes'

const dictionaries = {
  de: ERRORS_DE,
  en: ERRORS_EN,
}

let currentLocale: 'de' | 'en' = 'de'

export function setLocale(locale: 'de' | 'en') {
  currentLocale = locale
}

export function tError(code: ErrorCode): string {
  return dictionaries[currentLocale][code] ?? code
}