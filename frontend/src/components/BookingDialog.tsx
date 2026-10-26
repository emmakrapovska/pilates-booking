import { FormEvent, useEffect, useRef, useState } from 'react'
import { api } from '../api'
import { dayLabel, endTimeLabel, timeLabel } from '../format'
import { useAsync } from '../hooks/useAsync'
import type { Booking, PilatesClass } from '../types'

interface Props {
  pilatesClass: PilatesClass | null
  onClose: () => void
  onChanged: () => void
}

export function BookingDialog({ pilatesClass, onClose, onChanged }: Props) {
  const ref = useRef<HTMLDialogElement>(null)
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [booking, setBooking] = useState<Booking | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const { busy, error, setError, run } = useAsync()

  useEffect(() => {
    const d = ref.current
    if (!d) return
    if (pilatesClass && !d.open) {
      setError('')
      setBooking(null)
      d.showModal()
    }
    if (!pilatesClass && d.open) d.close()
  }, [pilatesClass, setError])

  useEffect(() => {
    if (!booking) return
    let ignore = false
    setMessage(null)
    api.confirmation(booking.classId, booking.id)
      .then((c) => { if (!ignore) setMessage(c.message) })
      .catch(() => { if (!ignore) setMessage(`See you on the mat, ${booking.name.split(' ')[0]}!`) })
    return () => { ignore = true }
  }, [booking])

  async function submit(e: FormEvent) {
    e.preventDefault()
    if (!pilatesClass) return
    await run(async () => setBooking(await api.book(pilatesClass.id, name, email)))
    onChanged()
  }

  return (
    <dialog ref={ref} className="dialog" onClose={onClose}>
      {pilatesClass && (
        <>
          <header className="dialog-head">
            <p className="muted">
              {dayLabel(pilatesClass.startTime)}, {timeLabel(pilatesClass.startTime)}–{endTimeLabel(pilatesClass)}
            </p>
            <h2>{pilatesClass.title}</h2>
          </header>

          {booking ? (
            <div className="dialog-body">
              <p className="success">You're booked!</p>
              {message === null
                ? <p className="muted confirmation" aria-live="polite">Writing your confirmation…</p>
                : <p className="confirmation" aria-live="polite">{message}</p>}
              <p className="muted">Booking made with {booking.email}.</p>
              <button className="btn" onClick={onClose}>Close</button>
            </div>
          ) : (
            <form className="dialog-body" onSubmit={submit}>
              <label>
                Name
                <input value={name} onChange={(e) => setName(e.target.value)} required maxLength={100} autoComplete="name" />
              </label>
              <label>
                Email
                <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required maxLength={160} autoComplete="email" />
              </label>
              {error && <p className="error" role="alert">{error}</p>}
              <div className="actions">
                <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
                <button className="btn" disabled={busy}>{busy ? 'Booking…' : 'Confirm booking'}</button>
              </div>
            </form>
          )}
        </>
      )}
    </dialog>
  )
}
