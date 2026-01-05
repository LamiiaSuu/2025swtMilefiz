import { defineStore } from 'pinia'
import type { IBoardDTD } from './IBoardDTD'
import { ref } from 'vue'
import { useMilefizStore } from './milefizstore'
import type { Player } from '../types/lobbyupdate'
import type { Lobby } from '../types/lobbyupdate'

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
    //testMeepleId: '123e4567-e89b-12d3-a456-426614174000' as string,
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

          const milefizStore = useMilefizStore()
          const lobby: Lobby | null = milefizStore.gamedata.lobby
          if (lobby) {
            const players: Player[] = lobby.players
            for (const player of players) {
              for (const meeple of player.meeples) {
                if (meeple.currentFieldId) {
                  this.meeplePositions[meeple.id] = meeple.currentFieldId
                }
                this.lastFields[meeple.id] = null
              }
              // Debug: Meeple Positionen loggen nach assignment
              console.log('boardStore.getBoard: meeplePositions after init:', JSON.stringify(this.meeplePositions))
            }
          }
        }
      } catch (error_) {
        console.log(error_)
        this.ok = false
        this.board = null
      }
    },

    // Variablen zurücksetzen
    resetBoardStore() {
      this.ok = false
      this.board = null
      this.meeplePositions = {}
      this.lastFields = {}
    },

    // meeple bewegen und letztes Feld merken
    updateMeeplePosition(meepleId: string, fieldId: string) {
      const previousField = this.meeplePositions[meepleId] ?? null
      if (previousField) {
        this.lastFields[meepleId] = previousField
      }
      this.meeplePositions[meepleId] = fieldId
    },

    updateBarrierPosition(barrierId: string, fieldId: string) {
      if (!this.board) return;

      //Alte Barriere entfernen
      const oldField = this.board.fields.find(f => f.barrier);
      if (oldField) {
        oldField.barrier = false;
      }

      //Neue Barriere setzen
      const newField = this.board.fields.find(f => f.id === fieldId);
      if (newField) {
        newField.barrier = true;
      } else {
        console.warn(`Barrier target field ${fieldId} not found.`);
        return;
      }

      //Reaktivität erzwingen (damit Vue neu rendert)
      this.board = {
        ...this.board,
        fields: [...this.board.fields],
      };

      console.log(`Barrier moved to field ${fieldId}`);
    },
    logOccupancySnapshot() {
      if (!this.board) {
        console.log('[BoardStore] No board loaded')
        return
      }

      const barrierFields = this.board.fields.filter(f => f.barrier).map(f => f.id)

      const meeplesByField: Record<string, string[]> = {}
      for (const [meepleId, fieldId] of Object.entries(this.meeplePositions)) {
        if (!meeplesByField[fieldId]) meeplesByField[fieldId] = []
        meeplesByField[fieldId].push(meepleId)
      }

      console.log('[BoardStore] Barrier:', barrierFields)
      console.log('[BoardStore] MeeplesByField:', meeplesByField)
    }
  },
  // Getter um alle Barriere-Meeple ans Frontend zu übergeben
  getters: {
    /**
     * Gibt alle Felder zurück, die eine Barriere haben
     */
    barrierFields: (state) => {
      return state.board?.fields.filter((f) => f.barrier) || []
    },

    /**
     * Prüft ob ein bestimmtes Feld eine Barriere hat
     * @param fieldId - ID des zu prüfenden Feldes
     * @returns true wenn Barriere vorhanden, sonst false
     */
    hasBarrier: (state) => (fieldId: string) => {
      return state.board?.fields.find((f) => f.id === fieldId)?.barrier || false
    },

    /**
     * Gibt alle Barrieren mit ihren 3D-Positionen für das Rendering zurück
     */
    barriersWithPositions: (state) => {
      if (!state.board) return []

      return state.board.fields
        .filter((f) => f.barrier)
        .map((field) => ({
          fieldId: field.id,
          position: [field.position.x, 0, field.position.y] as [number, number, number],
        }))
    },
  },
})
