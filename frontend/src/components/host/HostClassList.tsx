import { useState } from 'react'
import { api } from '../../api'
import { dayLabel, timeLabel } from '../../format'
import { useAsync } from '../../hooks/useAsync'
import type { PilatesClass } from '../../types'
import { ConfirmDialog } from '../ConfirmDialog'
import { AttendeesList } from './AttendeesList'

interface Props {
  classes: PilatesClass[]
  onChanged: () => void
}

export function HostClassList({ classes, onChanged }: Props) {
  const [openId, setOpenId] = useState<number | null>(null)
  const [toDelete, setToDelete] = useState<PilatesClass | null>(null)
  const { busy, error, setError, run } = useAsync()

  function askDelete(s: PilatesClass) {
    setError('')
    setToDelete(s)
  }

  async function remove() {
    if (!toDelete) return
    const id = toDelete.id
    if (await run(() => api.deleteClass(id))) {
      if (openId === id) setOpenId(null)
      setToDelete(null)
      onChanged()
    }
  }

  return (
    <div className="panel">
      <h2>Your upcoming classes</h2>
      {classes.length === 0 && <p className="muted">Nothing published yet. Add your first class on the left.</p>}
      <ul className="host-list">
        {classes.map((s) => (
          <li key={s.id}>
            <div className="host-row">
              <div>
                <strong>{s.title}</strong>
                <span className="muted">
                  {dayLabel(s.startTime)}, {timeLabel(s.startTime)} · {s.booked}/{s.capacity} booked
                </span>
              </div>
              <div className="actions">
                <button className="btn btn-ghost" onClick={() => setOpenId(openId === s.id ? null : s.id)}>
                  {openId === s.id ? 'Hide' : 'Attendees'}
                </button>
                <button className="btn btn-danger" onClick={() => askDelete(s)}>Delete</button>
              </div>
            </div>
            {openId === s.id && <AttendeesList classId={s.id} />}
          </li>
        ))}
      </ul>

      <ConfirmDialog
        open={toDelete !== null}
        title="Delete this class?"
        confirmLabel="Delete class"
        busyLabel="Deleting…"
        busy={busy}
        error={error}
        onConfirm={remove}
        onCancel={() => setToDelete(null)}
      >
        {toDelete && (
          <>
            <strong>{toDelete.title}</strong> on {dayLabel(toDelete.startTime)} at {timeLabel(toDelete.startTime)}.
            {toDelete.booked > 0 && ` ${toDelete.booked} ${toDelete.booked === 1 ? 'booking' : 'bookings'} will be removed too.`}
          </>
        )}
      </ConfirmDialog>
    </div>
  )
}
