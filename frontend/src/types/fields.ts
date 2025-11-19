export interface Field {
    id: number
    type: string
    position: {
        x: number
        y: number
    }
    north: number
    east: number
    south: number
    west: number
    barrier: boolean
}