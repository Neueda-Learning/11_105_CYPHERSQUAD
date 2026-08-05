import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { alertApi } from "../services/api";

const Alerts = () => {
  const [alerts, setAlerts] = useState([]);
  const [statusFilter, setStatusFilter] = useState("");
  const [viewMode, setViewMode] = useState("active");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const sortedAlerts = useMemo(
    () => [...alerts].sort((a, b) => new Date(b.createDate) - new Date(a.createDate)),
    [alerts]
  );

  useEffect(() => {
    loadAlerts(viewMode);
  }, [viewMode]);

  async function loadAlerts(mode = "active") {
    setLoading(true);
    setError("");
    try {
      if (mode === "archived") {
        setAlerts(await alertApi.getByStatus("DELETED"));
      } else {
        setAlerts(await alertApi.getAll());
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function onFilterByStatus() {
    if (!statusFilter.trim()) {
      loadAlerts(viewMode);
      return;
    }
    setLoading(true);
    setError("");
    try {
      setAlerts(await alertApi.getByStatus(statusFilter.trim().toUpperCase()));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function onQuickFilter(status) {
    setLoading(true);
    setError("");
    setStatusFilter(status);
    try {
      setAlerts(await alertApi.getByStatus(status));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function onCloseAlert(alert) {
    setError("");
    try {
      const updated = await alertApi.updateStatus(alert.alertId, "CLOSED");
      setAlerts((current) => current.map((item) => (item.alertId === updated.alertId ? updated : item)));
    } catch (err) {
      setError(err.message);
    }
  }

  async function onDeleteAlert(alertId) {
    setError("");
    try {
      await alertApi.remove(alertId);
      setAlerts((current) => current.filter((item) => item.alertId !== alertId));
    } catch (err) {
      setError(err.message);
    }
  }

  function toggleArchiveView() {
    setStatusFilter("");
    setViewMode((current) => (current === "active" ? "archived" : "active"));
  }

  return (
    <div className="space-y-6">
      <section className="rounded-xl border border-gray-200 bg-white p-4 md:p-6">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <h2 className="text-lg font-semibold text-gray-900">
            {viewMode === "archived" ? "Archived Alerts" : "Alerts"}
          </h2>
          <div className="flex items-center gap-2">
            <button
              className="rounded-lg border border-amber-300 px-3 py-2 text-sm text-amber-700"
              onClick={() => onQuickFilter("ACKNOWLEDGED")}
              title="Acknowledged alerts"
            >
              ✓
            </button>
            <button
              className="rounded-lg border border-indigo-300 px-3 py-2 text-sm text-indigo-700"
              onClick={() => onQuickFilter("INVESTIGATING")}
              title="Investigating alerts"
            >
              🔎
            </button>
            <input
              className="rounded-lg border border-gray-300 px-3 py-2 text-sm"
              placeholder="Filter status (OPEN, ACKNOWLEDGED, CLOSED...)"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
            />
            <button className="rounded-lg border border-gray-300 px-3 py-2 text-sm" onClick={onFilterByStatus}>Apply</button>
            <button className="rounded-lg border border-gray-300 px-3 py-2 text-sm" onClick={() => loadAlerts(viewMode)}>Reset</button>
            <button
              className="rounded-lg border border-gray-300 px-3 py-2 text-sm"
              onClick={toggleArchiveView}
              title={viewMode === "archived" ? "Show active alerts" : "Show archived alerts"}
            >
              🗂
            </button>
          </div>
        </div>

        {loading ? <p className="text-sm text-gray-500">Loading alerts...</p> : null}
        {error ? <p className="mb-3 text-sm text-red-600">{error}</p> : null}

        {!loading ? (
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="border-b border-gray-200 text-left text-xs uppercase text-gray-500">
                  <th className="py-3 pr-4">Alert</th>
                  <th className="py-3 pr-4">Transaction</th>
                  <th className="py-3 pr-4">Rule</th>
                  <th className="py-3 pr-4">Severity</th>
                  <th className="py-3 pr-4">Status</th>
                  <th className="py-3 pr-4">Created</th>
                  <th className="py-3 pr-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                {sortedAlerts.map((alert) => (
                  <tr key={alert.alertId} className="border-b border-gray-100">
                    <td className="py-3 pr-4">
                      <Link className="font-medium text-blue-700 hover:underline" to={`/alerts/${alert.alertId}`}>
                        #{alert.alertId}
                      </Link>
                    </td>
                    <td className="py-3 pr-4">{alert.transactionId ?? "--"}</td>
                    <td className="py-3 pr-4">{alert.ruleId ?? "--"}</td>
                    <td className="py-3 pr-4">{alert.severity ?? "--"}</td>
                    <td className="py-3 pr-4">{alert.status ?? "--"}</td>
                    <td className="py-3 pr-4">{alert.createDate ? new Date(alert.createDate).toLocaleString() : "--"}</td>
                    <td className="py-3 pr-4">
                      <div className="flex flex-wrap gap-2">
                        {viewMode === "active" ? (
                          <>
                            <button
                              className="rounded border border-green-300 px-2 py-1 text-xs text-green-700 disabled:opacity-50"
                              onClick={() => onCloseAlert(alert)}
                              disabled={(alert.status ?? "").toUpperCase() === "CLOSED"}
                            >
                              Close
                            </button>
                            <button className="rounded border border-red-300 px-2 py-1 text-xs text-red-700" onClick={() => onDeleteAlert(alert.alertId)}>
                              Delete
                            </button>
                          </>
                        ) : (
                          <span className="text-xs text-gray-500">Archived</span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {sortedAlerts.length === 0 ? <p className="pt-4 text-sm text-gray-500">No alerts found.</p> : null}
          </div>
        ) : null}
      </section>
    </div>
  );
};

export default Alerts;