const BarChart = ({ title, subtitle, data, total, barClass, badgeClass }) => {
  const max = Math.max(...data.map((d) => d.value), 1);
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h3 className="text-base font-semibold text-gray-800">{title}</h3>
          <p className="text-xs text-gray-400 mt-0.5">{subtitle}</p>
        </div>
        <span className={`text-xs font-medium px-3 py-1 rounded-full ${badgeClass}`}>
          {total} total
        </span>
      </div>
      <div className="flex items-end gap-2 h-32">
        {data.map((d, i) => (
          <div key={i} className="flex-1 flex flex-col items-center gap-1">
            <span className="text-xs text-gray-400">{d.value || ''}</span>
            <div
              className={`w-full rounded-t-md transition-all ${barClass}`}
              style={{ height: `${(d.value / max) * 100}%`, minHeight: d.value ? '4px' : '0' }}
            />
            <span className="text-xs text-gray-400">{d.label}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

const Charts = ({ txChartData = [], alertChartData = [] }) => {
  const txTotal = txChartData.reduce((s, d) => s + d.value, 0);
  const alertTotal = alertChartData.reduce((s, d) => s + d.value, 0);

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <BarChart
        title="Transaction Volume"
        subtitle="Last 7 days"
        data={txChartData}
        total={txTotal}
        barClass="bg-blue-500 hover:bg-blue-600"
        badgeClass="bg-blue-50 text-blue-600"
      />
      <BarChart
        title="Alert Frequency"
        subtitle="Last 7 days"
        data={alertChartData}
        total={alertTotal}
        barClass="bg-amber-400 hover:bg-amber-500"
        badgeClass="bg-amber-50 text-amber-600"
      />
    </div>
  );
};

export default Charts;