import { useAudioStore } from '@/stores/audioStore'

type Vec3 = { x: number; y: number; z: number }

class AudioEngine {
  public context = new AudioContext()

  public musicGain = this.context.createGain()
  public ambientGain = this.context.createGain()

  public musicSource: AudioBufferSourceNode | null = null
  public ambientSource: AudioBufferSourceNode | null = null

  constructor() {
    this.musicGain.connect(this.context.destination)
    this.ambientGain.connect(this.context.destination)
  }

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

    panner.coneInnerAngle = 360
    panner.coneOuterAngle = 360
    panner.coneOuterGain = 1

    source.connect(gain)
    gain.connect(panner)
    panner.connect(this.context.destination)

    source.start()
  }
  
  async playMusic(key: string) {
    const audioStore = useAudioStore()
    const music = audioStore.channels.music
    if (music.muted) return

    const url = audioStore.sfxMap[key]  
    if (!url) return console.warn(`[AudioEngine] Unknown music key: ${key}`)

    if (this.musicSource) this.musicSource.stop()

    const res = await fetch(url)
    const array = await res.arrayBuffer()
    const buffer = await this.context.decodeAudioData(array)

    const src = this.context.createBufferSource()
    src.buffer = buffer
    src.loop = true
    src.connect(this.musicGain)

    this.musicGain.gain.value = music.volume / 100

    await this.context.resume()
    src.start()

    this.musicSource = src
  }

  async playAmbient(key: string) {
    const audioStore = useAudioStore()
    const ambient = audioStore.channels.ambient
    if (ambient.muted) return

    const url = audioStore.sfxMap[key]  
    if (!url) return console.warn(`[AudioEngine] Unknown ambient key: ${key}`)

    if (this.ambientSource) this.ambientSource.stop()

    const res = await fetch(url)
    const array = await res.arrayBuffer()
    const buffer = await this.context.decodeAudioData(array)

    const src = this.context.createBufferSource()
    src.buffer = buffer
    src.loop = true
    src.connect(this.ambientGain)

    this.ambientGain.gain.value = ambient.volume / 100 * 0.25

    await this.context.resume()
    src.start()

    this.ambientSource = src
  }
  

}

export const audioEngine = new AudioEngine()
