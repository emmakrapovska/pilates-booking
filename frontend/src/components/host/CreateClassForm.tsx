import { FormEvent, useState } from 'react'
import { api } from '../../api'
import { dayLabel } from '../../format'
import { useAsync } from '../../hooks/useAsync'

interface Props {
  onCreated: () => void
}

const DURATIONS = [30, 45, 50, 55, 60, 90]

const pad = (n: number) => String(n).padStart(2, '0')
const todayStr = () => {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

export function CreateClassForm({ onCreated }: Props) {
  const [title, setTitle] = useState('Mat Pilates')
  const [date, setDate] = useState(todayStr())
  const [time, setTime] = useState('18:00')
  const [duration, setDuration] = useState(55)
  const [capacity, setCapacity] = useState(8)
  const [notice, setNotice] = useState('')
  const { busy, error, run } = useAsync()

  async function create(e: FormEvent) {
    e.preventDefault()
    setNotice('')
    const startTime = `${date}T${time}:00`
    const ok = await run(() => api.createClass({ title, startTime, durationMinutes: duration, capacity }))
    if (ok) {
      setNotice(`Published: ${title} on ${dayLabel(startTime)} at ${time}.`)
      onCreated()
    }
  }

  return (
    <form className="panel" onSubmit={create}>
      <h2>Publish a class</h2>
      <label>
        Class name
        <input value={title} onChange={(e) => setTitle(e.target.value)} required maxLength={80} />
      </label>
      <div className="grid2">
        <label>
          Date
          <input type="date" value={date} min={todayStr()} onChange={(e) => setDate(e.target.value)} required />
        </label>
        <label>
          Start time
          <input type="time" value={time} onChange={(e) => setTime(e.target.value)} required />
        </label>
        <label>
          Duration
          <select value={duration} onChange={(e) => setDuration(Number(e.target.value))}>
            {DURATIONS.map((d) => <option key={d} value={d}>{d} min</option>)}
          </select>
        </label>
        <label>
          Spots
          <input type="number" min={1} max={30} value={capacity} onChange={(e) => setCapacity(Number(e.target.value))} required />
        </label>
      </div>
      {error && <p className="error" role="alert">{error}</p>}
      {notice && <p className="notice" role="status">{notice}</p>}
      <button className="btn" disabled={busy}>{busy ? 'Publishing…' : 'Publish class'}</button>
    </form>
  )
}
