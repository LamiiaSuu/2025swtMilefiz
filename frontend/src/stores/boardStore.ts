import { defineStore } from 'pinia'
import type { IBoardDTD } from './IBoardDTD'
import { ref } from 'vue'

const gameBoardTiles = ref<IBoardDTD>()
/**
 *
 * Pinia Store für das Spielbrett.
 * Lädt Spielfelddaten vom Backend und verwaltet den Zustand.
 */
export const useBoardStore = defineStore('board', {
  state: () => ({
    /** Status ob das Board erfolgreich geladen wurde */
    ok: false,
    /** Das komplette Spielbrett mit allen Feldern */
    board: null as IBoardDTD | null,
  }),
  actions: {
    /**
     * Lädt das Spielbrett vom Backend.
     * Setzt ok=true bei Erfolg, leert bei Fehler den State.
     */
    async getBoard() {
      console.log('Start receiving Gameboard Data...')
      try {
        const resp = await fetch('/api/game/getBoard')
        if (!resp.ok) {
          console.error('Error while recieving Data:\n', resp.statusText)
          throw new Error(resp.statusText)
        }
        gameBoardTiles.value = (await resp.json()) as IBoardDTD

        this.ok = true
        this.board = gameBoardTiles.value

        console.log('GameBoard successfully loaded')

      } catch (error_) {
        console.log(error_)
        this.ok = false
        this.board = null
      }
    },
  },
})
