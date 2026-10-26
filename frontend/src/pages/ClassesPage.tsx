import { useState } from 'react'
import { BookingDialog } from '../components/BookingDialog'
import { ClassList } from '../components/ClassList'
import type { PilatesClass } from '../types'

interface Props {
  classes: PilatesClass[]
  onChanged: () => void
}

export function ClassesPage({ classes, onChanged }: Props) {
  const [selected, setSelected] = useState<PilatesClass | null>(null)

  return (
    <>
      <ClassList classes={classes} onBook={setSelected} />
      <BookingDialog pilatesClass={selected} onClose={() => setSelected(null)} onChanged={onChanged} />
    </>
  )
}
