import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { currencyApi, transactionApi } from "../services/api";

const INITIAL_FORM = {
  accountId: "",
  payeeId: "",
  amount: "",
  currency: "",
  type: "DEBIT",
  status: "SUCCESS",
};

const INITIAL_FILTERS = {
  transactionId: "",
  accountId: "",
  payeeId: "",
  amountMin: "",
  amountMax: "",
  amountUsdMin: "",
  amountUsdMax: "",
  currency: "",
  type: "",
  status: "",
  timestampFrom: "",
  timestampTo: "",
};

function formatUsd(value) {
  if (value == null) return "--";
  return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(value);
}

const Transactions = () => {
  const [transactions, setTransactions] = useState([]);
  const [currencies, setCurrencies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currencyLoading, setCurrencyLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [formError, setFormError] = useState("");
  const [formData, setFormData] = useState(INITIAL_FORM);
  const [filters, setFilters] = useState(INITIAL_FILTERS);

  const activeCurrencies = useMemo(
    () => currencies.filter((currency) => currency.active).sort((a, b) => a.currencyCode.localeCompare(b.currencyCode)),
    [currencies]
  );

  const sortedTransactions = useMemo(
    () => [...transactions].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp)),
    [transactions]
  );

  const filteredTransactions = useMemo(() => {
    const toNumber = (value) => {
      if (value === "") return null;
      const parsed = Number(value);
      return Number.isNaN(parsed) ? null : parsed;
    };

    const transactionId = toNumber(filters.transactionId);
    const accountId = toNumber(filters.accountId);
    const payeeId = toNumber(filters.payeeId);
    const amountMin = toNumber(filters.amountMin);
    const amountMax = toNumber(filters.amountMax);
    const amountUsdMin = toNumber(filters.amountUsdMin);
    const amountUsdMax = toNumber(filters.amountUsdMax);
    const currency = filters.currency.trim().toUpperCase();
    const type = filters.type.trim().toUpperCase();
    const status = filters.status.trim().toUpperCase();
    const timestampFromMs = filters.timestampFrom ? new Date(filters.timestampFrom).getTime() : null;
    const timestampToMs = filters.timestampTo ? new Date(filters.timestampTo).getTime() : null;

    return sortedTransactions.filter((transaction) => {
      const txAmount = Number(transaction.amount);
      const txAmountUsd = transaction.amountUsd == null ? null : Number(transaction.amountUsd);
      const txTimeMs = transaction.timestamp ? new Date(transaction.timestamp).getTime() : null;

      if (transactionId != null && Number(transaction.transactionId) !== transactionId) return false;
      if (accountId != null && Number(transaction.accountId) !== accountId) return false;
      if (payeeId != null && Number(transaction.payeeId) !== payeeId) return false;

      if (amountMin != null && (Number.isNaN(txAmount) || txAmount < amountMin)) return false;
      if (amountMax != null && (Number.isNaN(txAmount) || txAmount > amountMax)) return false;

      if (amountUsdMin != null && (txAmountUsd == null || Number.isNaN(txAmountUsd) || txAmountUsd < amountUsdMin)) return false;
      if (amountUsdMax != null && (txAmountUsd == null || Number.isNaN(txAmountUsd) || txAmountUsd > amountUsdMax)) return false;

      if (currency && String(transaction.currency || "").toUpperCase() !== currency) return false;
      if (type && String(transaction.type || "").toUpperCase() !== type) return false;
      if (status && String(transaction.status || "").toUpperCase() !== status) return false;

      if (timestampFromMs != null) {
        if (txTimeMs == null || Number.isNaN(txTimeMs) || txTimeMs < timestampFromMs) return false;
      }
      if (timestampToMs != null) {
        if (txTimeMs == null || Number.isNaN(txTimeMs) || txTimeMs > timestampToMs) return false;
      }

      return true;
    });
  }, [sortedTransactions, filters]);

  const filterOptions = useMemo(() => {
    const uniqueSorted = (values) => [...new Set(values)].filter(Boolean).sort((a, b) => a.localeCompare(b));
    return {
      currencies: uniqueSorted(transactions.map((tx) => String(tx.currency || "").toUpperCase())),
      types: uniqueSorted(transactions.map((tx) => String(tx.type || "").toUpperCase())),
      statuses: uniqueSorted(transactions.map((tx) => String(tx.status || "").toUpperCase())),
    };
  }, [transactions]);

  useEffect(() => {
    loadTransactions();
    loadCurrencies();
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

  async function loadCurrencies() {
    setCurrencyLoading(true);
    try {
      const data = await currencyApi.getAll();
      setCurrencies(data);

      const active = data.filter((currency) => currency.active);
      if (active.length > 0) {
        setFormData((current) => {
          if (current.currency) {
            return current;
          }
          return { ...current, currency: active[0].currencyCode };
        });
      }
    } catch (err) {
      setFormError(err.message);
    } finally {
      setCurrencyLoading(false);
    }
  }

  function onChange(event) {
    const { name, value } = event.target;
    setFormData((current) => ({ ...current, [name]: value }));
  }

  function onFilterChange(event) {
    const { name, value } = event.target;
    setFilters((current) => ({ ...current, [name]: value }));
  }

  function clearFilters() {
    setFilters(INITIAL_FILTERS);
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

      if (!payload.currency) {
        throw new Error("Select a currency from configured currency rates");
      }

      const created = await transactionApi.create(payload);
      setTransactions((current) => [created, ...current]);
      setFormData((current) => ({
        ...INITIAL_FORM,
        currency: current.currency,
      }));
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
            <select
              className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2"
              name="currency"
              value={formData.currency}
              onChange={onChange}
              required
              disabled={currencyLoading || activeCurrencies.length === 0}
            >
              {currencyLoading ? <option value="">Loading currencies...</option> : null}
              {!currencyLoading && activeCurrencies.length === 0 ? (
                <option value="">No active currencies available</option>
              ) : null}
              {!currencyLoading && activeCurrencies.length > 0
                ? activeCurrencies.map((currency) => (
                    <option key={currency.currencyId} value={currency.currencyCode}>
                      {currency.currencyCode} - {currency.currencyName}
                    </option>
                  ))
                : null}
            </select>
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
              disabled={submitting || currencyLoading || activeCurrencies.length === 0}
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

        <div className="mb-4 rounded-lg border border-gray-200 bg-gray-50 p-3">
          <div className="mb-2 flex items-center justify-between">
            <h3 className="text-sm font-semibold text-gray-800">Filters</h3>
            <button className="rounded-lg border border-gray-300 bg-white px-3 py-1 text-xs" onClick={clearFilters} type="button">
              Clear Filters
            </button>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <input
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
              name="transactionId"
              type="number"
              min="1"
              placeholder="ID"
              value={filters.transactionId}
              onChange={onFilterChange}
            />
            <input
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
              name="accountId"
              type="number"
              min="1"
              placeholder="Account ID"
              value={filters.accountId}
              onChange={onFilterChange}
            />
            <input
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
              name="payeeId"
              type="number"
              min="1"
              placeholder="Payee ID"
              value={filters.payeeId}
              onChange={onFilterChange}
            />
            <select
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
              name="currency"
              value={filters.currency}
              onChange={onFilterChange}
            >
              <option value="">All currencies</option>
              {filterOptions.currencies.map((code) => (
                <option key={code} value={code}>
                  {code}
                </option>
              ))}
            </select>

            <input
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
              name="amountMin"
              type="number"
              step="0.0001"
              placeholder="Amount min"
              value={filters.amountMin}
              onChange={onFilterChange}
            />
            <input
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
              name="amountMax"
              type="number"
              step="0.0001"
              placeholder="Amount max"
              value={filters.amountMax}
              onChange={onFilterChange}
            />
            <input
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
              name="amountUsdMin"
              type="number"
              step="0.0001"
              placeholder="Amount USD min"
              value={filters.amountUsdMin}
              onChange={onFilterChange}
            />
            <input
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
              name="amountUsdMax"
              type="number"
              step="0.0001"
              placeholder="Amount USD max"
              value={filters.amountUsdMax}
              onChange={onFilterChange}
            />

            <select
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
              name="type"
              value={filters.type}
              onChange={onFilterChange}
            >
              <option value="">All types</option>
              {filterOptions.types.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
            <select
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
              name="status"
              value={filters.status}
              onChange={onFilterChange}
            >
              <option value="">All statuses</option>
              {filterOptions.statuses.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
            <input
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
              name="timestampFrom"
              type="datetime-local"
              value={filters.timestampFrom}
              onChange={onFilterChange}
            />
            <input
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
              name="timestampTo"
              type="datetime-local"
              value={filters.timestampTo}
              onChange={onFilterChange}
            />
          </div>
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
                {filteredTransactions.map((transaction) => (
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
            {filteredTransactions.length === 0 ? <p className="pt-4 text-sm text-gray-500">No transactions match filters.</p> : null}
          </div>
        ) : null}
      </section>
    </div>
  );
};

export default Transactions;