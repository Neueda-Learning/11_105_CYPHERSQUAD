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
  const [alert, setAlert] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError("");
      try {
        setAlert(await alertApi.getById(id));
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [id]);

  return (
    <div className="space-y-5 max-w-3xl">

      {/* Back link */}
      <Link
        to="/alerts"
        className="inline-flex items-center gap-1.5 text-sm font-medium text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 transition-colors"
      >
        â† Back to Alerts
      </Link>

      <section className="rounded-xl border border-gray-200 bg-white p-6">
        <h2 className="text-lg font-semibold text-gray-900">Alert #{id}</h2>
        {loading ? <p className="mt-4 text-sm text-gray-500">Loading...</p> : null}
        {error ? <p className="mt-4 text-sm text-red-600">{error}</p> : null}

        {!loading && !error && alert ? (
          <dl className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div><dt className="text-xs uppercase text-gray-500">Alert ID</dt><dd className="text-sm text-gray-900">{alert.alertId}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Rule ID</dt><dd className="text-sm text-gray-900">{alert.ruleId ?? "--"}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Transaction ID</dt><dd className="text-sm text-gray-900">{alert.transactionId ?? "--"}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Account ID</dt><dd className="text-sm text-gray-900">{alert.accountId ?? "--"}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Severity</dt><dd className="text-sm text-gray-900">{alert.severity ?? "--"}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Status</dt><dd className="text-sm text-gray-900">{alert.status ?? "--"}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Description</dt><dd className="text-sm text-gray-900">{alert.description ?? "--"}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Created</dt><dd className="text-sm text-gray-900">{alert.createDate ? new Date(alert.createDate).toLocaleString() : "--"}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Closed</dt><dd className="text-sm text-gray-900">{alert.closeDate ? new Date(alert.closeDate).toLocaleString() : "--"}</dd></div>
          </dl>
        ) : null}
      </section>
    </div>
  );
};

export default AlertDetails;
