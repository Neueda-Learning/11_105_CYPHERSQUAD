import { useLocation, NavLink } from 'react-router-dom';
import { useTheme } from '../../context/ThemeContext';

const PAGE_TITLES = {
  '/': 'Dashboard',
  '/transactions': 'Transactions',
  '/alerts': 'Alerts',
  '/rules': 'Rules',
  '/currency': 'Currency Rates',
};

const Navbar = ({ onMenuToggle }) => {
  const { pathname } = useLocation();
  const { dark, toggle } = useTheme();
  const title = PAGE_TITLES[pathname] ?? 'Transaction Monitor';
  const today = new Date().toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  return (
    <header className="h-16 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between px-6 shadow-sm">
      <div className="flex items-center gap-3">
        <button
          type="button"
          className="md:hidden rounded-lg border border-gray-200 dark:border-gray-700 p-2 text-gray-600 dark:text-gray-300"
          onClick={onMenuToggle}
          aria-label="Open menu"
        >
          ☰
        </button>
        <div>
          <h1 className="text-lg font-semibold text-gray-800 dark:text-gray-100">{title}</h1>
          <p className="text-xs text-gray-400 dark:text-gray-500">{today}</p>
        </div>
      </div>

      <div className="flex items-center gap-3">
        {/* Dark / Light toggle */}
        <button
          onClick={toggle}
          aria-label="Toggle dark mode"
          className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors text-gray-500 dark:text-gray-400"
          title={dark ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          {dark ? (
            <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 3v1m0 16v1m8.66-9H21M3 12H2m15.36-6.36-.71.71M6.34 17.66l-.71.71M17.66 17.66l.71.71M6.34 6.34l-.71-.71M16 12a4 4 0 1 1-8 0 4 4 0 0 1 8 0z" />
            </svg>
          ) : (
            <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M21 12.79A9 9 0 1 1 11.21 3a7 7 0 1 0 9.79 9.79z" />
            </svg>
          )}
        </button>

        {/* Notification Bell */}
        <NavLink
          to="/alerts"
          className={({ isActive }) =>
            `relative p-2 rounded-lg transition-colors ${isActive ? 'bg-blue-50' : 'hover:bg-gray-100'}`
          }
          aria-label="Open alerts"
        >
          <span className="text-xl">🔔</span>
          <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full" />
        </NavLink>

        {/* User Avatar */}
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white text-sm font-bold">
            A
          </div>
          <div className="hidden sm:block">
            <p className="text-sm font-medium text-gray-700 dark:text-gray-200">User</p>
            <p className="text-xs text-gray-400 dark:text-gray-500">Analyst</p>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Navbar;