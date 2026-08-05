import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { alertApi } from "../services/api";

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
            <div><dt className="text-xs uppercase text-gray-500">Severity</dt><dd className="text-sm text-gray-900">{alert.severity ?? "--"}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Status</dt><dd className="text-sm text-gray-900">{alert.status ?? "--"}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Note</dt><dd className="text-sm text-gray-900">{alert.note ?? "--"}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Created</dt><dd className="text-sm text-gray-900">{alert.createDate ? new Date(alert.createDate).toLocaleString() : "--"}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Closed</dt><dd className="text-sm text-gray-900">{alert.closeDate ? new Date(alert.closeDate).toLocaleString() : "--"}</dd></div>
          </dl>
        ) : null}
      </section>
    </div>
  );
};

export default AlertDetails;