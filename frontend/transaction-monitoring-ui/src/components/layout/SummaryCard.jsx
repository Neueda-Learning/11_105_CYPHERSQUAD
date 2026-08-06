const ACCENT = {
  'bg-blue-50':    'border-blue-500',
  'bg-red-50':     'border-red-500',
  'bg-purple-50':  'border-purple-500',
  'bg-emerald-50': 'border-emerald-500',
};

const ICON_COLOR = {
  'bg-blue-50':    'text-blue-600',
  'bg-red-50':     'text-red-600',
  'bg-purple-50':  'text-purple-600',
  'bg-emerald-50': 'text-emerald-600',
};

const SummaryCard = ({ title, value, subtitle, icon, colorClass, trend }) => {
  const accent    = ACCENT[colorClass]    ?? 'border-blue-500';
  const iconColor = ICON_COLOR[colorClass] ?? 'text-blue-600';

  return (
    <div className={`bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 border-t-4 ${accent} p-5 flex items-start gap-4 hover:shadow-md transition-shadow`}>
      <div className={`rounded-xl p-3 ${colorClass ?? 'bg-blue-50'} dark:opacity-80 shrink-0`}>
        <span className={`text-xl font-bold ${iconColor}`}>{icon}</span>
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-xs font-semibold uppercase tracking-wide text-gray-400 dark:text-gray-500 truncate">{title}</p>
        <p className="text-3xl font-extrabold text-gray-900 dark:text-white mt-0.5 leading-none">{value}</p>
        {subtitle && (
          <p className="text-xs text-gray-400 dark:text-gray-500 mt-1.5">{subtitle}</p>
        )}
      </div>
      {trend !== undefined && (
        <span
          className={`text-xs font-semibold px-2 py-1 rounded-full self-start shrink-0 ${
            trend >= 0 ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'
          }`}
        >
          {trend >= 0 ? '▲' : '▼'} {Math.abs(trend)}%
        </span>
      )}
    </div>
  );
};

export default SummaryCard;