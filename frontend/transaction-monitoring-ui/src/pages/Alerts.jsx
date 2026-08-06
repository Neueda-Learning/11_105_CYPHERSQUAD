import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { alertApi } from "../services/api";

/* badge maps */
const SEVERITY_CLS = {
  high:     "bg-red-50   text-red-700   ring-1 ring-red-200",
  medium:   "bg-amber-50 text-amber-700 ring-1 ring-amber-200",
  low:      "bg-blue-50  text-blue-700  ring-1 ring-blue-200",
  critical: "bg-rose-50  text-rose-700  ring-1 ring-rose-200",
};
const STATUS_CLS = {
  open:          "bg-amber-50   text-amber-700   ring-1 ring-amber-200",
  closed:        "bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200",
  acknowledged:  "bg-blue-50    text-blue-700    ring-1 ring-blue-200",
  investigating: "bg-purple-50  text-purple-700  ring-1 ring-purple-200",
  dismissed:     "bg-gray-100   text-gray-500    ring-1 ring-gray-200",
  deleted:       "bg-slate-100  text-slate-600   ring-1 ring-slate-300",
};
const severityCls = (s = "") => SEVERITY_CLS[s.toLowerCase()] ?? "bg-gray-100 text-gray-500";
const statusCls   = (s = "") => STATUS_CLS[s.toLowerCase()]   ?? "bg-gray-100 text-gray-500";

const STATUS_FILTERS = [
  { label: "All", value: "" },
  { label: "Open", value: "OPEN" },
  { label: "Acknowledged", value: "ACKNOWLEDGED" },
  { label: "Investigating", value: "INVESTIGATING" },
  { label: "Closed", value: "CLOSED" },
  { label: "Dismissed", value: "DISMISSED" },
];

