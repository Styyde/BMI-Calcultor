import axios from "axios";
import { API_BASE_URL } from "@/api/config";
import type { AuthResponse } from "@/api/types";
import { getToken } from "@/lib/auth-storage";

/** Raw axios call — avoids circular dependency with client interceptors */
export async function refreshToken(): Promise<AuthResponse> {
  const token = getToken();
  const base = API_BASE_URL || "";
  const { data } = await axios.post<AuthResponse>(
    `${base}/api/auth/refresh`,
    null,
    {
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
      withCredentials: false,
    },
  );
  return data;
}
