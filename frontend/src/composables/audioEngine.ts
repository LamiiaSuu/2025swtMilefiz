import { useAudioStore } from '@/stores/audioStore'

type Vec3 = { x: number; y: number; z: number }

class AudioEngine {
  public context = new AudioContext()

  setListenerPosition(x: number, y: number, z: number) {
    const l = this.context.listener
    l.positionX.value = x
    l.positionY.value = y
    l.positionZ.value = z
  }

  async play3D(key: string, position: Vec3) {
    const audioStore = useAudioStore()
    const sfx = audioStore.channels.sfx

    if (sfx.muted) return

    const url = audioStore.sfxMap[key]
    if (!url) {
      console.warn(`[AudioEngine] Unknown sfx key: ${key}`)
      return
    }

    const response = await fetch(url)
    const buffer = await response.arrayBuffer()
    const audioBuffer = await this.context.decodeAudioData(buffer)

    const source = this.context.createBufferSource()
    source.buffer = audioBuffer

    const gain = this.context.createGain()
    gain.gain.value = sfx.volume / 100

    const panner = this.context.createPanner()
    panner.panningModel = 'HRTF'
    panner.distanceModel = 'inverse'
    panner.refDistance = 2
    panner.maxDistance = 14
    panner.rolloffFactor = 0.75

    panner.positionX.value = position.x
    panner.positionY.value = position.y
    panner.positionZ.value = position.z

    source.connect(gain)
    gain.connect(panner)
    panner.connect(this.context.destination)

    source.start()
  }

}

export const audioEngine = new AudioEngine()
