import { defineStore } from 'pinia'
import { reactive, watch } from 'vue'
import { audioEngine } from '@/composables/audioEngine'

/**
 * Verfügbare Audio-Kanäle.
 */
export type AudioChannelKey = 'music' | 'ambient' | 'sfx'

/**
 * Zustand eines einzelnen Audio-Kanals.
 */
type AudioChannel = {
  volume: number    // 0–100
  muted: boolean    //true = muted, false = unmuted
}

/**
 * Globaler Audio-Store.
 *
 * Verantwortlich für:
 * - Lautstärke pro Kanal
 * - Mute pro Kanal
 * - Synchronisierung mit AudioEngine
 * - Verwaltung aller Audio-Ressourcen (sfxMap)
 */
export const useAudioStore = defineStore('audio', () => {

  /**
   * Reaktiver Zustand aller Kanäle.
   */
  const channels = reactive<Record<AudioChannelKey, AudioChannel>>({
    music:  { volume: 50, muted: false },
    ambient:{ volume: 25, muted: false },
    sfx:    { volume: 65, muted: false },
  })

  /**
   * Setzt die Lautstärke eines Kanals.
   */
  function setVolume(channel: AudioChannelKey, volume: number) {
    channels[channel].volume = volume
  }

  /**
   * Wechselt Mute-Zustand eines Kanals.
   */
  function toggleMute(channel: AudioChannelKey) {
    channels[channel].muted = !channels[channel].muted
  }

  /**
   * Setzt Mute explizit.
   */
  function setMute(channel: AudioChannelKey, muted: boolean) {
    channels[channel].muted = muted
  }

  /**
   * Reagiert auf Musik-Lautstärkeänderung.
   */
  watch(() => channels.music.volume, (v) => {
    audioEngine.musicGain.gain.value = channels.music.muted ? 0 : v / 100
  })

  /**
   * Reagiert auf Musik-Mute.
   */
  watch(() => channels.music.muted, (m) => {
    audioEngine.musicGain.gain.value = m ? 0 : channels.music.volume / 100
  })

  /**
   * Reagiert auf Ambient-Lautstärke.
   */
  watch(() => channels.ambient.volume, (v) => {
    audioEngine.ambientGain.gain.value = channels.ambient.muted ? 0 : v / 100
  })

  /**
   * Reagiert auf Ambient-Mute.
   */
  watch(() => channels.ambient.muted, (m) => {
    audioEngine.ambientGain.gain.value = m ? 0 : channels.ambient.volume / 100
  })


  /**
   * Map aller Sounddateien im Spiel.
   * Dient als zentrale Referenz.
   *
   * Urheberfreie Soundeffekte von:
   * OpenGameArt.org
   * pixabay.com/sound-effects/
  */
  const sfxMap: Record<string, string> = {
    //UI Sounds
    click: '/audio/ui/ClickSound.mp3?v=2',
    hover: '/audio/ui/HoverSound.mp3?v=2',
    joinGame: '/audio/ui/JoinGame.mp3?v=2',
    copyLobby: '/audio/ui/CopyLobby.mp3?v=2',
    gameHUD: '/audio/ui/GameHUD.mp3?v=2',
    errorMessage: '/audio/ui/ErrorMessage.mp3?v=2',
    eventError: '/audio/ui/EventError.mp3',
    eventEnergySave: '/audio/ui/SaveEnergy.mp3',

    //Event Sounds
    win: '/audio/ui/WinSound.wav?v=2',

    //Movement Sounds
    meepleJump: '/audio/meeple/CartoonJump.mp3?v=2',
    meepleMove: '/audio/meeple/WalkOnGrass.mp3?v=2',
    impactBarrier: '/audio/meeple/ImpactBarrier2.mp3?v=2',

    //Music
    ariaMath: '/audio/music/AriaMath.mp3',
    cuddleClouds: '/audio/music/CuddleClouds.mp3',
    driftingMemories: '/audio/music/DriftingMemories.mp3',
    eveningHarmony: '/audio/music/EveningHarmony.mp3',
    floatingDream: '/audio/music/FloatingDream.mp3',
    forgottenBiomes: '/audio/music/ForgottenBiomes.mp3',
    gentleBreeze: '/audio/music/GentleBreeze.mp3',
    goldenGleam: '/audio/music/GoldenGleam.mp3',
    polarLights: '/audio/music/PolarLights.mp3',
    strangeWorlds: '/audio/music/StrangeWorlds.mp3',
    sunlightThroughLeaves: '/audio/music/SunlightThroughLeaves.mp3',
    wanderersTale: '/audio/music/WanderersTale.mp3',
    whisperingWoods: '/audio/music/WhisperingWoods.mp3',

    //Ambient
    ambientForest05: '/audio/ambient/ForestAmbient05.mp3',
    ambientForest04: '/audio/ambient/ForestAmbient04.mp3',
  }

  /**
   * Spielt einen UI-/SFX-Sound ab. Gedacht für kurze Sounds die keine Live-Audio-Anpassung benötigen. Sonst AudioEngine verwenden.
   *
   * @param key Key aus sfxMap
   */
  function playSfx(key: keyof typeof sfxMap) {
    const channel = channels.sfx
    if (channel.muted) return

    const audio = new Audio(sfxMap[key])
    audio.volume = channel.volume / 100
    audio.play()
  }

  return {
    channels,
    setVolume,
    toggleMute,
    setMute,
    playSfx,
    sfxMap,
  }
})
