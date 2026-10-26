import { ReactNode, useEffect, useRef } from 'react'

interface Props {
  open: boolean
  title: string
  children: ReactNode
  confirmLabel: string
  busyLabel: string
  busy: boolean
  error: string
  onConfirm: () => void
  onCancel: () => void
}

export function ConfirmDialog({ open, title, children, confirmLabel, busyLabel, busy, error, onConfirm, onCancel }: Props) {
  const ref = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const d = ref.current
    if (!d) return
    if (open && !d.open) d.showModal()
    if (!open && d.open) d.close()
  }, [open])

  return (
    <dialog ref={ref} className="dialog" onClose={onCancel}>
      {open && (
        <>
          <header className="dialog-head">
            <h2>{title}</h2>
          </header>
          <div className="dialog-body">
            <p className="confirm-text">{children}</p>
            {error && <p className="error" role="alert">{error}</p>}
            <div className="actions">
              <button type="button" className="btn btn-ghost" onClick={onCancel} disabled={busy}>Cancel</button>
              <button type="button" className="btn btn-danger" onClick={onConfirm} disabled={busy}>
                {busy ? busyLabel : confirmLabel}
              </button>
            </div>
          </div>
        </>
      )}
    </dialog>
  )
}
