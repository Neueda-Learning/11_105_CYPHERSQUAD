import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import ReactDOM from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import { router } from "./routes/AppRoutes";
import './index.css'
import './App.css'
import App from './App.jsx'

ReactDOM.createRoot(document.getElementById("root")).render(
<StrictMode>
    <RouterProvider router={router} />
  </StrictMode>
);
