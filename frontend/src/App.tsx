import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

type IncidentStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED'
type IncidentPriority = 'LOW' | 'MEDIUM' | 'HIGH'
type AlarmSeverity = 'CRITICAL' | 'MAJOR' | 'MINOR'

type Incident = {
  id: number
  title: string
  description: string
  status: IncidentStatus
  priority: IncidentPriority
  source: string
}

type Alarm = {
  id: number
  source: string
  message: string
  severity: AlarmSeverity
  createdAt: string
}

type IncidentFormState = {
  title: string
  description: string
  source: string
  status: IncidentStatus
  priority: IncidentPriority
}

type AlarmFormState = {
  source: string
  message: string
  severity: AlarmSeverity
}

const incidentStatuses: IncidentStatus[] = ['OPEN', 'IN_PROGRESS', 'RESOLVED']
const incidentPriorities: IncidentPriority[] = ['LOW', 'MEDIUM', 'HIGH']
const alarmSeverities: AlarmSeverity[] = ['CRITICAL', 'MAJOR', 'MINOR']

const initialIncidentForm: IncidentFormState = {
  title: '',
  description: '',
  source: '',
  status: 'OPEN',
  priority: 'MEDIUM',
}

const initialAlarmForm: AlarmFormState = {
  source: '',
  message: '',
  severity: 'MAJOR',
}

