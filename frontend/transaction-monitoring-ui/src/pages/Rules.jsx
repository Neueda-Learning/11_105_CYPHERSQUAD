import { useEffect, useMemo, useState } from "react";
import { ruleApi } from "../services/api";

const INITIAL_FORM = {
  ruleName: "",
  ruleType: "AMOUNT_THRESHOLD",
  metric: "AMOUNT",
  comparisonOperator: "GREATER_THAN",
  threshold: "",
  minimumThreshold: "",
  maximumThreshold: "",
  transactionCountThreshold: "",
  timeWindowValue: "",
  timeWindowUnit: "MINUTE",
  payeeScope: "",
  severity: "MEDIUM",
  active: true,
  parametersJson: "{}",
};

function toNullableNumber(value) {
  if (value === "" || value == null) return null;
  return Number(value);
}

function mapFormToPayload(formData) {
  let parsedParameters = {};
  const rawParameters = formData.parametersJson?.trim();

  if (rawParameters) {
    parsedParameters = JSON.parse(rawParameters);
    if (typeof parsedParameters !== "object" || Array.isArray(parsedParameters)) {
      throw new Error("Parameters JSON must be an object");
    }
  }

  return {
    ...formData,
    threshold: toNullableNumber(formData.threshold),
    minimumThreshold: toNullableNumber(formData.minimumThreshold),
    maximumThreshold: toNullableNumber(formData.maximumThreshold),
    transactionCountThreshold: toNullableNumber(formData.transactionCountThreshold),
    timeWindowValue: toNullableNumber(formData.timeWindowValue),
    payeeScope: formData.payeeScope || null,
    parameters: parsedParameters,
  };
}

function mapRuleToForm(rule) {
  return {
    ruleName: rule.ruleName ?? "",
    ruleType: rule.ruleType ?? "AMOUNT_THRESHOLD",
    metric: rule.metric ?? "AMOUNT",
    comparisonOperator: rule.comparisonOperator ?? "GREATER_THAN",
    threshold: rule.threshold ?? "",
    minimumThreshold: rule.minimumThreshold ?? "",
    maximumThreshold: rule.maximumThreshold ?? "",
    transactionCountThreshold: rule.transactionCountThreshold ?? "",
    timeWindowValue: rule.timeWindowValue ?? "",
    timeWindowUnit: rule.timeWindowUnit ?? "MINUTE",
    payeeScope: rule.payeeScope ?? "",
    severity: rule.severity ?? "MEDIUM",
    active: Boolean(rule.active),
    parametersJson: JSON.stringify(rule.parameters ?? {}, null, 2),
  };
}

