import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import SummaryCard from '../components/layout/SummaryCard';
import Charts from '../components/layout/Charts';
import { transactionApi, alertApi, ruleApi, currencyApi } from '../services/api';

/* ── badge helpers ────────────────────────────────────────── */
const STATUS_CLS = {
  success:   'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200',
  completed: 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200',
  pending:   'bg-amber-50   text-amber-700   ring-1 ring-amber-200',
  failed:    'bg-red-50     text-red-700     ring-1 ring-red-200',
  flagged:   'bg-red-50     text-red-700     ring-1 ring-red-200',
};

const SEVERITY_CLS = {
  high:     { badge: 'bg-red-50   text-red-700   ring-1 ring-red-200',   bar: 'bg-red-500' },
  medium:   { badge: 'bg-amber-50 text-amber-700 ring-1 ring-amber-200', bar: 'bg-amber-400' },
  low:      { badge: 'bg-blue-50  text-blue-700  ring-1 ring-blue-200',  bar: 'bg-blue-400' },
  critical: { badge: 'bg-rose-50  text-rose-700  ring-1 ring-rose-200',  bar: 'bg-rose-600' },
};

const statusCls   = (s = '') => STATUS_CLS[s.toLowerCase()]   ?? 'bg-gray-100 text-gray-500';
const severityCls = (s = '') => SEVERITY_CLS[s.toLowerCase()] ?? { badge: 'bg-gray-100 text-gray-500', bar: 'bg-gray-300' };

/* ── formatters ───────────────────────────────────────────── */
function formatUsd(v) {
  if (v == null) return '—';
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(v);
}

function timeAgo(d) {
  if (!d) return '—';
  const s = (Date.now() - new Date(d)) / 1000;
  if (s < 60)    return 'just now';
  if (s < 3600)  return `${Math.floor(s / 60)}m ago`;
  if (s < 86400) return `${Math.floor(s / 3600)}h ago`;
  return `${Math.floor(s / 86400)}d ago`;
}

/* ── distribution helpers (for donut charts) ─────────────────── */
const STATUS_PALETTE = {
  completed: '#10b981',
  success:   '#10b981',
  pending:   '#f59e0b',
  failed:    '#ef4444',
  flagged:   '#f43f5e',
};
const STATUS_ORDER = ['completed', 'success', 'pending', 'failed', 'flagged'];

const SEVERITY_PALETTE = {
  critical: '#e11d48',
  high:     '#ef4444',
  medium:   '#f59e0b',
  low:      '#3b82f6',
};
const SEVERITY_ORDER = ['critical', 'high', 'medium', 'low'];

function buildDistribution(items, field, palette, order) {
  const counts = {};
  items.forEach((item) => {
    const key = (item[field] ?? 'unknown').toLowerCase();
    counts[key] = (counts[key] ?? 0) + 1;
  });
  const entries = Object.entries(counts).map(([label, value]) => ({
    label,
    value,
    color: palette[label] ?? '#9ca3af',
  }));
  return entries.sort((a, b) => {
    const ai = order.indexOf(a.label);
    const bi = order.indexOf(b.label);
    if (ai === -1 && bi === -1) return 0;
    if (ai === -1) return 1;
    if (bi === -1) return -1;
    return ai - bi;
  });
}

function buildChartData(items, dateField) {
  const today = new Date();
  const days  = Array.from({ length: 7 }, (_, i) => {
    const d = new Date(today);
    d.setDate(today.getDate() - (6 - i));
    return d.toISOString().slice(0, 10);
  });
  const counts = Object.fromEntries(days.map((d) => [d, 0]));
  items.forEach((item) => {
    const raw = item[dateField];
    if (!raw) return;
    const key = new Date(raw).toISOString().slice(0, 10);
    if (key in counts) counts[key]++;
  });
  return days.map((d) => ({
    label: new Date(d + 'T12:00:00').toLocaleDateString('en-US', { weekday: 'short' }),
    value: counts[d],
  }));
}

