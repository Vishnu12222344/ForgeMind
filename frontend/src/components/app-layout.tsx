import { Link, useRouterState, useNavigate } from "@tanstack/react-router";
import { type ReactNode } from "react";
import {
  LayoutDashboard,
  FolderKanban,
  User as UserIcon,
  Settings,
  BookOpen,
  Bell,
  BarChart3,
  ShieldCheck,
  LogOut,
  Search,
} from "lucide-react";
import { Logo } from "./branding";
import { useAuthStore } from "@/store/auth-store";
import { authService } from "@/services";
import { toast } from "sonner";

const navItems = [
  { to: "/app/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { to: "/app/projects", label: "Projects", icon: FolderKanban },
  { to: "/app/documentation", label: "Documentation", icon: BookOpen },
  { to: "/app/notifications", label: "Notifications", icon: Bell },
  { to: "/app/analytics", label: "Analytics", icon: BarChart3 },
  { to: "/app/admin", label: "Admin", icon: ShieldCheck },
];

export function AppLayout({ children }: { children: ReactNode }) {
  const pathname = useRouterState({ select: (s) => s.location.pathname });
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const refreshToken = useAuthStore((s) => s.refreshToken);
  const logout = useAuthStore((s) => s.logout);

  async function handleLogout() {
    try {
      if (refreshToken) await authService.logout(refreshToken);
    } catch {
      /* ignore */
    } finally {
      logout();
      toast.success("Signed out");
      navigate({ to: "/login" });
    }
  }

  return (
    <div className="min-h-screen flex bg-background">
      <aside className="hidden lg:flex w-64 shrink-0 flex-col border-r border-border/60 bg-card/40">
        <div className="h-16 flex items-center px-6 border-b border-border/60">
          <Logo to="/app/dashboard" />
        </div>
        <nav className="flex-1 p-3 space-y-1">
          {navItems.map((item) => {
            const active =
              pathname === item.to || (item.to !== "/app/dashboard" && pathname.startsWith(item.to));
            const Icon = item.icon;
            return (
              <Link
                key={item.to}
                to={item.to}
                className={`flex items-center gap-3 rounded-md px-3 py-2 text-sm transition ${
                  active
                    ? "bg-brand-soft text-foreground ring-brand"
                    : "text-muted-foreground hover:text-foreground hover:bg-muted/40"
                }`}
              >
                <Icon className="h-4 w-4" />
                {item.label}
              </Link>
            );
          })}
        </nav>
        <div className="p-3 border-t border-border/60 space-y-1">
          <Link
            to="/app/profile"
            className="flex items-center gap-3 rounded-md px-3 py-2 text-sm text-muted-foreground hover:text-foreground hover:bg-muted/40"
          >
            <UserIcon className="h-4 w-4" /> Profile
          </Link>
          <Link
            to="/app/settings"
            className="flex items-center gap-3 rounded-md px-3 py-2 text-sm text-muted-foreground hover:text-foreground hover:bg-muted/40"
          >
            <Settings className="h-4 w-4" /> Settings
          </Link>
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-3 rounded-md px-3 py-2 text-sm text-muted-foreground hover:text-foreground hover:bg-muted/40"
          >
            <LogOut className="h-4 w-4" /> Sign out
          </button>
        </div>
      </aside>
      <div className="flex-1 flex flex-col min-w-0">
        <header className="h-16 border-b border-border/60 bg-background/70 backdrop-blur px-4 lg:px-8 flex items-center gap-4">
          <div className="flex-1 max-w-md relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <input
              placeholder="Search projects, files, prompts…"
              disabled
              className="w-full pl-9 pr-3 h-9 rounded-md bg-muted/40 border border-border/60 text-sm placeholder:text-muted-foreground/70"
            />
          </div>
          <button
            className="relative p-2 rounded-md hover:bg-muted/40 text-muted-foreground"
            aria-label="Notifications"
          >
            <Bell className="h-4 w-4" />
          </button>
          <Link
            to="/app/profile"
            className="flex items-center gap-2 rounded-full pl-1 pr-3 py-1 hover:bg-muted/40"
          >
            <div className="h-7 w-7 rounded-full bg-brand grid place-items-center text-xs font-semibold text-white">
              {(user?.fullName || user?.username || "U").slice(0, 1).toUpperCase()}
            </div>
            <span className="hidden sm:inline text-sm text-foreground">
              {user?.fullName || user?.username}
            </span>
          </Link>
        </header>
        <main className="flex-1 overflow-y-auto p-6 lg:p-8">{children}</main>
      </div>
    </div>
  );
}
