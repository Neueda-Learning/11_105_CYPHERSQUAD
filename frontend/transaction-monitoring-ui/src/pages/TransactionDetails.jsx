import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { transactionApi } from "../services/api";

const TransactionDetails = () => {
  const { id } = useParams();
  const [transaction, setTransaction] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError("");
      try {
        const data = await transactionApi.getById(id);
        setTransaction(data);
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
      <Link to="/transactions" className="inline-block text-sm font-medium text-blue-700 hover:underline">
        Back to Transactions
      </Link>

      <section className="rounded-xl border border-gray-200 bg-white p-6">
        <h2 className="text-lg font-semibold text-gray-900">Transaction #{id}</h2>
        {loading ? <p className="mt-4 text-sm text-gray-500">Loading...</p> : null}
        {error ? <p className="mt-4 text-sm text-red-600">{error}</p> : null}

        {!loading && !error && transaction ? (
          <dl className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div><dt className="text-xs uppercase text-gray-500">Transaction ID</dt><dd className="text-sm text-gray-900">{transaction.transactionId}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Account ID</dt><dd className="text-sm text-gray-900">{transaction.accountId}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Payee ID</dt><dd className="text-sm text-gray-900">{transaction.payeeId ?? "--"}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Amount</dt><dd className="text-sm text-gray-900">{transaction.amount} {transaction.currency}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Amount USD</dt><dd className="text-sm text-gray-900">{transaction.amountUsd ?? "--"}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Type</dt><dd className="text-sm text-gray-900">{transaction.type}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Status</dt><dd className="text-sm text-gray-900">{transaction.status}</dd></div>
            <div><dt className="text-xs uppercase text-gray-500">Timestamp</dt><dd className="text-sm text-gray-900">{transaction.timestamp ? new Date(transaction.timestamp).toLocaleString() : "--"}</dd></div>
          </dl>
        ) : null}
      </section>
    </div>
  );
};

export default TransactionDetails;