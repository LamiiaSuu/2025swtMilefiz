import { reactive, readonly, computed, ref } from 'vue'
import router from '@/router'
import { defineStore } from 'pinia'
import { Client, type Message } from '@stomp/stompjs'
import type { Direction, MoveBarrierCommand, MovementCommand } from "@/types/movement";
import type { EnergyCommand } from '@/types/energy'
import type { LobbyUpdateEvent, Lobby, Player, Meeple } from "@/types/lobbyupdate";
import { useBoardStore } from "./boardStore"
import { generateUUID } from 'three/src/math/MathUtils.js';
import { useErrorHandler } from '@/composables/useErrorHandler';
import { startingbaseColors, playerColors } from '@/types/colorsAssets';
import { useAudioStore } from '@/stores/audioStore'

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

  /** 
   * Gewinndialog
   * @prop {boolean} gameFinished - Wenn 'true' zählt das Spiel als beendet, weil jemand ins Ziel gekommen ist.
   * @prop {string} winnerName    - Name des gewinnenden Spielers.
   * @prop {string} winnerColor   - Farbe des Gewinners
  */
  const gameFinished = ref(false)
  const winnerName = ref<string | null>(null)
  const winnerColor = ref<string | null>(null)

  // Beispiele für Daten
  const gamedata = reactive<{
    playerId: string
    playerToken: string
    energy: number
    isJumping: boolean
    currentDiceRoll?: number
    lobby: Lobby | null
    moved: boolean
  }>({
    playerId: '', // UUID vom eigenen Spieler
    playerToken: '',
    energy: 0, //Energy des Spielers
    isJumping: false, //Flag, ob sich der Spieler in einer Sprungaktion befindet
    currentDiceRoll: undefined, //Würfel ergebnis
    lobby: null, // DummyLobby: 271c95db-3737-496f-9081-ae920e8ebbf7
    moved: false
  })

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
        console.log('Message received: ' + message + '\nBody:\n' + message.body)

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

        // Wenn der Spieler im Moment noch nicht Würfeln darf, weil er noch aktiven Cooldown hat, wird hier die Nachricht abgefangen und die verbleibenden Sekunden werden geupdatet.
        else if (event.type === 'ROLL_DICE_ERROR' && event.playerId === gamedata.playerId) {
          console.log(
            `Player ${event.playerId} still has ${event.seconds} seconds of cooldown to roll their dice!`,
          )
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
          if(event.playerId === gamedata.playerId){
            console.warn('Move rejected:', event.msg)
            if( event.msg === "MOVE_ERROR_INTO_START"){
              showWarning("MOVE_ERROR_INTO_START")
            }
            else if( event.msg === "MOVE_ERROR_NO_FIELD_IN_DIRECTION"){
              showWarning("MOVE_ERROR_NO_FIELD_IN_DIRECTION")
            }
            else if( event.msg === "MOVE_ERROR_NO_MOVES_LEFT"){
              showWarning("MOVE_ERROR_NO_MOVES_LEFT")
            }
            else if( event.msg === "MOVE_ERROR_CANT_CHANGE_DIRECTION"){
              showWarning("MOVE_ERROR_CANT_CHANGE_DIRECTION")
            }
            else if( event.msg === "MOVE_ERROR_TOO_MANY_MOVES_FOR_GOAL"){
              showWarning("MOVE_ERROR_TOO_MANY_MOVES_FOR_GOAL")
            }
            else if( event.msg === "MOVE_ERROR_OCCUPIED_BY_OWN_MEEPLE"){
              showWarning("MOVE_ERROR_OCCUPIED_BY_OWN_MEEPLE")
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
            if(gamedata.currentDiceRoll == 0) {
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
            showWarning(`REMAINING_MOVES_LOST`)
            //TODO moveloss animieren
            console.warn("lost remaining moves")
          }

        }
        if (event.type === "TRIGGER_BARRIER_MOVE") {
          //TODO verschieben der barriere implementieren
          //aktuell einfach random platzhalter uuid
          moveBarrier(event.barrierId, crypto.randomUUID())
          boardStore.updateMeeplePosition(event.meepleId, event.targetField)
          if (event.playerId === gamedata.playerId) {
            gamedata.currentDiceRoll = event.remainingMoves
          }

        }
        if (event.type === "MOVE_BARRIER") {
          console.log("MOVE_BARRIER event received:", event);
          boardStore.updateBarrierPosition(event.barrierId, event.targetField);
        }
        if (event.type === "REJECTED_BY_BARRIER") {
          //TODO rennen in Barriere visualisieren
          console.log("u ran into barrieeer oh no")
          if (event.playerId === gamedata.playerId) {
            audioStore.playSfx('impactBarrier')
            showWarning('REJECTED_BY_BARRIER')
            gamedata.moved = false
            gamedata.currentDiceRoll = event.remainingMoves
          }
        }
        if (event.type === "DUEL") {
          boardStore.updateMeeplePosition(event.firstMeepleId, event.targetField)
          if (event.playerId === gamedata.playerId) {
            gamedata.currentDiceRoll = event.remainingMoves
          }
          //TODO duel zwischen zwei meeples einleiten
        }
        if (event.type === "WIN") {
          boardStore.updateMeeplePosition(event.meepleId, event.targetField)
          gamedata.currentDiceRoll = 0
          gameFinished.value = true
          winnerName.value = event.playerName
          winnerColor.value = event.playerColor
        }
        if (event.type === "BARRIER_MOVE_ERROR") {
          console.warn("Barriermove rejected:", event.msg)
          if( event.msg === "MOVE_BARRIER_REJECTED_START_OR_END"){
            showWarning("MOVE_BARRIER_REJECTED_START_OR_END")
          }
          else if( event.msg === "MOVE_BARRIER_OCCUPIED"){
            showWarning("MOVE_BARRIER_OCCUPIED")
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
  async function joinLobby(lobbyId: string = 'random', username: string = 'Anonymer Kek') {
    console.log('Start receiving Gameboard Data...')
    try {
      if (lobbyId == null) lobbyId = 'random'
      const url = '/api/lobby/join/' + lobbyId + '?username=' + encodeURIComponent(username ?? '')
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

      for (const player of lobbyUpdate.lobby.players ?? []) {
        for (const meeple of player.meeples ?? []) {
          if (meeple.currentFieldId) {
            boardStore.meeplePositions[meeple.id] = meeple.currentFieldId
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
  //TODO tatsächliches moven der Barrier implementieren
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

  function sendRollDice() {
    if (!stompclient || !stompclient.connected) {
      console.error('Cannot roll dice: STOMP client not connected.')
      return
    }

    if (!gamedata.lobby?.id || !gamedata.playerId) {
      console.error('Cannot roll dice: Missing lobbyId or playerId')
      return
    }

    const rollDiceCommand = {
      playerId: gamedata.playerId,
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

    const energySaveCommand: EnergyCommand = { playerId: gamedata.playerId }

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
  function sendEnergyConsume() {
    if (!stompclient || !stompclient.connected) {
      console.error('Cannot save energy: STOMP client not connected.')
      return
    }

    if (!gamedata.lobby?.id || !gamedata.playerId) {
      console.error('Cannot save energy: Missing lobbyId or playerId')
      return
    }
    const energyConsumeCommand: EnergyCommand = { playerId: gamedata.playerId }
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

    const boardStore = useBoardStore()

    boardStore.resetBoardStore()


    console.log('Store reset complete')
  }



  return {
    gamedata,
    isJoined,
    startMilefizLiveUpdate,
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
  }
})
