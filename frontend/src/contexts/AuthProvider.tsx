import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { useQueryClient } from "@tanstack/react-query";
import * as authApi from "@/api/auth";
import { refreshToken as refreshTokenApi } from "@/api/refresh";
import { setOnUnauthorized } from "@/api/client";
import { USE_MOCKS } from "@/api/config";
import type { AuthRequest, AuthUser, RegisterRequest } from "@/api/types";
import {
  clearSession,
  getSession,
  getToken,
  setSessionFromResponse,
} from "@/lib/auth-storage";

interface AuthContextValue {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (request: AuthRequest) => Promise<void>;
  register: (request: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [user, setUser] = useState<AuthUser | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(!USE_MOCKS);

  const applySession = useCallback(
    (session: { token: string; user: AuthUser }) => {
      setToken(session.token);
      setUser(session.user);
    },
    [],
  );

  const handleLogout = useCallback(async () => {
    try {
      if (getToken()) {
        await authApi.logout();
      }
    } catch {
      // ignore logout API errors
    } finally {
      clearSession();
      setToken(null);
      setUser(null);
      queryClient.clear();
    }
  }, [queryClient]);

  useEffect(() => {
    setOnUnauthorized(() => {
      clearSession();
      setToken(null);
      setUser(null);
      queryClient.clear();
    });
  }, [queryClient]);

  useEffect(() => {
    if (USE_MOCKS) {
      setIsLoading(false);
      return;
    }

    const initAuth = async () => {
      const stored = getSession();
      if (stored?.token) {
        try {
          const response = await refreshTokenApi();
          const session = setSessionFromResponse(response);
          applySession(session);
        } catch {
          clearSession();
          setToken(null);
          setUser(null);
        }
      }
      setIsLoading(false);
    };

    void initAuth();
  }, [applySession]);

  useEffect(() => {
    const onStorage = (event: StorageEvent) => {
      if (event.key === "bmi_auth" && !event.newValue) {
        window.location.reload();
      }
    };
    window.addEventListener("storage", onStorage);
    return () => window.removeEventListener("storage", onStorage);
  }, []);

  const login = useCallback(
    async (request: AuthRequest) => {
      const response = await authApi.login(request);
      const session = setSessionFromResponse(response);
      applySession(session);
      await queryClient.invalidateQueries();
    },
    [applySession, queryClient],
  );

  const register = useCallback(
    async (request: RegisterRequest) => {
      const response = await authApi.register(request);
      const session = setSessionFromResponse(response);
      applySession(session);
      await queryClient.invalidateQueries();
    },
    [applySession, queryClient],
  );

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      token,
      isAuthenticated: !!token && !!user,
      isLoading,
      login,
      register,
      logout: handleLogout,
    }),
    [user, token, isLoading, login, register, handleLogout],
  );

  return (
    <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
