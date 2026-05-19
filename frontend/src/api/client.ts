import axios, { type AxiosError } from "axios";
import { API_BASE_URL } from "@/api/config";
import type { ErrorResponse } from "@/api/types";

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

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ErrorResponse>) => {
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
