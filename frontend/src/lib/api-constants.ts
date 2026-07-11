export const API_BASE_URL =
  (typeof import.meta !== "undefined" && import.meta.env?.VITE_API_BASE_URL) ||
  "https://forgemind-l98k.onrender.com/api/v1";

export const API_ENDPOINTS = {
  auth: {
    register: "/auth/register",
    login: "/auth/login",
    refresh: "/auth/refresh",
    logout: "/auth/logout",
    forgotPassword: "/auth/forgot-password",
    resetPassword: "/auth/reset-password",
  },
  users: {
    me: "/users/me",
    updateMe: "/users/me",
    changePassword: "/users/me/change-password",
  },
  workspaces: { me: "/workspaces/me" },
  projects: {
    list: "/projects",
    create: "/projects",
    detail: (id: string) => `/projects/${id}`,
    update: (id: string) => `/projects/${id}`,
    delete: (id: string) => `/projects/${id}`,
    archive: (id: string) => `/projects/${id}/archive`,
    unarchive: (id: string) => `/projects/${id}/unarchive`,
    favorite: (id: string) => `/projects/${id}/favorite`,
    unfavorite: (id: string) => `/projects/${id}/unfavorite`,
  },
  repository: {
    upload: (id: string) => `/projects/${id}/repository/upload`,
    detail: (id: string) => `/projects/${id}/repository`,
    tree: (id: string) => `/projects/${id}/repository/tree`,
    file: (id: string, fileId: string) => `/projects/${id}/repository/file/${fileId}`,
    delete: (id: string) => `/projects/${id}/repository`,
  },
  ai: {
    chat: (id: string) => `/projects/${id}/ai/chat`,
    conversations: (id: string) => `/projects/${id}/ai/conversations`,
    conversation: (id: string, cid: string) => `/projects/${id}/ai/conversations/${cid}`,
  },
};
