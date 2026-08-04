import { createBrowserRouter } from "react-router-dom";

import Layout from "../components/layout/Layout";

import Dashboard from "../pages/Dashboard";
import Transactions from "../pages/Transactions";
import TransactionDetails from "../pages/TransactionDetails";
import Alerts from "../pages/Alerts";
import AlertDetails from "../pages/AlertDetails";
import Rules from "../pages/Rules";
import NotFound from "../pages/NotFound";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <Layout />,
    children: [
      {
        index: true,
        element: <Dashboard />,
      },
      {
        path: "transactions",
        element: <Transactions />,
      },
      {
        path: "transactions/:id",
        element: <TransactionDetails />,
      },
      {
        path: "alerts",
        element: <Alerts />,
      },
      {
        path: "alerts/:id",
        element: <AlertDetails />,
      },
      {
        path: "rules",
        element: <Rules />,
      },
    ],
  },
  {
    path: "*",
    element: <NotFound />,
  },
]);