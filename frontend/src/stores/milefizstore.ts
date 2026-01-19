import { reactive, readonly, computed, ref } from 'vue'
import router from '@/router'
import { defineStore } from 'pinia'
import { Client, type Message } from '@stomp/stompjs'
import type { Direction, MoveBarrierCommand, MovementCommand, RotationCommand } from "@/types/movement";
import type { EnergyCommand } from '@/types/energy'
import type { LobbyUpdateEvent, Lobby, Player, Meeple } from "@/types/lobbyupdate";
import { useBoardStore } from "./boardStore"
import { generateUUID } from 'three/src/math/MathUtils.js';
import { useErrorHandler } from '@/composables/useErrorHandler';
import { startingbaseColors, playerColors } from '@/types/colorsAssets';
import { useAudioStore } from '@/stores/audioStore'
import { getAutomaticTypeDirectiveNames } from 'typescript';
import { tUI } from '@/i18n';

// const wsurl = `ws://${window.location.host}/milefiz`
const wsurl = `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}/ws`
const DEST = '/topic/milefiz/lobby/'

let stompclient: Client | null = null

export const useMilefizStore = defineStore('milefizstore', () => {
  const audioStore = useAudioStore()

  /**
   * Cooldown für das Würfelsystem
   * cooldown
   * @prop {number} remainingSeconds - Beschreibt verbleibende Sekunden des Würfelcooldowns.
   * @prop {boolean} active - Wenn 'true', dann läuft gerade aktiv ein Cooldown herunter. Wenn 'false' steht der Cooldown auf 0 und es läuft gerade kein Timer.
   */
  const cooldown = reactive({
    remainingSeconds: 0,
    active: false,
  })

  /**
   * Energy State für das Energiesystem
   * @prop {number} maxEnergy - Maximal speicherbare Energie, wird aus dem Backend gesetzt
   * @prop {boolean} isEnergyFull - True, genau dann, wenn gespeicherte Energie maxEnergy entspricht
   * @prop {boolean} isEnergyFresh - gibt an, ob es sich um "frische Energie" handelt, d.h. Würfelergebnis kann nur gespeichert werden, wenn der Spieler noch keine Moves mit dem Würfelergebnis getätigt hat
   */
  const energy = reactive<{
    maxEnergy: number,
    isEnergyFull: boolean,
    isEnergyFresh: boolean,
  }>({
    maxEnergy: 0,
    isEnergyFull: false,
    isEnergyFresh: false,
  })

  // UI/Animation Trigger: GameBoard kann darauf reagieren und jump() aufrufen
  const jumpTrigger = ref<{ meepleId: string; nonce: number } | null>(null)

  function triggerJumpLocally(meepleId: string) {
    gamedata.isJumping = true
    jumpTrigger.value = { meepleId, nonce: Date.now() }
  }

  /** 
   * Gewinndialog
   * @prop {boolean} gameFinished - Wenn 'true' zählt das Spiel als beendet, weil jemand ins Ziel gekommen ist.
   * @prop {string} winnerName    - Name des gewinnenden Spielers.
   * @prop {string} winnerColor   - Farbe des Gewinners
  */
  const gameFinished = ref(false)
  const winnerName = ref<string | null>(null)
  const winnerColor = ref<string | null>(null)

  /**
   * PopUp-Menu
   * @prop {boolean} popUpMenuOpen - True, wenn das PopUp-Menu offen ist
   * @prop {boolean} popUpSettingsOpen - True, wenn das PopUp-Menu fuer Einstellungen offen ist
  */
  const popUpMenuOpen = ref(false)
  const popUpSettingsOpen = ref(false)
  const popUpTutorialOpen = ref(false)


  /**
   * Beschreibt, wie ein Feld besetzt ist:
   *  - FREE: Sperre kann auf diesem Feld platziert werden
   *  - OCCUPIED: Feld ist durch Gegner oder Sperre besetzt
   *  - OWN_MEEPLE: Ein eigenes Meeple steht auf diesem Feld
   *  - INVALID: Feld ist nicht für eine Sperre auswählbar (Start-/Zielfeld)
   */
  type Occupancy = 'FREE' | 'OCCUPIED' | 'OWN_MEEPLE'
  /**
   * Reactive state für die MiniMap-Komponente.
   * Verwaltet die Anzeige und Interaktion mit der Barrieren-Verschiebungs-Map.
   */
  const minimap = reactive({
    isMiniMapOpen: false, // Ist MiniMap aktuell geöffnet
    currentPosition: "", // Aktuelle Position des aktiven Meeples
    selectedBarrierId: "", // Welche Barriere wird verschoben
    selectedFieldId: "", // Zielfeld für Verschiebung
    isMovingBarrier: false, // Verschiebt der Spieler, der in die Barrier gelaufen ist gerade? (für Event-Filter)
    occupancyByFieldId: {} as Record<string, Occupancy>, // Belegungsstatus aller Felder
    ownColor: "RED" as "RED" | "GREEN" | "BLUE" | "YELLOW", //Farbe des Spielers
  })


  // Beispiele für Daten
  const gamedata = reactive<{
    playerId: string
    playerToken: string
    energy: number
    isJumping: boolean
    currentDiceRoll?: number
    currentField: string
    activeMeeple: string
    lobby: Lobby | null
    moved: boolean
    selectRandomMinigame: boolean
  }>({
    playerId: '', // UUID vom eigenen Spieler
    playerToken: '',
    energy: 0, //Energy des Spielers
    isJumping: false, //Flag, ob sich der Spieler in einer Sprungaktion befindet
    currentDiceRoll: undefined, //Würfel ergebnis
    currentField: "",
    activeMeeple: "",
    lobby: null, // DummyLobby: 271c95db-3737-496f-9081-ae920e8ebbf7
    moved: false,
    selectRandomMinigame: true
  })
  const activeDuels = reactive<Record<string, any>>({})


  const isJoined = computed(() => {
    return Boolean(gamedata.lobby)
  })

  const { showError, showWarning, showCriticalError, showSuccess } = useErrorHandler()

  function startMilefizLiveUpdate() {
    console.log('Starting Liveupdater for Milefiz with playerToken ' + gamedata.playerToken)
    // Nur eine Instanz
    if (stompclient != null && stompclient.connected) {
      return
    }

    stompclient = new Client({
      brokerURL: wsurl,
      connectHeaders: {
        'player-token': gamedata.playerToken,
      },
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    })
    stompclient.onWebSocketError = (event) => {
      console.error(event)
      /* WS-Fehler */
    }
    stompclient.onStompError = (frame) => {
      console.error(frame)
      /* STOMP-Fehler */
    }

    stompclient.onConnect = (frame) => {
      console.log('Connected')

      if (stompclient == null) {
        console.error('Geht nicht')
        return
      }
      // Callback: erfolgreicher Verbindugsaufbau zu Broker
      stompclient.subscribe(DEST + gamedata.lobby?.id, (message) => {
        //console.log('Message received: ' + message + '\nBody:\n' + message.body)
        if (message.body === "KEEP CONNEC") return
        // Fängt die JSON message ab und bildet die Schnittstelle des Front- und Backends für den Cooldown des Würfelns
        const event = JSON.parse(message.body)
        const boardStore = useBoardStore()

        // Wenn der Spieler erfolgreich gewürfelt hat, wird hier die die Nachricht abgefangen und die entsprechenden Daten werden aktualisiert
        if (event.type === 'ROLL_DICE' && event.playerId === gamedata.playerId) {
          console.log(`Player ${event.playerId} rolled: ${event.number}`)
          gamedata.currentDiceRoll = event.number
          cooldown.active = true
          cooldown.remainingSeconds = event.cooldown
          energy.isEnergyFresh = true
        }

        // Wenn der Spieler im Moment noch nicht Würfeln darf
        else if (event.type === 'ROLL_DICE_ERROR' && event.playerId === gamedata.playerId) {
          console.log(`Player ${event.playerId} cannot roll their dice!`,)
          audioStore.playSfx('eventError')
          cooldown.remainingSeconds = event.seconds
        }

        // Wenn der Spieler im Moment noch nicht Würfeln darf, weil er noch Moves übrig hat, wird hier die Nachricht abgefangen und die verbleibenden Sekunden werden geupdatet.
        else if (
          event.type === 'ROLL_DICE_ERROR_MOVES_LEFT' &&
          event.playerId === gamedata.playerId
        ) {
          console.log(
            `Player ${event.playerId} still has ${event.moves} moves left and therefore can't roll their dice yet!`,
          )
          gamedata.currentDiceRoll = event.moves
          showWarning(`ROLL_DICE_ERROR_MOVES_LEFT`)
        }

        // Sobald der Cooldown eines Spielers ready ist wird vom Backend hier hin das Signal mit LobbyID und SpielerID gesendet und hier abgefangen.
        else if (event.type === 'COOLDOWN_READY' && event.playerId === gamedata.playerId) {
          console.log(`Player ${event.playerId} can roll again!`)
          cooldown.active = false
          cooldown.remainingSeconds = 0
        } else if (event.type === 'MOVE_ERROR') {
          if (event.playerId === gamedata.playerId) {
            console.warn('Move rejected:', event.msg)
            if (event.msg === "MOVE_ERROR_INTO_START") {
              showWarning("MOVE_ERROR_INTO_START")
            }
            else if (event.msg === "MOVE_ERROR_NO_VALID_FIELDS") {
              showWarning("MOVE_ERROR_NO_VALID_FIELDS")
            }
            else if (event.msg === "MOVE_ERROR_NO_FIELD_IN_DIRECTION") {
              showWarning("MOVE_ERROR_NO_FIELD_IN_DIRECTION")
            }
            else if (event.msg === "MOVE_ERROR_NO_MOVES_LEFT") {
              showWarning("MOVE_ERROR_NO_MOVES_LEFT")
            }
            else if (event.msg === "MOVE_ERROR_CANT_CHANGE_DIRECTION") {
              showWarning("MOVE_ERROR_CANT_CHANGE_DIRECTION")
            }
            else if (event.msg === "MOVE_ERROR_TOO_MANY_MOVES_FOR_GOAL") {
              showWarning("MOVE_ERROR_TOO_MANY_MOVES_FOR_GOAL")
            }
            else if (event.msg === "MOVE_ERROR_OCCUPIED_BY_OWN_MEEPLE") {
              showWarning("MOVE_ERROR_OCCUPIED_BY_OWN_MEEPLE")
            }
            else if (event.msg === "MEEPLE_IN_DUEL") {
              showWarning("MEEPLE_IN_DUEL")
            }
            return
          }
        } else if (event.type === 'CHEATED') {
          if (event.playerId === gamedata.playerId) {
            showWarning("CHEATED")
            window.setTimeout(cheatRedirect, 2500)
          }
        } else if (event.type === 'MOVE') {
          boardStore.updateMeeplePosition(event.id, event.targetField)
          energy.isEnergyFresh = false;
          if (event.playerId === gamedata.playerId) {
            gamedata.currentDiceRoll = event.remainingMoves
            gamedata.moved = event.moved
            gamedata.currentField = event.targetField
            gamedata.activeMeeple = event.id
          }
        }
        // LOBBY_UPDATE wird immer ausgerufen, wenn sich Werte der Lobby (außer das Board) geupdatet haben. Dazu zählt auch, wenn neue Spieler gejoint sind
        else if (event.type === 'LOBBY_UPDATE') {
          const lobbyUpdate = event as LobbyUpdateEvent
          handleLobbyUpdate(lobbyUpdate)

          //Wenn Energy erfolgreich gesaved wurde wird Frontendseitig der Würfelwurf ebenfalls auf 0 gesetzt und die gamedata.energy geupdated
        } else if (event.type === 'SAVE_ENERGY') {
          energy.maxEnergy = event.maxEnergy
          if (event.playerId === gamedata.playerId) {
            if (gamedata.currentDiceRoll == 0) {
              audioStore.playSfx('eventError')
              return
            }
            audioStore.playSfx('eventEnergySave')
            gamedata.currentDiceRoll = 0
            gamedata.energy = event.energy
            energy.isEnergyFresh = false;
            if (event.hasFullEnergy) {
              energy.isEnergyFull = true
            }
          }
          return
        } else if (event.type === 'SAVE_ENERGY_ERROR') {
          if (event.playerId == gamedata.playerId) {
            console.warn('Energy save rejected:', event.msg)
            showWarning(`SAVE_ENERGY_ERROR`)
          }
          return
        }
        // Wenn energy erfolgreich konsumiert wurde, wird die energy auch frontendseitig resettet
        else if (event.type === 'CONSUME_ENERGY') {
          if (event.playerId == gamedata.playerId) {
            gamedata.energy = event.energy
            energy.isEnergyFull = event.hasFullEnergy
          }
          if (event.meepleId) {
            triggerJumpLocally(event.meepleId)
          }
        } else if (event.type === 'CONSUME_ENERGY_ERROR') {
          if (event.playerId == gamedata.playerId) {
            console.warn('Consume energy rejected:', event.msg)
            //audioStore.playSfx('eventError')
            showWarning(`CONSUME_ENERGY_ERROR`)
          }
        } if (event.type === "MOVE_WITH_LOSS") {
          boardStore.updateMeeplePosition(event.id, event.targetField)
          if (event.playerId === gamedata.playerId) {
            gamedata.currentDiceRoll = event.remainingMoves
            gamedata.moved = event.moved
            gamedata.currentField = event.targetField
            gamedata.activeMeeple = event.id
            showWarning(`REMAINING_MOVES_LOST`)
            console.warn("lost remaining moves")
          }

        }
        if (event.type === "TRIGGER_BARRIER_MOVE") {
          boardStore.updateMeeplePosition(event.meepleId, event.targetField)
          if (event.playerId === gamedata.playerId) {
            gamedata.currentDiceRoll = event.remainingMoves
            gamedata.moved = false;
            gamedata.currentField = event.targetField
            gamedata.activeMeeple = event.id
            openMinimap(event.barrierId, event.playerId, event.currentField)
          }
        }
        if (event.type === "MOVE_BARRIER") {
          console.log("MOVE_BARRIER event received:", event);
          boardStore.updateBarrierPosition(event.id, event.currentField, event.targetField);

          if (minimap.isMovingBarrier && minimap.selectedBarrierId === event.id) {
            minimap.isMovingBarrier = false
          }
        }

        if (event.type === "ROTATE") {
          boardStore.updateMeepleRotation(event.meepleId, event.rotation)
        }

        if (event.type === "REJECTED_BY_BARRIER") {
          //TODO rennen in Barriere visualisieren
          console.log("u ran into barrieeer oh no")
          if (event.playerId === gamedata.playerId) {
            if (event.msg === "MOVE_ERROR_BARRIER_FIELD_OCCUPIED") {
              showWarning("MOVE_ERROR_BARRIER_FIELD_OCCUPIED")
            } else {
              showWarning('REJECTED_BY_BARRIER')
            }
            audioStore.playSfx('impactBarrier')
            gamedata.moved = false
            gamedata.currentDiceRoll = event.remainingMoves
          }
        }
        if (event.type === "DUEL") {

          boardStore.updateMeeplePosition(event.firstMeepleId, event.targetField)

          if (event.playerId === gamedata.playerId) {
            gamedata.currentDiceRoll = event.remainingMoves
          }
          if (event.playerId === gamedata.playerId || event.rivalId === gamedata.playerId) {
            const old = activeDuels[event.duelId] ?? { state: {} }

            activeDuels[event.duelId] = {
              ...old,
              duelId: event.duelId,

              firstMeeple: event.firstMeepleId,
              secondMeeple: event.secondMeepleId,
              targetField: event.targetField,

              miniGameId: event.miniGameId,
              miniGameName: event.miniGameName,
              miniGameType: event.miniGameType,

              timeOut: event.timeOut,

              state: { ...old.state }
            }
            gamedata.moved = false
            document.exitPointerLock()
          }

        }
        if (event.type === "DICE_GAME_UPDATE") {

          const duel = activeDuels[event.duelId]
          if (!duel) return

          duel.state.rollP1 = event.rollP1
          duel.state.rollP2 = event.rollP2
          duel.state.winner = event.winner
          duel.state.finished = event.finished
        }
        if (event.type === "ROCK_PAPER_SCISSORS_GAME_UPDATE") {

          const duel = activeDuels[event.duelId]
          if (!duel) return

          duel.state.moveP1 = event.moveP1
          duel.state.moveP2 = event.moveP2
          duel.state.winner = event.winner
          duel.state.finished = event.finished
        }
        if (event.type === "SLOT_MACHINE_GAME_UPDATE") {
          console.log("SLOT_MACHINE_GAME_UPDATE received:", event)
          const duel = activeDuels[event.duelId]
          if (!duel) {
            console.log("Duel not found for ID:", event.duelId)
            return
          }

          console.log("Updating duel state:", {
            resultP1: event.resultPlayer1,
            resultP2: event.resultPlayer2,
            resultComp: event.resultComp
          })

          duel.state.resultP1 = event.resultPlayer1
          duel.state.resultP2 = event.resultPlayer2
          duel.state.resultComp = event.resultComp
          duel.state.winner = event.winner
          duel.state.jackpot = event.jackpot
          duel.state.finished = event.finished

          if (gamedata.playerId === event.winner && event.jackpot) {
            gamedata.energy = event.jackpotEnergy
            showSuccess("MINIGAME_SLOT_JACKPOT_SUCCESS_MESSAGE")
          }
        }

        if (event.type === "BALLOON_GAME_UPDATE") {
          const duel = activeDuels[event.duelId]
          if (!duel) return

          duel.state.phasePlayer1 = event.phasePlayer1
          duel.state.phasePlayer2 = event.phasePlayer2
          duel.state.winner = event.winner
          duel.state.finished = event.finished
        }

        if (event.type === "MATH_GAME_UPDATE") {
          const duel = activeDuels[event.duelId]
          if (!duel) return

          console.log(event)
          duel.state.player1 = event.player1
          duel.state.player2 = event.player2
          duel.state.p1Value = event.p1Value
          duel.state.p2Value = event.p2Value
          duel.state.termRepresentation = event.termRepresentation
          duel.state.termValue = event.termValue
          duel.state.winner = event.winner
          duel.state.finished = event.finished
        }

        if (event.type === "QUIZ_GAME_UPDATE") {
          const duel = activeDuels[event.duelId]

          if (!duel) return
          duel.questionDTO = event.questionDTO
          duel.state.question = event.questionDTO?.question
          duel.state.answers = event.questionDTO?.answers
          duel.state.correctAnswer = event.correctAnswer ?? -1
          duel.state.winner = event.winner
          duel.state.finished = event.finished

        }

        if (event.type === "COLORBRAIN_GAME_UPDATE") {
          if (!activeDuels[event.duelId]) {
            activeDuels[event.duelId] = { duelId: event.duelId, state: {} }
          }

          const duel = activeDuels[event.duelId]

          duel.state.player1Pick = event.player1Pick
          duel.state.player2Pick = event.player2Pick
          duel.selectedColors = event.selectedColors
          duel.state.winner = event.winner
          duel.state.finished = event.finished
        }
        if (event.type === "MONKEY_TYPE_GAME_UPDATE") {
          console.log("Event details:", {
            duelId: event.duelId,
            targetWord: event.targetWord,
            targetWordLength: event.targetWord?.length,
            player1Input: event.player1Input,
            player2Input: event.player2Input
          })

          const duel = activeDuels[event.duelId]
          if (!duel) {
            return
          }

          duel.state = {
            ...duel.state,
            targetWord: event.targetWord || "",
            player1: event.player1,
            player2: event.player2,
            player1Progress: event.player1Progress,
            player2Progress: event.player2Progress,
            winner: event.winner,
            finished: event.finished,
            startedAt: event.startedAt
          }
        }
        if (event.type === "WIN") {
          boardStore.updateMeeplePosition(event.meepleId, event.targetField)
          gamedata.currentDiceRoll = 0
          gameFinished.value = true
          winnerName.value = event.playerName
          winnerColor.value = event.playerColor
        }
        if (event.type === "BARRIER_MOVE_ERROR") {
          if (minimap.isMovingBarrier) {
            console.warn("Barriermove rejected:", event.msg)
            if (event.msg === "MOVE_BARRIER_REJECTED_START_OR_END") {
              showWarning("MOVE_BARRIER_REJECTED_START_OR_END")
              minimap.isMiniMapOpen = true
              minimap.selectedFieldId = ""
              minimap.occupancyByFieldId = buildOccupancySnapshot()
            }
            else if (event.msg === "MOVE_BARRIER_OCCUPIED") {
              showWarning("MOVE_BARRIER_OCCUPIED")
              minimap.isMiniMapOpen = true
              minimap.selectedFieldId = ""
              minimap.occupancyByFieldId = buildOccupancySnapshot()
            }
          }
        }

        // SPIEL STARTET
        else if (event.type === 'GAME_START') {
          const event = JSON.parse(message.body)
          console.log('FULL EVENT:', event)

          console.log('Spiel startet')
          router.push({ name: 'game' })
        }
      })
    }
    stompclient.onDisconnect = () => {
      /* Verbindung abgebaut*/
      console.log('Disconnected')
    }
    // Verbindung zum Broker aufbauen
    stompclient.activate()
  }


  /**
   * redirected den Spieler zur Wikipedia Seite von Cheat
   */
  function cheatRedirect() {
    window.location.replace('https://de.wikipedia.org/wiki/Cheat_(Computerspiele)')
  }

  /**
  * Synchronisiert energiebezogene Zustände des eigenen Spielers aus dem aktuellen Lobby-State.
  *
  * <p>
  * Diese Funktion extrahiert den eigenen Spieler aus der übergebenen {@link Lobby}
  * anhand der {@code playerId} und übernimmt dessen energierelevante Werte in den
  * lokalen Pinia-Store.
  * </p>
  *
  * <p>
  * Konkret werden:
  * <ul>
  *   <li>die maximale Energie ({@code maxEnergy}) einmalig aus dem Backend übernommen</li>
  *   <li>der Status {@code isEnergyFull} basierend auf aktueller und maximaler Energie berechnet</li>
  * </ul>
  * </p>
  *
  * <p>
  * Die Funktion wird sowohl beim initialen Lobby-Join als auch bei jedem
  * {@code LOBBY_UPDATE}-Event aufgerufen, um sicherzustellen, dass der Frontend-State
  * stets konsistent mit dem Backend bleibt.
  * </p>
  *
  * <p>
  * Falls der eigene Spieler noch nicht in der Lobby vorhanden ist (z. B. während
  * früher Initialisierungsphasen), wird die Funktion ohne Seiteneffekte beendet.
  * </p>
  *
  * @param lobby Aktueller Lobby-Zustand vom Backend
  */
  function syncOwnPlayerEnergy(lobby: Lobby) {
    const ownPlayer = lobby.players.find(
      p => p.id === gamedata.playerId
    )

    if (!ownPlayer) return

    console.log('Own player:', ownPlayer)
    if (ownPlayer.maxEnergy !== undefined) {
      energy.maxEnergy = ownPlayer.maxEnergy
    }

    energy.isEnergyFull = gamedata.energy >= energy.maxEnergy
  }

  /**
   * Joint eine Lobby mit der angegebenen Id und startet den WebSocket zum ständigen synchronisieren von Daten.
   * @param lobbyId UUID der beizutretenen Lobby. 'random', um einer zufälligen Lobby beizutreten oder eine neue zu erstellen, sollte keine freie verfügbar sein.
   * @param username String des username des Spielers
   */
  async function joinLobby(lobbyId: string = 'random', username: string = '') {
    console.log('Start receiving Gameboard Data...')
    try {
      if (lobbyId == null) lobbyId = 'random'
      const url = '/api/lobby/join/' + lobbyId + '?username=' + encodeURIComponent(username)
      const resp = await fetch(url)
      if (!resp.ok) {
        console.error('Error while recieving Data:\n', resp.statusText)
        throw new Error(resp.statusText)
      }
      const responseMsg = await resp.json()
      handleLobbyJoin(responseMsg);
    } catch (error_) {
      console.log(error_)
    }
  }

  /**
   * Erstellt eine neue Lobby und joint dieser direkt
   */
  async function createJoinLobby() {
    console.log('Start receiving Gameboard Data...')
    try {
      const resp = await fetch('/api/lobby/create')
      if (!resp.ok) {
        console.error('Error while recieving Data:\n', resp.statusText)
        throw new Error(resp.statusText)
      }
      const responseMsg = await resp.json()
      handleLobbyJoin(responseMsg);
    } catch (error_) {
      console.log(error_)
    }
  }


  function handleLobbyJoin(responseMsg: any) {
    try {
      console.log(responseMsg.msg)
      gamedata.lobby = responseMsg.lobby as Lobby
      gamedata.playerId = responseMsg.playerId
      gamedata.playerToken = responseMsg.playerToken

      syncOwnPlayerEnergy(gamedata.lobby)

      startMilefizLiveUpdate()
    } catch (error_) {
      console.log(error_)
    }
  }

  function handleLobbyUpdate(lobbyUpdate: LobbyUpdateEvent) {
    if (lobbyUpdate.ownPlayerId) gamedata.playerId = lobbyUpdate.ownPlayerId
    if (lobbyUpdate.playerToken) gamedata.playerToken = lobbyUpdate.playerToken
    gamedata.lobby = lobbyUpdate.lobby

    const boardStore = useBoardStore()

    if (lobbyUpdate.lobby?.board) {
      boardStore.board = lobbyUpdate.lobby.board
      boardStore.ok = true

      // Meeple-Positionen neu setzen
      boardStore.meeplePositions = {}

      let foundCurrentField = false

      for (const player of lobbyUpdate.lobby.players ?? []) {
        for (const meeple of player.meeples ?? []) {
          if (meeple.currentFieldId) {
            boardStore.meeplePositions[meeple.id] = meeple.currentFieldId

            if (player.id === gamedata.playerId && !foundCurrentField) {
              gamedata.currentField = meeple.currentFieldId
              gamedata.activeMeeple = meeple.id
              foundCurrentField = true
            }
          }
        }
      }
    }
  }

  /**
   * Sendet einen "Spiel starten"-Befehl an den Server.
   * 
   * Diese Funktion wird aufgerufen, wenn der Lobby-Leader im Frontend den "Spiel starten" Button klickt.
   * 
   * Ablauf:
   *  1. Prüft, ob der STOMP Client verbunden ist
   *  2. Prüft nach einer gültigen Lobby ID
   *  3. Sende einen StartGameCommand mit eigener PlayerID an den Server-Endpunkt `/app/milefiz/lobby/${gamedata.lobby?.id}/startGame`
   * 
   * WS empfängt Serverantwort und verarbeitet diese als `GAME_START` Event weiter (--> Weiterleitung an GameView)
   * @returns void
   */
  function startGameCommand() {
    if (!stompclient || !stompclient.connected) {
      console.error('Cannot start game commnand: STOMP client not connected.')
      return
    }

    if (!gamedata.lobby?.id) {
      console.error('Cannot start game command.')
      return
    }

    try {
      stompclient.publish({
        destination: `/app/milefiz/lobby/${gamedata.lobby?.id}/startGame`,
        body: JSON.stringify({
          playerId: gamedata.playerId,
        })
      })
      console.log('Start game command sent for all players in lobby:', gamedata.lobby?.id)
    } catch (err) {
      console.error('Error sending start game command:', err)
    }
  }

  /**
  * Sendet eine Rotationsänderung eines Meeples an den Spielserver.
  *
  * Diese Funktion wird aufgerufen, wenn sich die Blickrichtung des
  * aktiven Meeples ändert.
  *
  * Die Rotation wird als RotationCommand an den Server gesendet
  * und anschließend an alle Clients der Lobby weiterverteilt,
  * um die Blickrichtung des Meeples visuell zu synchronisieren.
  *
  * Ablauf:
  * 1. Prüft, ob der STOMP-Client verbunden ist
  * 2. Erstellt ein RotationCommand mit Meeple-ID und Y-Rotation
  * 3. Serialisiert das Kommando als JSON
  * 4. Sendet die Nachricht an den WebSocket-Endpunkt /rotate
  *
  * @param meepleId  die eindeutige ID des Meeples, dessen Rotation geändert wurde
  * @param rotation die neue Y-Rotation des Meeples
  */
  function sendMeepleRotation(meepleId: string, rotation: number) {
    if (!stompclient || !stompclient.connected) {
      console.error('Cannot send move: STOMP client not connected.')
      return
    }

    const rtnCmd: RotationCommand = { meepleId, rotation }

    const body = JSON.stringify(rtnCmd)

    const DEST_APP = '/app/milefiz/lobby/' + gamedata.lobby?.id

    try {
      stompclient.publish({
        destination: DEST_APP + '/rotate',
        body,
      })
    } catch (err) {
      console.error('Error rotating:', err)
    }
  }

  /**
   * Sendet eine Bewegungsaktion (Move) an den Spielserver.
   *
   * Wird aufgerufen, wenn der Spieler im Frontend eine Bewegung
   * durchführt.
   *
   * Erstellt ein `MovementCommand`-Objekt mit Meeple-ID und Bewegungsrichtung
   * und veröffentlicht es über den STOMP-Endpunkt `/app/milefiz/lobby/{lobbyId}`.
   *
   * Ablauf:
   * 1. Verbindung prüfen – Abbruch, falls STOMP-Client nicht verbunden ist.
   * 2. Move-Daten serialisieren (`JSON.stringify`).
   * 3. Nachricht an den Server senden.
   *
   * @param meepleId - Eindeutige ID der Spielfigur, die bewegt werden soll
   * @param direction - Bewegungsrichtung (z. B. "NORTH", "SOUTH", "EAST", "WEST")
   */
  function sendMove(meepleId: string, direction: Direction) {
    if (!stompclient || !stompclient.connected) {
      console.error('Cannot send move: STOMP client not connected.')
      return
    }

    const moveCmd: MovementCommand = { meepleId, direction }

    const body = JSON.stringify(moveCmd)

    const DEST_APP = '/app/milefiz/lobby/' + gamedata.lobby?.id

    try {
      stompclient.publish({
        destination: DEST_APP + '/move',
        body,
      })
      console.log('Move sent:', body)
    } catch (err) {
      console.error('Error sending move:', err)
    }
  }

  function sendLobbyMessage(destination: string, payload: any) {
    if (!stompclient || !stompclient.connected) {
      console.error('Cannot update lobby settings: STOMP client not connected.')
      return
    }

    if (!gamedata.lobby?.id) {
      console.error('Cannot send lobby message: Missing lobbyId')
      return
    }
    try {
      stompclient.publish({
        destination: destination,
        body: JSON.stringify(payload),
      })
      console.log('Lobby message sent:', payload)
    } catch (err) {
      console.error('Error sending lobby message:', err)

    }
  }

  /**
   * Sendet ein Kommando zum Verschieben einer Barriere an den Spielserver.
   *
   * Diese Funktion wird aufgerufen, wenn ein Spieler eine Barriere
   * auf ein anderes Spielfeld bewegen möchte.
   *
   * Das MoveBarrierCommand wird an den Server gesendet und dort
   * validiert. Bei Erfolg wird die Barrierenbewegung an alle Clients
   * der Lobby broadcastet.
   *
   * Ablauf:
   * 1. Prüft, ob der STOMP-Client verbunden ist
   * 2. Erstellt ein MoveBarrierCommand mit Barrieren-ID und Ziel-Feld-ID
   * 3. Serialisiert das Kommando als JSON
   * 4. Sendet die Nachricht an den WebSocket-Endpunkt /movebarrier
   *
   * @param barrierId     die eindeutige ID der zu bewegenden Barriere
   * @param targetFieldId die ID des Spielfelds, auf das die Barriere
   *                      verschoben werden soll
   */
  function moveBarrier(barrierId: string, targetFieldId: string) {
    if (!stompclient || !stompclient.connected) {
      console.error("Cannot send move: STOMP client not connected.")
      return
    }
    const moveBarrCmd: MoveBarrierCommand = { barrierId, targetFieldId };
    const body = JSON.stringify(moveBarrCmd)
    const DEST_APP = '/app/milefiz/lobby/' + gamedata.lobby?.id

    try {
      stompclient.publish({
        destination: DEST_APP + "/movebarrier",
        body,
      })
      console.log("Move sent:", body)
    } catch (err) {
      console.error("Error sending move:", err)
    }
  }

  /**
   * Baut einen vollständigen Occupancy-Snapshot für alle Felder des Boards.
   * Markiert:
   * - FREE: leere Felder
   * - INVALID_START / INVALID_END: Start- und Zielfelder
   * - OCCUPIED: Felder mit Sperren oder fremden Meeples
   * - OWN_MEEPLE: Felder mit eigenen Meeples
   * @returns {Record<string, Occupancy>} Mapping von Feld-ID zu Occupancy-Status
   */
  function buildOccupancySnapshot(): Record<string, Occupancy> {
    const boardStore = useBoardStore()
    const board = boardStore.board
    const lobby = gamedata.lobby

    if (!board) {
      console.log('[minimap] buildOccupancySnapshot: board not loaded.')
      return {}
    }

    // Alle Felder zunächst auf FREE setzen
    const occ: Record<string, Occupancy> = Object.fromEntries(
      board.fields.map(f => [f.id, 'FREE' as Occupancy])
    )

    // Zunächst Start und Zielfelder als INVALID markieren, dann überprüfen, ob ein Feld bereits durch eine Sperre oder fremdes Meeple besetzt ist 
    for (const f of board.fields) {
      if (f.barrier || f.type != 'NORMAL') occ[f.id] = 'OCCUPIED'
    }

    // Wenn lobby fehlt, können own vs foreign meeples nicht unterschieden werden -> nur Barrieren markieren
    if (!lobby) {
      console.log('[minimap] buildOccupancySnapshot: lobby not available.')
      return occ
    }

    // Eigene Meeples auf OWN_MEEPLE setzen
    const ownPlayer = lobby.players.find(p => p.id === gamedata.playerId)
    const ownMeepleIds = new Set<string>(
      (ownPlayer!.meeples ?? []).map(m => m.id)
    )

    // Meeples auf Felder aus BoardStore abbilden
    for (const [meepleId, fieldId] of Object.entries(boardStore.meeplePositions)) {
      if (!fieldId) continue
      if (!(fieldId in occ)) continue //Falls FieldId nicht im Board existiert

      if (ownMeepleIds.has(meepleId)) {
        occ[fieldId] = 'OWN_MEEPLE'
      } else {
        if (occ[fieldId] !== 'OWN_MEEPLE') {
          occ[fieldId] = 'OCCUPIED'
        }
      }
    }

    return occ
  }


  /**
   * Öffnet das Minimap Pop-up, um Sperren umzuplatzieren.
   * Initialisiert Farbe, Status, ausgewählte Barrier-ID und Occupancy-Snapshot.
   * @param {string} barrierId ID der zu verschiebenden Sperre
   * @param {string} playerId ID des Spielers, der die Aktion ausgelöst hat
   */
  function openMinimap(barrierId: string, playerId: string, currentPositon: string) {
    if (playerId === gamedata.playerId) {
      minimap.ownColor = (getPlayerColor(playerId) ?? "RED") as any
      minimap.isMiniMapOpen = true;
      minimap.selectedFieldId = ''
      minimap.selectedBarrierId = barrierId
      minimap.isMovingBarrier = true
      minimap.currentPosition = currentPositon

      minimap.occupancyByFieldId = buildOccupancySnapshot()


      const occupied = Object.entries(minimap.occupancyByFieldId)
        .filter(([, v]) => v === 'OCCUPIED')
        .map(([k]) => k)

      const own = Object.entries(minimap.occupancyByFieldId)
        .filter(([, v]) => v === 'OWN_MEEPLE')
        .map(([k]) => k)

      console.log('[minimap] snapshot built',
        { occupiedCount: occupied.length, ownCount: own.length }
      )
    }

  }

  /**
   * Bestätigt die aktuell in der Minimap gewählte Zielposition für die Sperre.
   * Führt den Barrier-Move aus und schließt anschließend die Minimap.
   */
  function confirmMinimapSelection() {
    if (!minimap.selectedFieldId) return

    moveBarrier(minimap.selectedBarrierId, minimap.selectedFieldId)
    minimap.isMiniMapOpen = false

  }

  /**
   * Bestätigt die aktuell in der Minimap gewählte Zielposition für die Sperre.
   * Führt den Barrier-Move aus und schließt anschließend die Minimap.
   */
  function selectMinimapField(fieldId: string) {
    minimap.selectedFieldId = fieldId
  }

  /**
   * Ermittelt die Spielerfarbe aus der Lobby für eine gegebene Spieler-ID.
   * @param {string} playerId ID des Spielers
   * @returns {string | null} Farbcode des Spielers oder null, falls nicht gefunden / Lobby nicht verfügbar
   */
  function getPlayerColor(playerId: string): string | null {
    const lobby = gamedata.lobby
    if (!lobby) return null
    return lobby.players.find(p => p.id === playerId)?.color ?? null
  }

  /**
   * Zu Demo-Zwecken
   * Sendet einen ToggleMinigameSelectionMode Befehl ans Backend, um zwischen random
   * und inorder Auswahl zu wechseln.
   * 
   */
  function sendToggleSelectionMode() {
    if (!stompclient || !stompclient.connected) {
      console.error('Cannot toggle selection mode: STOMP client not connected.')
      return
    }

    if (!gamedata.lobby?.id || !gamedata.playerId) {
      console.error('Cannot roll dice: Missing lobbyId or playerId')
      return
    }
    gamedata.selectRandomMinigame = !gamedata.selectRandomMinigame
    const toggleSelectionModeCommand: any = {
      selectRandomMinigame: gamedata.selectRandomMinigame
    }

    try {
      stompclient.publish({
        destination: `/app/milefiz/lobby/${gamedata.lobby?.id}/toggleMinigameSelectionMode`,
        body: JSON.stringify(toggleSelectionModeCommand)
      })
      console.log('toggleSelectionModeCommand sent, selectRandomMinigame:', gamedata.selectRandomMinigame)
    } catch (err) {
      console.error('Error sending toggleSelectionModeCommand:', err)
    }

    gamedata.selectRandomMinigame ? showSuccess(`MINIGAME_SELECTION_MODE_RANDOM`) : showSuccess(`MINIGAME_SELECTION_MODE_INORDER`)
  }

  function sendRollDice(requestedValue?: number) {
    if (!stompclient || !stompclient.connected) {
      console.error('Cannot roll dice: STOMP client not connected.')
      return
    }

    if (!gamedata.lobby?.id || !gamedata.playerId) {
      console.error('Cannot roll dice: Missing lobbyId or playerId')
      return
    }

    const rollDiceCommand: any = {
      playerId: gamedata.playerId,
    }

    if (requestedValue !== undefined) {
      rollDiceCommand.requestedValue = requestedValue
    }

    try {
      stompclient.publish({
        destination: `/app/milefiz/lobby/${gamedata.lobby?.id}/rollDice`,
        body: JSON.stringify(rollDiceCommand),
      })
      console.log('Roll dice command sent for player:', gamedata.playerId)
    } catch (err) {
      console.error('Error sending roll dice command:', err)
    }
  }



  /**
   * Prüft, ob der eigene Spieler Leader der aktuellen Lobby ist
   *
   * Nutzt die in der Lobby vorhandene Spielerliste und vergleicht
   * die eigene playerId mit dem entsprechenden Eintrag.
   */
  function isOwnLeader(): boolean {
    return getOwnPlayer()?.leader ?? false
  }

  function getOwnPlayer(): Player | undefined {
    if (!gamedata.lobby || !gamedata.playerId) return

    // Spieler abgleichen mit eigenen Daten
    const me = gamedata.lobby.players?.find((p: Player) => p.id === gamedata.playerId) as Player
    return me
  }

  /* Sendeteinen Energie - Speichern - Befehl an den Spielserver
   *
  * Wird aufgerufen, wenn der Spieler im Fronten die gewürfelte Zahl als Energie speichern möchte.
  *
  * Erstellt ein EnergyCommand - Objekt mit der Spieler - ID und veröffentlicht es über den STOMP - Endpunkt`/app/milefiz/lobby/{lobbyId}/saveEnergy`.
  *
  * Ablauf:
  * 1. Verbindung prüfen – Abbruch, falls STOMP - Client nicht verbunden ist.
  * 2. Lobby - ID und Spieler - ID validieren – Abbruch bei fehlenden Daten.
  * 3. Energy - Command serialisieren(`JSON.stringify`).
  * 4. Nachricht an den Server senden.
  *
  * @returns void
  * @throws Loggt Fehler in der Konsole und bricht Ausführung ab
   *
  * @author Elisabeth Gehdt
   */
  function sendEnergySave() {
    if (!stompclient || !stompclient.connected) {
      console.error('Cannot save energy: STOMP client not connected.')
      return
    }

    if (!gamedata.lobby?.id || !gamedata.playerId) {
      console.error('Cannot save energy: Missing lobbyId or playerId')
      return
    }

    const energySaveCommand: EnergyCommand = { playerId: gamedata.playerId, meepleId: "" }

    const body = JSON.stringify(energySaveCommand)

    const DEST_APP = '/app/milefiz/lobby/' + gamedata.lobby?.id

    try {
      stompclient.publish({
        destination: DEST_APP + '/saveEnergy',
        body,
      })
      console.log('Energy saved:', body)
    } catch (err) {
      console.error('Error saving energy:', err)
    }
  }

  /* Sendet eine Energie-Verbrauchen-Anfrage an den Spielserver.
  *
  * Wird aufgerufen, wenn der Spieler springen möchte.
  *
  * Erstellt ein EnergyCommand-Objekt mit der Spieler-ID und veröffentlicht es über den STOMP-Endpunkt `/app/milefiz/lobby/{lobbyId}/consumeEnergy`.
  *
  * Ablauf:
  * 1. Verbindung prüfen – Abbruch, falls STOMP-Client nicht verbunden ist.
  * 2. Lobby-ID und Spieler-ID validieren – Abbruch bei fehlenden Daten.
  * 3. Energy-Command serialisieren(`JSON.stringify`).
  * 4. Nachricht an den Server senden.
  *
  * @returns void
  * @throws Loggt Fehler in der Konsole und bricht Ausführung ab
   *
  * @author Kevin Tran
   */
  function sendEnergyConsume(meepleId: string) {
    if (gamedata.isJumping) return
    if (!stompclient || !stompclient.connected) {
      console.error('Cannot consume energy: STOMP client not connected.')
      return
    }

    if (!gamedata.lobby?.id || !gamedata.playerId) {
      console.error('Cannot consume energy: Missing lobbyId or playerId')
      return
    }
    gamedata.isJumping = true
    const energyConsumeCommand: EnergyCommand = { playerId: gamedata.playerId, meepleId: meepleId }
    const body = JSON.stringify(energyConsumeCommand)

    const DEST_APP = '/app/milefiz/lobby/' + gamedata.lobby?.id

    try {
      stompclient.publish({
        destination: DEST_APP + '/consumeEnergy',
        body,
      })
      console.log('Energy consume:', body)
    } catch (err) {
      console.error('Error consuming energy:', err)
    }
  }

  /**
   * Prueft welche Farbe der Gewinner hat und gibt die entsprechende Koerper und Augenfarbe des Meeples zuruek
   * @returns Koerper und Augenfarbe des Meeples vom Gewinner
   */
  function getWinnerColor() {
    if (winnerColor.value == 'RED') {
      return playerColors.RED
    }

    if (winnerColor.value == 'GREEN') {
      return playerColors.GREEN
    }

    if (winnerColor.value == 'BLUE') {
      return playerColors.BLUE
    }

    if (winnerColor.value == 'YELLOW') {
      return playerColors.YELLOW
    }
  }


  /**
   * Pop Up Menu Funktionen
  */

  // Oeffnet PopUp Menu
  function openPopUpMenu() {
    popUpMenuOpen.value = true
  }

  // Schließt PopUp Menu
  function closePopUpMenu() {
    popUpMenuOpen.value = false
    popUpSettingsOpen.value = false
    popUpTutorialOpen.value = false
  }

  // Oeffnet PopUp Einstellungen
  function openPopUpSettings() {
    popUpSettingsOpen.value = true
  }

  // Schließt PopUp Einstellungen
  function closePopUpSettings() {
    popUpSettingsOpen.value = false
  }

  // Oeffnet PopUp Tutorial
  function openPopUpTutorial() {
    popUpTutorialOpen.value = true
  }

  // Schließt PopUp Tutorial
  function closePopUpTutorial() {
    popUpTutorialOpen.value = false
  }

  /**
   * Trennt die WebSocket-Verbindung und setzt den pinia-Store zurück
   */
  function disconnectAndReset() {
    // WebSocket-Verbindung trennen
    if (stompclient && stompclient.connected) {
      stompclient.deactivate()
      stompclient = null
    }

    // Store-State zurücksetzen
    gamedata.playerId = ''
    gamedata.playerToken = ''
    gamedata.energy = 0
    gamedata.currentDiceRoll = undefined
    gamedata.lobby = null

    cooldown.remainingSeconds = 0
    cooldown.active = false

    energy.maxEnergy = 0
    energy.isEnergyFull = false
    energy.isEnergyFresh = false

    gameFinished.value = false
    winnerName.value = ''
    winnerColor.value = ''

    popUpMenuOpen.value = false
    popUpSettingsOpen.value = false

    // Minimap State zurücksetzen
    minimap.isMiniMapOpen = false
    minimap.isMovingBarrier = false
    minimap.selectedBarrierId = ""
    minimap.selectedFieldId = ""
    minimap.occupancyByFieldId = {}
    minimap.ownColor = "RED"

    Object.keys(activeDuels).forEach(key => {
      delete activeDuels[key]
    })

    const boardStore = useBoardStore()

    boardStore.resetBoardStore()


    console.log('Store reset complete')
  }



  return {
    gamedata,
    isJoined,
    startMilefizLiveUpdate,
    sendToggleSelectionMode,
    sendRollDice,
    sendLobbyMessage,
    joinLobby,
    createJoinLobby,
    cooldown,
    energy,
    sendMove,
    sendEnergySave,
    sendEnergyConsume,
    startGameCommand,
    getOwnPlayer,
    isOwnLeader,
    disconnectAndReset,
    winnerName,
    gameFinished,
    getWinnerColor,
    popUpMenuOpen,
    popUpSettingsOpen,
    popUpTutorialOpen,
    openPopUpMenu,
    closePopUpMenu,
    openPopUpSettings,
    closePopUpSettings,
    openPopUpTutorial,
    closePopUpTutorial,
    activeDuels,
    minimap,
    openMinimap,
    confirmMinimapSelection,
    selectMinimapField,
    jumpTrigger,
    triggerJumpLocally,
    sendMeepleRotation,
  }
})
