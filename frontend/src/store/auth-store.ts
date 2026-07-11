import { create } from "zustand";
import type { User } from "@/types";

const LS_ACCESS = "fm.accessToken";
const LS_REFRESH = "fm.refreshToken";
const LS_USER = "fm.user";

function readLS(key: string): string | null {
  if (typeof window === "undefined") return null;
  try {
    return window.localStorage.getItem(key);
  } catch {
    return null;
  }
}
function writeLS(key: string, value: string | null) {
  if (typeof window === "undefined") return;
  try {
    if (value == null) window.localStorage.removeItem(key);
    else window.localStorage.setItem(key, value);
  } catch {
    /* ignore */
  }
}

type AuthState = {
  accessToken: string | null;
  refreshToken: string | null;
  user: User | null;
  isAuthenticated: boolean;
  hydrated: boolean;
  hydrate: () => void;
  login: (accessToken: string, refreshToken: string, user: User) => void;
  logout: () => void;
  setUser: (user: User) => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
};

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  refreshToken: null,
  user: null,
  isAuthenticated: false,
  hydrated: false,
  hydrate: () => {
    const accessToken = readLS(LS_ACCESS);
    const refreshToken = readLS(LS_REFRESH);
    const userRaw = readLS(LS_USER);
    const user = userRaw ? (JSON.parse(userRaw) as User) : null;
    set({
      accessToken,
      refreshToken,
      user,
      isAuthenticated: !!accessToken && !!user,
      hydrated: true,
    });
  },
  login: (accessToken, refreshToken, user) => {
    writeLS(LS_ACCESS, accessToken);
    writeLS(LS_REFRESH, refreshToken);
    writeLS(LS_USER, JSON.stringify(user));
    set({ accessToken, refreshToken, user, isAuthenticated: true });
  },
  logout: () => {
    writeLS(LS_ACCESS, null);
    writeLS(LS_REFRESH, null);
    writeLS(LS_USER, null);
    set({ accessToken: null, refreshToken: null, user: null, isAuthenticated: false });
  },
  setUser: (user) => {
    writeLS(LS_USER, JSON.stringify(user));
    set({ user });
  },
  setTokens: (accessToken, refreshToken) => {
    writeLS(LS_ACCESS, accessToken);
    writeLS(LS_REFRESH, refreshToken);
    set({ accessToken, refreshToken });
  },
}));

export function getAccessToken(): string | null {
  return useAuthStore.getState().accessToken ?? readLS(LS_ACCESS);
}
export function getRefreshToken(): string | null {
  return useAuthStore.getState().refreshToken ?? readLS(LS_REFRESH);
}
