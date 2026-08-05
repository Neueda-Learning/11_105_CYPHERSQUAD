import { Link } from "react-router-dom";

const NotFound = () => {
  return (
    <section className="mx-auto max-w-xl rounded-xl border border-gray-200 bg-white p-8 text-center">
      <h1 className="text-3xl font-bold text-gray-900">404</h1>
      <p className="mt-2 text-gray-600">The page you are looking for does not exist.</p>
      <div className="mt-6 flex flex-wrap justify-center gap-2">
        <Link className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white" to="/">
          Go to Dashboard
        </Link>
        <Link className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700" to="/transactions">
          Open Transactions
        </Link>
      </div>
    </section>
  );
};

export default NotFound;