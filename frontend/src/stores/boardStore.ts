import { defineStore } from 'pinia'
import type { IBoardDTD } from './IBoardDTD'
import { ref } from 'vue'
import { useMilefizStore } from './milefizstore'
import type { Player } from '../types/lobbyupdate'
import type { Lobby } from '../types/lobbyupdate'
import type { IFieldDTD } from './IFieldDTD'
import type { ITreeDTD } from './ITreeDTD'

const gameBoardTiles = ref<IFieldDTD[]>()
const gameTrees = ref<ITreeDTD[]>()
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
    /** meeple rotationen */
    meepleRotations: {} as Record<string, number>,
  }),
  actions: {
    /**
     * Lädt das Spielbrett vom Backend.
     * Setzt ok=true bei Erfolg, leert bei Fehler den State.
     */
    async getBoard() {
      console.log('Start receiving Gameboard Data...')
      try {
        const milefizStore = useMilefizStore()
        const lobbyId = milefizStore.gamedata.lobby?.id
        if (!lobbyId) {
          console.error("Keine Lobby ID - Board kann nicht geladen werden")
          return
        }
        const resp = await fetch(`/api/lobby/${lobbyId}/board/get`)
        if (!resp.ok) {
          console.error('Error while recieving Data:\n', resp.statusText)
          throw new Error(resp.statusText)
        }
        this.board = (await resp.json()) as IBoardDTD
        gameBoardTiles.value = this.board.fields
        gameTrees.value = this.board.trees

        this.ok = true

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
                this.meepleRotations[meeple.id] ??= 0
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
      this.meepleRotations = {}
    },

    // meeple bewegen und letztes Feld merken
    updateMeeplePosition(meepleId: string, fieldId: string) {
      const previousField = this.meeplePositions[meepleId] ?? null
      if (previousField) {
        this.lastFields[meepleId] = previousField
      }
      this.meeplePositions[meepleId] = fieldId
    },

    updateMeepleRotation(meepleId: string, rotation: number) {
      this.meepleRotations[meepleId] = rotation
    },

    updateBarrierPosition(barrierId: string, currentFieldId: string, targetFieldId: string) {
      if (!this.board) return;

      //Alte Barriere entfernen
      const oldField = this.board.fields.find(f => f.id === currentFieldId);
      if (oldField) {
        oldField.barrier = false;
      }

      //Neue Barriere setzen
      const newField = this.board.fields.find(f => f.id === targetFieldId);
      if (newField) {
        newField.barrier = true;
      } else {
        console.warn(`Barrier target field ${targetFieldId} not found.`);
        return;
      }

      //Re-render erzwingen 
      this.board = {
        ...this.board,
        fields: [...this.board.fields],
      };

      console.log(`Barrier moved to field ${targetFieldId}`);
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
