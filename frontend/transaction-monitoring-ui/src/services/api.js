// const BASE = "http://localhost:8080";
const BASE = import.meta.env.VITE_API_BASE_URL || "/api";

async function request(path, options = {}) {
  const response = await fetch(`${BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers ?? {}),
    },
    ...options,
  });

  if (!response.ok) {
    let message = `API error ${response.status}: ${response.statusText}`;
    try {
      const payload = await response.json();
      if (payload?.message) {
        message = payload.message;
      }
    } catch {
      // Ignore JSON parse errors for non-JSON responses.
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export const transactionApi = {
  getAll: () => request("/transactions"),
  getById: (id) => request(`/transactions/${id}`),
  create: (payload) => request("/transactions", { method: "POST", body: JSON.stringify(payload) }),
};

export const alertApi = {
  getAll: () => request("/alerts"),
  getById: (id) => request(`/alerts/${id}`),
  getByStatus: (status) => request(`/alerts/status/${encodeURIComponent(status)}`),
  update: (id, payload) => request(`/alerts/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
  updateStatus: (id, status) => request(`/alerts/${id}/status`, { method: "PUT", body: JSON.stringify({ status }) }),
  remove: (id) => request(`/alerts/${id}`, { method: "DELETE" }),
};

export const ruleApi = {
  getAll: () => request("/rules"),
  getById: (id) => request(`/rules/${id}`),
  create: (payload) => request("/rules", { method: "POST", body: JSON.stringify(payload) }),
  update: (id, payload) => request(`/rules/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
  remove: (id) => request(`/rules/${id}`, { method: "DELETE" }),
  evaluateOne: (ruleId, transactionId) => request(`/rules/${ruleId}/evaluate/transactions/${transactionId}`, { method: "POST" }),
  evaluateAllActive: (transactionId) => request(`/rules/evaluate/transactions/${transactionId}`, { method: "POST" }),
};

export const currencyApi = {
  getAll: () => request("/currency"),
  getById: (id) => request(`/currency/${id}`),
  create: (payload) => request("/currency", { method: "POST", body: JSON.stringify(payload) }),
  patch: (id, payload) => request(`/currency/${id}`, { method: "PATCH", body: JSON.stringify(payload) }),
  remove: (id) => request(`/currency/${id}`, { method: "DELETE" }),
};
