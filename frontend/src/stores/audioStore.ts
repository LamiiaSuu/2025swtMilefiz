import { defineStore } from 'pinia'
import { reactive } from 'vue'

export type AudioChannelKey = 'music' | 'ambient' | 'sfx'

type AudioChannel = {
  volume: number    // 0–100
  muted: boolean    //true = muted, false = unmuted
}

export const useAudioStore = defineStore('audio', () => {

  const channels = reactive<Record<AudioChannelKey, AudioChannel>>({
    music:  { volume: 80, muted: false },
    ambient:{ volume: 70, muted: false },
    sfx:    { volume: 90, muted: false },
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

  return {
    channels,
    setVolume,
    toggleMute,
    setMute,
  }
})
