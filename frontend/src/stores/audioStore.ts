import { defineStore } from 'pinia'
import { reactive } from 'vue'

export type AudioChannelKey = 'music' | 'ambient' | 'sfx'

type AudioChannel = {
  volume: number    // 0–100
  muted: boolean    //true = muted, false = unmuted
}

export const useAudioStore = defineStore('audio', () => {

  const channels = reactive<Record<AudioChannelKey, AudioChannel>>({
    music:  { volume: 50, muted: false },
    ambient:{ volume: 70, muted: false },
    sfx:    { volume: 50, muted: false },
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

  //Urheberfreie Soundeffekte von:
  //OpenGameArt.org
  //pixabay.com/sound-effects/
  const sfxMap: Record<string, string> = {
    click: '/audio/ui/ClickSound.mp3',
    win: '/audio/ui/WinSound.wav',
    copyLobby: '/audio/ui/CopyLobby.mp3',
    gameHUD: '/audio/ui/GameHUD.mp3',
    errorMessage: '/audio/ui/ErrorMessage.mp3',

    meepleJump: '/audio/meeple/CartoonJump.mp3',
    meepleMove: '/audio/meeple/WalkOnGrass.mp3',
    impactBarrier: '/audio/meeple/ImpactBarrier2.mp3',
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
