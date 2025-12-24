import { defineStore } from 'pinia'
import { reactive, watch } from 'vue'
import { audioEngine } from '@/composables/audioEngine'

export type AudioChannelKey = 'music' | 'ambient' | 'sfx'

type AudioChannel = {
  volume: number    // 0–100
  muted: boolean    //true = muted, false = unmuted
}

export const useAudioStore = defineStore('audio', () => {

  const channels = reactive<Record<AudioChannelKey, AudioChannel>>({
    music:  { volume: 5, muted: false },
    ambient:{ volume: 15, muted: false },
    sfx:    { volume: 65, muted: false },
  })

  function setVolume(channel: AudioChannelKey, volume: number) {
    channels[channel].volume = volume
  }

  function toggleMute(channel: AudioChannelKey) {
    channels[channel].muted = !channels[channel].muted
  }

  function setMute(channel: AudioChannelKey, muted: boolean) {
    channels[channel].muted = muted
  }

  watch(() => channels.music.volume, (v) => {
    audioEngine.musicGain.gain.value = channels.music.muted ? 0 : v / 100
  })

  watch(() => channels.music.muted, (m) => {
    audioEngine.musicGain.gain.value = m ? 0 : channels.music.volume / 100
  })

  watch(() => channels.ambient.volume, (v) => {
    audioEngine.ambientGain.gain.value = channels.ambient.muted ? 0 : v / 100
  })

  watch(() => channels.ambient.muted, (m) => {
    audioEngine.ambientGain.gain.value = m ? 0 : channels.ambient.volume / 100
  })

  //Urheberfreie Soundeffekte von:
  //OpenGameArt.org
  //pixabay.com/sound-effects/
  const sfxMap: Record<string, string> = {
    //UI Sounds
    click: '/audio/ui/ClickSound.mp3?v=2',
    hover: '/audio/ui/HoverSound.mp3?v=2',
    joinGame: '/audio/ui/JoinGame.mp3?v=2',
    copyLobby: '/audio/ui/CopyLobby.mp3?v=2',
    gameHUD: '/audio/ui/GameHUD.mp3?v=2',
    errorMessage: '/audio/ui/ErrorMessage.mp3?v=2',

    //Event Sounds
    win: '/audio/ui/WinSound.wav?v=2',

    //Movement Sounds
    meepleJump: '/audio/meeple/CartoonJump.mp3?v=2',
    meepleMove: '/audio/meeple/WalkOnGrass.mp3?v=2',
    impactBarrier: '/audio/meeple/ImpactBarrier2.mp3?v=2',

    //Music
    zambolinoCuckoo: '/audio/music/ZambolinoCuckoo.mp3',
    ariaMath: '/audio/music/AriaMath.mp3',

    //Ambient
    ambientForest05: '/audio/ambient/ForestAmbient05.mp3',
    ambientForest04: '/audio/ambient/ForestAmbient04.mp3',
  }

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
