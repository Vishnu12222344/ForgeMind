export type ApiResponse<T> = {
  success: boolean;
  message?: string;
  data: T;
  timestamp?: string;
};

export type UserRole = "USER" | "ADMIN";

export type User = {
  id: string;
  email: string;
  username: string;
  fullName: string | null;
  avatarUrl: string | null;
  bio: string | null;
  role: UserRole;
  emailVerified: boolean;
  createdAt: string;
};

export type AuthTokens = {
  accessToken: string;
  refreshToken: string;
  tokenType: "Bearer";
  expiresIn: number;
  user: User;
};

export type LoginRequest = { email: string; password: string };
export type RegisterRequest = {
  fullName: string;
  username: string;
  email: string;
  password: string;
};

export type Workspace = {
  id: string;
  name: string;
  slug: string;
  personal: boolean;
  createdAt: string;
};

export type ProjectVisibility = "PRIVATE" | "PUBLIC";

export type Project = {
  id: string;
  workspaceId: string;
  createdById: string;
  name: string;
  slug: string;
  description: string | null;
  visibility: ProjectVisibility;
  archived: boolean;
  favorite: boolean;
  tags: string[];
  createdAt: string;
  updatedAt: string;
};

export type CreateProjectRequest = {
  workspaceId?: string;
  name: string;
  description?: string | null;
  visibility?: ProjectVisibility;
  tags?: string[];
};
export type UpdateProjectRequest = Partial<CreateProjectRequest>;

export type PageResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
  numberOfElements: number;
};

export type RepositoryStatus = "PARSING" | "READY" | "FAILED";
export type RepositoryFileType = "FILE" | "FOLDER";

export type Repository = {
  id: string;
  projectId: string;
  uploadedById: string;
  originalFilename: string;
  status: RepositoryStatus;
  totalFiles: number;
  totalFolders: number;
  totalSizeBytes: number;
  primaryLanguage: string | null;
  languageStats: Record<string, number>;
  parseError: string | null;
  parsedAt: string | null;
  createdAt: string;
  updatedAt: string;
};

export type RepositoryTreeNode = {
  id: string | null;
  name: string;
  path: string;
  type: RepositoryFileType;
  language: string | null;
  extension: string | null;
  sizeBytes: number;
  binaryFile: boolean;
  contentTruncated: boolean;
  children: RepositoryTreeNode[];
};

export type RepositoryFile = {
  id: string;
  repositoryId: string;
  type: RepositoryFileType;
  path: string;
  name: string;
  extension: string | null;
  language: string | null;
  sizeBytes: number;
  depth: number;
  binaryFile: boolean;
  contentTruncated: boolean;
  content: string | null;
  createdAt: string;
  updatedAt: string;
};

export type AIMessageRole = "USER" | "ASSISTANT" | "SYSTEM";
export type ReferencedFile = {
  id: string;
  path: string;
  name: string;
  language: string | null;
};
export type AIMessage = {
  id: string;
  role: AIMessageRole;
  content: string;
  promptTokens: number;
  completionTokens: number;
  totalTokens: number;
  referencedFiles: ReferencedFile[];
  createdAt: string;
};
export type AIChatRequest = {
  conversationId?: string;
  message: string;
  fileIds?: string[];
};
export type AIChatResponse = {
  conversationId: string;
  title: string;
  userMessage: AIMessage;
  assistantMessage: AIMessage;
};
export type AIConversationSummary = {
  id: string;
  projectId: string;
  title: string;
  createdAt: string;
  updatedAt: string;
};
export type AIConversation = {
  id: string;
  projectId: string;
  title: string;
  messages: AIMessage[];
  createdAt: string;
  updatedAt: string;
};
