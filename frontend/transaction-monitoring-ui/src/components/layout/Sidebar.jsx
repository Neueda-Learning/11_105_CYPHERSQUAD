import { NavLink } from "react-router-dom";

const Sidebar = () => {
  const linkStyle = ({ isActive }) =>
    `block rounded-lg px-4 py-2 transition ${
      isActive
        ? "bg-blue-600 text-white"
        : "text-gray-700 hover:bg-gray-200"
    }`;

  return (
    <aside className="h-screen w-64 border-r bg-white p-4 shadow-md">
      <h2 className="mb-8 text-2xl font-bold text-blue-600">
        TM Dashboard
      </h2>

      <nav className="flex flex-col gap-2">
        <NavLink to="/" end className={linkStyle}>
          Dashboard
        </NavLink>

        <NavLink to="/transactions" className={linkStyle}>
          Transactions
        </NavLink>

        <NavLink to="/alerts" className={linkStyle}>
          Alerts
        </NavLink>

        <NavLink to="/rules" className={linkStyle}>
          Rules
        </NavLink>
      </nav>
    </aside>
  );
};

export default Sidebar;