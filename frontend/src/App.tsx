import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import provadisLogo from './assets/provadis_logo.png'
import './App.css'

type IncidentStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED'
type IncidentPriority = 'LOW' | 'MEDIUM' | 'HIGH'
type AlarmSeverity = 'CRITICAL' | 'MAJOR' | 'MINOR'

type Incident = {
  id: number
  title: string
  description: string
  status: IncidentStatus
  priority: IncidentPriority
  source: string | null
}

type Alarm = {
  id: number
  source: string
  message: string
  severity: AlarmSeverity
  createdAt: string
}

type AlarmFormState = {
  source: string
  message: string
  severity: AlarmSeverity
}

type IncidentFormState = {
  title: string
  description: string
  status: IncidentStatus
  priority: IncidentPriority
}

const incidentStatuses: IncidentStatus[] = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED']
const incidentPriorities: IncidentPriority[] = ['LOW', 'MEDIUM', 'HIGH']
const alarmSeverities: AlarmSeverity[] = ['CRITICAL', 'MAJOR', 'MINOR']

const initialAlarmForm: AlarmFormState = {
  source: '',
  message: '',
  severity: 'MAJOR',
}

const initialIncidentForm: IncidentFormState = {
  title: '',
  description: '',
  status: 'OPEN',
  priority: 'MEDIUM',
}

