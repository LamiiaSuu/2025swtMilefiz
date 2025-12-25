import { ERRORS_DE } from './de/error'
import { ERRORS_EN } from './en/error'
import { ERRORS_NL } from './nl/error'
import { UI_NL } from './nl/ui'
import { UI_DE } from './de/ui'
import { UI_EN } from './en/ui'
import type { UIKey } from './uiKeys'
import type { ErrorCode } from '@/errors/errorCodes'
import { ref } from 'vue'

const dictionaries = {
  de: { ...UI_DE, ...ERRORS_DE },
  en: { ...UI_EN, ...ERRORS_EN },
  nl: { ...UI_NL, ...ERRORS_NL }
}


export const currentLocale = ref<'de' | 'en' | 'nl'>('de')

export function setLocale(locale: 'de' | 'en' | 'nl') {
  currentLocale.value = locale
  
}

export function tUI(key: UIKey): string {
  return dictionaries[currentLocale.value][key] ?? key
}

export function tError(code: ErrorCode): string {
  return dictionaries[currentLocale.value][code] ?? code
}