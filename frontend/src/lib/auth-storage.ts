import type { AuthSession, AuthUser } from "@/api/types";

const AUTH_KEY = "bmi_auth";

export function getSession(): AuthSession | null {
  const raw = localStorage.getItem(AUTH_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthSession;
  } catch {
    return null;
  }
}

export function getToken(): string | null {
  return getSession()?.token ?? null;
}

export function setSession(token: string, user: AuthUser): void {
  const session: AuthSession = { token, user };
  localStorage.setItem(AUTH_KEY, JSON.stringify(session));
}

export function setSessionFromResponse(response: {
  token: string;
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: AuthUser["role"];
}): AuthSession {
  const user: AuthUser = {
    id: response.id,
    email: response.email,
    firstName: response.firstName,
    lastName: response.lastName,
    role: response.role,
  };
  setSession(response.token, user);
  return { token: response.token, user };
}

export function clearSession(): void {
  localStorage.removeItem(AUTH_KEY);
}
