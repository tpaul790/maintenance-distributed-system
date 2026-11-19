import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { api } from '../api/client';
import { getSession, logout } from '../lib/auth';

interface TestResult {
  label: string;
  status: number | 'network-error';
  body: unknown;
}

export default function StatusPage() {
  const navigate = useNavigate();
  const session = getSession();
  const [result, setResult] = useState<TestResult | null>(null);
  const [busy, setBusy] = useState<string | null>(null);

  if (!session) return <Navigate to="/login" replace />;

  const test = async (label: string, url: string) => {
    setBusy(label);
    try {
      const res = await api.get(url);
      setResult({ label, status: res.status, body: res.data });
    } catch (err) {
      if (axios.isAxiosError(err) && err.response) {
        setResult({ label, status: err.response.status, body: err.response.data });
      } else {
        setResult({ label, status: 'network-error', body: 'Cannot reach the API gateway' });
      }
    } finally {
      setBusy(null);
    }
  };

  const onLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  const ok = typeof result?.status === 'number' && result.status < 300;

  return (
    <main className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-100 to-indigo-100 px-4">
      <div className="w-full max-w-xl rounded-2xl bg-white p-8 shadow-xl">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-xl font-bold text-slate-900">Session status</h1>
            <p className="mt-2 text-sm text-slate-600">
              Logged in as <span className="font-semibold">{session.email}</span>
            </p>
            <span className="mt-2 inline-block rounded-full bg-indigo-100 px-3 py-0.5 text-xs font-semibold text-indigo-700">
              {session.role}
            </span>
          </div>
          <button
            onClick={onLogout}
            className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-50"
          >
            Logout
          </button>
        </div>

        <div className="mt-6 grid gap-3 sm:grid-cols-2">
          <button
            onClick={() => test('Tenant Service', '/api/tenants/health')}
            disabled={busy !== null}
            className="rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-60"
          >
            {busy === 'Tenant Service' ? 'Testing…' : 'Test Tenant Service [Admin Only]'}
          </button>
          <button
            onClick={() => test('Utility Service', '/api/utilities/health')}
            disabled={busy !== null}
            className="rounded-lg bg-emerald-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-emerald-700 disabled:opacity-60"
          >
            {busy === 'Utility Service' ? 'Testing…' : 'Test Utility Service [Admin & Tenant]'}
          </button>
        </div>

        {result && (
          <div
            className={`mt-6 rounded-lg border p-4 text-sm ${
              ok ? 'border-emerald-200 bg-emerald-50' : 'border-amber-200 bg-amber-50'
            }`}
          >
            <p className="font-semibold text-slate-800">
              {result.label}: HTTP {result.status}
            </p>
            <pre className="mt-2 overflow-x-auto whitespace-pre-wrap text-xs text-slate-700">
              {typeof result.body === 'string' ? result.body : JSON.stringify(result.body, null, 2)}
            </pre>
          </div>
        )}
      </div>
    </main>
  );
}