function App() {
  const [alarms, setAlarms] = useState<Alarm[]>([])
  const [incidents, setIncidents] = useState<Incident[]>([])
  const [alarmForm, setAlarmForm] = useState<AlarmFormState>(initialAlarmForm)
  const [incidentForm, setIncidentForm] = useState<IncidentFormState>(initialIncidentForm)
  const [selectedSource, setSelectedSource] = useState<string | null>(null)
  const [sourceMessages, setSourceMessages] = useState<Alarm[]>([])
  const [error, setError] = useState<string>('')
  const [busy, setBusy] = useState<boolean>(false)
  const [messagesLoading, setMessagesLoading] = useState<boolean>(false)

  useEffect(() => {
    void loadDashboard()
  }, [])

  async function apiFetch<T>(input: string, init?: RequestInit): Promise<T> {
    const response = await fetch(input, {
      headers: {
        'Content-Type': 'application/json',
        ...(init?.headers ?? {}),
      },
      ...init,
    })

    if (!response.ok) {
      const message = await response.text()
      throw new Error(message || 'Request failed')
    }

    if (response.status === 204) {
      return undefined as T
    }

    return (await response.json()) as T
  }

  async function loadDashboard() {
    try {
      setError('')
      const [alarmsResponse, incidentsResponse] = await Promise.all([
        apiFetch<Alarm[]>('/api/alarms'),
        apiFetch<Incident[]>('/api/incidents'),
      ])
      setAlarms(alarmsResponse)
      setIncidents(incidentsResponse)
    } catch (fetchError) {
      setError(getErrorMessage(fetchError))
    }
  }

  async function loadMessages(source: string) {
    try {
      setMessagesLoading(true)
      setError('')
      const encodedSource = encodeURIComponent(source)
      const messages = await apiFetch<Alarm[]>(`/api/alarms?source=${encodedSource}`)
      setSelectedSource(source)
      setSourceMessages(messages)
    } catch (fetchError) {
      setError(getErrorMessage(fetchError))
    } finally {
      setMessagesLoading(false)
    }
  }

  async function handleAlarmSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    try {
      setBusy(true)
      setError('')
      await apiFetch<Alarm>('/api/alarms', {
        method: 'POST',
        body: JSON.stringify(alarmForm),
      })
      setAlarmForm(initialAlarmForm)
      await loadDashboard()
      const currentSource = selectedSource
      if (currentSource) {
        await loadMessages(currentSource)
      }
    } catch (fetchError) {
      setError(getErrorMessage(fetchError))
    } finally {
      setBusy(false)
    }
  }

  async function handleIncidentSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    try {
      setBusy(true)
      setError('')
      await apiFetch<Incident>('/api/incidents', {
        method: 'POST',
        body: JSON.stringify(incidentForm),
      })
      setIncidentForm(initialIncidentForm)
      await loadDashboard()
    } catch (fetchError) {
      setError(getErrorMessage(fetchError))
    } finally {
      setBusy(false)
    }
  }

  async function handleAlarmDelete(id: number) {
    try {
      setBusy(true)
      setError('')
      await apiFetch<void>(`/api/alarms/${id}`, {
        method: 'DELETE',
      })
      await loadDashboard()
      const currentSource = selectedSource
      if (currentSource) {
        await loadMessages(currentSource)
      }
    } catch (fetchError) {
      setError(getErrorMessage(fetchError))
    } finally {
      setBusy(false)
    }
  }

  async function handleIncidentDelete(id: number) {
    try {
      setBusy(true)
      setError('')
      await apiFetch<void>(`/api/incidents/${id}`, {
        method: 'DELETE',
      })
      await loadDashboard()
    } catch (fetchError) {
      setError(getErrorMessage(fetchError))
    } finally {
      setBusy(false)
    }
  }

  async function updateIncidentStatus(id: number, status: IncidentStatus) {
    try {
      setError('')
      await apiFetch<Incident>(`/api/incidents/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status }),
      })
      await loadDashboard()
    } catch (fetchError) {
      setError(getErrorMessage(fetchError))
    }
  }

  async function updateIncidentPriority(id: number, priority: IncidentPriority) {
    try {
      setError('')
      await apiFetch<Incident>(`/api/incidents/${id}/priority`, {
        method: 'PATCH',
        body: JSON.stringify({ priority }),
      })
      await loadDashboard()
    } catch (fetchError) {
      setError(getErrorMessage(fetchError))
    }
  }

  function closeMessages() {
    setSelectedSource(null)
    setSourceMessages([])
  }

  return (
    <div className="page-shell">
      <div className="app-card">
        <header className="app-header">
          <div>
            <p className="eyebrow">Bachelorarbeit MVP</p>
            <h1>Incident Management</h1>
            <p className="subtitle">
              Synthetische Netzwerkalarme erfassen, Incidents daraus ableiten und Nachrichten je Quelle nachverfolgen.
            </p>
          </div>
          <img className="brand-logo" src={provadisLogo} alt="Provadis Logo" />
        </header>

        {error ? <div className="error-banner">{error}</div> : null}

        <section className="panel-grid">
          <article className="panel">
            <div className="panel-heading">
              <h2>Alarm senden</h2>
              <span>{alarms.length} Alarm(e)</span>
            </div>
            <form className="form-grid" onSubmit={handleAlarmSubmit}>
              <label>
                Quelle
                <input
                  value={alarmForm.source}
                  onChange={(event) => setAlarmForm({ ...alarmForm, source: event.target.value })}
                  placeholder="z. B. Router-01"
                  required
                />
              </label>
              <label>
                Nachricht
                <textarea
                  value={alarmForm.message}
                  onChange={(event) => setAlarmForm({ ...alarmForm, message: event.target.value })}
                  placeholder="Kurzbeschreibung des Alarms"
                  rows={4}
                  required
                />
              </label>
              <label>
                Schweregrad
                <select
                  value={alarmForm.severity}
                  onChange={(event) =>
                    setAlarmForm({ ...alarmForm, severity: event.target.value as AlarmSeverity })
                  }
                >
                  {alarmSeverities.map((severity) => (
                    <option key={severity} value={severity}>
                      {severity}
                    </option>
                  ))}
                </select>
              </label>
              <button className="primary-button" type="submit" disabled={busy}>
                Alarm senden
              </button>
            </form>
          </article>

          <article className="panel">
            <div className="panel-heading">
              <h2>Incident anlegen</h2>
              <span>{incidents.length} Incident(s)</span>
            </div>
            <form className="form-grid" onSubmit={handleIncidentSubmit}>
              <label>
                Titel
                <input
                  value={incidentForm.title}
                  onChange={(event) => setIncidentForm({ ...incidentForm, title: event.target.value })}
                  placeholder="Manueller Incident"
                  required
                />
              </label>
              <label>
                Beschreibung
                <textarea
                  value={incidentForm.description}
                  onChange={(event) => setIncidentForm({ ...incidentForm, description: event.target.value })}
                  placeholder="Beschreibung des Vorfalls"
                  rows={4}
                  required
                />
              </label>
              <div className="select-row">
                <label>
                  Status
                  <select
                    value={incidentForm.status}
                    onChange={(event) =>
                      setIncidentForm({ ...incidentForm, status: event.target.value as IncidentStatus })
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
                      setIncidentForm({ ...incidentForm, priority: event.target.value as IncidentPriority })
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
              <button className="primary-button" type="submit" disabled={busy}>
                Anlegen
              </button>
            </form>
          </article>
        </section>

        <section className="panel table-panel">
          <div className="panel-heading">
            <h2>Alarme</h2>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Alarm-ID</th>
                  <th>Quelle</th>
                  <th>Schweregrad</th>
                  <th>Nachricht</th>
                  <th>Erfassungszeit</th>
                  <th>Aktion</th>
                </tr>
              </thead>
              <tbody>
                {alarms.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="empty-cell">
                      Noch keine Alarme vorhanden.
                    </td>
                  </tr>
                ) : (
                  alarms.map((alarm) => (
                    <tr key={alarm.id}>
                      <td>{alarm.id}</td>
                      <td>{alarm.source}</td>
                      <td>
                        <span className={`badge severity-${alarm.severity.toLowerCase()}`}>{alarm.severity}</span>
                      </td>
                      <td>{alarm.message}</td>
                      <td>{formatDate(alarm.createdAt)}</td>
                      <td>
                        <button
                          type="button"
                          className="table-button danger-button"
                          onClick={() => void handleAlarmDelete(alarm.id)}
                          disabled={busy}
                        >
                          Loeschen
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>

        <section className="panel table-panel">
          <div className="panel-heading">
            <h2>Incidents</h2>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Incident-ID</th>
                  <th>Quelle</th>
                  <th>Status</th>
                  <th>Prioritaet</th>
                  <th>Beschreibung</th>
                  <th>Nachrichten</th>
                  <th>Aktion</th>
                </tr>
              </thead>
              <tbody>
                {incidents.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="empty-cell">
                      Noch keine Incidents vorhanden.
                    </td>
                  </tr>
                ) : (
                  incidents.map((incident) => (
                    <tr key={incident.id}>
                      <td>{incident.id}</td>
                      <td>{incident.source || incident.title}</td>
                      <td>
                        <select
                          value={incident.status}
                          onChange={(event) =>
                            void updateIncidentStatus(incident.id, event.target.value as IncidentStatus)
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
                            void updateIncidentPriority(incident.id, event.target.value as IncidentPriority)
                          }
                        >
                          {incidentPriorities.map((priority) => (
                            <option key={priority} value={priority}>
                              {priority}
                            </option>
                          ))}
                        </select>
                      </td>
                      <td>{incident.description}</td>
                      <td>
                        {incident.source ? (
                          <button
                            type="button"
                            className="table-button"
                            onClick={() => {
                              if (incident.source) {
                                void loadMessages(incident.source)
                              }
                            }}
                          >
                            Nachrichten
                          </button>
                        ) : (
                          <span className="muted-text">Keine Quelle</span>
                        )}
                      </td>
                      <td>
                        <button
                          type="button"
                          className="table-button danger-button"
                          onClick={() => void handleIncidentDelete(incident.id)}
                          disabled={busy}
                        >
                          Loeschen
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>
      </div>

      {selectedSource ? (
        <div className="modal-backdrop" role="presentation" onClick={closeMessages}>
          <div
            className="modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="messages-title"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="modal-header">
              <div>
                <p className="eyebrow">Nachrichtenhistorie</p>
                <h2 id="messages-title">Quelle: {selectedSource}</h2>
              </div>
              <button type="button" className="table-button" onClick={closeMessages}>
                Schliessen
              </button>
            </div>
            {messagesLoading ? (
              <p className="modal-placeholder">Nachrichten werden geladen...</p>
            ) : sourceMessages.length === 0 ? (
              <p className="modal-placeholder">Keine Nachrichten fuer diese Quelle gefunden.</p>
            ) : (
              <div className="message-list">
                {sourceMessages.map((message) => (
                  <article key={message.id} className="message-item">
                    <div className="message-meta">
                      <span className={`badge severity-${message.severity.toLowerCase()}`}>{message.severity}</span>
                      <span>{formatDate(message.createdAt)}</span>
                    </div>
                    <p>{message.message}</p>
                  </article>
                ))}
              </div>
            )}
          </div>
        </div>
      ) : null}
    </div>
  )
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('de-DE', {
    dateStyle: 'short',
    timeStyle: 'medium',
  }).format(new Date(value))
}

function getErrorMessage(error: unknown) {
  if (error instanceof Error && error.message.trim()) {
    return error.message
  }
  return 'Ein unerwarteter Fehler ist aufgetreten.'
}

export default App

