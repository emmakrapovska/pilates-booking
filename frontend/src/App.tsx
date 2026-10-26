import { Navigate, NavLink, Route, Routes } from 'react-router'
import { HostPanel } from './components/HostPanel'
import { useClasses } from './hooks/useClasses'
import { ClassesPage } from './pages/ClassesPage'

const STUDIO = import.meta.env.VITE_STUDIO_NAME ?? 'Pilates Studio'

export default function App() {
  const { classes, loading, error, refresh } = useClasses()

  return (
    <div className="page">
      <header className="top">
        <div>
          <h1>{STUDIO}</h1>
          <p className="muted">Pick a class, leave your name and email, and your spot is held.</p>
        </div>
        <nav className="tabs" aria-label="View">
          <NavLink to="/" end>Classes</NavLink>
          <NavLink to="/instructor">Instructor</NavLink>
        </nav>
      </header>

      <main>
        {error && <p className="error banner" role="alert">{error}</p>}
        {loading ? (
          <p className="muted">Loading classes…</p>
        ) : (
          <Routes>
            <Route path="/" element={<ClassesPage classes={classes} onChanged={refresh} />} />
            <Route path="/instructor" element={<HostPanel classes={classes} onChanged={refresh} />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        )}
      </main>
    </div>
  )
}
