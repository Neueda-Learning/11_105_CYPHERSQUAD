import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { alertApi } from "../services/api";

const AlertDetails = () => {
  const { id } = useParams();
  const [alert, setAlert] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionLoading, setActionLoading] = useState(false);
  const [actionError, setActionError] = useState("");

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError("");
      try {
        const fetched = await alertApi.getById(id);
        if ((fetched.status ?? "").toUpperCase() === "OPEN") {
          const acknowledged = await alertApi.updateStatus(id, "ACKNOWLEDGED");
          setAlert(acknowledged);
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

  async function updateStatus(status) {
    setActionLoading(true);
    setActionError("");
    try {
      const updated = await alertApi.updateStatus(id, status);
      setAlert(updated);
    } catch (err) {
      setActionError(err.message);
    } finally {
      setActionLoading(false);
    }
  }

  return (
    <div className="space-y-4">
      <Link to="/alerts" className="inline-block text-sm font-medium text-blue-700 hover:underline">
        Back to Alerts
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

        {!loading && !error && alert ? (
          <div className="mt-6 rounded-lg border border-gray-200 bg-gray-50 p-4">
            <p className="text-sm font-medium text-gray-800">Alert Actions</p>
            <p className="mt-1 text-xs text-gray-500">Opening an OPEN alert marks it as ACKNOWLEDGED automatically.</p>
            <div className="mt-3 flex flex-wrap gap-2">
              <button
                className="rounded-lg border border-indigo-300 px-3 py-2 text-sm text-indigo-700 disabled:opacity-50"
                onClick={() => updateStatus("INVESTIGATING")}
                disabled={actionLoading}
              >
                Mark Investigating
              </button>
              <button
                className="rounded-lg border border-amber-300 px-3 py-2 text-sm text-amber-700 disabled:opacity-50"
                onClick={() => updateStatus("DISMISSED")}
                disabled={actionLoading}
              >
                Dismiss Alert
              </button>
            </div>
            {actionError ? <p className="mt-2 text-xs text-red-600">{actionError}</p> : null}
          </div>
        ) : null}
      </section>
    </div>
  );
};

export default AlertDetails;