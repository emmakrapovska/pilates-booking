import type { Booking, Confirmation, NewPilatesClass, PilatesClass } from './types'

const BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let res: Response
  try {
    res = await fetch(`${BASE}/api${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...init,
    })
  } catch {
    throw new Error('Cannot reach the server. Check your connection and try again.')
  }
  if (!res.ok) {
    const body = await res.json().catch(() => null)
    throw new Error(body?.message ?? `Something went wrong (${res.status}).`)
  }
  return res.status === 204 ? (undefined as T) : res.json()
}

export const api = {
  listClasses: () => request<PilatesClass[]>('/classes'),
  createClass: (pilatesClass: NewPilatesClass) =>
    request<PilatesClass>('/classes', { method: 'POST', body: JSON.stringify(pilatesClass) }),
  deleteClass: (id: number) => request<void>(`/classes/${id}`, { method: 'DELETE' }),
  book: (classId: number, name: string, email: string) =>
    request<Booking>(`/classes/${classId}/bookings`, {
      method: 'POST',
      body: JSON.stringify({ name, email }),
    }),
  bookings: (classId: number) => request<Booking[]>(`/classes/${classId}/bookings`),
  confirmation: (classId: number, bookingId: number) =>
    request<Confirmation>(`/classes/${classId}/bookings/${bookingId}/confirmation`),
}
