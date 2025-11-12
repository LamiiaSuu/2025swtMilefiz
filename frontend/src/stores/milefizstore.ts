import { reactive, readonly, computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { Client, type Message } from '@stomp/stompjs'

const wsurl = `ws://${window.location.host}/milefiz`
const DEST = '/topic/milefiz'

let stompclient: Client | null = null

export const useMilefizStore = defineStore('milefizstore', () => {
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
        console.log('Message received: ' + message)
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
  return {
    startMilefizLiveUpdate,
  }
})
