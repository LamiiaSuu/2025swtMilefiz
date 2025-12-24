import { reactive, readonly } from 'vue'

/**
 * Globaler reaktiver State für Error-Anzeige
 * Wird zwischen allen Komponenten geteilt
 */
const errorState = reactive({
  show: false,
  message: '',
  type: 'error' as 'error' | 'warning' | 'info' | 'critical',
  autoHide: true,
})

/**
 * Timer für automatisches Ausblenden
 * null = kein aktiver Timer
 */
let autoHideTimer: number | null = null

/**
 * Composable für zentrales Error-Management
 * Bietet verschiedene Error-Types mit konfigurierbaren Auto-Hide Timings
 */
export function useErrorHandler() {
  /**
   * Hauptfunktion zum Anzeigen von Error-Messages
   * @param message - Der anzuzeigende Text
   * @param type - Art des Errors (bestimmt UI-Modus und Timing)
   * @param autoHide - Ob die Message automatisch verschwinden soll
   */
  function showError(
    message: string,
    type: 'error' | 'warning' | 'info' | 'critical' = 'error', //error ist default Wert
    autoHide = true,
  ) {
    // Clear existing timer
    if (autoHideTimer) {
      clearTimeout(autoHideTimer)
      autoHideTimer = null
    }

    // Error State aktualisieren
    errorState.show = true
    errorState.message = message
    errorState.type = type
    errorState.autoHide = autoHide

    if (autoHide) {
      let timeout = 2500 // Default 2.5 extra Sekunden
      //Verschiedene Timings je nach Type:
      switch (type) {
        case 'info':
          timeout = 1000 // Success: 1 extra Sekunden
          break
        case 'warning':
          timeout = 500 // Warning: .5 extra Sekunden
          break
        case 'error':
          timeout = 500 // Error: .5 extra Sekunden
          break
        case 'critical':
          timeout = 2000 // Critical: 2 extra Sekunden
      }
      autoHideTimer = setTimeout(() => {
        if (errorState.show) hideError()
      }, timeout)
    }
  }

  function hideError() {
    if (autoHideTimer) {
      clearTimeout(autoHideTimer)
      autoHideTimer = null
    }

    errorState.show = false
    errorState.message = ''
  }

  /**
   * Zeigt eine Erfolgs-Message als blaues Info-Popup
   * @param message - Der Erfolgstext
   */
  function showSuccess(message: string) {
    showError(message, 'info', true)
  }
  /**
   * Zeigt eine Warnung als zentriertes Orange-Overlay
   * @param message - Der Warnungstext
   */
  function showWarning(message: string) {
    showError(message, 'warning', true)
  }

  /**
   * Zeigt einen kritischen Fehler als rotes Popup (8s Auto-Hide)
   * @param message - Der kritische Fehlertext
   */
  function showCriticalError(message: string) {
    showError(message, 'critical', true)
  }
  return {
    errorState: readonly(errorState),
    showError,
    hideError,
    showSuccess,
    showWarning,
    showCriticalError,
  }
}
