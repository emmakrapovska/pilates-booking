import type { PilatesClass } from '../types'
import { dayLabel, endTimeLabel, groupByDay, timeLabel } from '../format'

interface Props {
  classes: PilatesClass[]
  onBook: (pilatesClass: PilatesClass) => void
}

export function ClassList({ classes, onBook }: Props) {
  if (classes.length === 0) {
    return (
      <p className="empty">
        No classes are published yet. Check back soon, or ask your instructor to add some.
      </p>
    )
  }

  return (
    <div className="timetable">
      {groupByDay(classes).map(([day, dayClasses]) => (
        <section key={day} className="day">
          <h2 className="day-title">{dayLabel(dayClasses[0].startTime)}</h2>
          <ul className="rows">
            {dayClasses.map((s) => (
              <li key={s.id} className={`row ${s.available ? '' : 'row-full'}`}>
                <div className="when">
                  <span className="start">{timeLabel(s.startTime)}</span>
                  <span className="end">until {endTimeLabel(s)}</span>
                </div>
                <div className="what">
                  <strong>{s.title}</strong>
                  <span className="muted">
                    {s.durationMinutes} min ·{' '}
                    {s.available
                      ? `${s.spotsLeft} of ${s.capacity} ${s.capacity === 1 ? 'spot' : 'spots'} left`
                      : 'Fully booked'}
                  </span>
                </div>
                <button className="btn" disabled={!s.available} onClick={() => onBook(s)}>
                  {s.available ? 'Book' : 'Full'}
                </button>
              </li>
            ))}
          </ul>
        </section>
      ))}
    </div>
  )
}
