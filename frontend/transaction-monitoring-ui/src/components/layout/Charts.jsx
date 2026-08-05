const GRID_LINES = 4;
const DONUT_R    = 15.9155; // circumference ≈ 100

/* ── Bar Chart ──────────────────────────────────────────────── */
const GHOST_HEIGHTS = [38, 65, 28, 72, 48, 85, 42]; // placeholder bar heights for empty state

const BarChart = ({ title, subtitle, data, total, barColor, badgeClass, emptyLabel }) => {
  const max = Math.max(...data.map((d) => d.value), 1);

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      {/* Header */}
      <div className="flex items-start justify-between mb-5">
        <div>
          <h3 className="text-sm font-semibold text-gray-800">{title}</h3>
          <p className="text-xs text-gray-400 mt-0.5">{subtitle}</p>
        </div>
        <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${badgeClass}`}>
          {total} total
        </span>
      </div>

      {/* Chart area — fixed height, absolute layout */}
      <div className="relative h-52">

        {/* Horizontal grid lines (top 80% = bar zone) */}
        <div className="absolute inset-x-0 top-0 bottom-9 flex flex-col justify-between pointer-events-none">
          {Array.from({ length: GRID_LINES + 1 }).map((_, i) => (
            <div key={i} className="w-full border-t border-gray-100" />
          ))}
        </div>

        {/* y-axis max label */}
        <span className="absolute top-0 left-0 text-[10px] text-gray-300 -translate-y-1/2 select-none">
          {max}
        </span>

        {/* Bars */}
        <div className="absolute inset-x-0 top-0 bottom-9 flex items-end gap-2 px-1">
          {total === 0
            ? /* ghost bars */ data.map((d, i) => (
                <div key={i} className="flex-1 flex flex-col items-center">
                  <div
                    className="w-full rounded-t-md bg-gray-100"
                    style={{ height: `${GHOST_HEIGHTS[i % GHOST_HEIGHTS.length]}%` }}
                  />
                </div>
              ))
            : data.map((d, i) => (
                <div key={i} className="flex-1 flex flex-col items-end group">
                  {/* value label — always visible, hidden when 0 */}
                  <span className={`text-[11px] font-semibold mb-1 transition-colors ${d.value > 0 ? 'text-gray-500 group-hover:text-gray-800' : 'text-transparent'}`}>
                    {d.value > 0 ? d.value : '·'}
                  </span>
                  <div
                    className={`w-full rounded-t-md transition-all duration-500 ease-out group-hover:brightness-110 ${barColor}`}
                    style={{
                      height: `calc(${(d.value / max) * 100}% - 20px)`,
                      minHeight: d.value ? '4px' : '0',
                    }}
                  />
                </div>
              ))
          }
        </div>

        {/* x-axis line */}
        <div className="absolute inset-x-0 bottom-9 border-t-2 border-gray-200" />

        {/* Day labels */}
        <div className="absolute inset-x-0 bottom-0 h-9 flex items-center gap-2 px-1">
          {data.map((d, i) => (
            <div key={i} className="flex-1 flex justify-center">
              <span className="text-[11px] text-gray-400 font-medium">{d.label}</span>
            </div>
          ))}
        </div>

        {/* Empty overlay message */}
        {total === 0 && (
          <div className="absolute inset-0 bottom-9 flex items-center justify-center">
            <span className="text-xs text-gray-400 bg-white/90 px-3 py-1.5 rounded-lg border border-gray-100 shadow-sm">
              {emptyLabel ?? 'No data for this period'}
            </span>
          </div>
        )}
      </div>
    </div>
  );
};

/* ── Donut Chart ─────────────────────────────────────────────── */
const DonutChart = ({ title, subtitle, data = [] }) => {
  const total = data.reduce((s, d) => s + d.value, 0);

  let cumulative = 0;
  const segments = data
    .filter((d) => d.value > 0)
    .map((d) => {
      const pct    = (d.value / total) * 100;
      const offset = 25 - cumulative;
      cumulative  += pct;
      return { ...d, pct, offset };
    });

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <div className="mb-5">
        <h3 className="text-sm font-semibold text-gray-800">{title}</h3>
        <p className="text-xs text-gray-400 mt-0.5">{subtitle}</p>
      </div>

      {total === 0 ? (
        <div className="h-40 flex items-center justify-center">
          <p className="text-sm text-gray-300">No data available</p>
        </div>
      ) : (
        <div className="flex items-center gap-6">
          {/* SVG donut */}
          <div className="relative w-32 h-32 shrink-0">
            <svg viewBox="0 0 36 36" className="w-full h-full">
              {/* background ring */}
              <circle cx="18" cy="18" r={DONUT_R} fill="none" stroke="#f3f4f6" strokeWidth="3.5" />
              {/* coloured segments — rotated so 0° = 12 o'clock */}
              <g transform="rotate(-90 18 18)">
                {segments.map((seg, i) => (
                  <circle
                    key={i}
                    cx="18" cy="18" r={DONUT_R}
                    fill="none"
                    stroke={seg.color}
                    strokeWidth="3.5"
                    strokeDasharray={`${seg.pct} ${100 - seg.pct}`}
                    strokeDashoffset={seg.offset}
                    strokeLinecap="butt"
                  />
                ))}
              </g>
            </svg>
            {/* centre label */}
            <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
              <span className="text-xl font-extrabold text-gray-800 leading-none">{total}</span>
              <span className="text-[9px] text-gray-400 uppercase tracking-widest mt-0.5">total</span>
            </div>
          </div>

          {/* Legend */}
          <div className="flex-1 space-y-2.5">
            {data.map((d, i) => (
              <div key={i} className="flex items-center justify-between gap-2">
                <div className="flex items-center gap-2 min-w-0">
                  <div className="w-2.5 h-2.5 rounded-sm shrink-0" style={{ backgroundColor: d.color }} />
                  <span className="text-xs text-gray-600 truncate capitalize">{d.label}</span>
                </div>
                <div className="flex items-center gap-1.5 shrink-0">
                  <span className="text-xs font-semibold text-gray-800">{d.value}</span>
                  <span className="text-[10px] text-gray-400 w-7 text-right">
                    {Math.round((d.value / total) * 100)}%
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

/* ── Charts wrapper ──────────────────────────────────────────── */
const Charts = ({ txChartData = [], alertChartData = [], txStatusData = [], alertSeverityData = [] }) => {
  const txTotal    = txChartData.reduce((s, d) => s + d.value, 0);
  const alertTotal = alertChartData.reduce((s, d) => s + d.value, 0);

  return (
    <div className="space-y-6">
      {/* Bar charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <BarChart
          title="Transaction Volume"
          subtitle="Last 7 days"
          data={txChartData}
          total={txTotal}
          barColor="bg-blue-500 hover:bg-blue-600"
          badgeClass="bg-blue-50 text-blue-700"
          emptyLabel="No transactions this week"
        />
        <BarChart
          title="Alert Frequency"
          subtitle="Last 7 days"
          data={alertChartData}
          total={alertTotal}
          barColor="bg-amber-400 hover:bg-amber-500"
          badgeClass="bg-amber-50 text-amber-700"
          emptyLabel="No alerts this week"
        />
      </div>

      {/* Donut charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <DonutChart
          title="Transaction Status"
          subtitle="Breakdown by current status"
          data={txStatusData}
        />
        <DonutChart
          title="Alert Severity"
          subtitle="Breakdown by severity level"
          data={alertSeverityData}
        />
      </div>
    </div>
  );
};

export default Charts;