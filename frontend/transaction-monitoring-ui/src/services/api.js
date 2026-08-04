const BASE = '/api';

async function request(path) {
  const res = await fetch(`${BASE}${path}`);
  if (!res.ok) throw new Error(`API error ${res.status}: ${res.statusText}`);
  return res.json();
}

export const transactionApi = {
  getAll:   ()  => request('/transactions'),
  getById:  (id) => request(`/transactions/${id}`),
};

export const alertApi = {
  getAll:   ()  => request('/alerts'),
  getById:  (id) => request(`/alerts/${id}`),
};

export const ruleApi = {
  getAll:   ()  => request('/rules'),
  getById:  (id) => request(`/rules/${id}`),
};

export const currencyApi = {
  getAll:   ()  => request('/currency'),
  getById:  (id) => request(`/currency/${id}`),
};
