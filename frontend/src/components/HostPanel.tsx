import type { PilatesClass } from '../types'
import { CreateClassForm } from './host/CreateClassForm'
import { HostClassList } from './host/HostClassList'

interface Props {
  classes: PilatesClass[]
  onChanged: () => void
}

export function HostPanel({ classes, onChanged }: Props) {
  return (
    <div className="host">
      <CreateClassForm onCreated={onChanged} />
      <HostClassList classes={classes} onChanged={onChanged} />
    </div>
  )
}
