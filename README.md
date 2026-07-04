# ForgeMind AI Frontend

ForgeMind AI is an AI-powered software engineering SaaS platform.  
This frontend is built with **React + TypeScript + Vite** and connects to the ForgeMind backend running on:

```txt
http://localhost:8080
```

API base URL:

```txt
http://localhost:8080/api/v1
```

---

## Tech Stack

- React
- TypeScript
- Vite
- Tailwind CSS
- React Router
- TanStack Query
- Axios
- Zustand
- React Hook Form
- Zod
- Lucide React
- Recharts
- Toast notifications

---

## Prerequisites

Make sure you have installed:

- Node.js 20+
- npm
- Backend running on port `8080`

Check backend:

```bash
http://localhost:8080/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

---

## Setup

Install dependencies:

```bash
npm install
```

Create local environment file:

```bash
cp .env.example .env.local
```

For Windows PowerShell:

```powershell
copy .env.example .env.local
```

Inside `.env.local`:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Run frontend:

```bash
npm run dev
```

Frontend will run at:

```txt
http://localhost:5173
```

---

## Available Scripts

```bash
npm run dev
```

Starts development server.

```bash
npm run build
```

Creates production build.

```bash
npm run preview
```

Previews production build locally.

```bash
npm run lint
```

Runs linting if configured.

---

## Folder Structure

```txt
frontend/
├── public/
├── src/
│   ├── components/
│   │   ├── auth/
│   │   ├── navigation/
│   │   ├── projects/
│   │   └── ui/
│   ├── layouts/
│   │   ├── AppLayout.tsx
│   │   └── PublicLayout.tsx
│   ├── lib/
│   │   ├── api.ts
│   │   ├── auth.ts
│   │   ├── queryClient.ts
│   │   └── utils.ts
│   ├── pages/
│   │   ├── app/
│   │   └── public/
│   ├── routes/
│   │   ├── AdminRoute.tsx
│   │   ├── ProtectedRoute.tsx
│   │   └── PublicRoute.tsx
│   ├── services/
│   │   ├── authService.ts
│   │   ├── projectService.ts
│   │   ├── userService.ts
│   │   └── workspaceService.ts
│   ├── store/
│   │   └── authStore.ts
│   ├── types/
│   ├── App.tsx
│   ├── main.tsx
│   └── index.css
├── .env.example
├── package.json
└── vite.config.ts
```

---

## Environment Variables

Create `.env.local`:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

If this variable is missing, the frontend should default to:

```txt
http://localhost:8080/api/v1
```

---

## Authentication Flow

The backend returns:

```json
{
  "accessToken": "jwt-token",
  "refreshToken": "refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {}
}
```

Frontend behavior:

1. Store `accessToken`, `refreshToken`, and `user` in Zustand/localStorage.
2. Add this header to authenticated requests:

```txt
Authorization: Bearer ACCESS_TOKEN
```

3. If API returns `401`, call:

```txt
POST /auth/refresh
```

4. If refresh succeeds, retry the original request.
5. If refresh fails, logout and redirect to `/login`.

---

## Routes

### Public Routes

| Route | Page |
|---|---|
| `/` | Landing page |
| `/login` | Login |
| `/register` | Register |
| `/forgot-password` | Forgot password |
| `/reset-password` | Reset password |
| `/verify-email` | Verify email placeholder |

### Protected Routes

| Route | Page |
|---|---|
| `/app/dashboard` | Dashboard |
| `/app/projects` | Project list |
| `/app/projects/new` | Create project |
| `/app/projects/:projectId` | Project details |
| `/app/profile` | User profile |
| `/app/settings` | Settings |
| `/app/repositories` | Repository placeholder |
| `/app/ai` | AI assistant placeholder |
| `/app/documentation` | Documentation placeholder |
| `/app/admin` | Admin placeholder |

---

## Backend API Response Format

All backend responses follow this structure:

```json
{
  "success": true,
  "message": "Optional message",
  "data": {},
  "timestamp": "2026-07-04T..."
}
```

Error response:

```json
{
  "success": false,
  "message": "Error message",
  "data": null,
  "timestamp": "2026-07-04T..."
}
```

Validation error example:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "must be a well-formed email address",
    "password": "size must be between 8 and 100"
  }
}
```

---

# API Endpoints Used

Base URL:

```txt
http://localhost:8080/api/v1
```

---

## Auth Endpoints

### Register

```http
POST /auth/register
```

Request:

```json
{
  "fullName": "Jane Doe",
  "username": "janedoe",
  "email": "jane@example.com",
  "password": "password123"
}
```

Response data:

