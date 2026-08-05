import { useEffect, useMemo, useState } from "react";
import { currencyApi } from "../services/api";

const INITIAL_FORM = {
  currencyCode: "",
  currencyName: "",
  usdRate: "",
  active: true,
};

const Currency = () => {
  const [currencies, setCurrencies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [formData, setFormData] = useState(INITIAL_FORM);
  const [submitting, setSubmitting] = useState(false);

  const sortedCurrencies = useMemo(
    () => [...currencies].sort((a, b) => a.currencyCode.localeCompare(b.currencyCode)),
    [currencies]
  );

  useEffect(() => {
    loadCurrencies();
  }, []);

  async function loadCurrencies() {
    setLoading(true);
    setError("");
    try {
      setCurrencies(await currencyApi.getAll());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function onChange(event) {
    const { name, value, type, checked } = event.target;
    setFormData((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
  }

  async function onCreate(event) {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      const payload = {
        currencyCode: formData.currencyCode.trim().toUpperCase(),
        currencyName: formData.currencyName.trim(),
        usdRate: Number(formData.usdRate),
        active: formData.active,
      };
      const created = await currencyApi.create(payload);
      setCurrencies((current) => [...current, created]);
      setFormData(INITIAL_FORM);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function onToggleActive(currency) {
    setError("");
    try {
      const updated = await currencyApi.patch(currency.currencyId, { active: !currency.active });
      setCurrencies((current) => current.map((item) => (item.currencyId === updated.currencyId ? updated : item)));
    } catch (err) {
      setError(err.message);
    }
  }

  async function onDelete(currencyId) {
    setError("");
    try {
      await currencyApi.remove(currencyId);
      setCurrencies((current) => current.filter((item) => item.currencyId !== currencyId));
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="space-y-6">
      <section className="rounded-xl border border-gray-200 bg-white p-4 md:p-6">
        <h2 className="text-lg font-semibold text-gray-900">Add Currency Rate</h2>
        <form className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4" onSubmit={onCreate}>
          <label className="text-sm font-medium text-gray-700">Currency Code
            <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 uppercase" maxLength={10} name="currencyCode" value={formData.currencyCode} onChange={onChange} required />
          </label>
          <label className="text-sm font-medium text-gray-700">Currency Name
            <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="currencyName" value={formData.currencyName} onChange={onChange} required />
          </label>
          <label className="text-sm font-medium text-gray-700">USD Rate
            <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" type="number" min="0" step="0.00000001" name="usdRate" value={formData.usdRate} onChange={onChange} required />
          </label>
          <label className="flex items-center gap-2 text-sm font-medium text-gray-700 self-end pb-2">
            <input name="active" type="checkbox" checked={formData.active} onChange={onChange} />
            Active
          </label>
          <div className="sm:col-span-2 lg:col-span-4">
            <button className="rounded-lg bg-blue-600 px-4 py-2 font-medium text-white disabled:bg-blue-300" type="submit" disabled={submitting}>
              {submitting ? "Creating..." : "Create Currency"}
            </button>
          </div>
        </form>
      </section>

      <section className="rounded-xl border border-gray-200 bg-white p-4 md:p-6">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-gray-900">Currency Rates</h2>
          <button className="rounded-lg border border-gray-300 px-3 py-2 text-sm" onClick={loadCurrencies}>Refresh</button>
        </div>

        {loading ? <p className="text-sm text-gray-500">Loading currencies...</p> : null}
        {error ? <p className="mb-3 text-sm text-red-600">{error}</p> : null}

        {!loading ? (
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="border-b border-gray-200 text-left text-xs uppercase text-gray-500">
                  <th className="py-3 pr-4">Code</th>
                  <th className="py-3 pr-4">Name</th>
                  <th className="py-3 pr-4">USD Rate</th>
                  <th className="py-3 pr-4">Active</th>
                  <th className="py-3 pr-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                {sortedCurrencies.map((currency) => (
                  <tr key={currency.currencyId} className="border-b border-gray-100">
                    <td className="py-3 pr-4 font-medium">{currency.currencyCode}</td>
                    <td className="py-3 pr-4">{currency.currencyName}</td>
                    <td className="py-3 pr-4">{currency.usdRate}</td>
                    <td className="py-3 pr-4">{currency.active ? "Yes" : "No"}</td>
                    <td className="py-3 pr-4">
                      <div className="flex gap-2">
                        <button className="rounded border border-gray-300 px-2 py-1 text-xs" onClick={() => onToggleActive(currency)}>
                          {currency.active ? "Deactivate" : "Activate"}
                        </button>
                        <button className="rounded border border-red-300 px-2 py-1 text-xs text-red-700" onClick={() => onDelete(currency.currencyId)}>
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {sortedCurrencies.length === 0 ? <p className="pt-4 text-sm text-gray-500">No currencies found.</p> : null}
          </div>
        ) : null}
      </section>
    </div>
  );
};

export default Currency;