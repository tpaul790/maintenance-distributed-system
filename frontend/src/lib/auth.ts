import Cookies from 'js-cookie';
import { api } from '../api/client';

export const TOKEN_COOKIE = 'jwt_token';

export type Role = 'ADMIN' | 'TENANT';

export interface LoginResponse {
  token: string;
  email: string;
  role: Role;
}

export interface Session {
  email: string;
  role: Role;
  exp?: number;
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  const { data } = await api.post<LoginResponse>('/api/auth/login', { email, password });
  Cookies.set(TOKEN_COOKIE, data.token, { path: '/', sameSite: 'Lax' });
  return data;
}

function decodePayload(token: string): Record<string, unknown> | null {
  try {
    const part = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(part)
        .split('')
        .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join(''),
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

// Display-only decoding; the gateway is the one that actually verifies the signature.
export function getSession(): Session | null {
  const token = Cookies.get(TOKEN_COOKIE);
  if (!token) return null;
  const payload = decodePayload(token);
  if (!payload || typeof payload.sub !== 'string' || typeof payload.role !== 'string') return null;
  const exp = typeof payload.exp === 'number' ? payload.exp : undefined;
  if (exp !== undefined && exp * 1000 < Date.now()) return null;
  return { email: payload.sub, role: payload.role as Role, exp };
}

export async function logout(): Promise<void> {
  try {
    await api.post('/api/auth/logout');
  } catch {
    // clearing the local cookie is what matters; the server call is best-effort
  }
  Cookies.remove(TOKEN_COOKIE, { path: '/' });
}
