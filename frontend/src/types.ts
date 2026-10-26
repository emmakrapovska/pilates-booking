export interface PilatesClass {
  id: number
  title: string
  startTime: string
  durationMinutes: number
  capacity: number
  booked: number
  spotsLeft: number
  available: boolean
}

export interface Booking {
  id: number
  classId: number
  name: string
  email: string
  createdAt: string
}

export interface NewPilatesClass {
  title: string
  startTime: string
  durationMinutes: number
  capacity: number
}

export interface Confirmation {
  message: string
  aiGenerated: boolean
}
