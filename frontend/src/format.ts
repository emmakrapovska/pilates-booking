import type { PilatesClass } from './types'

const parse = (iso: string) => new Date(iso)

export const dayKey = (iso: string) => iso.slice(0, 10)

export const dayLabel = (iso: string) =>
  new Intl.DateTimeFormat('en-GB', { weekday: 'long', day: 'numeric', month: 'long' }).format(parse(iso))

export const timeLabel = (iso: string) =>
  new Intl.DateTimeFormat('en-GB', { hour: '2-digit', minute: '2-digit', hour12: false }).format(parse(iso))

export const endTimeLabel = (s: PilatesClass) => timeLabel(addMinutesLocal(s.startTime, s.durationMinutes))

function addMinutesLocal(iso: string, minutes: number): string {
  const d = parse(iso)
  d.setMinutes(d.getMinutes() + minutes)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}:00`
}

export function groupByDay(classes: PilatesClass[]): [string, PilatesClass[]][] {
  const map = new Map<string, PilatesClass[]>()
  for (const s of classes) {
    const k = dayKey(s.startTime)
    map.set(k, [...(map.get(k) ?? []), s])
  }
  return [...map.entries()]
}