```json
{
  "accessToken": "jwt-token",
  "refreshToken": "refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "uuid",
    "email": "jane@example.com",
    "username": "janedoe",
    "fullName": "Jane Doe",
    "avatarUrl": null,
    "bio": null,
    "role": "USER",
    "emailVerified": false,
    "createdAt": "2026-07-04T..."
  }
}
```

---

### Login

```http
POST /auth/login
```

Request:

```json
{
  "email": "jane@example.com",
  "password": "password123"
}
```

Response data is the same as register.

---

### Refresh Token

```http
POST /auth/refresh
```

Request:

```json
{
  "refreshToken": "refresh-token"
}
```

Response data:

```json
{
  "accessToken": "new-jwt-token",
  "refreshToken": "new-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {}
}
```

---

### Logout

```http
POST /auth/logout
```

Request:

```json
{
  "refreshToken": "refresh-token"
}
```

Response:

```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null
}
```

---

### Forgot Password

```http
POST /auth/forgot-password
```

Request:

```json
{
  "email": "jane@example.com"
}
```

Response:

```json
{
  "success": true,
  "message": "If the email exists, a reset link has been sent",
  "data": null
}
```

---

### Reset Password

```http
POST /auth/reset-password
```

Request:

```json
{
  "token": "reset-token",
  "newPassword": "newpassword123"
}
```

Response:

```json
{
  "success": true,
  "message": "Password reset successfully",
  "data": null
}
```

---

## User Endpoints

All user endpoints require:

```txt
Authorization: Bearer ACCESS_TOKEN
```

### Get Current User

```http
GET /users/me
```

Response data:

```json
{
  "id": "uuid",
  "email": "jane@example.com",
  "username": "janedoe",
  "fullName": "Jane Doe",
  "avatarUrl": null,
  "bio": null,
  "role": "USER",
  "emailVerified": false,
  "createdAt": "2026-07-04T..."
}
```

---

### Update Profile

```http
PUT /users/me
```

Request:

```json
{
  "fullName": "Jane Updated",
  "avatarUrl": "https://example.com/avatar.png",
  "bio": "Full-stack developer"
}
```

Response data:

```json
{
  "id": "uuid",
  "email": "jane@example.com",
  "username": "janedoe",
  "fullName": "Jane Updated",
  "avatarUrl": "https://example.com/avatar.png",
  "bio": "Full-stack developer",
  "role": "USER",
  "emailVerified": false,
  "createdAt": "2026-07-04T..."
}
```

---

### Change Password

```http
POST /users/me/change-password
```

Request:

```json
{
  "currentPassword": "oldpassword123",
  "newPassword": "newpassword123"
}
```

Response:

```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": null
}
```

---

## Workspace Endpoints

### Get Personal Workspace

```http
GET /workspaces/me
```

Response data:

```json
{
  "id": "uuid",
  "name": "Jane Doe's Workspace",
  "slug": "janedoe",
  "personal": true,
  "createdAt": "2026-07-04T..."
}
```

---

## Project Endpoints

All project endpoints require:

```txt
Authorization: Bearer ACCESS_TOKEN
```

Project visibility values:

```txt
PRIVATE
PUBLIC
```

Project response shape:

```json
{
  "id": "uuid",
  "workspaceId": "uuid",
  "createdById": "uuid",
  "name": "ForgeMind Backend",
  "slug": "forgemind-backend",
  "description": "Backend API for ForgeMind AI",
  "visibility": "PRIVATE",
  "archived": false,
  "favorite": false,
  "tags": ["ai", "java", "spring-boot"],
  "createdAt": "2026-07-04T...",
  "updatedAt": "2026-07-04T..."
}
```

---

### List Projects

```http
GET /projects
```

Optional query params:

| Param | Type | Example |
|---|---|---|
| `search` | string | `backend` |
| `archived` | boolean | `false` |
| `favorite` | boolean | `true` |
| `visibility` | string | `PRIVATE` |
| `tag` | string | `java` |
| `page` | number | `0` |
| `size` | number | `20` |
| `sort` | string | `updatedAt,desc` |

Example:

```http
GET /projects?search=backend&page=0&size=20&sort=updatedAt,desc
```

Response data:

```json
{
  "content": [
    {
      "id": "uuid",
      "workspaceId": "uuid",
      "createdById": "uuid",
      "name": "ForgeMind Backend",
      "slug": "forgemind-backend",
      "description": "Backend API for ForgeMind AI",
      "visibility": "PRIVATE",
      "archived": false,
      "favorite": false,
      "tags": ["ai", "java", "spring-boot"],
      "createdAt": "2026-07-04T...",
      "updatedAt": "2026-07-04T..."
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false,
  "numberOfElements": 1
}
```

---

### Create Project

```http
POST /projects
```

Request:

