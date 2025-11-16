import { reactive, readonly, computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { Client, type Message } from '@stomp/stompjs'

const wsurl = `ws://${window.location.host}/milefiz`
const DEST = '/topic/milefiz'

let stompclient: Client | null = null

export const useMilefizStore = defineStore('milefizstore', () => {

  // Beispiele für Daten
  const gamedata = reactive<{ id: string; mana: number }>({
    id: "", // UUID vom Spieler
    mana: 100,
  })

  function startMilefizLiveUpdate() {
    console.log('Starting Liveupdater for Milefiz')
    // Nur eine Instanz
    if (stompclient != null && stompclient.connected) {
      return
    }

    stompclient = new Client({ brokerURL: wsurl })
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
      stompclient.subscribe(DEST, (message) => {
        console.log('Message received: ' + message + "\nBody:\n" + message.body)
        // const eventobjekt: IZutatDTD = JSON.parse(message.body)
        // console.log(JSON.stringify(eventobjekt))
        // if (eventobjekt.type === 'DOENER') {
        // updateDoenerListe()
        // }
        // Callback: Nachricht auf DEST empfangen
        // empfangene Nutzdaten in message.body abrufbar,
        // ggf. mit JSON.parse(message.body) zu JS konvertieren
      })
    }
    stompclient.onDisconnect = () => {
      /* Verbindung abgebaut*/
      console.log('Disconnected')
    }
    // Verbindung zum Broker aufbauen
    stompclient.activate()
  }

  function sendSocketMessage(payload: any) {
    if (!stompclient || !stompclient.connected) {
      console.error("Cannot send message: STOMP client not connected.")
      return
    }

    const body = JSON.stringify(payload)

    try {
      stompclient.publish({
        destination: "/app/milefiz",
        body,
      })
      console.log("Message sent to /app/milefiz: " + body)
    } catch (err) {
      console.error("Error sending message:", err)
    }
  }

  return {
    gamedata,
    startMilefizLiveUpdate,
    sendSocketMessage,
  }
})
