import { useCallback, useState } from 'react'

export function useAsync() {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const run = useCallback(async (action: () => Promise<unknown>): Promise<boolean> => {
    setBusy(true)
    setError('')
    try {
      await action()
      return true
    } catch (e) {
      setError((e as Error).message)
      return false
    } finally {
      setBusy(false)
    }
  }, [])

  return { busy, error, setError, run }
}
