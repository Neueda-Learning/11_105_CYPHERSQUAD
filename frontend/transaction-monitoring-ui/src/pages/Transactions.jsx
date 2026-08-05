import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { transactionApi } from "../services/api";

const INITIAL_FORM = {
  accountId: "",
  payeeId: "",
  amount: "",
  currency: "USD",
  type: "DEBIT",
  status: "SUCCESS",
};

function formatUsd(value) {
  if (value == null) return "--";
  return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(value);
}

const Transactions = () => {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [formError, setFormError] = useState("");
  const [formData, setFormData] = useState(INITIAL_FORM);

  const sortedTransactions = useMemo(
    () => [...transactions].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp)),
    [transactions]
  );

  useEffect(() => {
    loadTransactions();
  }, []);

  async function loadTransactions() {
    setLoading(true);
    setError("");
    try {
      const data = await transactionApi.getAll();
      setTransactions(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function onChange(event) {
    const { name, value } = event.target;
    setFormData((current) => ({ ...current, [name]: value }));
  }

  async function onSubmit(event) {
    event.preventDefault();
    setFormError("");
    setSubmitting(true);

    try {
      const payload = {
        accountId: Number(formData.accountId),
        payeeId: formData.payeeId ? Number(formData.payeeId) : null,
        amount: Number(formData.amount),
        currency: formData.currency.trim().toUpperCase(),
        type: formData.type,
        status: formData.status,
      };

      if (!payload.accountId || !payload.amount) {
        throw new Error("accountId and amount are required");
      }

      const created = await transactionApi.create(payload);
      setTransactions((current) => [created, ...current]);
      setFormData(INITIAL_FORM);
    } catch (err) {
      setFormError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="space-y-6">
      <section className="rounded-xl border border-gray-200 bg-white p-4 md:p-6">
        <h2 className="text-lg font-semibold text-gray-900">Create Transaction</h2>
        <p className="mt-1 text-sm text-gray-500">Amount in USD is calculated by backend using currency rates.</p>

        <form className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3" onSubmit={onSubmit}>
          <label className="text-sm font-medium text-gray-700">
            Account ID
            <input
              className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2"
              name="accountId"
              type="number"
              min="1"
              value={formData.accountId}
              onChange={onChange}
              required
            />
          </label>

          <label className="text-sm font-medium text-gray-700">
            Payee ID
            <input
              className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2"
              name="payeeId"
              type="number"
              min="1"
              value={formData.payeeId}
              onChange={onChange}
            />
          </label>

          <label className="text-sm font-medium text-gray-700">
            Amount
            <input
              className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2"
              name="amount"
              type="number"
              min="0"
              step="0.0001"
              value={formData.amount}
              onChange={onChange}
              required
            />
          </label>

          <label className="text-sm font-medium text-gray-700">
            Currency
            <input
              className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 uppercase"
              name="currency"
              maxLength={10}
              value={formData.currency}
              onChange={onChange}
              required
            />
          </label>

          <label className="text-sm font-medium text-gray-700">
            Type
            <select className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="type" value={formData.type} onChange={onChange}>
              <option value="DEBIT">DEBIT</option>
              <option value="CREDIT">CREDIT</option>
            </select>
          </label>

          <label className="text-sm font-medium text-gray-700">
            Status
            <select className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="status" value={formData.status} onChange={onChange}>
              <option value="SUCCESS">SUCCESS</option>
              <option value="PENDING">PENDING</option>
              <option value="FAILED">FAILED</option>
            </select>
          </label>

          <div className="sm:col-span-2 lg:col-span-3 flex items-center gap-3">
            <button
              className="rounded-lg bg-blue-600 px-4 py-2 font-medium text-white disabled:bg-blue-300"
              type="submit"
              disabled={submitting}
            >
              {submitting ? "Creating..." : "Create Transaction"}
            </button>
            {formError && <p className="text-sm text-red-600">{formError}</p>}
          </div>
        </form>
      </section>

      <section className="rounded-xl border border-gray-200 bg-white p-4 md:p-6">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-gray-900">Transactions</h2>
          <button className="rounded-lg border border-gray-300 px-3 py-2 text-sm" onClick={loadTransactions}>
            Refresh
          </button>
        </div>

        {loading ? <p className="text-sm text-gray-500">Loading transactions...</p> : null}
        {error ? <p className="text-sm text-red-600">{error}</p> : null}

        {!loading && !error ? (
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="border-b border-gray-200 text-left text-xs uppercase text-gray-500">
                  <th className="py-3 pr-4">ID</th>
                  <th className="py-3 pr-4">Account</th>
                  <th className="py-3 pr-4">Payee</th>
                  <th className="py-3 pr-4">Amount</th>
                  <th className="py-3 pr-4">Amount USD</th>
                  <th className="py-3 pr-4">Type</th>
                  <th className="py-3 pr-4">Status</th>
                  <th className="py-3 pr-4">Time</th>
                </tr>
              </thead>
              <tbody>
                {sortedTransactions.map((transaction) => (
                  <tr key={transaction.transactionId} className="border-b border-gray-100">
                    <td className="py-3 pr-4">
                      <Link className="font-medium text-blue-700 hover:underline" to={`/transactions/${transaction.transactionId}`}>
                        #{transaction.transactionId}
                      </Link>
                    </td>
                    <td className="py-3 pr-4">{transaction.accountId}</td>
                    <td className="py-3 pr-4">{transaction.payeeId ?? "--"}</td>
                    <td className="py-3 pr-4">{transaction.amount} {transaction.currency}</td>
                    <td className="py-3 pr-4">{formatUsd(transaction.amountUsd)}</td>
                    <td className="py-3 pr-4">{transaction.type}</td>
                    <td className="py-3 pr-4">{transaction.status}</td>
                    <td className="py-3 pr-4">{transaction.timestamp ? new Date(transaction.timestamp).toLocaleString() : "--"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            {sortedTransactions.length === 0 ? <p className="pt-4 text-sm text-gray-500">No transactions found.</p> : null}
          </div>
        ) : null}
      </section>
    </div>
  );
};

export default Transactions;