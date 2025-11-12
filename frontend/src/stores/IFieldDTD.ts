export interface IFieldDTD{
    id: number
    north?: number
    south?: number
    west?: number
    east?: number
    type: string
    position: Array<number>
    isBarrier: boolean
}