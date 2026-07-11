import { api, unwrap } from "@/lib/api-client";
import { API_ENDPOINTS } from "@/lib/api-constants";
import type {
  AIChatRequest,
  AIChatResponse,
  AIConversation,
  AIConversationSummary,
  AuthTokens,
  CreateProjectRequest,
  LoginRequest,
  PageResponse,
  Project,
  ProjectVisibility,
  RegisterRequest,
  Repository,
  RepositoryFile,
  RepositoryTreeNode,
  UpdateProjectRequest,
  User,
  Workspace,
  ApiResponse,
} from "@/types";

// AUTH
export const authService = {
  register: async (body: RegisterRequest) =>
    unwrap(await api.post<ApiResponse<AuthTokens>>(API_ENDPOINTS.auth.register, body)),
  login: async (body: LoginRequest) =>
    unwrap(await api.post<ApiResponse<AuthTokens>>(API_ENDPOINTS.auth.login, body)),
  logout: async (refreshToken: string) =>
    unwrap(await api.post<ApiResponse<null>>(API_ENDPOINTS.auth.logout, { refreshToken })),
  forgotPassword: async (email: string) =>
    unwrap(await api.post<ApiResponse<null>>(API_ENDPOINTS.auth.forgotPassword, { email })),
  resetPassword: async (token: string, newPassword: string) =>
    unwrap(
      await api.post<ApiResponse<null>>(API_ENDPOINTS.auth.resetPassword, { token, newPassword }),
    ),
};

// USER
export const userService = {
  me: async () => unwrap(await api.get<ApiResponse<User>>(API_ENDPOINTS.users.me)),
  updateMe: async (body: Partial<Pick<User, "fullName" | "avatarUrl" | "bio">>) =>
    unwrap(await api.put<ApiResponse<User>>(API_ENDPOINTS.users.updateMe, body)),
  changePassword: async (currentPassword: string, newPassword: string) =>
    unwrap(
      await api.post<ApiResponse<null>>(API_ENDPOINTS.users.changePassword, {
        currentPassword,
        newPassword,
      }),
    ),
};

// WORKSPACE
export const workspaceService = {
  me: async () => unwrap(await api.get<ApiResponse<Workspace>>(API_ENDPOINTS.workspaces.me)),
};

// PROJECT
export type ProjectListParams = {
  search?: string;
  archived?: boolean;
  favorite?: boolean;
  visibility?: ProjectVisibility;
  tag?: string;
  page?: number;
  size?: number;
  sort?: string;
};

export const projectService = {
  list: async (params: ProjectListParams = {}) =>
    unwrap(
      await api.get<ApiResponse<PageResponse<Project>>>(API_ENDPOINTS.projects.list, { params }),
    ),
  create: async (body: CreateProjectRequest) =>
    unwrap(await api.post<ApiResponse<Project>>(API_ENDPOINTS.projects.create, body)),
  detail: async (id: string) =>
    unwrap(await api.get<ApiResponse<Project>>(API_ENDPOINTS.projects.detail(id))),
  update: async (id: string, body: UpdateProjectRequest) =>
    unwrap(await api.put<ApiResponse<Project>>(API_ENDPOINTS.projects.update(id), body)),
  remove: async (id: string) =>
    unwrap(await api.delete<ApiResponse<null>>(API_ENDPOINTS.projects.delete(id))),
  archive: async (id: string) =>
    unwrap(await api.patch<ApiResponse<Project>>(API_ENDPOINTS.projects.archive(id))),
  unarchive: async (id: string) =>
    unwrap(await api.patch<ApiResponse<Project>>(API_ENDPOINTS.projects.unarchive(id))),
  favorite: async (id: string) =>
    unwrap(await api.patch<ApiResponse<Project>>(API_ENDPOINTS.projects.favorite(id))),
  unfavorite: async (id: string) =>
    unwrap(await api.patch<ApiResponse<Project>>(API_ENDPOINTS.projects.unfavorite(id))),
};

// REPOSITORY
export const repositoryService = {
  upload: async (projectId: string, file: File, replaceExisting = true) => {
    const fd = new FormData();
    fd.append("file", file);
    fd.append("replaceExisting", String(replaceExisting));
    return unwrap(
      await api.post<ApiResponse<Repository>>(API_ENDPOINTS.repository.upload(projectId), fd, {
        headers: { "Content-Type": "multipart/form-data" },
      }),
    );
  },
  detail: async (projectId: string) =>
    unwrap(await api.get<ApiResponse<Repository>>(API_ENDPOINTS.repository.detail(projectId))),
  tree: async (projectId: string) =>
    unwrap(
      await api.get<ApiResponse<RepositoryTreeNode[]>>(API_ENDPOINTS.repository.tree(projectId)),
    ),
  file: async (projectId: string, fileId: string) =>
    unwrap(
      await api.get<ApiResponse<RepositoryFile>>(API_ENDPOINTS.repository.file(projectId, fileId)),
    ),
  remove: async (projectId: string) =>
    unwrap(await api.delete<ApiResponse<null>>(API_ENDPOINTS.repository.delete(projectId))),
};

// AI
export const aiService = {
  chat: async (projectId: string, body: AIChatRequest) =>
    unwrap(await api.post<ApiResponse<AIChatResponse>>(API_ENDPOINTS.ai.chat(projectId), body)),
  conversations: async (projectId: string) =>
    unwrap(
      await api.get<ApiResponse<AIConversationSummary[]>>(
        API_ENDPOINTS.ai.conversations(projectId),
      ),
    ),
  conversation: async (projectId: string, conversationId: string) =>
    unwrap(
      await api.get<ApiResponse<AIConversation>>(
        API_ENDPOINTS.ai.conversation(projectId, conversationId),
      ),
    ),
  deleteConversation: async (projectId: string, conversationId: string) =>
    unwrap(
      await api.delete<ApiResponse<null>>(API_ENDPOINTS.ai.conversation(projectId, conversationId)),
    ),
};
