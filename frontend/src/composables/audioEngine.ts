import { useAudioStore } from '@/stores/audioStore'

type Vec3 = { x: number; y: number; z: number }

class AudioEngine {
  public context = new AudioContext()

  public musicGain = this.context.createGain()
  public ambientGain = this.context.createGain()

  public musicSource: AudioBufferSourceNode | null = null
  public ambientSource: AudioBufferSourceNode | null = null

  public ambientPlaylist: string[] = []
  public ambientIndex = 0
  public ambientLoop = true

  public musicPlaylist: string[] = []
  public musicIndex = 0
  public musicLoop = true

  constructor() {
    this.musicGain.connect(this.context.destination)
    this.ambientGain.connect(this.context.destination)
  }

  async playAmbientPlaylist(keys: string[], loop = true) {
    this.ambientPlaylist = keys
    this.ambientIndex = 0
    this.ambientLoop = loop
    this.playNextAmbientTrack()
  }

  async playMusicPlaylist(keys: string[], loop = true) {
    this.musicPlaylist = keys
    this.musicIndex = 0
    this.musicLoop = loop
    this.playNextMusicTrack()
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
  
  private async playNextMusicTrack() {
    if (this.musicPlaylist.length === 0) return

    const key = this.musicPlaylist[this.musicIndex]
    const audioStore = useAudioStore()
    const music = audioStore.channels.music
    if (music.muted) return
    if (!key) return

    const url = audioStore.sfxMap[key]
    if (!url) return console.warn(`[AudioEngine] Unknown music key: ${key}`)

    if (this.musicSource) this.musicSource.stop()

    const res = await fetch(url)
    const array = await res.arrayBuffer()
    const buffer = await this.context.decodeAudioData(array)

    const src = this.context.createBufferSource()
    src.buffer = buffer
    src.connect(this.musicGain)

    this.musicGain.gain.value = music.volume / 100

    await this.context.resume()
    src.start()

    src.onended = () => {
      this.musicIndex++
      if (this.musicIndex >= this.musicPlaylist.length) {
        if (this.musicLoop) this.musicIndex = 0
        else return
      }
      this.playNextMusicTrack()
    }

    this.musicSource = src
  }

  private async playNextAmbientTrack() {
    if (this.ambientPlaylist.length === 0) return

    const key = this.ambientPlaylist[this.ambientIndex]
    const audioStore = useAudioStore()
    const ambient = audioStore.channels.ambient
    if (ambient.muted) return
    if (!key) return
    const url = audioStore.sfxMap[key]
    if (!url) return console.warn(`[AudioEngine] Unknown ambient key: ${key}`)

    if (this.ambientSource) this.ambientSource.stop()

    const res = await fetch(url)
    const array = await res.arrayBuffer()
    const buffer = await this.context.decodeAudioData(array)

    const src = this.context.createBufferSource()
    src.buffer = buffer
    src.connect(this.ambientGain)

    // optional: 50% leiser
    this.ambientGain.gain.value = (ambient.volume / 100) * 0.5

    await this.context.resume()
    src.start()

    src.onended = () => {
      this.ambientIndex++
      if (this.ambientIndex >= this.ambientPlaylist.length) {
        if (this.ambientLoop) this.ambientIndex = 0
        else return
      }
      this.playNextAmbientTrack()
    }

    this.ambientSource = src
  }

stopMusic() {
  this.musicLoop = false
  this.musicPlaylist = []
  this.musicIndex = 0
  if (this.musicSource) {
    this.musicSource.stop()
    this.musicSource = null
  }
}

stopAmbient() {
  this.ambientLoop = false
  this.ambientPlaylist = []
  this.ambientIndex = 0
  if (this.ambientSource) {
    this.ambientSource.stop()
    this.ambientSource = null
  }
}
  

}

export const audioEngine = new AudioEngine()
