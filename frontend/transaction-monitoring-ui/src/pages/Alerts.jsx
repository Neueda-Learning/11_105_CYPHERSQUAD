import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { alertApi } from "../services/api";

/* ── badge maps ─────────────────────────────────────────────── */
const SEVERITY_CLS = {
  high:     "bg-red-50   text-red-700   ring-1 ring-red-200",
  medium:   "bg-amber-50 text-amber-700 ring-1 ring-amber-200",
  low:      "bg-blue-50  text-blue-700  ring-1 ring-blue-200",
  critical: "bg-rose-50  text-rose-700  ring-1 ring-rose-200",
};
const STATUS_CLS = {
  open:   "bg-amber-50   text-amber-700   ring-1 ring-amber-200",
  closed: "bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200",
};
const severityCls = (s = "") => SEVERITY_CLS[s.toLowerCase()] ?? "bg-gray-100 text-gray-500";
const statusCls   = (s = "") => STATUS_CLS[s.toLowerCase()]   ?? "bg-gray-100 text-gray-500";

const STATUS_TABS = [
  { label: "All",    value: "" },
  { label: "Open",   value: "OPEN" },
  { label: "Closed", value: "CLOSED" },
];

/* ── component ──────────────────────────────────────────────── */
const Alerts = () => {
  const [alerts,       setAlerts]       = useState([]);
  const [activeTab,    setActiveTab]    = useState("");
  const [loading,      setLoading]      = useState(true);
  const [error,        setError]        = useState("");

  useEffect(() => { loadAlerts(); }, []);

  async function loadAlerts() {
    setLoading(true);
    setError("");
    try {
      setAlerts(await alertApi.getAll());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  /* tab counts */
  const counts = useMemo(() => ({
    "":       alerts.length,
    "OPEN":   alerts.filter((a) => (a.status ?? "").toUpperCase() === "OPEN").length,
    "CLOSED": alerts.filter((a) => (a.status ?? "").toUpperCase() === "CLOSED").length,
  }), [alerts]);

  /* filtered + sorted list */
  const visible = useMemo(() => {
    const list = activeTab
      ? alerts.filter((a) => (a.status ?? "").toUpperCase() === activeTab)
      : [...alerts];
    return list.sort((a, b) => new Date(b.createDate) - new Date(a.createDate));
  }, [alerts, activeTab]);

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
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">

        {/* ── header + tabs ── */}
        <div className="px-5 pt-5 pb-0 border-b border-gray-100">
          <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
            <div>
              <h2 className="text-lg font-bold text-gray-900 tracking-tight">Alerts</h2>
              <p className="text-xs text-gray-400 mt-0.5">
                {counts[""] === 0 ? "No alerts" : `${counts[""]} total · ${counts["OPEN"]} open`}
              </p>
            </div>
            <button
              onClick={loadAlerts}
              className="flex items-center gap-1.5 border border-gray-200 hover:border-blue-300 hover:text-blue-600 text-gray-500 text-xs font-medium px-3 py-2 rounded-lg shadow-sm transition-colors bg-white"
            >
              ↻ Refresh
            </button>
          </div>

          {/* status tab bar */}
          <div className="flex gap-1 bg-gray-100 rounded-lg p-1 w-fit">
            {STATUS_TABS.map((tab) => (
              <button
                key={tab.value}
                onClick={() => setActiveTab(tab.value)}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium transition-all ${
                  activeTab === tab.value
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-500 hover:text-gray-700"
                }`}
              >
                {tab.label}
                <span
                  className={`text-[10px] px-1.5 py-0.5 rounded-full font-semibold tabular-nums ${
                    activeTab === tab.value
                      ? "bg-blue-100 text-blue-700"
                      : "bg-gray-200 text-gray-500"
                  }`}
                >
                  {counts[tab.value]}
                </span>
              </button>
            ))}
          </div>
        </div>

        {/* ── error ── */}
        {error && (
          <div className="mx-5 mt-4 px-4 py-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
            {error}
          </div>
        )}

        {/* ── loading ── */}
        {loading ? (
          <div className="flex items-center justify-center py-16">
            <div className="w-8 h-8 rounded-full border-4 border-blue-100 border-t-blue-600 animate-spin" />
          </div>
        ) : (
          <>
            {/* ── table ── */}
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-gray-50 border-b border-gray-100 text-left">
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400">Alert</th>
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400">Transaction</th>
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400">Rule</th>
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400">Severity</th>
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400">Status</th>
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400">Created</th>
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {visible.map((alert, i) => (
                    <tr
                      key={alert.alertId}
                      className={`border-b border-gray-50 hover:bg-blue-50/30 transition-colors ${i % 2 === 1 ? "bg-gray-50/40" : "bg-white"}`}
                    >
                      <td className="px-5 py-3.5">
                        <Link className="font-mono text-xs font-semibold text-blue-600 hover:underline" to={`/alerts/${alert.alertId}`}>
                          #{alert.alertId}
                        </Link>
                      </td>
                      <td className="px-5 py-3.5 text-xs text-gray-600 font-mono">
                        {alert.transactionId ? `#${alert.transactionId}` : "—"}
                      </td>
                      <td className="px-5 py-3.5 text-xs text-gray-600 font-mono">
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
                      <td className="px-5 py-3.5 text-xs text-gray-500 whitespace-nowrap">
                        {alert.createDate ? new Date(alert.createDate).toLocaleString() : "—"}
                      </td>
                      <td className="px-5 py-3.5">
                        <div className="flex items-center gap-2">
                          <button
                            className="text-xs font-medium px-2.5 py-1 rounded-lg border border-emerald-200 text-emerald-700 hover:bg-emerald-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                            onClick={() => onCloseAlert(alert)}
                            disabled={(alert.status ?? "").toUpperCase() === "CLOSED"}
                          >
                            Close
                          </button>
                          <button
                            className="text-xs font-medium px-2.5 py-1 rounded-lg border border-red-200 text-red-700 hover:bg-red-50 transition-colors"
                            onClick={() => onDeleteAlert(alert.alertId)}
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {visible.length === 0 && (
                <div className="py-16 text-center">
                  <p className="text-sm font-medium text-gray-400">
                    {activeTab ? `No ${activeTab.toLowerCase()} alerts` : "No alerts found"}
                  </p>
                </div>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default Alerts;