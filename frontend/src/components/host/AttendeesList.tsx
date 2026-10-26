import { useEffect, useState } from 'react'
import { api } from '../../api'
import type { Booking } from '../../types'

interface Props {
  classId: number
}

export function AttendeesList({ classId }: Props) {
  const [attendees, setAttendees] = useState<Booking[] | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let ignore = false
    api.bookings(classId)
      .then((list) => { if (!ignore) setAttendees(list) })
      .catch((e) => { if (!ignore) setError((e as Error).message) })
    return () => { ignore = true }
  }, [classId])

  if (error) return <p className="error" role="alert">{error}</p>
  if (!attendees) return <p className="muted">Loading…</p>

  return (
    <ul className="attendees">
      {attendees.length === 0 && <li className="muted">No bookings yet.</li>}
      {attendees.map((b) => <li key={b.id}>{b.name} <span className="muted">{b.email}</span></li>)}
    </ul>
  )
}
