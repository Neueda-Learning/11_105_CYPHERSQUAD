import { useEffect, useState } from 'react';
import SummaryCard from '../components/layout/SummaryCard';
import Charts from '../components/layout/Charts';
import { transactionApi, alertApi, ruleApi, currencyApi } from '../services/api';

/* â”€â”€ helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
const STATUS_BADGE = {
  completed: 'bg-emerald-100 text-emerald-700',
  pending:   'bg-yellow-100  text-yellow-700',
  flagged:   'bg-red-100     text-red-700',
};

const SEVERITY_BADGE = {
  high:   'bg-red-100   text-red-700',
  medium: 'bg-amber-100 text-amber-700',
  low:    'bg-blue-100  text-blue-700',
};

function statusBadge(status = '') {
  return STATUS_BADGE[status.toLowerCase()] ?? 'bg-gray-100 text-gray-600';
}

function severityBadge(severity = '') {
  return SEVERITY_BADGE[severity.toLowerCase()] ?? 'bg-gray-100 text-gray-600';
}

function formatUsd(value) {
  if (value == null) return 'â€”';
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);
}

function timeAgo(dateStr) {
  if (!dateStr) return 'â€”';
  const secs = (Date.now() - new Date(dateStr)) / 1000;
  if (secs < 60)    return 'just now';
  if (secs < 3600)  return `${Math.floor(secs / 60)} min ago`;
  if (secs < 86400) return `${Math.floor(secs / 3600)} hr ago`;
  return `${Math.floor(secs / 86400)}d ago`;
}

/** Build 7-day chart data: [{label:'Mon', value:3}, ...] */
function buildChartData(items, dateField) {
  const today = new Date();
  const days = Array.from({ length: 7 }, (_, i) => {
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

/* â”€â”€ component â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
const Dashboard = () => {
  const [transactions, setTransactions] = useState([]);
  const [alerts,       setAlerts]       = useState([]);
  const [rules,        setRules]        = useState([]);
  const [currencies,   setCurrencies]   = useState([]);
  const [loading,      setLoading]      = useState(true);
  const [error,        setError]        = useState(null);

  useEffect(() => {
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
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  /* â”€â”€ derived stats â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
  const activeAlerts     = alerts.filter((a) => a.status?.toLowerCase() !== 'closed');
  const highAlerts       = activeAlerts.filter((a) => a.severity?.toLowerCase() === 'high');
  const activeRules      = rules.filter((r) => r.active);
  const pausedRules      = rules.filter((r) => !r.active);
  const activeCurrencies = currencies.filter((c) => c.active);

  const rulesById = Object.fromEntries(rules.map((r) => [r.ruleId, r]));

  const recentTransactions = [...transactions]
    .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
    .slice(0, 5);

  const recentAlerts = [...alerts]
    .sort((a, b) => new Date(b.createDate) - new Date(a.createDate))
    .slice(0, 4);

  const txChartData    = buildChartData(transactions, 'timestamp');
  const alertChartData = buildChartData(alerts,       'createDate');

  /* â”€â”€ loading / error â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="flex flex-col items-center gap-3 text-gray-400">
          <div className="w-8 h-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
          <p className="text-sm">Loading dashboardâ€¦</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center max-w-sm">
          <div className="text-4xl mb-3">âš ï¸</div>
          <p className="font-semibold text-gray-800 mb-1">Failed to load data</p>
          <p className="text-sm text-gray-500">{error}</p>
          <p className="text-xs text-gray-400 mt-2">Make sure the backend is running on port 8080.</p>
        </div>
      </div>
    );
  }

  /* â”€â”€ render â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
  return (
    <div className="space-y-6">

      {/* Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <SummaryCard
          title="Total Transactions"
          value={transactions.length.toLocaleString()}
          subtitle="All time"
          icon="ðŸ’³"
          colorClass="bg-blue-50"
        />
        <SummaryCard
          title="Active Alerts"
          value={activeAlerts.length.toLocaleString()}
          subtitle={highAlerts.length ? `${highAlerts.length} high severity` : 'No high severity'}
          icon="ðŸš¨"
          colorClass="bg-red-50"
        />
        <SummaryCard
          title="Rules Active"
          value={activeRules.length.toLocaleString()}
          subtitle={pausedRules.length ? `${pausedRules.length} paused` : 'All rules active'}
          icon="ðŸ“‹"
          colorClass="bg-purple-50"
        />
        <SummaryCard
          title="Currencies Tracked"
          value={currencies.length.toLocaleString()}
          subtitle={`${activeCurrencies.length} active`}
          icon="ðŸ’±"
          colorClass="bg-emerald-50"
        />
      </div>

      {/* Charts */}
      <Charts txChartData={txChartData} alertChartData={alertChartData} />

      {/* Recent Transactions + Recent Alerts */}
      <div className="grid grid-cols-1 xl:grid-cols-5 gap-6">

        {/* Recent Transactions */}
        <div className="xl:col-span-3 bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h3 className="text-base font-semibold text-gray-800">Recent Transactions</h3>
            <a href="/transactions" className="text-xs text-blue-600 hover:underline font-medium">
              View all â†’
            </a>
          </div>

          {recentTransactions.length === 0 ? (
            <p className="px-6 py-8 text-sm text-gray-400 text-center">No transactions found.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
                  <tr>
                    <th className="px-6 py-3 text-left font-medium">ID</th>
                    <th className="px-6 py-3 text-left font-medium">Account</th>
                    <th className="px-6 py-3 text-left font-medium">Amount (USD)</th>
                    <th className="px-6 py-3 text-left font-medium">Type</th>
                    <th className="px-6 py-3 text-left font-medium">Status</th>
                    <th className="px-6 py-3 text-left font-medium">Time</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {recentTransactions.map((tx) => (
                    <tr key={tx.transactionId} className="hover:bg-gray-50 transition-colors">
                      <td className="px-6 py-3 font-mono text-gray-700">#{tx.transactionId}</td>
                      <td className="px-6 py-3 text-gray-600">{tx.accountId}</td>
                      <td className="px-6 py-3 font-semibold text-gray-800">{formatUsd(tx.amountUsd ?? tx.amount)}</td>
                      <td className="px-6 py-3 text-gray-600 capitalize">{tx.type ?? 'â€”'}</td>
                      <td className="px-6 py-3">
                        <span className={`text-xs font-semibold px-2 py-1 rounded-full ${statusBadge(tx.status)}`}>
                          {tx.status ?? 'â€”'}
                        </span>
                      </td>
                      <td className="px-6 py-3 text-gray-400 whitespace-nowrap">{timeAgo(tx.timestamp)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Recent Alerts */}
        <div className="xl:col-span-2 bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h3 className="text-base font-semibold text-gray-800">Recent Alerts</h3>
            <a href="/alerts" className="text-xs text-blue-600 hover:underline font-medium">
              View all â†’
            </a>
          </div>

          {recentAlerts.length === 0 ? (
            <p className="px-6 py-8 text-sm text-gray-400 text-center">No alerts found.</p>
          ) : (
            <ul className="divide-y divide-gray-50">
              {recentAlerts.map((alert) => {
                const rule = rulesById[alert.ruleId];
                return (
                  <li key={alert.alertId} className="px-6 py-4 hover:bg-gray-50 transition-colors">
                    <div className="flex items-start justify-between gap-2">
                      <div className="min-w-0">
                        <p className="text-sm font-medium text-gray-800 truncate">
                          {rule?.ruleName ?? `Rule #${alert.ruleId ?? 'â€”'}`}
                        </p>
                        <p className="text-xs text-gray-400 mt-0.5">
                          Txn #{alert.transactionId ?? 'â€”'} Â· {timeAgo(alert.createDate)}
                        </p>
                      </div>
                      <span className={`text-xs font-semibold px-2 py-1 rounded-full whitespace-nowrap ${severityBadge(alert.severity)}`}>
                        {alert.severity ?? 'â€”'}
                      </span>
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </div>

      </div>

      {/* System Status */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 px-6 py-4 flex flex-wrap gap-6 items-center">
        <p className="text-sm font-semibold text-gray-700">System Status</p>
        <div className="flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-emerald-400" />
          <span className="text-sm text-gray-600">API Connected</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className={`w-2 h-2 rounded-full ${activeRules.length > 0 ? 'bg-emerald-400' : 'bg-red-400'}`} />
          <span className="text-sm text-gray-600">Rule Engine ({activeRules.length} active)</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className={`w-2 h-2 rounded-full ${highAlerts.length === 0 ? 'bg-emerald-400' : 'bg-red-400'}`} />
          <span className="text-sm text-gray-600">
            {highAlerts.length === 0 ? 'No Critical Alerts' : `${highAlerts.length} Critical Alert${highAlerts.length > 1 ? 's' : ''}`}
          </span>
        </div>
        <p className="ml-auto text-xs text-gray-400">
          Last refreshed: {new Date().toLocaleTimeString()}
        </p>
      </div>

    </div>
  );
};

export default Dashboard;
