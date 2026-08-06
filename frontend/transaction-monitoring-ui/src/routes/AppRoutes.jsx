import { createBrowserRouter } from "react-router-dom";

import Layout from "../components/layout/Layout";

import Dashboard from "../pages/Dashboard";
import Transactions from "../pages/Transactions";
import TransactionDetails from "../pages/TransactionDetails";
import Alerts from "../pages/Alerts";
import AlertDetails from "../pages/AlertDetails";
import Rules from "../pages/Rules";
import Currency from "../pages/Currency";
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
      {
        path: "currency",
        element: <Currency />,
      },
    ],
  },
  {
    path: "*",
    element: <NotFound />,
  },
]);