import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { alertApi } from "../services/api";

/* â”€â”€ badge maps â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
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
};
const severityCls = (s = "") => SEVERITY_CLS[s.toLowerCase()] ?? "bg-gray-100 text-gray-500";
const statusCls   = (s = "") => STATUS_CLS[s.toLowerCase()]   ?? "bg-gray-100 text-gray-500";

/* â”€â”€ component â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
const AlertDetails = () => {
  const { id } = useParams();
  const [alert,         setAlert]         = useState(null);
  const [loading,       setLoading]       = useState(true);
  const [error,         setError]         = useState("");
  const [actionLoading, setActionLoading] = useState(false);
  const [actionError,   setActionError]   = useState("");
  const [actionSuccess, setActionSuccess] = useState("");

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError("");
      try {
        const fetched = await alertApi.getById(id);
        /* auto-acknowledge when someone opens an OPEN alert */
        if ((fetched.status ?? "").toUpperCase() === "OPEN") {
          const acked = await alertApi.updateStatus(id, "ACKNOWLEDGED");
          setAlert(acked);
        } else {
          setAlert(fetched);
        }
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [id]);

  async function applyStatus(status) {
    setActionLoading(true);
    setActionError("");
    setActionSuccess("");
    try {
      const updated = await alertApi.updateStatus(id, status);
      setAlert(updated);
      setActionSuccess(`Status updated to ${status}`);
    } catch (err) {
      setActionError(err.message);
    } finally {
      setActionLoading(false);
    }
  }

  /* â”€â”€ field row helper â”€â”€ */
  const Field = ({ label, children }) => (
    <div className="flex flex-col gap-0.5">
      <dt className="text-[10px] font-semibold uppercase tracking-widest text-gray-400 dark:text-gray-500">
        {label}
      </dt>
      <dd className="text-sm font-medium text-gray-800 dark:text-gray-100">{children}</dd>
    </div>
  );

  return (
    <div className="space-y-5 max-w-3xl">

      {/* Back link */}
      <Link
        to="/alerts"
        className="inline-flex items-center gap-1.5 text-sm font-medium text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 transition-colors"
      >
        â† Back to Alerts
      </Link>

      {/* Main card */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">

        {/* Card header */}
        <div className="flex items-center justify-between px-6 py-5 border-b border-gray-100 dark:border-gray-700">
          <div>
            <h2 className="text-lg font-bold text-gray-900 dark:text-white tracking-tight">
              Alert <span className="font-mono">#{id}</span>
            </h2>
            <p className="text-xs text-gray-400 dark:text-gray-500 mt-0.5">
              Opening this alert automatically acknowledged it.
            </p>
          </div>
          {alert && (
            <span className={`inline-flex items-center text-xs font-semibold px-3 py-1 rounded-full capitalize ${statusCls(alert.status)}`}>
              {alert.status ?? "â€”"}
            </span>
          )}
        </div>

        {/* Loading */}
        {loading && (
          <div className="flex items-center justify-center py-16">
            <div className="w-8 h-8 rounded-full border-4 border-blue-100 border-t-blue-600 animate-spin" />
          </div>
        )}

        {/* Error */}
        {error && (
          <div className="m-6 px-4 py-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg text-sm text-red-700 dark:text-red-400">
            {error}
          </div>
        )}

        {/* Detail fields */}
        {!loading && !error && alert && (
          <>
            <dl className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-5 px-6 py-6">
              <Field label="Alert ID">
                <span className="font-mono">#{alert.alertId}</span>
              </Field>

              <Field label="Severity">
                <span className={`inline-flex items-center text-[11px] font-semibold px-2 py-0.5 rounded-full capitalize ${severityCls(alert.severity)}`}>
                  {alert.severity ?? "â€”"}
                </span>
              </Field>

              <Field label="Rule ID">
                <span className="font-mono">{alert.ruleId ? `#${alert.ruleId}` : "â€”"}</span>
              </Field>

              <Field label="Transaction ID">
                <span className="font-mono">{alert.transactionId ? `#${alert.transactionId}` : "â€”"}</span>
              </Field>

              <Field label="Note">
                {alert.note ?? "â€”"}
              </Field>

              <Field label="Created">
                {alert.createDate ? new Date(alert.createDate).toLocaleString() : "â€”"}
              </Field>

              <Field label="Closed">
                {alert.closeDate ? new Date(alert.closeDate).toLocaleString() : "Not closed"}
              </Field>
            </dl>

            {/* Action panel */}
            <div className="mx-6 mb-6 rounded-xl bg-gray-50 dark:bg-gray-700/40 border border-gray-100 dark:border-gray-700 p-5">
              <p className="text-sm font-semibold text-gray-800 dark:text-gray-100 mb-1">Actions</p>
              <p className="text-xs text-gray-400 dark:text-gray-500 mb-4">
                Update the alert status to progress through the investigation workflow.
              </p>

              <div className="flex flex-wrap gap-2">
                <button
                  className="text-xs font-semibold px-3 py-2 rounded-lg border border-purple-200 dark:border-purple-800 text-purple-700 dark:text-purple-300 hover:bg-purple-50 dark:hover:bg-purple-900/20 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                  onClick={() => applyStatus("INVESTIGATING")}
                  disabled={actionLoading || (alert.status ?? "").toUpperCase() === "INVESTIGATING"}
                >
                  ðŸ”Ž Investigating
                </button>
                <button
                  className="text-xs font-semibold px-3 py-2 rounded-lg border border-emerald-200 dark:border-emerald-800 text-emerald-700 dark:text-emerald-300 hover:bg-emerald-50 dark:hover:bg-emerald-900/20 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                  onClick={() => applyStatus("CLOSED")}
                  disabled={actionLoading || (alert.status ?? "").toUpperCase() === "CLOSED"}
                >
                  âœ“ Close Alert
                </button>
                <button
                  className="text-xs font-semibold px-3 py-2 rounded-lg border border-gray-200 dark:border-gray-600 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                  onClick={() => applyStatus("DISMISSED")}
                  disabled={actionLoading || (alert.status ?? "").toUpperCase() === "DISMISSED"}
                >
                  âœ• Dismiss
                </button>
              </div>

              {actionSuccess && (
                <p className="mt-3 text-xs font-medium text-emerald-600 dark:text-emerald-400">{actionSuccess}</p>
              )}
              {actionError && (
                <p className="mt-3 text-xs text-red-600 dark:text-red-400">{actionError}</p>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default AlertDetails;
