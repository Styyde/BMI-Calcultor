import { apiClient } from "@/api/client";
import type { AuthRequest, AuthResponse, RegisterRequest } from "@/api/types";

export { refreshToken } from "@/api/refresh";

export async function login(request: AuthRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>("/api/auth/login", request);
  return data;
}

export async function register(
  request: RegisterRequest,
): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>(
    "/api/auth/register",
    request,
  );
  return data;
}

export async function logout(): Promise<void> {
  await apiClient.post("/api/auth/logout");
}
