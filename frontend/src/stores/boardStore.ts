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
    /** meeple positionen */
    meeplePositions: {} as Record<string, string>,
    testMeepleId: "123e4567-e89b-12d3-a456-426614174000" as string,
    lastFields: {} as Record<string, string | null>,
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

        // noch zum testen
        if (this.board) {
          const startField = this.board.fields.find(
            (f) => f.position.x === 0 && f.position.y === 0
          )

          if (startField) {
            this.meeplePositions[this.testMeepleId] = startField.id
            this.lastFields[this.testMeepleId] = null
            console.log(
              `TestMeeple ${this.testMeepleId} startet auf Feld ${startField.id}`
            )
          } else {
            console.warn("Kein Startfeld bei (0,0) gefunden!")
          }
        }

      } catch (error_) {
        console.log(error_)
        this.ok = false
        this.board = null
      }
    },

    // meeple bewegen und letztes Feld merken
    updateMeeplePosition(meepleId: string, fieldId: string) {
      const previousField = this.meeplePositions[meepleId] ?? null
      if (previousField) {
        this.lastFields[meepleId] = previousField
      }
      this.meeplePositions[meepleId] = fieldId
    },
  },
})
