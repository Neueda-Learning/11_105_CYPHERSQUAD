import { useLocation } from 'react-router-dom';

const PAGE_TITLES = {
  '/': 'Dashboard',
  '/transactions': 'Transactions',
  '/alerts': 'Alerts',
  '/rules': 'Rules',
  '/currency': 'Currency Rates',
};

const Navbar = ({ onMenuToggle }) => {
  const { pathname } = useLocation();
  const title = PAGE_TITLES[pathname] ?? 'Transaction Monitor';
  const today = new Date().toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  return (
    <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 shadow-sm">
      <div className="flex items-center gap-3">
        <button
          type="button"
          className="md:hidden rounded-lg border border-gray-200 p-2 text-gray-600"
          onClick={onMenuToggle}
          aria-label="Open menu"
        >
          ☰
        </button>
        <div>
        <h1 className="text-lg font-semibold text-gray-800">{title}</h1>
        <p className="text-xs text-gray-400">{today}</p>
        </div>
      </div>

      <div className="flex items-center gap-4">
        {/* Notification Bell */}
        <button className="relative p-2 rounded-lg hover:bg-gray-100 transition-colors">
          <span className="text-xl">🔔</span>
          <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full" />
        </button>

        {/* User Avatar */}
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white text-sm font-bold">
            A
          </div>
          <div className="hidden sm:block">
            <p className="text-sm font-medium text-gray-700">User</p>
            <p className="text-xs text-gray-400">Analyst</p>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Navbar;