```json
{
  "workspaceId": "optional-workspace-uuid",
  "name": "ForgeMind Backend",
  "description": "Backend API for ForgeMind AI",
  "visibility": "PRIVATE",
  "tags": ["spring-boot", "java", "ai"]
}
```

Notes:

- `workspaceId` is optional.
- If `workspaceId` is omitted, backend uses the user's personal workspace.
- `name` is required.
- `tags` is optional.

Response data:

```json
{
  "id": "uuid",
  "workspaceId": "uuid",
  "createdById": "uuid",
  "name": "ForgeMind Backend",
  "slug": "forgemind-backend",
  "description": "Backend API for ForgeMind AI",
  "visibility": "PRIVATE",
  "archived": false,
  "favorite": false,
  "tags": ["ai", "java", "spring-boot"],
  "createdAt": "2026-07-04T...",
  "updatedAt": "2026-07-04T..."
}
```

---

### Get Project By ID

```http
GET /projects/{projectId}
```

Response data:

```json
{
  "id": "uuid",
  "workspaceId": "uuid",
  "createdById": "uuid",
  "name": "ForgeMind Backend",
  "slug": "forgemind-backend",
  "description": "Backend API for ForgeMind AI",
  "visibility": "PRIVATE",
  "archived": false,
  "favorite": false,
  "tags": ["ai", "java", "spring-boot"],
  "createdAt": "2026-07-04T...",
  "updatedAt": "2026-07-04T..."
}
```

---

### Update Project

```http
PUT /projects/{projectId}
```

Request:

```json
{
  "name": "ForgeMind Backend Updated",
  "description": "Updated description",
  "visibility": "PRIVATE",
  "tags": ["java", "spring", "saas"]
}
```

All fields are optional.

Response data:

```json
{
  "id": "uuid",
  "workspaceId": "uuid",
  "createdById": "uuid",
  "name": "ForgeMind Backend Updated",
  "slug": "forgemind-backend-updated",
  "description": "Updated description",
  "visibility": "PRIVATE",
  "archived": false,
  "favorite": false,
  "tags": ["java", "saas", "spring"],
  "createdAt": "2026-07-04T...",
  "updatedAt": "2026-07-04T..."
}
```

---

### Delete Project

```http
DELETE /projects/{projectId}
```

Response:

```json
{
  "success": true,
  "message": "Project deleted successfully",
  "data": null
}
```

---

### Archive Project

```http
PATCH /projects/{projectId}/archive
```

Response data includes:

```json
{
  "id": "uuid",
  "archived": true
}
```

---

### Unarchive Project

```http
PATCH /projects/{projectId}/unarchive
```

Response data includes:

```json
{
  "id": "uuid",
  "archived": false
}
```

---

### Favorite Project

```http
PATCH /projects/{projectId}/favorite
```

Response data includes:

```json
{
  "id": "uuid",
  "favorite": true
}
```

---

### Unfavorite Project

```http
PATCH /projects/{projectId}/unfavorite
```

Response data includes:

```json
{
  "id": "uuid",
  "favorite": false
}
```

---

## Current Implemented Backend Modules

The frontend currently connects to:

- Authentication
- User Profile
- Personal Workspace
- Project Management

These modules are currently placeholders in frontend because backend APIs are not implemented yet:

- Repository Explorer
- AI Assistant
- Documentation
- Notifications
- Admin Dashboard

---

## Default Dev Admin

If backend seeded the admin user, you can login with:

```txt
Email: admin@forgemind.ai
Password: Admin@12345
```

If this account does not exist, register a new user from the frontend.

---

## Troubleshooting

### Backend not reachable

Make sure backend is running:

```txt
http://localhost:8080
```

Frontend expects API at:

```txt
http://localhost:8080/api/v1
```

---

### CORS error

Backend must allow frontend origin:

```txt
http://localhost:5173
```

If CORS fails, update backend CORS configuration.

---

### Login works but protected routes fail

Check that Axios sends:

```txt
Authorization: Bearer ACCESS_TOKEN
```

Also check token exists in localStorage/Zustand.

---

### Refresh token loop

If refresh repeatedly fails:

1. Clear browser localStorage.
2. Login again.

In browser console:

```js
localStorage.clear()
```

---

### Validation errors not showing

Backend validation errors may arrive in:

```json
{
  "message": "Validation failed",
  "data": {
    "fieldName": "error message"
  }
}
```

Frontend forms should map `data.fieldName` to field-level errors.

---

## Development Notes

For now, token storage uses `localStorage`.

This is acceptable for local MVP development. Later, for production, consider:

- HttpOnly secure cookies
- CSRF protection
- stricter CORS
- refresh token rotation hardening
- role-based route permissions
```
