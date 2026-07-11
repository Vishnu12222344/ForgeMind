import { Link } from "@tanstack/react-router";
import { type ReactNode } from "react";
import { Logo } from "./branding";
import { useAuthStore } from "@/store/auth-store";

export function PublicLayout({ children }: { children: ReactNode }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  return (
    <div className="min-h-screen flex flex-col">
      <header className="sticky top-0 z-30 border-b border-border/60 backdrop-blur bg-background/70">
        <div className="mx-auto max-w-7xl px-6 h-16 flex items-center justify-between">
          <Logo />
          <nav className="hidden md:flex items-center gap-8 text-sm text-muted-foreground">
            <Link to="/features" className="hover:text-foreground transition">Features</Link>
            <Link to="/about" className="hover:text-foreground transition">About</Link>
          </nav>
          <div className="flex items-center gap-2">
            {isAuthenticated ? (
              <Link to="/app/dashboard" className="px-4 py-2 text-sm rounded-md bg-brand text-white shadow-glow">
                Open app
              </Link>
            ) : (
              <>
                <Link to="/login" className="px-4 py-2 text-sm text-muted-foreground hover:text-foreground">Log in</Link>
                <Link to="/register" className="px-4 py-2 text-sm rounded-md bg-brand text-white shadow-glow">
                  Get started
                </Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main className="flex-1">{children}</main>
      <footer className="border-t border-border/60 py-8 text-sm text-muted-foreground">
        <div className="mx-auto max-w-7xl px-6 flex flex-wrap items-center justify-between gap-4">
          <div>© {new Date().getFullYear()} ForgeMind AI</div>
          <div className="flex gap-6">
            <Link to="/features">Features</Link>
            <Link to="/about">About</Link>
          </div>
        </div>
      </footer>
    </div>
  );
}