/* ── component ────────────────────────────────────────────── */
const Dashboard = () => {
  const [transactions, setTransactions] = useState([]);
  const [alerts,       setAlerts]       = useState([]);
  const [rules,        setRules]        = useState([]);
  const [currencies,   setCurrencies]   = useState([]);
  const [loading,      setLoading]      = useState(true);
  const [error,        setError]        = useState(null);
  const [refreshedAt,  setRefreshedAt]  = useState(null);

  function load() {
    setLoading(true);
    setError(null);
    Promise.all([
      transactionApi.getAll(),
      alertApi.getAll(),
      ruleApi.getAll(),
      currencyApi.getAll(),
    ])
      .then(([txs, alts, rls, curs]) => {
        setTransactions(txs);
        setAlerts(alts);
        setRules(rls);
        setCurrencies(curs);
        setRefreshedAt(new Date());
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }

  useEffect(() => { load(); }, []);

  /* derived */
  const activeAlerts     = alerts.filter((a) => a.status?.toLowerCase() !== 'closed');
  const highAlerts       = activeAlerts.filter((a) => a.severity?.toLowerCase() === 'high');
  const activeRules      = rules.filter((r) => r.active);
  const pausedRules      = rules.filter((r) => !r.active);
  const activeCurrencies = currencies.filter((c) => c.active);
  const rulesById        = Object.fromEntries(rules.map((r) => [r.ruleId, r]));

  const recentTransactions = [...transactions]
    .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
    .slice(0, 5);

  const recentAlerts = [...alerts]
    .sort((a, b) => new Date(b.createDate) - new Date(a.createDate))
    .slice(0, 5);

  const txChartData       = buildChartData(transactions, 'timestamp');
  const alertChartData    = buildChartData(alerts,       'createDate');
  const txStatusData      = buildDistribution(transactions, 'status',   STATUS_PALETTE,   STATUS_ORDER);
  const alertSeverityData = buildDistribution(alerts,       'severity', SEVERITY_PALETTE, SEVERITY_ORDER);

  /* ── loading ─────────────────────────────────────────────── */
  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-64">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 rounded-full border-4 border-blue-100 border-t-blue-600 animate-spin" />
          <p className="text-sm font-medium text-gray-400">Loading dashboard…</p>
        </div>
      </div>
    );
  }

  /* ── error ───────────────────────────────────────────────── */
  if (error) {
    return (
      <div className="flex items-center justify-center min-h-64">
        <div className="bg-white rounded-2xl border border-red-100 shadow-sm p-8 text-center max-w-sm w-full">
          <div className="mx-auto mb-4 w-12 h-12 rounded-full bg-red-50 flex items-center justify-center">
            <span className="text-xl font-bold text-red-500">!</span>
          </div>
          <p className="font-semibold text-gray-800 mb-1">Failed to load dashboard</p>
          <p className="text-sm text-gray-500 mb-5">{error}</p>
          <button
            onClick={load}
            className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-lg transition-colors"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  /* ── render ──────────────────────────────────────────────── */
  return (
    <div className="space-y-6">

      {/* Page header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-gray-900 tracking-tight">Overview</h2>
          <p className="text-xs text-gray-400 mt-0.5">
            {refreshedAt ? `Last updated ${refreshedAt.toLocaleTimeString()}` : 'Transaction monitoring'}
          </p>
        </div>
        <button
          onClick={load}
          className="flex items-center gap-1.5 bg-white border border-gray-200 hover:border-blue-300 hover:text-blue-600 text-gray-500 text-xs font-medium px-3 py-2 rounded-lg shadow-sm transition-colors"
        >
          ↻ Refresh
        </button>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <SummaryCard
          title="Total Transactions"
          value={transactions.length.toLocaleString()}
          subtitle="All time"
          icon="💳"
          colorClass="bg-blue-50"
        />
        <SummaryCard
          title="Active Alerts"
          value={activeAlerts.length.toLocaleString()}
          subtitle={highAlerts.length ? `${highAlerts.length} high severity` : 'None high severity'}
          icon="🚨"
          colorClass="bg-red-50"
        />
        <SummaryCard
          title="Rules Active"
          value={activeRules.length.toLocaleString()}
          subtitle={pausedRules.length ? `${pausedRules.length} paused` : 'All rules active'}
          icon="📋"
          colorClass="bg-purple-50"
        />
        <SummaryCard
          title="Currencies Tracked"
          value={currencies.length.toLocaleString()}
          subtitle={`${activeCurrencies.length} active`}
          icon="💱"
          colorClass="bg-emerald-50"
        />
      </div>

      {/* Charts */}
      <Charts
        txChartData={txChartData}
        alertChartData={alertChartData}
        txStatusData={txStatusData}
        alertSeverityData={alertSeverityData}
      />

      {/* Tables row */}
      <div className="grid grid-cols-1 xl:grid-cols-5 gap-6">

        {/* Recent Transactions */}
        <div className="xl:col-span-3 bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">

          {/* card header */}
          <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
            <h3 className="text-sm font-semibold text-gray-800 tracking-tight">Recent Transactions</h3>
            <Link
              to="/transactions"
              className="text-xs font-medium text-blue-600 hover:text-blue-800 transition-colors"
            >
              View all →
            </Link>
          </div>

          {recentTransactions.length === 0 ? (
            <p className="px-6 py-10 text-sm text-gray-400 text-center">No transactions recorded yet.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="bg-gray-50 border-b border-gray-100 text-left">
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400">ID</th>
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400">Account</th>
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400 text-right">Amount (USD)</th>
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400">Type</th>
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400">Status</th>
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wide text-gray-400 text-right">Time</th>
                  </tr>
                </thead>
                <tbody>
                  {recentTransactions.map((tx, i) => (
                    <tr
                      key={tx.transactionId}
                      className={`border-b border-gray-50 hover:bg-blue-50/40 transition-colors ${i % 2 === 1 ? 'bg-gray-50/40' : 'bg-white'}`}
                    >
                      <td className="px-5 py-3.5">
                        <Link
                          to={`/transactions/${tx.transactionId}`}
                          className="font-mono text-xs font-semibold text-blue-600 hover:underline"
                        >
                          #{tx.transactionId}
                        </Link>
                      </td>
                      <td className="px-5 py-3.5 text-xs text-gray-600">{tx.accountId}</td>
                      <td className="px-5 py-3.5 text-xs font-semibold text-gray-800 text-right tabular-nums">
                        {formatUsd(tx.amountUsd ?? tx.amount)}
                      </td>
                      <td className="px-5 py-3.5 text-xs text-gray-500 capitalize">{tx.type ?? '—'}</td>
                      <td className="px-5 py-3.5">
                        <span className={`inline-flex items-center text-[11px] font-semibold px-2 py-0.5 rounded-full ${statusCls(tx.status)}`}>
                          {tx.status ?? '—'}
                        </span>
                      </td>
                      <td className="px-5 py-3.5 text-xs text-gray-400 text-right whitespace-nowrap">
                        {timeAgo(tx.timestamp)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Recent Alerts */}
        <div className="xl:col-span-2 bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">

          {/* card header */}
          <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
            <h3 className="text-sm font-semibold text-gray-800 tracking-tight">Recent Alerts</h3>
            <Link
              to="/alerts"
              className="text-xs font-medium text-blue-600 hover:text-blue-800 transition-colors"
            >
              View all →
            </Link>
          </div>

          {recentAlerts.length === 0 ? (
            <p className="px-6 py-10 text-sm text-gray-400 text-center">No alerts found.</p>
          ) : (
            <ul>
              {recentAlerts.map((alert) => {
                const rule = rulesById[alert.ruleId];
                const sev  = severityCls(alert.severity);
                return (
                  <li key={alert.alertId} className="flex border-b border-gray-50 last:border-0 hover:bg-gray-50/60 transition-colors">
                    {/* severity left bar */}
                    <div className={`w-1 shrink-0 ${sev.bar}`} />
                    <Link to={`/alerts/${alert.alertId}`} className="flex-1 min-w-0 px-4 py-3.5">
                      <div className="flex items-start justify-between gap-3">
                        <div className="min-w-0">
                          <p className="text-xs font-semibold text-gray-800 truncate leading-snug">
                            {rule?.ruleName ?? `Rule #${alert.ruleId ?? '—'}`}
                          </p>
                          <p className="text-[11px] text-gray-400 mt-0.5">
                            Txn&nbsp;#{alert.transactionId ?? '—'}&nbsp;·&nbsp;{timeAgo(alert.createDate)}
                          </p>
                        </div>
                        <span className={`shrink-0 inline-flex items-center text-[11px] font-semibold px-2 py-0.5 rounded-full ${sev.badge}`}>
                          {alert.severity ?? '—'}
                        </span>
                      </div>
                    </Link>
                  </li>
                );
              })}
            </ul>
          )}
        </div>

      </div>

      {/* System Status */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 px-5 py-4">
        <div className="flex flex-wrap items-center gap-3">
          <span className="text-xs font-semibold uppercase tracking-wide text-gray-400 mr-2">
            System Status
          </span>

          {/* API */}
          <span className="inline-flex items-center gap-1.5 bg-gray-50 border border-gray-100 rounded-lg px-3 py-1.5 text-xs">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 shrink-0" />
            <span className="font-medium text-gray-700">API</span>
            <span className="text-gray-400">Connected</span>
          </span>

          {/* Rule Engine */}
          <span className={`inline-flex items-center gap-1.5 border rounded-lg px-3 py-1.5 text-xs ${activeRules.length > 0 ? 'bg-gray-50 border-gray-100' : 'bg-red-50 border-red-100'}`}>
            <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${activeRules.length > 0 ? 'bg-emerald-400' : 'bg-red-400'}`} />
            <span className="font-medium text-gray-700">Rule Engine</span>
            <span className="text-gray-400">
              {activeRules.length} active{pausedRules.length ? `, ${pausedRules.length} paused` : ''}
            </span>
          </span>

          {/* Critical Alerts */}
          <span className={`inline-flex items-center gap-1.5 border rounded-lg px-3 py-1.5 text-xs ${highAlerts.length === 0 ? 'bg-gray-50 border-gray-100' : 'bg-red-50 border-red-100'}`}>
            <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${highAlerts.length === 0 ? 'bg-emerald-400' : 'bg-red-500 animate-pulse'}`} />
            <span className="font-medium text-gray-700">Alerts</span>
            <span className="text-gray-400">
              {highAlerts.length === 0 ? 'No critical' : `${highAlerts.length} critical`}
            </span>
          </span>

          {/* Currencies */}
          <span className="inline-flex items-center gap-1.5 bg-gray-50 border border-gray-100 rounded-lg px-3 py-1.5 text-xs">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 shrink-0" />
            <span className="font-medium text-gray-700">Currencies</span>
            <span className="text-gray-400">{activeCurrencies.length}/{currencies.length} active</span>
          </span>

          <span className="ml-auto text-[11px] text-gray-400">
            {refreshedAt ? `Updated ${refreshedAt.toLocaleTimeString()}` : ''}
          </span>
        </div>
      </div>

    </div>
  );
};

export default Dashboard;
