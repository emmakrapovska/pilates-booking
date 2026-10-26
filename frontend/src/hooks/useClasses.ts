import { useCallback, useEffect, useState } from 'react'
import { api } from '../api'
import type { PilatesClass } from '../types'
import { useAsync } from './useAsync'

export function useClasses() {
  const [classes, setClasses] = useState<PilatesClass[]>([])
  const [loading, setLoading] = useState(true)
  const { error, run } = useAsync()

  const refresh = useCallback(async () => {
    await run(async () => setClasses(await api.listClasses()))
    setLoading(false)
  }, [run])

  useEffect(() => { refresh() }, [refresh])

  return { classes, loading, error, refresh }
}