const Alerts = () => {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showArchive, setShowArchive] = useState(false);
  const [selectedStatus, setSelectedStatus] = useState("");
  const [appliedStatus, setAppliedStatus] = useState("");
  const [quickStatus, setQuickStatus] = useState("");

  useEffect(() => {
    loadAlerts(showArchive);
  }, [showArchive]);

  async function loadAlerts(archiveMode = showArchive) {
    setLoading(true);
    setError("");
    try {
      const list = archiveMode ? await alertApi.getByStatus("DELETED") : await alertApi.getAll();
      setAlerts(list);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  const counts = useMemo(() => ({
    all: alerts.length,
    acknowledged: alerts.filter((a) => (a.status ?? "").toUpperCase() === "ACKNOWLEDGED").length,
    investigating: alerts.filter((a) => (a.status ?? "").toUpperCase() === "INVESTIGATING").length,
  }), [alerts]);

  const visible = useMemo(() => {
    const statusFilter = quickStatus || appliedStatus;
    const list = statusFilter
      ? alerts.filter((a) => (a.status ?? "").toUpperCase() === statusFilter)
      : [...alerts];
    return list.sort((a, b) => new Date(b.createDate) - new Date(a.createDate));
  }, [alerts, appliedStatus, quickStatus]);

  function onApplyFilters() {
    setQuickStatus("");
    setAppliedStatus(selectedStatus);
  }

  function onResetFilters() {
    setSelectedStatus("");
    setAppliedStatus("");
    setQuickStatus("");
  }

  function onQuickFilter(status) {
    setSelectedStatus(status);
    setAppliedStatus("");
    setQuickStatus((prev) => (prev === status ? "" : status));
  }

  async function onCloseAlert(alert) {
    setError("");
    try {
      const updated = await alertApi.updateStatus(alert.alertId, "CLOSED");
      setAlerts((cur) => cur.map((item) => (item.alertId === updated.alertId ? updated : item)));
    } catch (err) {
      setError(err.message);
    }
  }

  async function onDeleteAlert(alertId) {
    setError("");
    try {
      await alertApi.remove(alertId);
      setAlerts((cur) => cur.filter((item) => item.alertId !== alertId));
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="space-y-6">
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">

        <div className="px-5 pt-5 pb-0 border-b border-gray-100 dark:border-gray-700">
          <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
            <div>
              <h2 className="text-lg font-bold text-gray-900 dark:text-white tracking-tight">Alerts</h2>
              <p className="text-xs text-gray-400 dark:text-gray-500 mt-0.5">
                {showArchive
                  ? `${counts.all} deleted alerts`
                  : (counts.all === 0 ? "No alerts" : `${counts.all} total · ${counts.acknowledged} acknowledged · ${counts.investigating} investigating`) }
              </p>
            </div>
            <button
              onClick={() => loadAlerts(showArchive)}
              className="flex items-center gap-1.5 border border-gray-200 dark:border-gray-700 hover:border-blue-300 hover:text-blue-600 dark:hover:border-blue-500 text-gray-500 dark:text-gray-400 text-xs font-medium px-3 py-2 rounded-lg shadow-sm transition-colors bg-white dark:bg-gray-700"
            >
              Refresh
            </button>
          </div>

          <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-center gap-2">
              <button
                type="button"
                title="Show acknowledged alerts"
                onClick={() => onQuickFilter("ACKNOWLEDGED")}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium transition-all ${
                  quickStatus === "ACKNOWLEDGED"
                    ? "bg-blue-100 text-blue-700 ring-1 ring-blue-300"
                    : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                }`}
              >
                <span aria-hidden="true">✓</span>
                Acknowledged
              </button>
              <button
                type="button"
                title="Show investigating alerts"
                onClick={() => onQuickFilter("INVESTIGATING")}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium transition-all ${
                  quickStatus === "INVESTIGATING"
                    ? "bg-purple-100 text-purple-700 ring-1 ring-purple-300"
                    : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                }`}
              >
                <span aria-hidden="true">🔎</span>
                Investigating
              </button>

              <div className="flex items-center gap-2 rounded-lg border border-gray-200 px-2 py-1.5">
                <select
                  className="rounded border border-gray-300 px-2 py-1 text-xs"
                  value={selectedStatus}
                  onChange={(event) => setSelectedStatus(event.target.value)}
                >
                  {STATUS_FILTERS.map((option) => (
                    <option key={option.value} value={option.value}>{option.label}</option>
                  ))}
                </select>
                <button
                  type="button"
                  onClick={onApplyFilters}
                  className="rounded border border-blue-300 px-2 py-1 text-xs text-blue-700"
                >
                  Apply
                </button>
                <button
                  type="button"
                  onClick={onResetFilters}
                  className="rounded border border-gray-300 px-2 py-1 text-xs text-gray-700"
                >
                  Reset
                </button>
              </div>
            </div>

            <button
              type="button"
              title={showArchive ? "Show active alerts" : "Show deleted alerts archive"}
              onClick={() => {
                setShowArchive((prev) => !prev);
                setSelectedStatus("");
                setAppliedStatus("");
                setQuickStatus("");
              }}
              className={`rounded-lg border px-3 py-2 text-xs font-medium transition-colors ${
                showArchive
                  ? "border-slate-300 bg-slate-100 text-slate-700"
                  : "border-gray-300 bg-white text-gray-700 hover:bg-gray-50"
              }`}
            >
              <span aria-hidden="true" className="mr-1">🗂</span>
              {showArchive ? "Archive View" : "Archive"}
            </button>
          </div>
        </div>

        {error && (
          <div className="mx-5 mt-4 px-4 py-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg text-sm text-red-700 dark:text-red-400">
            {error}
          </div>
        )}

        {loading ? (
          <div className="flex items-center justify-center py-16">
            <div className="w-8 h-8 rounded-full border-4 border-blue-100 border-t-blue-600 animate-spin" />
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-gray-50 dark:bg-gray-700/50 border-b border-gray-100 dark:border-gray-700 text-left">
                  <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400 dark:text-gray-500">Alert</th>
                  <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400 dark:text-gray-500">Transaction</th>
                  <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400 dark:text-gray-500">Rule</th>
                  <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400 dark:text-gray-500">Severity</th>
                  <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400 dark:text-gray-500">Status</th>
                  <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400 dark:text-gray-500">Created</th>
                  <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400 dark:text-gray-500">Actions</th>
                </tr>
              </thead>
              <tbody>
                {visible.map((alert, i) => (
                  <tr
                    key={alert.alertId}
                    className={`border-b border-gray-50 dark:border-gray-700/50 hover:bg-blue-50/30 dark:hover:bg-blue-900/10 transition-colors ${
                      i % 2 === 1 ? "bg-gray-50/40 dark:bg-gray-700/20" : "bg-white dark:bg-gray-800"
                    }`}
                  >
                    <td className="px-5 py-3.5">
                      <Link className="font-mono text-xs font-semibold text-blue-600 hover:underline" to={`/alerts/${alert.alertId}`}>
                        #{alert.alertId}
                      </Link>
                    </td>
                    <td className="px-5 py-3.5 text-xs text-gray-600 dark:text-gray-300 font-mono">
                      {alert.transactionId ? `#${alert.transactionId}` : "—"}
                    </td>
                    <td className="px-5 py-3.5 text-xs text-gray-600 dark:text-gray-300 font-mono">
                      {alert.ruleId ? `#${alert.ruleId}` : "—"}
                    </td>
                    <td className="px-5 py-3.5">
                      <span className={`inline-flex items-center text-[11px] font-semibold px-2 py-0.5 rounded-full capitalize ${severityCls(alert.severity)}`}>
                        {alert.severity ?? "—"}
                      </span>
                    </td>
                    <td className="px-5 py-3.5">
                      <span className={`inline-flex items-center text-[11px] font-semibold px-2 py-0.5 rounded-full capitalize ${statusCls(alert.status)}`}>
                        {alert.status ?? "—"}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 text-xs text-gray-500 dark:text-gray-400 whitespace-nowrap">
                      {alert.createDate ? new Date(alert.createDate).toLocaleString() : "—"}
                    </td>
                    <td className="px-5 py-3.5">
                      {showArchive ? (
                        <span className="text-xs text-gray-400">Archived</span>
                      ) : (
                        <div className="flex items-center gap-2">
                          <button
                            className="text-xs font-medium px-2.5 py-1 rounded-lg border border-emerald-200 text-emerald-700 hover:bg-emerald-50 dark:border-emerald-800 dark:text-emerald-400 dark:hover:bg-emerald-900/20 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                            onClick={() => onCloseAlert(alert)}
                            disabled={(alert.status ?? "").toUpperCase() === "CLOSED"}
                          >
                            Close
                          </button>
                          <button
                            className="text-xs font-medium px-2.5 py-1 rounded-lg border border-red-200 text-red-700 hover:bg-red-50 dark:border-red-800 dark:text-red-400 dark:hover:bg-red-900/20 transition-colors"
                            onClick={() => onDeleteAlert(alert.alertId)}
                          >
                            Delete
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {visible.length === 0 && (
              <div className="py-16 text-center">
                <p className="text-sm font-medium text-gray-400 dark:text-gray-500">
                  {showArchive ? "No deleted alerts found" : "No alerts found"}
                </p>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default Alerts;