async function apiRequest<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    headers: {
      'Content-Type': 'application/json',
      ...init?.headers,
    },
    ...init,
  })

  if (!response.ok) {
    const errorText = await response.text()
    throw new Error(errorText || `Request failed with status ${response.status}`)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

function App() {
  const [incidents, setIncidents] = useState<Incident[]>([])
  const [alarms, setAlarms] = useState<Alarm[]>([])
  const [incidentForm, setIncidentForm] = useState<IncidentFormState>(
    initialIncidentForm,
  )
  const [alarmForm, setAlarmForm] = useState<AlarmFormState>(initialAlarmForm)
  const [alarmSourceFilter, setAlarmSourceFilter] = useState('')
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    void refreshData()
  }, [])

  async function refreshData(sourceFilter?: string) {
    setLoading(true)
    setError('')

    try {
      const [incidentData, alarmData] = await Promise.all([
        apiRequest<Incident[]>('/api/incidents'),
        apiRequest<Alarm[]>(
          sourceFilter && sourceFilter.trim()
            ? `/api/alarms?source=${encodeURIComponent(sourceFilter.trim())}`
            : '/api/alarms',
        ),
      ])

      setIncidents(incidentData)
      setAlarms(alarmData)
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }

  async function handleCreateIncident(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    setMessage('')

    try {
      await apiRequest<Incident>('/api/incidents', {
        method: 'POST',
        body: JSON.stringify(incidentForm),
      })

      setIncidentForm(initialIncidentForm)
      setMessage('Incident wurde erstellt.')
      await refreshData(alarmSourceFilter)
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleCreateAlarm(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    setMessage('')

    try {
      await apiRequest<Alarm>('/api/alarms', {
        method: 'POST',
        body: JSON.stringify(alarmForm),
      })

      setAlarmForm(initialAlarmForm)
      setMessage('Alarm wurde gesendet.')
      await refreshData(alarmSourceFilter)
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDeleteIncident(id: number) {
    setError('')
    setMessage('')

    try {
      await apiRequest<void>(`/api/incidents/${id}`, { method: 'DELETE' })
      setMessage('Incident wurde gelöscht.')
      await refreshData(alarmSourceFilter)
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    }
  }

  async function handleDeleteAlarm(id: number) {
    setError('')
    setMessage('')

    try {
      await apiRequest<void>(`/api/alarms/${id}`, { method: 'DELETE' })
      setMessage('Alarm wurde gelöscht.')
      await refreshData(alarmSourceFilter)
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    }
  }

  async function handleStatusChange(id: number, status: IncidentStatus) {
    setError('')
    setMessage('')

    try {
      await apiRequest<Incident>(`/api/incidents/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status }),
      })

      setMessage('Status wurde aktualisiert.')
      await refreshData(alarmSourceFilter)
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    }
  }

  async function handlePriorityChange(id: number, priority: IncidentPriority) {
    setError('')
    setMessage('')

    try {
      await apiRequest<Incident>(`/api/incidents/${id}/priority`, {
        method: 'PATCH',
        body: JSON.stringify({ priority }),
      })

      setMessage('Priorität wurde aktualisiert.')
      await refreshData(alarmSourceFilter)
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    }
  }

  async function handleAlarmFilterSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setMessage('')
    await refreshData(alarmSourceFilter)
  }

  async function handleAlarmFilterReset() {
    setAlarmSourceFilter('')
    setMessage('')
    await refreshData('')
  }

  return (
    <main className="app-shell">
      <header className="page-header">
        <div>
          <h1>Incident Management MVP</h1>
          <p>Einfache Oberflaeche fuer manuelle Incidents und eingehende Alarme.</p>
        </div>
        <button type="button" onClick={() => void refreshData(alarmSourceFilter)}>
          Aktualisieren
        </button>
      </header>

      {(message || error) && (
        <section className="feedback-area">
          {message && <p className="feedback success">{message}</p>}
          {error && <p className="feedback error">{error}</p>}
        </section>
      )}

      <section className="form-grid">
        <article className="panel">
          <h2>Incident erstellen</h2>
          <form className="stack-form" onSubmit={handleCreateIncident}>
            <label>
              Titel
              <input
                value={incidentForm.title}
                onChange={(event) =>
                  setIncidentForm((current) => ({
                    ...current,
                    title: event.target.value,
                  }))
                }
                required
              />
            </label>

            <label>
              Beschreibung
              <textarea
                value={incidentForm.description}
                onChange={(event) =>
                  setIncidentForm((current) => ({
                    ...current,
                    description: event.target.value,
                  }))
                }
                rows={4}
                required
              />
            </label>

            <label>
              Source
              <input
                value={incidentForm.source}
                onChange={(event) =>
                  setIncidentForm((current) => ({
                    ...current,
                    source: event.target.value,
                  }))
                }
                required
              />
            </label>

            <div className="inline-fields">
              <label>
                Status
                <select
                  value={incidentForm.status}
                  onChange={(event) =>
                    setIncidentForm((current) => ({
                      ...current,
                      status: event.target.value as IncidentStatus,
                    }))
                  }
                >
                  {incidentStatuses.map((status) => (
                    <option key={status} value={status}>
                      {status}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Prioritaet
                <select
                  value={incidentForm.priority}
                  onChange={(event) =>
                    setIncidentForm((current) => ({
                      ...current,
                      priority: event.target.value as IncidentPriority,
                    }))
                  }
                >
                  {incidentPriorities.map((priority) => (
                    <option key={priority} value={priority}>
                      {priority}
                    </option>
                  ))}
                </select>
              </label>
            </div>

            <button type="submit" disabled={submitting}>
              Incident speichern
            </button>
          </form>
        </article>

        <article className="panel">
          <h2>Alarm senden</h2>
          <form className="stack-form" onSubmit={handleCreateAlarm}>
            <label>
              Source
              <input
                value={alarmForm.source}
                onChange={(event) =>
                  setAlarmForm((current) => ({
                    ...current,
                    source: event.target.value,
                  }))
                }
                required
              />
            </label>

            <label>
              Meldung
              <textarea
                value={alarmForm.message}
                onChange={(event) =>
                  setAlarmForm((current) => ({
                    ...current,
                    message: event.target.value,
                  }))
                }
                rows={4}
                required
              />
            </label>

            <label>
              Severity
              <select
                value={alarmForm.severity}
                onChange={(event) =>
                  setAlarmForm((current) => ({
                    ...current,
                    severity: event.target.value as AlarmSeverity,
                  }))
                }
              >
                {alarmSeverities.map((severity) => (
                  <option key={severity} value={severity}>
                    {severity}
                  </option>
                ))}
              </select>
            </label>

            <button type="submit" disabled={submitting}>
              Alarm senden
            </button>
          </form>
        </article>
      </section>

      <section className="data-grid">
        <article className="panel">
          <div className="panel-header">
            <div>
              <h2>Incidents</h2>
              <p>{incidents.length} Eintraege</p>
            </div>
          </div>

          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Titel</th>
                  <th>Beschreibung</th>
                  <th>Source</th>
                  <th>Status</th>
                  <th>Prioritaet</th>
                  <th>Aktionen</th>
                </tr>
              </thead>
              <tbody>
                {incidents.length === 0 && (
                  <tr>
                    <td colSpan={7} className="empty-state">
                      {loading ? 'Lade Daten...' : 'Keine Incidents vorhanden.'}
                    </td>
                  </tr>
                )}

                {incidents.map((incident) => (
                  <tr key={incident.id}>
                    <td>{incident.id}</td>
                    <td>{incident.title}</td>
                    <td>{incident.description}</td>
                    <td>{incident.source}</td>
                    <td>
                      <select
                        value={incident.status}
                        onChange={(event) =>
                          void handleStatusChange(
                            incident.id,
                            event.target.value as IncidentStatus,
                          )
                        }
                      >
                        {incidentStatuses.map((status) => (
                          <option key={status} value={status}>
                            {status}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <select
                        value={incident.priority}
                        onChange={(event) =>
                          void handlePriorityChange(
                            incident.id,
                            event.target.value as IncidentPriority,
                          )
                        }
                      >
                        {incidentPriorities.map((priority) => (
                          <option key={priority} value={priority}>
                            {priority}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <button
                        type="button"
                        className="danger-button"
                        onClick={() => void handleDeleteIncident(incident.id)}
                      >
                        Loeschen
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </article>

        <article className="panel">
          <div className="panel-header">
            <div>
              <h2>Alarme</h2>
              <p>{alarms.length} Eintraege</p>
            </div>

            <form className="filter-form" onSubmit={handleAlarmFilterSubmit}>
              <input
                placeholder="Nach source filtern"
                value={alarmSourceFilter}
                onChange={(event) => setAlarmSourceFilter(event.target.value)}
              />
              <button type="submit">Filter</button>
              <button type="button" onClick={() => void handleAlarmFilterReset()}>
                Reset
              </button>
            </form>
          </div>

          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Source</th>
                  <th>Meldung</th>
                  <th>Severity</th>
                  <th>Created At</th>
                  <th>Aktionen</th>
                </tr>
              </thead>
              <tbody>
                {alarms.length === 0 && (
                  <tr>
                    <td colSpan={6} className="empty-state">
                      {loading ? 'Lade Daten...' : 'Keine Alarme vorhanden.'}
                    </td>
                  </tr>
                )}

                {alarms.map((alarm) => (
                  <tr key={alarm.id}>
                    <td>{alarm.id}</td>
                    <td>{alarm.source}</td>
                    <td>{alarm.message}</td>
                    <td>{alarm.severity}</td>
                    <td>{formatDateTime(alarm.createdAt)}</td>
                    <td>
                      <button
                        type="button"
                        className="danger-button"
                        onClick={() => void handleDeleteAlarm(alarm.id)}
                      >
                        Loeschen
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </article>
      </section>
    </main>
  )
}

function formatDateTime(value: string) {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('de-DE', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date)
}

function getErrorMessage(error: unknown) {
  if (error instanceof Error) {
    return error.message
  }

  return 'Unbekannter Fehler'
}

export default App
