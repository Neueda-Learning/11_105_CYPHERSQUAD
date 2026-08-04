const SummaryCard = ({ title, value, subtitle, icon, colorClass, trend }) => {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 flex items-start gap-4 hover:shadow-md transition-shadow">
      <div className={`rounded-xl p-3 ${colorClass ?? 'bg-blue-50'}`}>
        <span className="text-2xl">{icon}</span>
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-gray-500 truncate">{title}</p>
        <p className="text-3xl font-bold text-gray-900 mt-1">{value}</p>
        {subtitle && (
          <p className="text-xs text-gray-400 mt-1">{subtitle}</p>
        )}
      </div>
      {trend !== undefined && (
        <span
          className={`text-xs font-semibold px-2 py-1 rounded-full self-start ${
            trend >= 0
              ? 'bg-emerald-100 text-emerald-700'
              : 'bg-red-100 text-red-700'
          }`}
        >
          {trend >= 0 ? '↑' : '↓'} {Math.abs(trend)}%
        </span>
      )}
    </div>
  );
};

export default SummaryCard;