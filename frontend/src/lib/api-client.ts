import axios, {
  AxiosError,
  type AxiosInstance,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from "axios";
import { API_BASE_URL, API_ENDPOINTS } from "./api-constants";
import { getAccessToken, getRefreshToken, useAuthStore } from "@/store/auth-store";
import type { ApiResponse, AuthTokens } from "@/types";

export const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const url = config.url ?? "";
  const isAuth = url.startsWith("/auth/");
  if (!isAuth) {
    const token = getAccessToken();
    if (token) {
      config.headers.set?.("Authorization", `Bearer ${token}`);
    }
  }
  return config;
});

let refreshPromise: Promise<string | null> | null = null;

async function performRefresh(): Promise<string | null> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return null;
  try {
    const res = await axios.post<ApiResponse<AuthTokens>>(
      `${API_BASE_URL}${API_ENDPOINTS.auth.refresh}`,
      { refreshToken },
      { headers: { "Content-Type": "application/json" } },
    );
    const tokens = res.data.data;
    useAuthStore.getState().login(tokens.accessToken, tokens.refreshToken, tokens.user);
    return tokens.accessToken;
  } catch {
    useAuthStore.getState().logout();
    if (typeof window !== "undefined" && !window.location.pathname.startsWith("/login")) {
      window.location.assign("/login");
    }
    return null;
  }
}

api.interceptors.response.use(
  (r) => r,
  async (error: AxiosError) => {
    const original = error.config as (AxiosRequestConfig & { _retry?: boolean }) | undefined;
    const status = error.response?.status;
    const url = original?.url ?? "";
    if (
      status === 401 &&
      original &&
      !original._retry &&
      !url.startsWith("/auth/")
    ) {
      original._retry = true;
      if (!refreshPromise) refreshPromise = performRefresh().finally(() => (refreshPromise = null));
      const newToken = await refreshPromise;
      if (newToken) {
        original.headers = { ...(original.headers ?? {}), Authorization: `Bearer ${newToken}` };
        return api.request(original);
      }
    }
    return Promise.reject(error);
  },
);

export function unwrap<T>(res: { data: ApiResponse<T> }): T {
  return res.data.data;
}

export function apiErrorMessage(err: unknown, fallback = "Something went wrong"): string {
  if (axios.isAxiosError(err)) {
    const data = err.response?.data as ApiResponse<unknown> | undefined;
    if (data?.message) return data.message;
    return err.message || fallback;
  }
  if (err instanceof Error) return err.message;
  return fallback;
}

export function fieldErrorsFromApi(err: unknown): Record<string, string> | null {
  if (!axios.isAxiosError(err)) return null;
  const data = err.response?.data as ApiResponse<unknown> | undefined;
  if (data && typeof data.data === "object" && data.data && !Array.isArray(data.data)) {
    return data.data as Record<string, string>;
  }
  return null;
}
