import axios, {
  type AxiosError,
  type InternalAxiosRequestConfig,
} from "axios";
import { refreshToken as refreshTokenApi } from "@/api/refresh";
import { API_BASE_URL } from "@/api/config";
import type { ErrorResponse } from "@/api/types";
import {
  clearSession,
  getToken,
  setSessionFromResponse,
} from "@/lib/auth-storage";

export class ApiError extends Error {
  status: number;
  error: string;
  path?: string;
  fieldErrors?: Record<string, string>;

  constructor(payload: ErrorResponse) {
    super(payload.message);
    this.name = "ApiError";
    this.status = payload.status;
    this.error = payload.error;
    this.path = payload.path;
    this.fieldErrors = payload.errors;
  }
}

type QueueItem = {
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
};

let isRefreshing = false;
let failedQueue: QueueItem[] = [];
let onUnauthorized: (() => void) | null = null;

export function setOnUnauthorized(callback: () => void): void {
  onUnauthorized = callback;
}

function processQueue(error: unknown | null): void {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error);
    } else {
      promise.resolve();
    }
  });
  failedQueue = [];
}

function isAuthRoute(url?: string): boolean {
  if (!url) return false;
  return (
    url.includes("/api/auth/login") ||
    url.includes("/api/auth/register") ||
    url.includes("/api/auth/refresh")
  );
}

export const apiClient = axios.create({
  baseURL: API_BASE_URL || undefined,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: false,
});

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken();
  if (token && !isAuthRoute(config.url)) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ErrorResponse>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    if (
      error.response?.status === 401 &&
      originalRequest &&
      !originalRequest._retry &&
      !isAuthRoute(originalRequest.url)
    ) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(() => apiClient(originalRequest));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const response = await refreshTokenApi();
        setSessionFromResponse(response);
        processQueue(null);
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError);
        clearSession();
        onUnauthorized?.();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    if (error.response?.data) {
      throw new ApiError(error.response.data);
    }
    throw new ApiError({
      status: error.response?.status ?? 500,
      error: "Network Error",
      message: error.message || "Unable to reach the server.",
    });
  },
);
