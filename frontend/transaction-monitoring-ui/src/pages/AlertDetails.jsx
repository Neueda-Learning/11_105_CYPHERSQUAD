import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { alertApi } from "../services/api";

const AlertDetails = () => {
  const { id } = useParams();
  const [alert, setAlert] = useState(null);
  const [loading, setLoading] = useState(true);
  const [statusLoading, setStatusLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError("");
      try {
        const fetched = await alertApi.getById(id);

        // First open of an OPEN alert should auto-acknowledge.
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

  async function updateStatus(nextStatus) {
    setStatusLoading(true);
    setError("");
    try {
      const updated = await alertApi.updateStatus(id, nextStatus);
      setAlert(updated);
    } catch (err) {
      setError(err.message);
    } finally {
      setStatusLoading(false);
    }
  }

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
          <>
            <dl className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div><dt className="text-xs uppercase text-gray-500">Alert ID</dt><dd className="text-sm text-gray-900">{alert.alertId}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Rule ID</dt><dd className="text-sm text-gray-900">{alert.ruleId ?? "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Transaction ID</dt><dd className="text-sm text-gray-900">{alert.transactionId ?? "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Severity</dt><dd className="text-sm text-gray-900">{alert.severity ?? "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Status</dt><dd className="text-sm text-gray-900">{alert.status ?? "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Note</dt><dd className="text-sm text-gray-900">{alert.note ?? "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Created</dt><dd className="text-sm text-gray-900">{alert.createDate ? new Date(alert.createDate).toLocaleString() : "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Closed</dt><dd className="text-sm text-gray-900">{alert.closeDate ? new Date(alert.closeDate).toLocaleString() : "--"}</dd></div>
            </dl>

            <div className="mt-5 flex flex-wrap items-center gap-2">
              <button
                type="button"
                onClick={() => updateStatus("INVESTIGATING")}
                disabled={statusLoading || (alert.status ?? "").toUpperCase() === "INVESTIGATING"}
                className="rounded-lg border border-purple-300 px-3 py-2 text-sm font-medium text-purple-700 disabled:opacity-50"
              >
                {statusLoading ? "Updating..." : "Investigating"}
              </button>
              <button
                type="button"
                onClick={() => updateStatus("DISMISSED")}
                disabled={statusLoading || (alert.status ?? "").toUpperCase() === "DISMISSED"}
                className="rounded-lg border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 disabled:opacity-50"
              >
                {statusLoading ? "Updating..." : "Dismissed"}
              </button>
            </div>
          </>
        ) : null}
      </section>
    </div>
  );
};

export default AlertDetails;
