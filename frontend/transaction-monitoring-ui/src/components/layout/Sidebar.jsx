import { NavLink } from "react-router-dom";

const Sidebar = ({ open = false, onClose }) => {
  const linkStyle = ({ isActive }) =>
    `block rounded-lg px-4 py-2 transition font-medium ${
      isActive
        ? "bg-blue-600 text-white"
        : "text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700"
    }`;

  return (
    <>
      <div
        className={`fixed inset-0 z-30 bg-black/40 transition-opacity md:hidden ${open ? "opacity-100" : "pointer-events-none opacity-0"}`}
        onClick={onClose}
      />
      <aside
        className={`fixed inset-y-0 left-0 z-40 h-screen w-64 border-r border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 p-4 shadow-md transition-transform md:static md:translate-x-0 ${
          open ? "translate-x-0" : "-translate-x-full"
        }`}
      >
      <h2 className="mb-8 text-2xl font-bold text-blue-600 dark:text-blue-400">
        TM Dashboard
      </h2>

      <nav className="flex flex-col gap-2">
        <NavLink to="/" end className={linkStyle} onClick={onClose}>
          Dashboard
        </NavLink>

        <NavLink to="/transactions" className={linkStyle} onClick={onClose}>
          Transactions
        </NavLink>

        <NavLink to="/alerts" className={linkStyle} onClick={onClose}>
          Alerts
        </NavLink>

        <NavLink to="/rules" className={linkStyle} onClick={onClose}>
          Rules
        </NavLink>

        <NavLink to="/currency" className={linkStyle} onClick={onClose}>
          Currency
        </NavLink>
      </nav>
      </aside>
    </>
  );
};

export default Sidebar;