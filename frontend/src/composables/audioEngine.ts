import { useAudioStore } from '@/stores/audioStore'

/**
 * 3D-Positionsvector für Audioquellen.
 */
type Vec3 = { x: number; y: number; z: number }

/**
 * Zentrale Engine für alle Audiofunktionen im Spiel.
 *
 * Verantwortlich für:
 * - Musik-Playlisten
 * - Ambient-Playlisten
 * - Lautstärke / Gain-Kontrolle der Kanäle (Mit den zentralen Infos aus audioStore.ts)
 * - 3D-Sound (positionsabhängig)
 *
 * Audio wird über die Web Audio API gesteuert.
 */
class AudioEngine {
  public context = new AudioContext()

  /** Lautstärke-Regler für Musik */
  public musicGain = this.context.createGain()
  /** Lautstärke-Regler für Ambient */
  public ambientGain = this.context.createGain()

   /** Aktuelle Musikquelle */
  public musicSource: AudioBufferSourceNode | null = null

  /** Aktuelle Ambientquelle */
  public ambientSource: AudioBufferSourceNode | null = null

  /** Ambient Playlist und Status */
  public ambientPlaylist: string[] = []
  public ambientIndex = 0
  public ambientLoop = true

  /** Musik Playlist und Status */
  public musicPlaylist: string[] = []
  public musicIndex = 0
  public musicLoop = true

  /**
   * Initialisiert Audio-Routing.
   */
  constructor() {
    this.musicGain.connect(this.context.destination)
    this.ambientGain.connect(this.context.destination)
  }

  /**
   * Startet eine Ambient-Playlist.
   *
   * @param keys Liste der Audio-Keys
   * @param loop Ob die Playlist wiederholt werden soll
   */
  async playAmbientPlaylist(keys: string[], loop = true) {
    this.ambientPlaylist = keys
    this.ambientIndex = 0
    this.ambientLoop = loop
    this.playNextAmbientTrack()
  }

  /**
   * Startet eine Musik-Playlist.
   *
   * @param keys Liste der Audio-Keys
   * @param loop Ob die Playlist wiederholt werden soll
   */
  async playMusicPlaylist(keys: string[], loop = true) {
    this.musicPlaylist = keys
    this.musicIndex = 0
    this.musicLoop = loop
    this.playNextMusicTrack()
  }

  /**
   * Setzt die Listener-Position für 3D-Audio.
   */
  setListenerPosition(x: number, y: number, z: number) {
    const l = this.context.listener
    l.positionX.value = x
    l.positionY.value = y
    l.positionZ.value = z
  }

  /**
   * Spielt einen Sound positionsabhängig im Raum ab.
   *
   * @param key Sound-Key
   * @param position Position im Raum
   */
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
  
  /**
   * Spielt den nächsten Musiktitel in der Playlist.
   * Wird intern verwendet.
   */
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

  /**
   * Spielt den nächsten Ambient-Titel in der Playlist.
   * Wird intern verwendet.
   */
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

/**
 * Stoppt alle Musik-Streams und leert die Playlist.
 */
stopMusic() {
  this.musicLoop = false
  this.musicPlaylist = []
  this.musicIndex = 0
  if (this.musicSource) {
    this.musicSource.stop()
    this.musicSource = null
  }
}

/**
 * Stoppt alle Ambient-Streams und leert die Playlist.
 */
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