const Rules = () => {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [formData, setFormData] = useState(INITIAL_FORM);
  const [formError, setFormError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [evalResults, setEvalResults] = useState([]);
  const [transactionIdForEval, setTransactionIdForEval] = useState("");
  const [detailRule, setDetailRule] = useState(null);
  const [editRuleId, setEditRuleId] = useState(null);
  const [editFormData, setEditFormData] = useState(INITIAL_FORM);
  const [editSubmitting, setEditSubmitting] = useState(false);
  const [editError, setEditError] = useState("");

  const sortedRules = useMemo(() => [...rules].sort((a, b) => a.ruleId - b.ruleId), [rules]);

  useEffect(() => {
    loadRules();
  }, []);

  async function loadRules() {
    setLoading(true);
    setError("");
    try {
      setRules(await ruleApi.getAll());
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

  function onEditChange(event) {
    const { name, value, type, checked } = event.target;
    setEditFormData((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
  }

  async function onCreate(event) {
    event.preventDefault();
    setSubmitting(true);
    setFormError("");
    try {
      const created = await ruleApi.create(mapFormToPayload(formData));
      setRules((current) => [...current, created]);
      setFormData(INITIAL_FORM);
    } catch (err) {
      setFormError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function onToggleActive(rule) {
    try {
      const updated = await ruleApi.update(rule.ruleId, { ...rule, active: !rule.active });
      setRules((current) => current.map((item) => (item.ruleId === updated.ruleId ? updated : item)));
    } catch (err) {
      setError(err.message);
    }
  }

  async function onDelete(ruleId) {
    try {
      await ruleApi.remove(ruleId);
      setRules((current) => current.filter((item) => item.ruleId !== ruleId));
      if (detailRule?.ruleId === ruleId) {
        setDetailRule(null);
      }
      if (editRuleId === ruleId) {
        setEditRuleId(null);
      }
    } catch (err) {
      setError(err.message);
    }
  }

  async function onViewDetails(ruleId) {
    setError("");
    try {
      const rule = await ruleApi.getById(ruleId);
      setDetailRule(rule);
    } catch (err) {
      setError(err.message);
    }
  }

  async function onStartEdit(ruleId) {
    setError("");
    setEditError("");
    try {
      const rule = await ruleApi.getById(ruleId);
      setEditRuleId(ruleId);
      setEditFormData(mapRuleToForm(rule));
    } catch (err) {
      setError(err.message);
    }
  }

  async function onUpdateRule(event) {
    event.preventDefault();
    if (!editRuleId) return;

    setEditSubmitting(true);
    setEditError("");
    try {
      const updated = await ruleApi.update(editRuleId, mapFormToPayload(editFormData));
      setRules((current) => current.map((item) => (item.ruleId === updated.ruleId ? updated : item)));
      setDetailRule(updated);
      setEditRuleId(null);
    } catch (err) {
      setEditError(err.message);
    } finally {
      setEditSubmitting(false);
    }
  }

  async function onEvaluate(ruleId) {
    if (!transactionIdForEval) {
      setError("Enter a transaction ID to evaluate rules.");
      return;
    }
    setError("");
    try {
      const result = await ruleApi.evaluateOne(ruleId, transactionIdForEval);
      setEvalResults([result]);
    } catch (err) {
      setError(err.message);
    }
  }

  async function onEvaluateAllActive() {
    if (!transactionIdForEval) {
      setError("Enter a transaction ID to evaluate rules.");
      return;
    }
    setError("");
    try {
      const result = await ruleApi.evaluateAllActive(transactionIdForEval);
      setEvalResults(Array.isArray(result) ? result : [result]);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="space-y-6">
      <section className="rounded-xl border border-gray-200 bg-white p-4 md:p-6">
        <h2 className="text-lg font-semibold text-gray-900">Create Rule</h2>
        <p className="mt-1 text-sm text-gray-500">Fields marked with * are required.</p>
        <form className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3" onSubmit={onCreate}>
          <label className="text-sm font-medium text-gray-700">Rule Name *
            <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="ruleName" value={formData.ruleName} onChange={onChange} required />
          </label>
          <label className="text-sm font-medium text-gray-700">Rule Type *
            <select className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="ruleType" value={formData.ruleType} onChange={onChange}>
              <option value="AMOUNT_THRESHOLD">AMOUNT_THRESHOLD</option>
              <option value="AMOUNT_RANGE">AMOUNT_RANGE</option>
              <option value="COUNT_IN_TIME_WINDOW">COUNT_IN_TIME_WINDOW</option>
              <option value="DAILY_LIMIT">DAILY_LIMIT</option>
              <option value="NEW_PAYEE">NEW_PAYEE</option>
            </select>
          </label>
          <label className="text-sm font-medium text-gray-700">Metric
            <select className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="metric" value={formData.metric} onChange={onChange}>
              <option value="AMOUNT">AMOUNT</option>
              <option value="TRANSACTION_COUNT">TRANSACTION_COUNT</option>
              <option value="PAYEE">PAYEE</option>
            </select>
          </label>
          <label className="text-sm font-medium text-gray-700">Operator
            <select className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="comparisonOperator" value={formData.comparisonOperator} onChange={onChange}>
              <option value="GREATER_THAN">GREATER_THAN</option>
              <option value="GREATER_THAN_OR_EQUAL">GREATER_THAN_OR_EQUAL</option>
              <option value="LESS_THAN">LESS_THAN</option>
              <option value="LESS_THAN_OR_EQUAL">LESS_THAN_OR_EQUAL</option>
              <option value="EQUAL">EQUAL</option>
            </select>
          </label>
          <label className="text-sm font-medium text-gray-700">Threshold
            <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="threshold" type="number" step="0.0001" value={formData.threshold} onChange={onChange} />
          </label>
          <label className="text-sm font-medium text-gray-700">Transaction Count Threshold
            <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="transactionCountThreshold" type="number" min="1" value={formData.transactionCountThreshold} onChange={onChange} />
          </label>
          <label className="text-sm font-medium text-gray-700">Time Window Value
            <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="timeWindowValue" type="number" min="1" value={formData.timeWindowValue} onChange={onChange} />
          </label>
          <label className="text-sm font-medium text-gray-700">Time Window Unit
            <select className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="timeWindowUnit" value={formData.timeWindowUnit} onChange={onChange}>
              <option value="MINUTE">MINUTE</option>
              <option value="HOUR">HOUR</option>
              <option value="DAY">DAY</option>
            </select>
          </label>
          <label className="text-sm font-medium text-gray-700">Minimum Threshold
            <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="minimumThreshold" type="number" step="0.0001" value={formData.minimumThreshold} onChange={onChange} />
          </label>
          <label className="text-sm font-medium text-gray-700">Maximum Threshold
            <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="maximumThreshold" type="number" step="0.0001" value={formData.maximumThreshold} onChange={onChange} />
          </label>
          <label className="text-sm font-medium text-gray-700">Payee Scope
            <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="payeeScope" value={formData.payeeScope} onChange={onChange} />
          </label>
          <label className="text-sm font-medium text-gray-700">Severity
            <select className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="severity" value={formData.severity} onChange={onChange}>
              <option value="LOW">LOW</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HIGH">HIGH</option>
              <option value="CRITICAL">CRITICAL</option>
            </select>
          </label>
          <label className="text-sm font-medium text-gray-700 sm:col-span-2 lg:col-span-3">Parameters JSON
            <textarea
              className="mt-1 min-h-24 w-full rounded-lg border border-gray-300 px-3 py-2 font-mono text-xs"
              name="parametersJson"
              value={formData.parametersJson}
              onChange={onChange}
              placeholder="{}"
            />
          </label>
          <label className="flex items-center gap-2 text-sm font-medium text-gray-700">
            <input name="active" type="checkbox" checked={formData.active} onChange={onChange} />
            Active
          </label>
          <div className="sm:col-span-2 lg:col-span-3 flex items-center gap-3">
            <button className="rounded-lg bg-blue-600 px-4 py-2 font-medium text-white disabled:bg-blue-300" type="submit" disabled={submitting}>
              {submitting ? "Creating..." : "Create Rule"}
            </button>
            {formError ? <p className="text-sm text-red-600">{formError}</p> : null}
          </div>
        </form>
      </section>

      <section className="rounded-xl border border-gray-200 bg-white p-4 md:p-6">
        <div className="mb-4 flex flex-wrap items-center gap-3 justify-between">
          <h2 className="text-lg font-semibold text-gray-900">Rules</h2>
          <div className="flex items-center gap-2">
            <input
              className="rounded-lg border border-gray-300 px-3 py-2 text-sm"
              placeholder="Transaction ID for evaluation"
              type="number"
              min="1"
              value={transactionIdForEval}
              onChange={(event) => setTransactionIdForEval(event.target.value)}
            />
            <button className="rounded-lg border border-gray-300 px-3 py-2 text-sm" onClick={loadRules}>Refresh</button>
            <button className="rounded-lg border border-blue-300 px-3 py-2 text-sm text-blue-700" onClick={onEvaluateAllActive}>
              Evaluate All Active
            </button>
          </div>
        </div>

        {loading ? <p className="text-sm text-gray-500">Loading rules...</p> : null}
        {error ? <p className="mb-3 text-sm text-red-600">{error}</p> : null}

        {!loading ? (
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="border-b border-gray-200 text-left text-xs uppercase text-gray-500">
                  <th className="py-3 pr-4">Rule</th>
                  <th className="py-3 pr-4">Type</th>
                  <th className="py-3 pr-4">Operator</th>
                  <th className="py-3 pr-4">Threshold</th>
                  <th className="py-3 pr-4">Active</th>
                  <th className="py-3 pr-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                {sortedRules.map((rule) => (
                  <tr key={rule.ruleId} className="border-b border-gray-100">
                    <td className="py-3 pr-4">#{rule.ruleId} {rule.ruleName}</td>
                    <td className="py-3 pr-4">{rule.ruleType}</td>
                    <td className="py-3 pr-4">{rule.comparisonOperator ?? "--"}</td>
                    <td className="py-3 pr-4">{rule.threshold ?? "--"}</td>
                    <td className="py-3 pr-4">{rule.active ? "Yes" : "No"}</td>
                    <td className="py-3 pr-4">
                      <div className="flex flex-wrap gap-2">
                        <button className="rounded border border-indigo-300 px-2 py-1 text-xs text-indigo-700" onClick={() => onViewDetails(rule.ruleId)}>
                          Detail
                        </button>
                        <button className="rounded border border-amber-300 px-2 py-1 text-xs text-amber-700" onClick={() => onStartEdit(rule.ruleId)}>
                          Edit
                        </button>
                        <button className="rounded border border-gray-300 px-2 py-1 text-xs" onClick={() => onToggleActive(rule)}>
                          {rule.active ? "Pause" : "Activate"}
                        </button>
                        <button className="rounded border border-blue-300 px-2 py-1 text-xs text-blue-700" onClick={() => onEvaluate(rule.ruleId)}>
                          Evaluate
                        </button>
                        <button className="rounded border border-red-300 px-2 py-1 text-xs text-red-700" onClick={() => onDelete(rule.ruleId)}>
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {sortedRules.length === 0 ? <p className="pt-4 text-sm text-gray-500">No rules found.</p> : null}
          </div>
        ) : null}

        {detailRule ? (
          <div className="mt-4 rounded-lg border border-gray-200 bg-gray-50 p-4 text-sm">
            <div className="mb-2 flex items-center justify-between">
              <p className="font-medium text-gray-800">Rule Detail #{detailRule.ruleId}</p>
              <button className="rounded border border-gray-300 px-2 py-1 text-xs" onClick={() => setDetailRule(null)}>
                Hide
              </button>
            </div>
            <dl className="grid grid-cols-1 gap-2 sm:grid-cols-2">
              <div><dt className="text-xs uppercase text-gray-500">Rule Name</dt><dd>{detailRule.ruleName}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Rule Type</dt><dd>{detailRule.ruleType}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Metric</dt><dd>{detailRule.metric ?? "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Operator</dt><dd>{detailRule.comparisonOperator ?? "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Threshold</dt><dd>{detailRule.threshold ?? "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Min Threshold</dt><dd>{detailRule.minimumThreshold ?? "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Max Threshold</dt><dd>{detailRule.maximumThreshold ?? "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Txn Count Threshold</dt><dd>{detailRule.transactionCountThreshold ?? "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Time Window Value</dt><dd>{detailRule.timeWindowValue ?? "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Time Window Unit</dt><dd>{detailRule.timeWindowUnit ?? "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Payee Scope</dt><dd>{detailRule.payeeScope ?? "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Severity</dt><dd>{detailRule.severity ?? "--"}</dd></div>
              <div><dt className="text-xs uppercase text-gray-500">Active</dt><dd>{detailRule.active ? "Yes" : "No"}</dd></div>
              <div className="sm:col-span-2"><dt className="text-xs uppercase text-gray-500">Parameters JSON</dt><dd className="whitespace-pre-wrap rounded border border-gray-200 bg-white p-2 font-mono text-xs">{JSON.stringify(detailRule.parameters ?? {}, null, 2)}</dd></div>
            </dl>
          </div>
        ) : null}

        {editRuleId ? (
          <form className="mt-4 rounded-lg border border-amber-200 bg-amber-50 p-4" onSubmit={onUpdateRule}>
            <p className="mb-3 text-sm font-semibold text-amber-900">Edit Rule #{editRuleId}</p>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <label className="text-sm font-medium text-gray-700">Rule Name *
                <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="ruleName" value={editFormData.ruleName} onChange={onEditChange} required />
              </label>
              <label className="text-sm font-medium text-gray-700">Rule Type *
                <select className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="ruleType" value={editFormData.ruleType} onChange={onEditChange}>
                  <option value="AMOUNT_THRESHOLD">AMOUNT_THRESHOLD</option>
                  <option value="AMOUNT_RANGE">AMOUNT_RANGE</option>
                  <option value="COUNT_IN_TIME_WINDOW">COUNT_IN_TIME_WINDOW</option>
                  <option value="DAILY_LIMIT">DAILY_LIMIT</option>
                  <option value="NEW_PAYEE">NEW_PAYEE</option>
                </select>
              </label>
              <label className="text-sm font-medium text-gray-700">Metric
                <select className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="metric" value={editFormData.metric} onChange={onEditChange}>
                  <option value="AMOUNT">AMOUNT</option>
                  <option value="TRANSACTION_COUNT">TRANSACTION_COUNT</option>
                  <option value="PAYEE">PAYEE</option>
                </select>
              </label>
              <label className="text-sm font-medium text-gray-700">Operator
                <select className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="comparisonOperator" value={editFormData.comparisonOperator} onChange={onEditChange}>
                  <option value="GREATER_THAN">GREATER_THAN</option>
                  <option value="GREATER_THAN_OR_EQUAL">GREATER_THAN_OR_EQUAL</option>
                  <option value="LESS_THAN">LESS_THAN</option>
                  <option value="LESS_THAN_OR_EQUAL">LESS_THAN_OR_EQUAL</option>
                  <option value="EQUAL">EQUAL</option>
                </select>
              </label>
              <label className="text-sm font-medium text-gray-700">Threshold
                <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="threshold" type="number" step="0.0001" value={editFormData.threshold} onChange={onEditChange} />
              </label>
              <label className="text-sm font-medium text-gray-700">Minimum Threshold
                <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="minimumThreshold" type="number" step="0.0001" value={editFormData.minimumThreshold} onChange={onEditChange} />
              </label>
              <label className="text-sm font-medium text-gray-700">Maximum Threshold
                <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="maximumThreshold" type="number" step="0.0001" value={editFormData.maximumThreshold} onChange={onEditChange} />
              </label>
              <label className="text-sm font-medium text-gray-700">Txn Count Threshold
                <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="transactionCountThreshold" type="number" min="1" value={editFormData.transactionCountThreshold} onChange={onEditChange} />
              </label>
              <label className="text-sm font-medium text-gray-700">Time Window Value
                <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="timeWindowValue" type="number" min="1" value={editFormData.timeWindowValue} onChange={onEditChange} />
              </label>
              <label className="text-sm font-medium text-gray-700">Time Window Unit
                <select className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="timeWindowUnit" value={editFormData.timeWindowUnit} onChange={onEditChange}>
                  <option value="MINUTE">MINUTE</option>
                  <option value="HOUR">HOUR</option>
                  <option value="DAY">DAY</option>
                </select>
              </label>
              <label className="text-sm font-medium text-gray-700">Payee Scope
                <input className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="payeeScope" value={editFormData.payeeScope} onChange={onEditChange} />
              </label>
              <label className="text-sm font-medium text-gray-700">Severity
                <select className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2" name="severity" value={editFormData.severity} onChange={onEditChange}>
                  <option value="LOW">LOW</option>
                  <option value="MEDIUM">MEDIUM</option>
                  <option value="HIGH">HIGH</option>
                  <option value="CRITICAL">CRITICAL</option>
                </select>
              </label>
              <label className="flex items-center gap-2 text-sm font-medium text-gray-700">
                <input name="active" type="checkbox" checked={editFormData.active} onChange={onEditChange} />
                Active
              </label>
              <label className="text-sm font-medium text-gray-700 sm:col-span-2 lg:col-span-3">Parameters JSON
                <textarea className="mt-1 min-h-24 w-full rounded-lg border border-gray-300 px-3 py-2 font-mono text-xs" name="parametersJson" value={editFormData.parametersJson} onChange={onEditChange} />
              </label>
            </div>
            <div className="mt-4 flex items-center gap-2">
              <button className="rounded-lg bg-amber-600 px-4 py-2 text-sm font-medium text-white disabled:bg-amber-300" type="submit" disabled={editSubmitting}>
                {editSubmitting ? "Updating..." : "Update Rule"}
              </button>
              <button className="rounded-lg border border-gray-300 px-4 py-2 text-sm" type="button" onClick={() => setEditRuleId(null)}>
                Cancel
              </button>
              {editError ? <p className="text-sm text-red-600">{editError}</p> : null}
            </div>
          </form>
        ) : null}

        {evalResults.length > 0 ? (
          <div className="mt-4 rounded-lg border border-gray-200 bg-gray-50 p-3 text-sm">
            <p className="font-medium text-gray-800">Evaluation Result</p>
            {evalResults.map((result, index) => (
              <div key={`${result.ruleId ?? "r"}-${index}`} className="mt-2 rounded border border-gray-200 bg-white p-2">
                <p className="text-gray-700">Rule #{result.ruleId ?? "--"}, Transaction #{result.transactionId}</p>
                <p className="text-gray-700">Triggered: {String(result.triggered)}</p>
                <p className="text-gray-700">Message: {result.message}</p>
                {result.alertId ? <p className="text-gray-700">Alert ID: {result.alertId}</p> : null}
              </div>
            ))}
          </div>
        ) : null}
      </section>
    </div>
  );
};

export default Rules;